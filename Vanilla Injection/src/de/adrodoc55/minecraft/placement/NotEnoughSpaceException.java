package de.adrodoc55.minecraft.placement;

/**
 * An exception indicating that there was not enough space to place command blocks in a certain
 * area.
 *
 * @author Adrodoc55
 */
public class NotEnoughSpaceException extends Exception {
  private static final long serialVersionUID = 1L;

  public NotEnoughSpaceException() {}

  public NotEnoughSpaceException(String message) {
    super(message);
  }

  public NotEnoughSpaceException(Throwable cause) {
    super(cause);
  }

  public NotEnoughSpaceException(String message, Throwable cause) {
    super(message, cause);
  }
}
