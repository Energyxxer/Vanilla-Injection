package com.energyxxer.inject.v2;

/**
 * @author Adrodoc55
 */
public class BufferOverflowException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public BufferOverflowException() {}

  public BufferOverflowException(String message) {
    super(message);
  }

  public BufferOverflowException(Throwable cause) {
    super(cause);
  }

  public BufferOverflowException(String message, Throwable cause) {
    super(message, cause);
  }
}
