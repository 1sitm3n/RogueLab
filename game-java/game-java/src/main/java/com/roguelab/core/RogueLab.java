package com.roguelab.core;

import com.roguelab.combat.*;
import com.roguelab.domain.*;
import com.roguelab.dungeon.*;
import com.roguelab.game.*;
import com.roguelab.util.GameRandom;

/**
 * Main entry point for RogueLab.
 * Demonstrates the complete game loop with dungeon generation and combat.
 */
public final class RogueLab {
    
    public static final String VERSION = "0.2.0";
    
    public static void main(String[] args) {
        printBanner();
        
        // Parse seed from args or use current time
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.currentTimeMillis();
        
        runGameDemo(seed);
    }
    
    private static void printBanner() {
        System.out.println("+--------------------------------------+");
        System.out.println("|           R O G U E L A B            |");
        System.out.println("|              v" + VERSION + "                  |");
        System.out.println("+--------------------------------------+");
    }
    
    /**
     * Run a complete game demonstration.
     */
    private static void runGameDemo(long seed) {
        // Create game session
        GameSession session = new GameSession(
            "Hero",
            PlayerClass.WARRIOR,
            seed,
            Difficulty.NORMAL,
            DungeonConfig.easy() // Smaller floors for demo
        );
        
        // Set up listeners
        session.setListener(new LoggingSessionListener());
        session.setCombatListener(new LoggingCombatListener());
        
        // Start the game
        session.start();
        
        // Game loop - play until dead or 2 floors completed
        int maxFloorsToPlay = 2;
        
        while (session.isActive() && session.getCurrentFloorNumber() <= maxFloorsToPlay) {
            playFloor(session);
            
            if (!session.isActive()) break;
            
            // Try to descend if possible
            if (session.getDungeon().canDescend() && 
                session.getCurrentFloorNumber() < maxFloorsToPlay) {
                session.descendFloor();
            } else if (session.getDungeon().canDescend()) {
                // Reached the end
                session.endRun(GameSessionListener.RunEndReason.VICTORY);
            } else {
                break;
            }
        }
        
        // End run if still active
        if (session.isActive()) {
            session.endRun(GameSessionListener.RunEndReason.VICTORY);
        }
        
        System.out.println("Demo complete! Seed: " + seed);
    }
    
    /**
     * Play through all rooms on the current floor.
     */
    private static void playFloor(GameSession session) {
        Floor floor = session.getCurrentFloor();
        
        while (session.isActive()) {
            Room room = session.getCurrentRoom();
            
            // Handle room based on state
            switch (session.getState()) {
                case IN_COMBAT -> {
                    CombatResult result = session.executeCombat();
                    if (result.isDefeat()) {
                        return; // Player died
                    }
                }
                case IN_SHOP -> {
                    // In a real game, player would choose items
                    // For demo, just leave
                    session.leaveShop();
                }
                case AT_REST -> {
                    session.rest();
                    session.leaveRest();
                }
                case EXPLORING -> {
                    // Pick up any items in current room
                    for (Item item : new java.util.ArrayList<>(room.getItems())) {
                        session.pickUpItem(item);
                    }
                    
                    // Move to next room if available
                    if (floor.hasNextRoom()) {
                        session.advanceRoom();
                    } else {
                        // Floor complete
                        return;
                    }
                }
                default -> {
                    // Other states - just return
                    return;
                }
            }
        }
    }
}

/**
 * Simple combat event listener that logs to console.
 */
class LoggingCombatListener implements CombatEventListener {
    
    @Override
    public void onCombatStarted(CombatContext ctx) {
        System.out.println();
        System.out.println("  ⚔ COMBAT STARTED ⚔");
    }
    
    @Override
    public void onDamageDealt(CombatContext ctx, AttackResult result, boolean playerAttack) {
        String attacker = playerAttack ? "Player" : "Enemy";
        String defender = playerAttack ? "Enemy" : "Player";
        String crit = result.critical() ? " CRIT!" : "";
        
        System.out.printf("    %s attacks %s for %d damage%s%n",
            attacker, defender, result.actualDamage(), crit);
        
        if (result.killed()) {
            System.out.printf("    %s was slain!%n", defender);
        }
    }
    
    @Override
    public void onCombatEnded(CombatContext ctx, CombatResult result) {
        // Handled by session listener
    }
    
    @Override
    public void onPlayerDied(CombatContext ctx) {
        System.out.println();
        System.out.println("  ☠ PLAYER DIED ☠");
    }
}
