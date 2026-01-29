package com.roguelab.event;

import com.roguelab.domain.DamageType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Emitted when the player's health reaches zero.
 */
public final class PlayerDiedEvent extends AbstractGameEvent {
    
    /**
     * What killed the player.
     */
    public enum CauseType {
        ENEMY,
        TRAP,
        POISON,
        BURNING,
        UNKNOWN
    }
    
    private final CauseType causeType;
    private final String causeId;      // Enemy ID, trap ID, etc.
    private final String causeName;    // "Goblin King", "Spike Trap", etc.
    private final DamageType damageType;
    private final int finalBlow;       // Damage of the killing hit
    private final int floor;
    private final List<String> itemsHeld;
    private final int gold;
    private final int enemiesKilledThisRun;
    
    public PlayerDiedEvent(UUID runId, int tick, CauseType causeType, String causeId,
                           String causeName, DamageType damageType, int finalBlow,
                           int floor, List<String> itemsHeld, int gold, int enemiesKilledThisRun) {
        super(EventType.PLAYER_DIED, runId, tick);
        this.causeType = causeType;
        this.causeId = causeId;
        this.causeName = causeName;
        this.damageType = damageType;
        this.finalBlow = finalBlow;
        this.floor = floor;
        this.itemsHeld = Collections.unmodifiableList(itemsHeld);
        this.gold = gold;
        this.enemiesKilledThisRun = enemiesKilledThisRun;
    }
    
    public PlayerDiedEvent(Instant timestamp, UUID runId, int tick, CauseType causeType, 
                           String causeId, String causeName, DamageType damageType, 
                           int finalBlow, int floor, List<String> itemsHeld, int gold, 
                           int enemiesKilledThisRun) {
        super(EventType.PLAYER_DIED, timestamp, runId, tick);
        this.causeType = causeType;
        this.causeId = causeId;
        this.causeName = causeName;
        this.damageType = damageType;
        this.finalBlow = finalBlow;
        this.floor = floor;
        this.itemsHeld = Collections.unmodifiableList(itemsHeld);
        this.gold = gold;
        this.enemiesKilledThisRun = enemiesKilledThisRun;
    }
    
    public CauseType getCauseType() {
        return causeType;
    }
    
    public String getCauseId() {
        return causeId;
    }
    
    public String getCauseName() {
        return causeName;
    }
    
    public DamageType getDamageType() {
        return damageType;
    }
    
    public int getFinalBlow() {
        return finalBlow;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public List<String> getItemsHeld() {
        return itemsHeld;
    }
    
    public int getGold() {
        return gold;
    }
    
    public int getEnemiesKilledThisRun() {
        return enemiesKilledThisRun;
    }
    
    @Override
    public String toString() {
        return String.format("PlayerDiedEvent[killed by %s on floor %d, %d gold, %d kills]",
            causeName, floor, gold, enemiesKilledThisRun);
    }
}
