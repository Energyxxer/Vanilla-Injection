package com.energyxxer.log;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

/**
 * @author Adrodoc55
 */
public class SuccessListener {
  private final String invoker;
  private final boolean repeat;
  private final Consumer<SuccessEvent> consumer;

  public SuccessListener(String invoker, boolean repeat, Consumer<SuccessEvent> consumer) {
    this.invoker = checkNotNull(invoker, "invoker == null!");
    this.repeat = repeat;
    this.consumer = checkNotNull(consumer, "consumer == null!");
  }

  /**
   * @return the value of {@link #invoker}
   */
  public String getInvoker() {
    return invoker;
  }

  /**
   * @return the value of {@link #repeat}
   */
  public boolean isRepeat() {
    return repeat;
  }

  /**
   * @return the value of {@link #consumer}
   */
  public Consumer<SuccessEvent> getConsumer() {
    return consumer;
  }

  @Override
  public String toString() {
    return "SuccessListener [invoker=" + invoker + ", repeat=" + repeat + ", consumer=" + consumer
        + "]";
  }
}
