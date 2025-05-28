package itis.kpfu.formatter;

import itis.kpfu.model.RatingCard;
import lombok.experimental.UtilityClass;

import java.util.List;



@UtilityClass
public class RatingFormatter {

    public String formatRating(List<RatingCard> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            RatingCard card = cards.get(i);

            sb.append("▛ ").append(card.name()).append(" ▜").append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").append("\n");

            appendField(sb, "Достижения", card.achievements());
            appendField(sb, "Направление", card.direction());
            appendField(sb, "Всего", card.total());
            appendField(sb, "Форма", card.form());
            appendField(sb, "Документ", card.document());
            appendField(sb, "Финансирование", card.financing());
            appendField(sb, "План", card.plan());
            appendField(sb, "Основание", card.basis());
            appendField(sb, "Остаток", card.remaining());
            appendField(sb, "Приоритет", card.priority());

            sb.append("┣ Ссылка: ").append(card.url()).append("\n");

            if (i < cards.size() - 1) {
                sb.append("\n▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄\n\n");
            } else {
                sb.append("▗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━▖");
            }
        }
        return sb.toString();
    }

    private void appendField(StringBuilder sb, String fieldName, String value) {
        String formattedValue = formatLongText(value);
        sb.append("┃ ")
                .append(fieldName)
                .append(": ")
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
