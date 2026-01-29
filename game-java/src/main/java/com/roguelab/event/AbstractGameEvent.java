package com.roguelab.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all game events.
 * Provides common fields and validation.
 * 
 * Subclasses should be implemented as immutable records or final classes.
 */
public abstract class AbstractGameEvent implements GameEvent {
    
    private final EventType eventType;
    private final Instant timestamp;
    private final UUID runId;
    private final int tick;
    
    protected AbstractGameEvent(EventType eventType, UUID runId, int tick) {
        this(eventType, Instant.now(), runId, tick);
    }
    
    protected AbstractGameEvent(EventType eventType, Instant timestamp, UUID runId, int tick) {
        this.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        this.runId = Objects.requireNonNull(runId, "runId cannot be null");
        if (tick < 0) {
            throw new IllegalArgumentException("tick cannot be negative");
        }
        this.tick = tick;
    }
    
    @Override
    public String getEventType() {
        return eventType.name();
    }
    
    @Override
    public String getEventVersion() {
        return eventType.getCurrentVersion();
    }
    
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public UUID getRunId() {
        return runId;
    }
    
    @Override
    public int getTick() {
        return tick;
    }
    
    /**
     * Get the event type enum value.
     */
    public EventType getEventTypeEnum() {
        return eventType;
    }
}
