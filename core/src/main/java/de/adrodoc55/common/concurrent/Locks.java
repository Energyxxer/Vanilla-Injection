package de.adrodoc55.common.concurrent;

import java.util.concurrent.locks.Lock;

/**
 * @author Adrodoc55
 */
public class Locks {
  public static void runLocked(Lock lock, Runnable runnable) {
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }
}
