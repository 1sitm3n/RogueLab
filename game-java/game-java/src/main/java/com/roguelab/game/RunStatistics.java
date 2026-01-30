package com.roguelab.game;

/**
 * Tracks statistics for a single game run.
 * Accumulated throughout gameplay for telemetry and end-of-run summary.
 */
public final class RunStatistics {
    
    private int roomsVisited;
    private int roomsCleared;
    private int floorsCompleted;
    private int enemiesKilled;
    private int bossesKilled;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int totalHealing;
    private int goldEarned;
    private int goldSpent;
    private int itemsCollected;
    private int itemsUsed;
    private int turnsInCombat;
    private int totalTicks;
    
    public RunStatistics() {
        // All start at 0
    }
    
    // === INCREMENTAL UPDATES ===
    
    public void recordRoomVisited() { roomsVisited++; }
    public void recordRoomCleared() { roomsCleared++; }
    public void recordFloorCompleted() { floorsCompleted++; }
    public void recordEnemyKilled() { enemiesKilled++; }
    public void recordBossKilled() { bossesKilled++; }
    public void recordDamageDealt(int amount) { totalDamageDealt += amount; }
    public void recordDamageTaken(int amount) { totalDamageTaken += amount; }
    public void recordHealing(int amount) { totalHealing += amount; }
    public void recordGoldEarned(int amount) { goldEarned += amount; }
    public void recordGoldSpent(int amount) { goldSpent += amount; }
    public void recordItemCollected() { itemsCollected++; }
    public void recordItemUsed() { itemsUsed++; }
    public void recordCombatTurns(int turns) { turnsInCombat += turns; }
    public void recordTick() { totalTicks++; }
    public void recordTicks(int ticks) { totalTicks += ticks; }
    
    // === GETTERS ===
    
    public int getRoomsVisited() { return roomsVisited; }
    public int getRoomsCleared() { return roomsCleared; }
    public int getFloorsCompleted() { return floorsCompleted; }
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getBossesKilled() { return bossesKilled; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
    public int getTotalHealing() { return totalHealing; }
    public int getGoldEarned() { return goldEarned; }
    public int getGoldSpent() { return goldSpent; }
    public int getNetGold() { return goldEarned - goldSpent; }
    public int getItemsCollected() { return itemsCollected; }
    public int getItemsUsed() { return itemsUsed; }
    public int getTurnsInCombat() { return turnsInCombat; }
    public int getTotalTicks() { return totalTicks; }
    
    // === COMPUTED METRICS ===
    
    public double getAverageRoomClearRate() {
        return roomsVisited == 0 ? 0 : (double) roomsCleared / roomsVisited;
    }
    
    public double getDamageEfficiency() {
        return totalDamageTaken == 0 ? Double.MAX_VALUE : (double) totalDamageDealt / totalDamageTaken;
    }
    
    @Override
    public String toString() {
        return String.format(
            "RunStats[rooms=%d/%d, floors=%d, kills=%d, damage=%d/%d, gold=%d]",
            roomsCleared, roomsVisited, floorsCompleted, enemiesKilled,
            totalDamageDealt, totalDamageTaken, getNetGold()
        );
    }
}
