package com.roguelab.core;

import com.roguelab.combat.*;
import com.roguelab.domain.*;
import com.roguelab.dungeon.*;
import com.roguelab.game.*;
import com.roguelab.telemetry.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * Main entry point for RogueLab.
 * Demonstrates the complete game loop with telemetry capture.
 */
public final class RogueLab {
    
    public static final String VERSION = "0.2.0";
    
    public static void main(String[] args) {
        printBanner();
        
        // Parse seed from args or use current time
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.currentTimeMillis();
        
        try {
            runGameWithTelemetry(seed);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printBanner() {
        System.out.println("+--------------------------------------+");
        System.out.println("|           R O G U E L A B            |");
        System.out.println("|              v" + VERSION + "                  |");
        System.out.println("+--------------------------------------+");
    }
    
    /**
     * Run a complete game with telemetry capture.
     */
    private static void runGameWithTelemetry(long seed) throws IOException {
        // Create telemetry output
        Path runsDir = Paths.get("runs");
        Files.createDirectories(runsDir);
        
        String runId = "run_" + System.currentTimeMillis();
        Path telemetryFile = runsDir.resolve(runId + ".jsonl");
        
        System.out.println("Telemetry file: " + telemetryFile);
        System.out.println("Seed: " + seed);
        System.out.println();
        
        // Create telemetry writer
        try (TelemetryWriter telemetry = new TelemetryWriter(telemetryFile, runId, true)) {
            
            // Create game session
            GameSession session = new GameSession(
                "Hero",
                PlayerClass.WARRIOR,
                seed,
                Difficulty.NORMAL,
                DungeonConfig.easy()
            );
            
            // Set up listeners - both logging and telemetry
            GameSessionListener loggingListener = new LoggingSessionListener();
            GameSessionListener telemetryListener = new SimpleTelemetrySessionListener(telemetry);
            session.setListener(new CompositeSessionListener(loggingListener, telemetryListener));
            
            // Combat listeners
            CombatEventListener combatLogging = new LoggingCombatListener();
            CombatEventListener combatTelemetry = new SimpleTelemetryCombatListener(telemetry);
            session.setCombatListener(new CompositeCombatListener(combatLogging, combatTelemetry));
            
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
                    session.endRun(GameSessionListener.RunEndReason.VICTORY);
                } else {
                    break;
                }
            }
            
            // End run if still active
            if (session.isActive()) {
                session.endRun(GameSessionListener.RunEndReason.VICTORY);
            }
            
            System.out.println();
            System.out.println("Telemetry saved to: " + telemetryFile.toAbsolutePath());
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
            
            switch (session.getState()) {
                case IN_COMBAT -> {
                    CombatResult result = session.executeCombat();
                    if (result.isDefeat()) {
                        return;
                    }
                }
                case IN_SHOP -> session.leaveShop();
                case AT_REST -> {
                    session.rest();
                    session.leaveRest();
                }
                case EXPLORING -> {
                    // Pick up items
                    for (Item item : new java.util.ArrayList<>(room.getItems())) {
                        session.pickUpItem(item);
                    }
                    
                    if (floor.hasNextRoom()) {
                        session.advanceRoom();
                    } else {
                        return;
                    }
                }
                default -> { return; }
            }
        }
    }
}

/**
 * Combines multiple session listeners.
 */
class CompositeSessionListener implements GameSessionListener {
    private final GameSessionListener[] listeners;
    
    CompositeSessionListener(GameSessionListener... listeners) {
        this.listeners = listeners;
    }
    
    @Override public void onRunStarted(GameSession s) { 
        for (var l : listeners) l.onRunStarted(s); 
    }
    @Override public void onFloorEntered(GameSession s, Floor f) { 
        for (var l : listeners) l.onFloorEntered(s, f); 
    }
    @Override public void onRoomEntered(GameSession s, Room r) { 
        for (var l : listeners) l.onRoomEntered(s, r); 
    }
    @Override public void onRoomCleared(GameSession s, Room r) { 
        for (var l : listeners) l.onRoomCleared(s, r); 
    }
    @Override public void onCombatCompleted(GameSession s, CombatResult r) { 
        for (var l : listeners) l.onCombatCompleted(s, r); 
    }
    @Override public void onItemPicked(GameSession s, Item i) { 
        for (var l : listeners) l.onItemPicked(s, i); 
    }
    @Override public void onItemUsed(GameSession s, Item i) { 
        for (var l : listeners) l.onItemUsed(s, i); 
    }
    @Override public void onShopPurchase(GameSession s, Item i, int c) { 
        for (var l : listeners) l.onShopPurchase(s, i, c); 
    }
    @Override public void onPlayerRested(GameSession s, int h) { 
        for (var l : listeners) l.onPlayerRested(s, h); 
    }
    @Override public void onPlayerLevelUp(GameSession s, int l) { 
        for (var x : listeners) x.onPlayerLevelUp(s, l); 
    }
    @Override public void onRunEnded(GameSession s, RunEndReason r) { 
        for (var l : listeners) l.onRunEnded(s, r); 
    }
}

/**
 * Combines multiple combat listeners.
 */
class CompositeCombatListener implements CombatEventListener {
    private final CombatEventListener[] listeners;
    
    CompositeCombatListener(CombatEventListener... listeners) {
        this.listeners = listeners;
    }
    
    @Override public void onCombatStarted(CombatContext ctx) { 
        for (var l : listeners) l.onCombatStarted(ctx); 
    }
    @Override public void onDamageDealt(CombatContext ctx, AttackResult r, boolean p) { 
        for (var l : listeners) l.onDamageDealt(ctx, r, p); 
    }
    @Override public void onCombatEnded(CombatContext ctx, CombatResult r) { 
        for (var l : listeners) l.onCombatEnded(ctx, r); 
    }
    @Override public void onPlayerDied(CombatContext ctx) { 
        for (var l : listeners) l.onPlayerDied(ctx); 
    }
}

/**
 * Simple combat event listener that logs to console.
 */
class LoggingCombatListener implements CombatEventListener {
    
    @Override
    public void onCombatStarted(CombatContext ctx) {
        System.out.println();
        System.out.println("  [COMBAT STARTED]");
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
        System.out.println("  [PLAYER DIED]");
    }
}
