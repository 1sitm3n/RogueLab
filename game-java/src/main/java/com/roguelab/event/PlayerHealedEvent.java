package com.roguelab.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when player health is restored.
 */
public final class PlayerHealedEvent extends AbstractGameEvent {
    
    /**
     * What healed the player.
     */
    public enum HealSource {
        CONSUMABLE,
        REST_SITE,
        LEVEL_UP,
        REGENERATING,  // Status effect
        PASSIVE,       // Equipment passive
        UNKNOWN
    }
    
    private final HealSource source;
    private final String sourceId;  // Item ID, room ID, etc. (may be null)
    private final int amount;
    private final int healthBefore;
    private final int healthAfter;
    private final int maxHealth;
    private final int overheal;  // Amount that would have exceeded max
    
    public PlayerHealedEvent(UUID runId, int tick, HealSource source, String sourceId,
                             int amount, int healthBefore, int healthAfter, 
                             int maxHealth, int overheal) {
        super(EventType.PLAYER_HEALED, runId, tick);
        this.source = source;
        this.sourceId = sourceId;
        this.amount = amount;
        this.healthBefore = healthBefore;
        this.healthAfter = healthAfter;
        this.maxHealth = maxHealth;
        this.overheal = overheal;
    }
    
    public PlayerHealedEvent(Instant timestamp, UUID runId, int tick, HealSource source, 
                             String sourceId, int amount, int healthBefore, int healthAfter,
                             int maxHealth, int overheal) {
        super(EventType.PLAYER_HEALED, timestamp, runId, tick);
        this.source = source;
        this.sourceId = sourceId;
        this.amount = amount;
        this.healthBefore = healthBefore;
        this.healthAfter = healthAfter;
        this.maxHealth = maxHealth;
        this.overheal = overheal;
    }
    
    public HealSource getSource() {
        return source;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getHealthBefore() {
        return healthBefore;
    }
    
    public int getHealthAfter() {
        return healthAfter;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public int getOverheal() {
        return overheal;
    }
    
    @Override
    public String toString() {
        return String.format("PlayerHealedEvent[%s +%d HP, %d->%d/%d]",
            source, amount, healthBefore, healthAfter, maxHealth);
    }
}
