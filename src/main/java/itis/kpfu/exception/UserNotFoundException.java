package itis.kpfu.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long message) {
        super("Не удалось найти пользователя %d.".formatted(message));
    }
}
