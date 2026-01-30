package com.roguelab.combat;

import com.roguelab.domain.*;
import com.roguelab.util.GameRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DamageCalculator")
class DamageCalculatorTest {
    
    private static final long SEED = 12345L;
    private GameRandom random;
    private DamageCalculator calculator;
    
    @BeforeEach
    void setUp() {
        random = new GameRandom(SEED);
        calculator = new DamageCalculator(random);
    }
    
    @Nested
    @DisplayName("Player attacks")
    class PlayerAttacks {
        
        @Test
        @DisplayName("should deal damage based on attack minus defense")
        void dealsBaseDamage() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EnemyType.RAT, 1);
            
            AttackResult result = calculator.calculatePlayerAttack(player, enemy);
            
            assertThat(result.actualDamage()).isGreaterThanOrEqualTo(1);
            assertThat(result.attackerId()).isEqualTo("player");
            assertThat(result.defenderId()).contains("rat");
        }
        
        @Test
        @DisplayName("should reduce enemy health")
        void reducesEnemyHealth() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EnemyType.RAT, 1);
            int initialHealth = enemy.getHealth().getCurrent();
            
            AttackResult result = calculator.calculatePlayerAttack(player, enemy);
            
            assertThat(result.healthBefore()).isEqualTo(initialHealth);
            assertThat(result.healthAfter()).isLessThan(initialHealth);
            assertThat(enemy.getHealth().getCurrent()).isEqualTo(result.healthAfter());
        }
        
        @Test
        @DisplayName("should kill enemy when health reaches 0")
        void killsEnemyAtZeroHealth() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EntityId.of("weak"), EnemyType.RAT, 1, 5, 1, 0);
            
            AttackResult result = calculator.calculatePlayerAttack(player, enemy);
            
            assertThat(result.killed()).isTrue();
            assertThat(enemy.isDead()).isTrue();
        }
        
        @Test
        @DisplayName("should always deal at least 1 damage")
        void minimumOneDamage() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EntityId.of("tank"), EnemyType.GOLEM, 1, 100, 5, 50);
            
            AttackResult result = calculator.calculatePlayerAttack(player, enemy);
            
            assertThat(result.finalDamage()).isGreaterThanOrEqualTo(1);
        }
    }
    
    @Nested
    @DisplayName("Enemy attacks")
    class EnemyAttacks {
        
        @Test
        @DisplayName("should deal damage to player")
        void dealsDamageToPlayer() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EnemyType.GOBLIN, 1);
            int initialHealth = player.getHealth().getCurrent();
            
            AttackResult result = calculator.calculateEnemyAttack(enemy, player);
            
            assertThat(result.healthBefore()).isEqualTo(initialHealth);
            assertThat(result.healthAfter()).isLessThan(initialHealth);
            assertThat(result.attackerId()).contains("goblin");
            assertThat(result.defenderId()).isEqualTo("player");
        }
        
        @Test
        @DisplayName("should not critically hit by default")
        void noCriticalHits() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EnemyType.RAT, 1);
            
            for (int i = 0; i < 10; i++) {
                AttackResult result = calculator.calculateEnemyAttack(enemy, player);
                assertThat(result.critical()).isFalse();
            }
        }
    }
    
    @Nested
    @DisplayName("Status effects")
    class StatusEffects {
        
        @Test
        @DisplayName("STRENGTHENED should increase damage")
        void strengthenedIncreasesDamage() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EntityId.of("rat1"), EnemyType.RAT, 1, 50, 4, 0);
            
            AttackResult normalResult = calculator.calculatePlayerAttack(player, enemy);
            
            Player buffedPlayer = new Player("Test2", PlayerClass.WARRIOR);
            buffedPlayer.getStatuses().apply(StatusType.STRENGTHENED, Player.PLAYER_ID, 3);
            Enemy enemy2 = new Enemy(EntityId.of("rat2"), EnemyType.RAT, 1, 50, 4, 0);
            
            AttackResult buffedResult = calculator.calculatePlayerAttack(buffedPlayer, enemy2);
            
            assertThat(buffedResult.finalDamage()).isGreaterThan(normalResult.finalDamage());
        }
        
        @Test
        @DisplayName("WEAKENED should decrease damage")
        void weakenedDecreasesDamage() {
            Player player = new Player("Test", PlayerClass.WARRIOR);
            Enemy enemy = new Enemy(EntityId.of("rat1"), EnemyType.RAT, 1, 50, 4, 0);
            
            AttackResult normalResult = calculator.calculatePlayerAttack(player, enemy);
            
            Player weakPlayer = new Player("Test2", PlayerClass.WARRIOR);
            weakPlayer.getStatuses().apply(StatusType.WEAKENED, Player.PLAYER_ID, 3);
            Enemy enemy2 = new Enemy(EntityId.of("rat2"), EnemyType.RAT, 1, 50, 4, 0);
            
            AttackResult weakResult = calculator.calculatePlayerAttack(weakPlayer, enemy2);
            
            assertThat(weakResult.finalDamage()).isLessThan(normalResult.finalDamage());
        }
    }
    
    @Nested
    @DisplayName("DoT calculations")
    class DoTCalculations {
        
        @Test
        @DisplayName("should calculate poison damage as 2 per stack")
        void poisonDamage() {
            assertThat(calculator.calculatePoisonDamage(1)).isEqualTo(2);
            assertThat(calculator.calculatePoisonDamage(3)).isEqualTo(6);
            assertThat(calculator.calculatePoisonDamage(5)).isEqualTo(10);
        }
        
        @Test
        @DisplayName("should calculate burning damage as 3 per stack")
        void burningDamage() {
            assertThat(calculator.calculateBurningDamage(1)).isEqualTo(3);
            assertThat(calculator.calculateBurningDamage(2)).isEqualTo(6);
        }
        
        @Test
        @DisplayName("should calculate regeneration healing as 3 per stack")
        void regenerationHealing() {
            assertThat(calculator.calculateRegenerationHealing(1)).isEqualTo(3);
            assertThat(calculator.calculateRegenerationHealing(2)).isEqualTo(6);
        }
    }
}
