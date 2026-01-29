package com.roguelab.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all game events.
 * 
 * Every event in RogueLab must implement this interface.
 * Events are immutable, versioned, and self-contained.
 * 
 * The event system is the backbone of telemetry - every meaningful
 * game action emits an event that can be serialized to JSON and
 * analyzed later.
 */
public interface GameEvent {
    
    /**
     * The type identifier for this event (e.g., "RUN_STARTED", "DAMAGE_DEALT").
     * Used for deserialization and filtering.
     */
    String getEventType();
    
    /**
     * Schema version for this event type (semantic versioning).
     * Allows consumers to handle schema evolution.
     */
    String getEventVersion();
    
    /**
     * When this event occurred.
     */
    Instant getTimestamp();
    
    /**
     * Unique identifier for the game run this event belongs to.
     */
    UUID getRunId();
    
    /**
     * The game tick/turn when this event occurred.
     * Tick 0 is the start of the run.
     */
    int getTick();
}
