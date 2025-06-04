package itis.kpfu.exception;

public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super("Ошибка парсинга страницы %s".formatted(message.substring(100)));
    }
}
