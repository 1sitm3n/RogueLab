package com.roguelab.combat;

import com.roguelab.domain.*;
import com.roguelab.util.GameRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CombatEngine")
class CombatEngineTest {
    
    private static final long SEED = 12345L;
    private GameRandom random;
    private CombatEngine engine;
    
    @BeforeEach
    void setUp() {
        random = new GameRandom(SEED);
        engine = new CombatEngine(random);
    }
    
    @Nested
    @DisplayName("Basic combat")
    class BasicCombat {
        
        @Test
        @DisplayName("should defeat weak enemy")
        void defeatsWeakEnemy() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EnemyType.RAT, 1));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.outcome()).isEqualTo(CombatResult.Outcome.VICTORY);
            assertThat(result.enemiesKilled()).isEqualTo(1);
            assertThat(player.isAlive()).isTrue();
        }
        
        @Test
        @DisplayName("should track damage dealt and taken")
        void tracksDamage() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EnemyType.GOBLIN, 1));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.totalDamageDealt()).isGreaterThan(0);
        }
        
        @Test
        @DisplayName("should award gold and XP on victory")
        void awardsGoldAndXP() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            int initialGold = player.getInventory().getGold();
            int initialXP = player.getExperience();
            
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EnemyType.RAT, 1));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.goldEarned()).isGreaterThan(0);
            assertThat(result.experienceGained()).isGreaterThan(0);
            assertThat(player.getInventory().getGold()).isGreaterThan(initialGold);
            assertThat(player.getExperience()).isGreaterThan(initialXP);
        }
    }
    
    @Nested
    @DisplayName("Multiple enemies")
    class MultipleEnemies {
        
        @Test
        @DisplayName("should defeat multiple enemies")
        void defeatsMultipleEnemies() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EnemyType.RAT, 1));
            room.addEnemy(new Enemy(EnemyType.RAT, 1));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.outcome()).isEqualTo(CombatResult.Outcome.VICTORY);
            assertThat(result.enemiesKilled()).isEqualTo(2);
        }
    }
    
    @Nested
    @DisplayName("Player death")
    class PlayerDeath {
        
        @Test
        @DisplayName("should detect player death against strong enemy")
        void detectsPlayerDeath() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EntityId.of("boss"), EnemyType.DRAGON, 5, 500, 50, 20));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.outcome()).isEqualTo(CombatResult.Outcome.DEFEAT);
            assertThat(player.isDead()).isTrue();
        }
        
        @Test
        @DisplayName("should not award gold or XP on defeat")
        void noRewardsOnDefeat() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            int initialGold = player.getInventory().getGold();
            
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EntityId.of("boss"), EnemyType.DRAGON, 5, 500, 50, 20));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.goldEarned()).isEqualTo(0);
            assertThat(result.experienceGained()).isEqualTo(0);
            assertThat(player.getInventory().getGold()).isEqualTo(initialGold);
        }
    }
    
    @Nested
    @DisplayName("Determinism")
    class Determinism {
        
        @Test
        @DisplayName("should produce same result with same seed")
        void sameResultWithSameSeed() {
            Player player1 = new Player("Test1", PlayerClass.WARRIOR);
            Room room1 = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room1.addEnemy(new Enemy(EntityId.of("gob1"), EnemyType.GOBLIN, 1, 18, 6, 2));
            CombatResult result1 = new CombatEngine(new GameRandom(SEED))
                .runCombat("run1", player1, room1, new GameRandom(SEED), 0);
            
            Player player2 = new Player("Test2", PlayerClass.WARRIOR);
            Room room2 = new Room(EntityId.of("room2"), RoomType.COMBAT, 1, 0);
            room2.addEnemy(new Enemy(EntityId.of("gob2"), EnemyType.GOBLIN, 1, 18, 6, 2));
            CombatResult result2 = new CombatEngine(new GameRandom(SEED))
                .runCombat("run2", player2, room2, new GameRandom(SEED), 0);
            
            assertThat(result1.outcome()).isEqualTo(result2.outcome());
            assertThat(result1.turnsElapsed()).isEqualTo(result2.turnsElapsed());
            assertThat(result1.totalDamageDealt()).isEqualTo(result2.totalDamageDealt());
            assertThat(result1.totalDamageTaken()).isEqualTo(result2.totalDamageTaken());
        }
    }
    
    @Nested
    @DisplayName("Turn counting")
    class TurnCounting {
        
        @Test
        @DisplayName("should count turns correctly")
        void countsTurnsCorrectly() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EnemyType.RAT, 1));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.turnsElapsed()).isGreaterThan(0);
        }
        
        @Test
        @DisplayName("should not exceed max turns")
        void doesNotExceedMaxTurns() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Room room = new Room(EntityId.of("room1"), RoomType.COMBAT, 1, 0);
            room.addEnemy(new Enemy(EntityId.of("invincible"), EnemyType.GOLEM, 1, 999999, 1, 99999));
            
            CombatResult result = engine.runCombat("run1", player, room, random, 0);
            
            assertThat(result.turnsElapsed()).isLessThanOrEqualTo(100);
        }
    }
}
