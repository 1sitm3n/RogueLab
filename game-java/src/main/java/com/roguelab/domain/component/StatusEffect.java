package com.roguelab.domain.component;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.StatusType;

import java.util.Objects;

/**
 * An active status effect on an entity.
 * Status effects have a duration (in turns) and optional stacking.
 */
public final class StatusEffect {
    
    private final StatusType type;
    private final EntityId sourceId;
    private int remainingDuration;
    private int stacks;
    
    public StatusEffect(StatusType type, EntityId sourceId, int duration) {
        this(type, sourceId, duration, 1);
    }
    
    public StatusEffect(StatusType type, EntityId sourceId, int duration, int stacks) {
        this.type = Objects.requireNonNull(type);
        this.sourceId = Objects.requireNonNull(sourceId);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (stacks <= 0) {
            throw new IllegalArgumentException("Stacks must be positive");
        }
        this.remainingDuration = duration;
        this.stacks = stacks;
    }
    
    public StatusType getType() {
        return type;
    }
    
    public EntityId getSourceId() {
        return sourceId;
    }
    
    public int getRemainingDuration() {
        return remainingDuration;
    }
    
    public int getStacks() {
        return stacks;
    }
    
    public boolean isExpired() {
        return remainingDuration <= 0;
    }
    
    /**
     * Decrement duration by one turn.
     * @return true if the effect has expired
     */
    public boolean tick() {
        remainingDuration--;
        return isExpired();
    }
    
    /**
     * Add stacks to this effect.
     */
    public void addStacks(int amount) {
        if (amount > 0) {
            this.stacks += amount;
        }
    }
    
    /**
     * Refresh duration (e.g., when effect is reapplied).
     */
    public void refreshDuration(int newDuration) {
        this.remainingDuration = Math.max(this.remainingDuration, newDuration);
    }
    
    /**
     * Calculate damage-over-time for this tick.
     * Only applicable for damaging status types.
     */
    public int calculateTickDamage() {
        if (!type.isDamaging()) {
            return 0;
        }
        // Base damage per stack per tick
        return switch (type) {
            case POISON -> 2 * stacks;
            case BURNING -> 3 * stacks;
            default -> 0;
        };
    }
    
    @Override
    public String toString() {
        return String.format("%s x%d (%d turns)", type, stacks, remainingDuration);
    }
}
