package com.roguelab.telemetry;

import com.roguelab.event.GameEvent;

/**
 * Interface for emitting telemetry events.
 * Implementations may write to files, send over network, etc.
 */
public interface TelemetryEmitter {
    
    /**
     * Emit an event to the telemetry system.
     * @param event The event to emit
     * @throws TelemetryException if emission fails
     */
    void emit(GameEvent event);
    
    /**
     * Flush any buffered events.
     * @throws TelemetryException if flush fails
     */
    void flush();
    
    /**
     * Close the emitter and release resources.
     */
    void close();
}
