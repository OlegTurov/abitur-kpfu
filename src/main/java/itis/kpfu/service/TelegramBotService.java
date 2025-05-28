package itis.kpfu.service;

import com.pengrad.telegrambot.request.SendMessage;
import itis.kpfu.config.BotConfig;
import itis.kpfu.encryptor.AESPasswordEncryptor;
import itis.kpfu.exception.EncryptingException;
import itis.kpfu.exception.GettingCurrentPositionException;
import itis.kpfu.exception.UserNotFoundException;
import itis.kpfu.formatter.RatingFormatter;
import itis.kpfu.model.RatingCard;
import itis.kpfu.model.UserEntity;
import itis.kpfu.parser.CookieParser;
import itis.kpfu.parser.RatingParser;
import itis.kpfu.request.UserRequest;
import itis.kpfu.statehandler.ChatState;
import itis.kpfu.statehandler.SessionTracker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
@RequiredArgsConstructor
@EnableConfigurationProperties(BotConfig.class)
public class TelegramBotService {

    private final AESPasswordEncryptor encryptor;
    private final TelegramBot bot;
    private final KpfuService kpfuService;
    private final UserService userService;
    private final ConcurrentHashMap<Long, SessionTracker> sessionTrackers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::processUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, Throwable::printStackTrace);
    }

    public void processUpdate(Update update) {
        try {
            if (update.message() == null || update.message().text() == null || update.message().text().isBlank())
                return;
            long chatId = update.message().chat().id();
            String text = update.message().text();
            log.info("Processing update with ID {} and text {}.", chatId, text);
            if (sessionTrackers.containsKey(chatId)) {
                if ("/cancel".equals(text)) {
                    sessionTrackers.remove(chatId);
                    sendMessage(chatId, "Регистрация успешно отменена.");
                } else {
                    try {
                        SessionTracker sessionTracker = sessionTrackers.get(chatId);
                        ChatState state = sessionTracker.getState();
                        switch (state) {
                            case AWAITING_EMAIL -> {
                                if (validateEmail(text)) {
                                    sessionTracker.setEmail(text);
                                    sessionTracker.setState(ChatState.AWAITING_PASSWORD);
                                    sendMessage(chatId, "Введите пароль от сайта https://abiturient.kpfu.ru/");
                                } else {
                                    sendMessage(chatId, "Укажите email.");
                                }
                            }
                            case AWAITING_PASSWORD -> {
                                sessionTracker.setPassword(text);
                                String password = encryptor.encrypt(sessionTracker.getPassword());
                                userService.registerUser(UserEntity.builder()
                                        .id(chatId)
                                        .email(sessionTracker.getEmail())
                                        .password(password)
                                        .build());
                                sendMessage(chatId, "Вы успешно зарегистрировались." +
                                        " Для того чтобы получить текущий рейтинг введите /check.");
                                sessionTrackers.remove(chatId);
                            }
                        }
                    } catch (EncryptingException e) {
                        log.error("Не удалось зашифровать или дешифровать пароль пользователя {}", chatId);
                        throw new RuntimeException();
                    }
                }
            } else {
                try {
                    switch (text) {
                        case "/start":
                            sendMessage(chatId, "Добро пожаловать!" +
                                    " Для регистрации отслеживания текущего рейтинга введите /register");
                            break;
                        case "/register":
                            sessionTrackers.put(chatId, new SessionTracker());
                            sendMessage(chatId,
                                    "Введите email зарегистрированный на сайте https://abiturient.kpfu.ru/");
                            break;
                        case "/check":
                            UserEntity user = userService.findUserById(chatId);
                            UserRequest request = new UserRequest(user.getEmail(),
                                    encryptor.decrypt(user.getPassword()));
                            String html = kpfuService.getAuthorizeCookies(request);
                            String h = kpfuService.getPId(request, html);
                            String uri = CookieParser.parsePId(h);
                            String response = kpfuService.getRatingPage(request, html, uri);
                            List<RatingCard> cardList = RatingParser.parseRating(response);
                            sendRating(chatId, cardList);
                            break;
                        case "/stop":
                            try {
                                userService.unregisterUser(chatId);
                                log.info("Пользователь {} успешно удален", chatId);
                                sendMessage(chatId, "Отслеживание успешно отменено.");
                            } catch (UserNotFoundException e) {
                                log.error("Пользователь {} не существует.", chatId);
                                sendMessage(chatId,
                                        "Не удалось отменить отслеживание. Возможно вы не зарегистрированы.");
                            }
                            break;
                    }

                } catch (GettingCurrentPositionException e) {
                    sendMessage(chatId, "Не удалось получить позиции. Проверьте корректность данных.");
                } catch (UserNotFoundException e) {
                    sendMessage(chatId, "Не удалось найти пользователя. Для регистрации введите /register");
                } catch (Exception e) {
                    sendMessage(chatId, "Что-то пошло не так при попытке получения рейтинга.");
                }
            }
        } catch (Exception e) {
            sendMessage(update.message().chat().id(), "Что-то пошло не так.");
        }
    }

    private void sendMessage(Long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    private boolean validateEmail(String email) {
        return email.contains("@");
    }

    private void sendRating(Long chatId, List<RatingCard> cardList) {
        sendMessage(chatId, RatingFormatter.formatRating(cardList));
    }

}
