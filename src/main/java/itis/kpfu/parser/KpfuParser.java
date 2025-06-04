package itis.kpfu.parser;

import itis.kpfu.exception.ParsingException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Log4j2
public class KpfuParser {

    public String parsePId(String html) {
        try {
            Pattern pattern = Pattern.compile("(?:window|document)\\.location\\.href\\s*=\\s*[\"'][^\"']*\\?([^\"']+)[\"']");
            Matcher urlMatcher = pattern.matcher(html);
            String uri = "";
            while (urlMatcher.find()) {
                uri = urlMatcher.group(1);
            }
            String[] parts = uri.split("&");
            for (String part : parts) {
                if (part.startsWith("p_page") || part.startsWith("p_vib")) {
                    uri = uri.replace("&" + part, "");
                }
            }
            return "/entrant/abiturient_new_form.rating?%s"
                    .formatted("p_type_menu=6&p_educ_choice_type=1&%s"
                            .formatted(uri).replace("lan", "lang"));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ParsingException(html);
        }
    }

    public String prepareGettingPIdUri(String html) {
        try {
            Pattern pattern = Pattern.compile("(?:window|document)\\.location\\.href\\s*=\\s*[\"'][^\"']*\\?([^\"']+)[\"']");
            Matcher urlMatcher = pattern.matcher(html);
            String uri = "";
            while (urlMatcher.find()) {
                uri = urlMatcher.group(1);
            }
            return "/entrant/abiturient_cabinet.entrant_info?%s".formatted(uri);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ParsingException(html);
        }
    }

    public LinkedMultiValueMap<String, String> prepareCookies(String html) {
        try {
            Pattern cookiePattern = Pattern.compile("setCookie\\s*\\(\\s*'([^']+)'\\s*,\\s*'([^']+)'");
            Matcher cookieMatcher = cookiePattern.matcher(html);

            LinkedMultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();
            while (cookieMatcher.find()) {
                String name = cookieMatcher.group(1);
                String value = cookieMatcher.group(2);
                cookies.put(name, Collections.singletonList(value));
            }

            return cookies;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ParsingException(html);
        }
    }

}
