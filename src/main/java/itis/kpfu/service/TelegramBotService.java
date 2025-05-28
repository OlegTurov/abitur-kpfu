package itis.kpfu.service;

import com.pengrad.telegrambot.request.SendMessage;
import itis.kpfu.config.BotConfig;
import itis.kpfu.exception.DecryptingException;
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
@RequiredArgsConstructor
@EnableConfigurationProperties(BotConfig.class)
public class TelegramBotService {

    private final BotConfig botConfig;
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
        if (update.message() == null || update.message().text() == null || update.message().text().isBlank()) return;
        long chatId = update.message().chat().id();
        String text = update.message().text();
        log.info("Processing update with ID {} and text {}.", chatId, text);
        if (sessionTrackers.containsKey(chatId)) {
            SessionTracker sessionTracker = sessionTrackers.get(chatId);
            ChatState state = sessionTracker.getState();
            switch (state){
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
                    userService.registerUser(UserEntity.builder()
                            .id(chatId)
                            .email(sessionTracker.getEmail())
                            .password(encryptPassword(sessionTracker.getPassword()))
                            .build());
                    sendMessage(chatId, "Вы успешно зарегистрировались." +
                            " Для того чтобы получить текущий рейтинг введите /check.");
                    sessionTrackers.remove(chatId);
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
                        sendMessage(chatId, "Введите email зарегистрированный на сайте https://abiturient.kpfu.ru/");
                        break;
                    case "/check":
                        UserEntity user = userService.findUserById(chatId);
                        UserRequest request = new UserRequest(user.getEmail(), decryptPassword(user.getPassword()));
                        String html = kpfuService.getAuthorizeCookies(request);
                        String h = kpfuService.getPId(request, html);
                        String uri = CookieParser.parsePId(h);
                        String response = kpfuService.getRatingPage(request, html, uri);
                        List<RatingCard> cardList = RatingParser.parseRating(response);
                        sendRating(chatId, cardList);
                        break;
                    case "/stop":
                        userService.unregisterUser(chatId);
                        break;
                }

            } catch (GettingCurrentPositionException e) {
                sendMessage(chatId, "Не удалось получить позиции. Проверьте корректность данных.");
            } catch (UserNotFoundException e) {
                sendMessage(chatId, "Не удалось найти пользователя. Для регистрации введите /register");
            } catch (Exception e) {
                sendMessage(chatId, "Что-то пошло не так.");
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    private boolean validateEmail(String email) {
        return email.contains("@");
    }


    private String encryptPassword(String password) {
        try {
            SecretKeySpec secretKey = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptingException();
        }
    }

    private String decryptPassword(String password) {
        try {
            SecretKeySpec secretKey = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(password);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptingException();
        }
    }

    private SecretKeySpec getSecretKeySpec() {
        byte[] keyBytes = botConfig.aesKey().getBytes(StandardCharsets.UTF_8);
        byte[] key = Arrays.copyOf(keyBytes, keyBytes.length);
        return new SecretKeySpec(key, "AES");
    }

    private void sendRating(Long chatId, List<RatingCard> cardList) {
        sendMessage(chatId, RatingFormatter.formatRating(cardList));
    }

}
