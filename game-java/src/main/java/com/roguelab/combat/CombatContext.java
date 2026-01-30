package com.roguelab.combat;

import com.roguelab.domain.Enemy;
import com.roguelab.domain.Player;
import com.roguelab.domain.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds mutable state during a combat encounter.
 * Passed through combat methods to track progress.
 */
public final class CombatContext {
    
    private final String runId;
    private final Player player;
    private final Room room;
    
    private int currentTurn;
    private int startTick;
    private int currentTick;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int goldEarned;
    private int experienceGained;
    private final List<String> killedEnemyIds;
    
    public CombatContext(String runId, Player player, Room room, int startTick) {
        this.runId = Objects.requireNonNull(runId);
        this.player = Objects.requireNonNull(player);
        this.room = Objects.requireNonNull(room);
        this.startTick = startTick;
        this.currentTick = startTick;
        this.currentTurn = 0;
        this.totalDamageDealt = 0;
        this.totalDamageTaken = 0;
        this.goldEarned = 0;
        this.experienceGained = 0;
        this.killedEnemyIds = new ArrayList<>();
    }
    
    // === GETTERS ===
    
    public String getRunId() {
        return runId;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Room getRoom() {
        return room;
    }
    
    public int getCurrentTurn() {
        return currentTurn;
    }
    
    public int getStartTick() {
        return startTick;
    }
    
    public int getCurrentTick() {
        return currentTick;
    }
    
    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }
    
    public int getTotalDamageTaken() {
        return totalDamageTaken;
    }
    
    public int getGoldEarned() {
        return goldEarned;
    }
    
    public int getExperienceGained() {
        return experienceGained;
    }
    
    public List<String> getKilledEnemyIds() {
        return new ArrayList<>(killedEnemyIds);
    }
    
    // === COMBAT STATE ===
    
    public List<Enemy> getAliveEnemies() {
        return room.getAliveEnemies();
    }
    
    public boolean hasAliveEnemies() {
        return !getAliveEnemies().isEmpty();
    }
    
    public boolean isPlayerAlive() {
        return player.isAlive();
    }
    
    public boolean isCombatActive() {
        return isPlayerAlive() && hasAliveEnemies();
    }
    
    // === MUTATORS ===
    
    public void nextTurn() {
        currentTurn++;
        currentTick++;
    }
    
    public void advanceTick() {
        currentTick++;
    }
    
    public void addDamageDealt(int amount) {
        totalDamageDealt += amount;
    }
    
    public void addDamageTaken(int amount) {
        totalDamageTaken += amount;
    }
    
    public void recordKill(Enemy enemy) {
        killedEnemyIds.add(enemy.getId().value());
        goldEarned += enemy.calculateGoldDrop();
        experienceGained += enemy.calculateExperience();
    }
    
    public int getTurnsElapsed() {
        return currentTurn;
    }
    
    public CombatResult buildResult() {
        if (player.isDead()) {
            return CombatResult.defeat(
                currentTurn, totalDamageDealt, totalDamageTaken, killedEnemyIds
            );
        } else {
            return CombatResult.victory(
                currentTurn, totalDamageDealt, totalDamageTaken,
                goldEarned, experienceGained, killedEnemyIds
            );
        }
    }
}
