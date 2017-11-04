package com.energyxxer.inject.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.energyxxer.inject.listeners.SuccessEvent;

public class SuccessListenerEntry {
  private final String invoker;
  private final boolean repeat;
  private final Consumer<SuccessEvent> listener;

  public SuccessListenerEntry(String invoker, boolean repeat, Consumer<SuccessEvent> listener) {
    this.invoker = checkNotNull(invoker, "invoker == null!");
    this.repeat = repeat;
    this.listener = checkNotNull(listener, "listener == null!");
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
   * @return the value of {@link #listener}
   */
  public Consumer<SuccessEvent> getListener() {
    return listener;
  }

  @Override
  public String toString() {
    return "SuccessListenerEntry [invoker=" + invoker + ", repeat=" + repeat + ", listener="
        + listener + "]";
  }
}
