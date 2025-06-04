package itis.kpfu.formatter;

import itis.kpfu.model.RatingCard;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class RatingFormatter {

    public String formatRating(List<RatingCard> cards) {
        StringBuilder sb = new StringBuilder();
        if (!cards.isEmpty()) {
            for (int i = 0; i < cards.size(); i++) {
                RatingCard card = cards.get(i);

                sb.append("▛ ").append(card.name()).append(" ▜").append("\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").append("\n");

                appendField(sb, card.achievements());
                appendField(sb, card.direction());
                appendField(sb, card.total());
                appendField(sb, card.form());
                appendField(sb, card.document());
                appendField(sb, card.financing());
                appendField(sb, card.plan());
                appendField(sb, card.basis());
                appendField(sb, card.remaining());
                appendField(sb, card.priority());

                sb.append("┣ Ссылка: ").append(card.url()).append("\n");

                if (i < cards.size() - 1) {
                    sb.append("\n▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄\n\n");
                } else {
                    sb.append("▗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━▖");
                }
            }
        } else {
            sb.append("Список заявлений пуст");
        }
        return sb.toString();
    }

    private void appendField(StringBuilder sb, String value) {
        String formattedValue = formatLongText(value);
        sb.append("┃ ")
                .append(formattedValue)
                .append("\n");
    }

    private String formatLongText(String text) {
        if (text == null || text.isEmpty()) return "-";

        StringBuilder formatted = new StringBuilder();
        String[] words = text.split("\\s+");
        int currentLength = 0;

        for (String word : words) {
            if (currentLength + word.length() + 1 > 40) {
                formatted.append("\n┃     ");
                currentLength = 0;
            }
            formatted.append(word).append(" ");
            currentLength += word.length() + 1;
        }

        return formatted.toString().trim();
    }

}
