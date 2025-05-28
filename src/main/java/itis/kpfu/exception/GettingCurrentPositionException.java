package itis.kpfu.exception;

public class GettingCurrentPositionException extends RuntimeException {
    public GettingCurrentPositionException(String email) {
        super("Не удалось обработать получение текущей позиции для пользователя %s".formatted(email));
    }
}
