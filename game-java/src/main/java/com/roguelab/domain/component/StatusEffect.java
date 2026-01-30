package com.roguelab.domain.component;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.StatusType;

import java.util.Objects;

/**
 * An active status effect on an entity.
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
        if (duration <= 0) throw new IllegalArgumentException("Duration must be positive");
        if (stacks <= 0) throw new IllegalArgumentException("Stacks must be positive");
        this.remainingDuration = duration;
        this.stacks = stacks;
    }
    
    public StatusType getType() { return type; }
    public EntityId getSourceId() { return sourceId; }
    public int getRemainingDuration() { return remainingDuration; }
    public int getStacks() { return stacks; }
    public boolean isExpired() { return remainingDuration <= 0; }
    
    public boolean tick() {
        remainingDuration--;
        return isExpired();
    }
    
    public void addStacks(int amount) {
        if (amount > 0) {
            int maxStacks = type.getMaxStacks();
            this.stacks = Math.min(this.stacks + amount, maxStacks);
        }
    }
    
    public void refreshDuration(int newDuration) {
        this.remainingDuration = Math.max(this.remainingDuration, newDuration);
    }
    
    public int getDamagePerTick() {
        if (!type.isDamaging()) return 0;
        return switch (type) {
            case POISONED -> 2 * stacks;
            case BURNING -> 3 * stacks;
            case BLEEDING -> 1 * stacks;
            default -> 0;
        };
    }
    
    public int getHealingPerTick() {
        if (!type.isHealing()) return 0;
        return switch (type) {
            case REGENERATING -> 3 * stacks;
            default -> 0;
        };
    }
    
    @Override
    public String toString() {
        return String.format("%s(%dt, %dx)", type.name(), remainingDuration, stacks);
    }
}
