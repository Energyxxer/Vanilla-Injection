package com.energyxxer.inject.exceptions;

/**
 * Generic "you can't start running if you already are" injector exception.
 */
public class IllegalStateException extends RuntimeException {
    /**
     * Creates an <code>IllegalStateException</code>.
     * */
    public IllegalStateException() {
    }

    /**
     * Creates an <code>IllegalStateException</code> with the given message.
     *
     * @param message The exception's message.
     * */
    public IllegalStateException(String message) {
        super(message);
    }
}
