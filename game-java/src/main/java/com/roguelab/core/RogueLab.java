package com.roguelab.core;

import com.roguelab.combat.*;
import com.roguelab.domain.*;
import com.roguelab.util.GameRandom;

/**
 * Main entry point for RogueLab.
 * Demonstrates the combat system.
 */
public final class RogueLab {
    
    public static final String VERSION = "0.1.0";
    
    public static void main(String[] args) {
        printBanner();
        runCombatDemo();
    }
    
    private static void printBanner() {
        System.out.println("+--------------------------------------+");
        System.out.println("|           R O G U E L A B            |");
        System.out.println("|              v" + VERSION + "                  |");
        System.out.println("+--------------------------------------+");
        System.out.println();
    }
    
    private static void runCombatDemo() {
        System.out.println("=== Combat System Demo ===");
        System.out.println();
        
        // Setup
        long seed = System.currentTimeMillis();
        GameRandom random = new GameRandom(seed);
        String runId = "run_" + seed;
        
        System.out.println("Seed: " + seed);
        System.out.println();
        
        // Create player
        Player player = new Player("Hero", PlayerClass.WARRIOR);
        System.out.println("Created: " + player);
        System.out.println();
        
        // Create combat engine with logging listener
        CombatEngine combatEngine = new CombatEngine(random);
        combatEngine.setEventListener(new LoggingCombatListener());
        
        // === Combat 1: Easy Fight (2 rats) ===
        System.out.println("--- Combat 1: Easy Fight ---");
        Room room1 = new Room(EntityId.of("room_1"), RoomType.COMBAT, 1, 0);
        room1.addEnemy(new Enemy(EnemyType.RAT, 1));
        room1.addEnemy(new Enemy(EnemyType.RAT, 1));
        
        printRoomInfo(room1);
        
        CombatResult result1 = combatEngine.runCombat(runId, player, room1, random, 0);
        
        printCombatResult(result1);
        printPlayerStatus(player);
        System.out.println();
        
        // === Combat 2: Medium Fight (goblin + slime) ===
        System.out.println("--- Combat 2: Medium Fight ---");
        Room room2 = new Room(EntityId.of("room_2"), RoomType.COMBAT, 1, 1);
        room2.addEnemy(new Enemy(EnemyType.GOBLIN, 1));
        room2.addEnemy(new Enemy(EnemyType.SLIME, 1));
        
        printRoomInfo(room2);
        
        CombatResult result2 = combatEngine.runCombat(runId, player, room2, random, 10);
        
        printCombatResult(result2);
        printPlayerStatus(player);
        System.out.println();
        
        // === Combat 3: Challenging Fight (orc) ===
        System.out.println("--- Combat 3: Challenging Fight ---");
        Room room3 = new Room(EntityId.of("room_3"), RoomType.COMBAT, 2, 0);
        room3.addEnemy(new Enemy(EnemyType.ORC, 2));
        
        printRoomInfo(room3);
        
        // Heal player a bit first
        int healed = player.getHealth().heal(30);
        if (healed > 0) {
            System.out.println("Player healed for " + healed + " HP");
        }
        
        CombatResult result3 = combatEngine.runCombat(runId, player, room3, random, 20);
        
        printCombatResult(result3);
        printPlayerStatus(player);
        System.out.println();
        
        // Summary
        System.out.println("=== Demo Complete ===");
        System.out.println("Total enemies killed: " + player.getEnemiesKilled());
        System.out.println("Total gold earned: " + player.getInventory().getGold());
        System.out.println("Player level: " + player.getLevel());
        System.out.println("Player XP: " + player.getExperience());
    }
    
    private static void printRoomInfo(Room room) {
        System.out.println("Room: " + room.getId().value() + " (" + room.getType() + ")");
        System.out.println("Enemies:");
        for (Enemy enemy : room.getEnemies()) {
            System.out.println("  - " + enemy.getName() + 
                " [" + enemy.getId().value() + "] HP:" + 
                enemy.getHealth().getCurrent() + "/" + enemy.getHealth().getMaximum());
        }
        System.out.println();
    }
    
    private static void printCombatResult(CombatResult result) {
        System.out.println("Result: " + result.outcome());
        System.out.println("  Turns: " + result.turnsElapsed());
        System.out.println("  Damage dealt: " + result.totalDamageDealt());
        System.out.println("  Damage taken: " + result.totalDamageTaken());
        System.out.println("  Gold dropped: " + result.goldEarned());
        System.out.println("  XP gained: " + result.experienceGained());
        System.out.println();
    }
    
    private static void printPlayerStatus(Player player) {
        System.out.println("Player: " + player.getName() + 
            " HP:" + player.getHealth().getCurrent() + "/" + player.getHealth().getMaximum() +
            " Gold:" + player.getInventory().getGold() +
            " XP:" + player.getExperience() +
            " Kills:" + player.getEnemiesKilled());
    }
    
    /**
     * Simple logging listener for combat events.
     */
    private static class LoggingCombatListener implements CombatEventListener {
        
        @Override
        public void onCombatStarted(CombatContext ctx) {
            System.out.println("[COMBAT] Started in " + ctx.getRoom().getId().value());
        }
        
        @Override
        public void onDamageDealt(CombatContext ctx, AttackResult result, boolean playerAttack) {
            String attacker = playerAttack ? "Player" : result.attackerId();
            String defender = playerAttack ? result.defenderId() : "Player";
            String critMarker = result.critical() ? " (CRIT!)" : "";
            System.out.println("[COMBAT] " + attacker + " hit " + defender + 
                " for " + result.actualDamage() + " damage" + critMarker);
            if (result.killed()) {
                System.out.println("[COMBAT] " + defender + " was killed!");
            }
        }
        
        @Override
        public void onCombatEnded(CombatContext ctx, CombatResult result) {
            System.out.println("[COMBAT] Ended: " + result.outcome());
        }
        
        @Override
        public void onPlayerDied(CombatContext ctx) {
            System.out.println("[COMBAT] Player has died!");
        }
    }
}
