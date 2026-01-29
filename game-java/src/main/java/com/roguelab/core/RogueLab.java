package com.roguelab.core;

import com.roguelab.domain.*;
import com.roguelab.event.*;
import com.roguelab.telemetry.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main entry point for RogueLab.
 * 
 * This class bootstraps the game, initializing all subsystems
 * and starting the game loop.
 */
public final class RogueLab {
    
    public static final String VERSION = "0.1.0";
    
    public static void main(String[] args) {
        System.out.println("+--------------------------------------+");
        System.out.println("|           R O G U E L A B            |");
        System.out.println("|              v" + VERSION + "                  |");
        System.out.println("+--------------------------------------+");
        System.out.println();
        
        // Demonstrate the telemetry system
        demonstrateTelemetrySystem();
        
        System.out.println();
        System.out.println("Game engine initialized successfully.");
        System.out.println("Full game loop coming in next iteration.");
    }
    
    /**
     * Demonstrate the event system and telemetry emission.
     */
    private static void demonstrateTelemetrySystem() {
        System.out.println("=== Telemetry System Demo ===");
        System.out.println();
        
        // Create a run ID and seed
        UUID runId = UUID.randomUUID();
        long seed = System.currentTimeMillis();
        
        // Create emitters - file + console for demo
        FileEmitter fileEmitter = new FileEmitter(runId);
        ConsoleEmitter consoleEmitter = new ConsoleEmitter(true);
        CompositeEmitter emitter = new CompositeEmitter(fileEmitter, consoleEmitter);
        
        System.out.println("Telemetry file: " + fileEmitter.getOutputFile());
        System.out.println();
        
        try {
            // 1. Run started
            RunStartedEvent runStarted = new RunStartedEvent(
                runId, seed, VERSION,
                PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
            );
            emitter.emit(runStarted);
            
            // 2. Enter first room
            RoomEnteredEvent roomEntered = new RoomEnteredEvent(
                runId, 1, 1, "room_1_0",
                RoomType.COMBAT, 2, false, 1.0
            );
            emitter.emit(roomEntered);
            
            // 3. Combat starts
            List<EnemyInfo> enemies = List.of(
                new EnemyInfo("goblin_1", EnemyType.GOBLIN, 20, 5),
                new EnemyInfo("goblin_2", EnemyType.GOBLIN, 20, 5)
            );
            CombatStartedEvent combatStarted = new CombatStartedEvent(
                runId, 2, "room_1_0", enemies, 120, 120
            );
            emitter.emit(combatStarted);
            
            // 4. Player attacks
            DamageDealtEvent playerAttack = new DamageDealtEvent(
                runId, 3, "player", DamageDealtEvent.EntityType.PLAYER,
                "goblin_1", DamageDealtEvent.EntityType.ENEMY,
                12, 12, DamageType.PHYSICAL, false,
                20, 8, false
            );
            emitter.emit(playerAttack);
            
            // 5. Player crits and kills
            DamageDealtEvent critKill = new DamageDealtEvent(
                runId, 4, "player", DamageDealtEvent.EntityType.PLAYER,
                "goblin_1", DamageDealtEvent.EntityType.ENEMY,
                12, 18, DamageType.PHYSICAL, true,
                8, 0, true
            );
            emitter.emit(critKill);
            
            // 6. Enemy attacks back
            DamageDealtEvent enemyAttack = new DamageDealtEvent(
                runId, 5, "goblin_2", DamageDealtEvent.EntityType.ENEMY,
                "player", DamageDealtEvent.EntityType.PLAYER,
                5, 5, DamageType.PHYSICAL, false,
                120, 115, false
            );
            emitter.emit(enemyAttack);
            
            // 7. Pick up item from chest
            ItemPickedEvent itemPicked = new ItemPickedEvent(
                runId, 10, "sword_001", ItemType.WEAPON,
                "Iron Sword", Rarity.COMMON, 1,
                ItemPickedEvent.ItemSource.CHEST,
                Map.of("attack", 5)
            );
            emitter.emit(itemPicked);
            
            // 8. Player heals at rest site
            PlayerHealedEvent healed = new PlayerHealedEvent(
                runId, 50, PlayerHealedEvent.HealSource.REST_SITE, "room_1_2",
                30, 85, 115, 120, 0
            );
            emitter.emit(healed);
            
            // 9. Run ends (victory)
            RunEndedEvent runEnded = new RunEndedEvent(
                runId, 500, RunEndedEvent.Outcome.VICTORY,
                5, 2500, 350, 8, 25, 1800
            );
            emitter.emit(runEnded);
            
            System.out.println("=== Demo Complete ===");
            System.out.println("Events written to: " + fileEmitter.getOutputFile());
            
        } finally {
            emitter.close();
        }
    }
}
