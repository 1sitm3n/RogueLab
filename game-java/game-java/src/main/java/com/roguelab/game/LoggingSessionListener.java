package com.roguelab.game;

import com.roguelab.combat.CombatResult;
import com.roguelab.domain.Enemy;
import com.roguelab.domain.Item;
import com.roguelab.domain.Room;
import com.roguelab.dungeon.Floor;

/**
 * A session listener that logs events to the console.
 * Useful for debugging and CLI gameplay.
 */
public final class LoggingSessionListener implements GameSessionListener {
    
    private final boolean verbose;
    
    public LoggingSessionListener() {
        this(false);
    }
    
    public LoggingSessionListener(boolean verbose) {
        this.verbose = verbose;
    }
    
    @Override
    public void onRunStarted(GameSession session) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.printf("  RUN STARTED: %s%n", session.getRunId());
        System.out.printf("  Player: %s the %s%n", 
            session.getPlayer().getName(), 
            session.getPlayer().getPlayerClass());
        System.out.printf("  Seed: %d | Difficulty: %s%n", 
            session.getSeed(), session.getDifficulty());
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
    }
    
    @Override
    public void onFloorEntered(GameSession session, Floor floor) {
        System.out.println();
        System.out.println("───────────────────────────────────────────────");
        System.out.printf("  FLOOR %d%s%n", 
            floor.getFloorNumber(),
            floor.hasBoss() ? " (BOSS FLOOR)" : "");
        System.out.printf("  Rooms: %d | Current: %d%n", 
            floor.getRoomCount(), floor.getCurrentRoomIndex() + 1);
        System.out.println("───────────────────────────────────────────────");
    }
    
    @Override
    public void onRoomEntered(GameSession session, Room room) {
        System.out.println();
        int roomIndex = session.getCurrentFloor().getCurrentRoomIndex();
        System.out.printf("[Room %d] %s%n", roomIndex + 1, room.getType());
        
        if (!room.getEnemies().isEmpty()) {
            System.out.println("  Enemies:");
            for (Enemy enemy : room.getEnemies()) {
                String status = enemy.isDead() ? " [DEAD]" : "";
                System.out.printf("    • %s HP:%d/%d%s%n", 
                    enemy.getName(),
                    enemy.getHealth().getCurrent(),
                    enemy.getHealth().getMaximum(),
                    status);
            }
        }
        
        if (!room.getItems().isEmpty()) {
            System.out.println("  Items:");
            for (Item item : room.getItems()) {
                System.out.printf("    • %s (%s)%n", item.getName(), item.getRarity());
            }
        }
    }
    
    @Override
    public void onRoomCleared(GameSession session, Room room) {
        int roomIndex = session.getCurrentFloor().getCurrentRoomIndex();
        System.out.printf("[Room %d] CLEARED!%n", roomIndex + 1);
    }
    
    @Override
    public void onCombatCompleted(GameSession session, CombatResult result) {
        System.out.println();
        if (result.isVictory()) {
            System.out.println("  ★ VICTORY ★");
        } else {
            System.out.println("  ✗ DEFEAT ✗");
        }
        
        System.out.printf("  Turns: %d | Damage: %d dealt / %d taken%n",
            result.turnsElapsed(),
            result.totalDamageDealt(),
            result.totalDamageTaken());
        
        if (result.isVictory()) {
            System.out.printf("  Rewards: %d gold, %d XP%n",
                result.goldEarned(),
                result.experienceGained());
        }
        
        // Player status
        System.out.printf("  Player HP: %d/%d%n",
            session.getPlayer().getHealth().getCurrent(),
            session.getPlayer().getHealth().getMaximum());
    }
    
    @Override
    public void onItemPicked(GameSession session, Item item) {
        System.out.printf("  + Picked up: %s%n", item.getName());
    }
    
    @Override
    public void onItemUsed(GameSession session, Item item) {
        System.out.printf("  Used: %s%n", item.getName());
    }
    
    @Override
    public void onShopPurchase(GameSession session, Item item, int cost) {
        System.out.printf("  $ Purchased %s for %d gold%n", item.getName(), cost);
    }
    
    @Override
    public void onPlayerRested(GameSession session, int healAmount) {
        System.out.printf("  ♥ Rested and healed %d HP%n", healAmount);
    }
    
    @Override
    public void onPlayerLevelUp(GameSession session, int newLevel) {
        System.out.println();
        System.out.printf("  ★★★ LEVEL UP! Now level %d ★★★%n", newLevel);
    }
    
    @Override
    public void onRunEnded(GameSession session, RunEndReason reason) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.printf("  RUN ENDED: %s%n", reason);
        System.out.println("───────────────────────────────────────────────");
        
        RunStatistics stats = session.getStatistics();
        System.out.printf("  Floors: %d | Rooms: %d/%d%n",
            stats.getFloorsCompleted(),
            stats.getRoomsCleared(),
            stats.getRoomsVisited());
        System.out.printf("  Kills: %d (Bosses: %d)%n",
            stats.getEnemiesKilled(),
            stats.getBossesKilled());
        System.out.printf("  Damage: %d dealt / %d taken%n",
            stats.getTotalDamageDealt(),
            stats.getTotalDamageTaken());
        System.out.printf("  Gold: %d earned, %d spent%n",
            stats.getGoldEarned(),
            stats.getGoldSpent());
        System.out.printf("  Items: %d collected, %d used%n",
            stats.getItemsCollected(),
            stats.getItemsUsed());
        
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
    }
}
