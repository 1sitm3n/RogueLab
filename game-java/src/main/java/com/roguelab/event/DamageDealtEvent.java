package com.roguelab.event;

import com.roguelab.domain.DamageType;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted for each instance of damage during combat.
 * This is a high-frequency event - one per attack.
 */
public final class DamageDealtEvent extends AbstractGameEvent {
    
    /**
     * Type of entity that dealt or received damage.
     */
    public enum EntityType {
        PLAYER,
        ENEMY
    }
    
    private final String sourceId;
    private final EntityType sourceType;
    private final String targetId;
    private final EntityType targetType;
    private final int baseDamage;
    private final int finalDamage;
    private final DamageType damageType;
    private final boolean isCritical;
    private final int targetHealthBefore;
    private final int targetHealthAfter;
    private final boolean targetKilled;
    
    public DamageDealtEvent(UUID runId, int tick, String sourceId, EntityType sourceType,
                            String targetId, EntityType targetType, int baseDamage,
                            int finalDamage, DamageType damageType, boolean isCritical,
                            int targetHealthBefore, int targetHealthAfter, boolean targetKilled) {
        super(EventType.DAMAGE_DEALT, runId, tick);
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.baseDamage = baseDamage;
        this.finalDamage = finalDamage;
        this.damageType = damageType;
        this.isCritical = isCritical;
        this.targetHealthBefore = targetHealthBefore;
        this.targetHealthAfter = targetHealthAfter;
        this.targetKilled = targetKilled;
    }
    
    public DamageDealtEvent(Instant timestamp, UUID runId, int tick, String sourceId, 
                            EntityType sourceType, String targetId, EntityType targetType, 
                            int baseDamage, int finalDamage, DamageType damageType, 
                            boolean isCritical, int targetHealthBefore, int targetHealthAfter, 
                            boolean targetKilled) {
        super(EventType.DAMAGE_DEALT, timestamp, runId, tick);
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.baseDamage = baseDamage;
        this.finalDamage = finalDamage;
        this.damageType = damageType;
        this.isCritical = isCritical;
        this.targetHealthBefore = targetHealthBefore;
        this.targetHealthAfter = targetHealthAfter;
        this.targetKilled = targetKilled;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public EntityType getSourceType() {
        return sourceType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public EntityType getTargetType() {
        return targetType;
    }
    
    public int getBaseDamage() {
        return baseDamage;
    }
    
    public int getFinalDamage() {
        return finalDamage;
    }
    
    public DamageType getDamageType() {
        return damageType;
    }
    
    public boolean isCritical() {
        return isCritical;
    }
    
    public int getTargetHealthBefore() {
        return targetHealthBefore;
    }
    
    public int getTargetHealthAfter() {
        return targetHealthAfter;
    }
    
    public boolean isTargetKilled() {
        return targetKilled;
    }
    
    @Override
    public String toString() {
        return String.format("DamageDealtEvent[%s->%s, %d dmg%s, HP:%d->%d%s]",
            sourceId, targetId, finalDamage, isCritical ? " (CRIT)" : "",
            targetHealthBefore, targetHealthAfter, targetKilled ? " (KILLED)" : "");
    }
}
