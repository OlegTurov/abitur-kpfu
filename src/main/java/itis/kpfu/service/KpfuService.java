package itis.kpfu.service;

import itis.kpfu.exception.GettingCurrentPositionException;
import itis.kpfu.parser.KpfuParser;
import itis.kpfu.request.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class KpfuService {

    private final WebClient.Builder client;
    private final Map<Long, MultiValueMap<String, String>> cookies = new HashMap<>();

    public String getAuthorizeCookies(UserRequest request) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("p_mail", request.getEmail());
            formData.add("p_pass", request.getPassword());
            formData.add("x", String.valueOf(request.getX()));
            formData.add("y", String.valueOf(request.getY()));
            formData.add("p_video_p", request.getVideo());
            formData.add("p_lan", request.getLan());


            String html = client.baseUrl("https://abitur-2024.kpfu.ru")
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 YaBrowser/25.4.0.0 Safari/537.36")
                    .build()
                    .post()
                    .uri("/entrant/abit_registration.kfuscript")
                    .header("accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;" +
                                    "q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "ru,en;q=0.9,cy;q=0.8")
                    .header("cache-control", "max-age=0")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("origin", "https://abitur-2024.kpfu.ru")
                    .header("priority", "u=0, i")
                    .header("referer", "https://abitur-2024.kpfu.ru/")
                    .header("sec-ch-ua", "\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\"," +
                            " \"YaBrowser\";v=\"25.4\", \"Yowser\";v=\"2.5\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(success -> log.info("Ответ успешно получен для пользователя %s"
                            .formatted(request.getEmail())))
                    .block();
            return html;
        } catch (Exception e) {
            log.error(e);
            throw new GettingCurrentPositionException(request.getEmail());
        }
    }

    public String getPId(UserRequest request, String authorizePage) {
        try {
            MultiValueMap<String, String> cookie = KpfuParser.prepareCookies(authorizePage);
            cookies.put(request.getId(), cookie);
            return client.baseUrl("https://abitur-2024.kpfu.ru")
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 YaBrowser/25.4.0.0 Safari/537.36")
                    .build()
                    .get()
                    .uri(KpfuParser.prepareGettingPIdUri(authorizePage))
                    .cookies(cookies -> cookies.addAll(cookie))
                    .header("accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;" +
                                    "q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "ru,en;q=0.9,cy;q=0.8")
                    .header("cache-control", "max-age=0")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("origin", "https://abitur-2024.kpfu.ru")
                    .header("priority", "u=0, i")
                    .header("referer", "https://abitur-2024.kpfu.ru/")
                    .header("sec-ch-ua", "\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\"," +
                            " \"YaBrowser\";v=\"25.4\", \"Yowser\";v=\"2.5\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(success -> log.info("Рейтинг успешно получен для пользователя %s"
                            .formatted(request.getEmail())))
                    .block();
        } catch (Exception e) {
            log.error(e);
            throw new GettingCurrentPositionException(request.getEmail());
        }
    }

    public String getRatingPage(UserRequest request, String uri) {
        try {
            MultiValueMap<String, String> cookiesMap = cookies.get(request.getId());
            return client.baseUrl("https://abitur-2024.kpfu.ru")
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 YaBrowser/25.4.0.0 Safari/537.36")
                    .build()
                    .get()
                    .uri(uri)
                    .cookies(cookies1 -> cookies1.addAll(cookiesMap))
                    .header("accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;" +
                                    "q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "ru,en;q=0.9,cy;q=0.8")
                    .header("cache-control", "max-age=0")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("origin", "https://abitur-2024.kpfu.ru")
                    .header("priority", "u=0, i")
                    .header("referer", "https://abitur-2024.kpfu.ru/")
                    .header("sec-ch-ua", "\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\"," +
                            " \"YaBrowser\";v=\"25.4\", \"Yowser\";v=\"2.5\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(success -> log.info("Рейтинг успешно получен для пользователя %s"
                            .formatted(request.getEmail())))
                    .block();
        } catch (Exception e) {
            log.error(e);
            throw new GettingCurrentPositionException(request.getEmail());
        }
    }

}
