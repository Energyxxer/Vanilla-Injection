package com.energyxxer.inject.listeners;

import com.energyxxer.inject.InjectionMaster;
import com.energyxxer.inject.Injector;

/**
 * The listener interface for receiving command success events.
 * If you're interesting in reading changes in the log file, use a LogListener instead.
 *
 * @see InjectionMaster#addSuccessListener
 * @see Injector#insertFetchCommand(String, SuccessListener)
 * @see Injector#insertFetchCommand(String, String, SuccessListener)
 */
public interface SuccessListener {
    /**
     * Fires every time an entity with the name associated with this listener successfully executes a command.
     *
     * @param e The SuccessEvent indicating that the command was successful.
     * */
    void onSuccess(SuccessEvent e);

    /**
     * Whether this listener should be removed from the list after it has been fired once.
     * Anonymous classes may override this.
     * <br>
     *     Note that calls to {@link Injector#insertFetchCommand} will automatically set this to true.
     *
     * @return true if should fire only once, false if multiple times.
     * */
    default boolean doOnce() {
        return false;
    }
}
