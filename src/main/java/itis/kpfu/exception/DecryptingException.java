package itis.kpfu.exception;

public class DecryptingException extends RuntimeException {
  public DecryptingException() {
    super("Что-то произошло во время дешифрования пароля.");
  }
}
