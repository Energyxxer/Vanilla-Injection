package de.adrodoc55.common.util;

import java.util.function.Consumer;

/**
 * A {@link Consumer} with {@code throws} clause
 *
 * @author Adrodoc55
 */
@FunctionalInterface
public interface CheckedConsumer<A, T extends Throwable> {
  void accept(A a) throws T;
}
