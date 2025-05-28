package itis.kpfu.parser;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CookieParser {

    public String parsePId(String html) {
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
    }

}
