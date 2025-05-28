package itis.kpfu.parser;

import itis.kpfu.model.RatingCard;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RatingParser {

    public List<RatingCard> parseRating(String html) {

        Document doc = Jsoup.parse(html);
        List<RatingCard> list = new ArrayList<>();
        Elements cards = doc.select(".block-light.card_rating");

        for (Element card : cards) {
            System.out.println("=== Новая заявка ===");
            List<String> fields = new ArrayList<>();
            Elements groups = card.select(".property_group");
            for (Element group : groups) {
                String name = group.selectFirst(".name") != null ? group.selectFirst(".name").text().trim() : "_";
                String value = group.selectFirst(".value") != null ? group.selectFirst(".value").text().trim() : "_";
                if (!"_".equals(name)) {
                    fields.add(name + " : " + value);
                }
            }
            Element link = card.selectFirst(".link a");
            if (link != null) {
                fields.add(link.attr("href"));
            }
            list.add(new RatingCard(
                    fields.get(0),
                    fields.get(1),
                    fields.get(2),
                    fields.get(3),
                    fields.get(4),
                    fields.get(5),
                    fields.get(6),
                    fields.get(7),
                    fields.get(8),
                    fields.get(9),
                    fields.get(10),
                    fields.get(11)
            ));
        }
        return list;
    }
}
