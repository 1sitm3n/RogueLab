package com.roguelab.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a boss enemy is killed.
 */
public final class BossDefeatedEvent extends AbstractGameEvent {
    
    private final String bossId;
    private final String bossName;
    private final int floor;
    private final int combatTurns;
    private final int damageDealt;
    private final int damageTaken;
    private final int playerHealthRemaining;
    private final int rewardGold;
    private final RewardItem rewardItem; // May be null
    
    public BossDefeatedEvent(UUID runId, int tick, String bossId, String bossName,
                             int floor, int combatTurns, int damageDealt, int damageTaken,
                             int playerHealthRemaining, int rewardGold, RewardItem rewardItem) {
        super(EventType.BOSS_DEFEATED, runId, tick);
        this.bossId = bossId;
        this.bossName = bossName;
        this.floor = floor;
        this.combatTurns = combatTurns;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.playerHealthRemaining = playerHealthRemaining;
        this.rewardGold = rewardGold;
        this.rewardItem = rewardItem;
    }
    
    public BossDefeatedEvent(Instant timestamp, UUID runId, int tick, String bossId, 
                             String bossName, int floor, int combatTurns, int damageDealt, 
                             int damageTaken, int playerHealthRemaining, int rewardGold, 
                             RewardItem rewardItem) {
        super(EventType.BOSS_DEFEATED, timestamp, runId, tick);
        this.bossId = bossId;
        this.bossName = bossName;
        this.floor = floor;
        this.combatTurns = combatTurns;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.playerHealthRemaining = playerHealthRemaining;
        this.rewardGold = rewardGold;
        this.rewardItem = rewardItem;
    }
    
    public String getBossId() {
        return bossId;
    }
    
    public String getBossName() {
        return bossName;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public int getCombatTurns() {
        return combatTurns;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public int getDamageTaken() {
        return damageTaken;
    }
    
    public int getPlayerHealthRemaining() {
        return playerHealthRemaining;
    }
    
    public int getRewardGold() {
        return rewardGold;
    }
    
    public RewardItem getRewardItem() {
        return rewardItem;
    }
    
    @Override
    public String toString() {
        return String.format("BossDefeatedEvent[%s on floor %d, %d turns, reward=%d gold]",
            bossName, floor, combatTurns, rewardGold);
    }
}
