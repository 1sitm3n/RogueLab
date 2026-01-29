package com.roguelab.event;

import com.roguelab.domain.StatusType;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a status effect is applied to any entity.
 */
public final class StatusAppliedEvent extends AbstractGameEvent {
    
    private final String targetId;
    private final DamageDealtEvent.EntityType targetType;
    private final StatusType statusType;
    private final int duration;
    private final int stacks;
    private final String sourceId;
    private final DamageDealtEvent.EntityType sourceType;
    
    public StatusAppliedEvent(UUID runId, int tick, String targetId, 
                              DamageDealtEvent.EntityType targetType, StatusType statusType,
                              int duration, int stacks, String sourceId, 
                              DamageDealtEvent.EntityType sourceType) {
        super(EventType.STATUS_APPLIED, runId, tick);
        this.targetId = targetId;
        this.targetType = targetType;
        this.statusType = statusType;
        this.duration = duration;
        this.stacks = stacks;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
    }
    
    public StatusAppliedEvent(Instant timestamp, UUID runId, int tick, String targetId,
                              DamageDealtEvent.EntityType targetType, StatusType statusType,
                              int duration, int stacks, String sourceId,
                              DamageDealtEvent.EntityType sourceType) {
        super(EventType.STATUS_APPLIED, timestamp, runId, tick);
        this.targetId = targetId;
        this.targetType = targetType;
        this.statusType = statusType;
        this.duration = duration;
        this.stacks = stacks;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public DamageDealtEvent.EntityType getTargetType() {
        return targetType;
    }
    
    public StatusType getStatusType() {
        return statusType;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public int getStacks() {
        return stacks;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public DamageDealtEvent.EntityType getSourceType() {
        return sourceType;
    }
    
    @Override
    public String toString() {
        return String.format("StatusAppliedEvent[%s on %s, %d stacks, %d turns]",
            statusType, targetId, stacks, duration);
    }
}
