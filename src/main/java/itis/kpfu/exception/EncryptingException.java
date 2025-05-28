package itis.kpfu.exception;

public class EncryptingException extends RuntimeException {
    public EncryptingException() {
        super("Что-то произошло во время шифрования пароля.");
    }
}
