package com.energyxxer.inject.listeners;

/**
 * The listener interface for receiving processing tick events. If you need to run injection code periodically, use
 * this, instead of a generic timer task attached to a timer.
 * <br>
 * To avoid the structures folder filling with structure
 * files when the game is closed or paused, the injector detects when structures aren't being loaded anymore and pauses
 * the injector until the game catches back up.
 * <br>
 * While the injector is paused, processing ticks will not occur, and therefore, code within a processing tick listener
 * will not run while the injector is paused.
 * <br>
 * The injector will ignore calls to <code>insertImpulseCommand</code> calls whilst paused, which may lead to some unexpected
 * results if using a timer task.
 */
public interface ProcessingTickListener {
    /**
     * Fires every processing tick while the injector isn't paused.
     * */
    void onTick();
}
