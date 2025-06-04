package itis.kpfu.exception;

public class UserExistsException extends RuntimeException {
    public UserExistsException(Long id) {
        super("Пользователь %d уже существует.".formatted(id));
    }
}
