package com.roguelab.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Player Entity")
class PlayerTest {
    
    private Player warrior;
    private Player rogue;
    private Player mage;
    
    @BeforeEach
    void setUp() {
        warrior = new Player("TestWarrior", PlayerClass.WARRIOR);
        rogue = new Player("TestRogue", PlayerClass.ROGUE);
        mage = new Player("TestMage", PlayerClass.MAGE);
    }
    
    @Test
    @DisplayName("should initialize with class-appropriate stats")
    void initializesWithClassStats() {
        // Warrior: High HP, balanced attack/defense
        assertThat(warrior.getHealth().getMaximum())
            .isEqualTo(PlayerClass.WARRIOR.getStartingHealth());
        assertThat(warrior.getCombat().getBaseAttack())
            .isEqualTo(PlayerClass.WARRIOR.getStartingAttack());
        
        // Rogue: Lower HP, higher attack
        assertThat(rogue.getHealth().getMaximum())
            .isLessThan(warrior.getHealth().getMaximum());
        assertThat(rogue.getCombat().getBaseAttack())
            .isGreaterThan(warrior.getCombat().getBaseAttack());
        
        // Mage: Lowest HP, highest attack
        assertThat(mage.getHealth().getMaximum())
            .isLessThan(rogue.getHealth().getMaximum());
    }
    
    @Test
    @DisplayName("rogue should have higher crit chance")
    void rogueHasHigherCritChance() {
        assertThat(rogue.getCombat().getCriticalChance())
            .isGreaterThan(warrior.getCombat().getCriticalChance());
        assertThat(rogue.getCombat().getCriticalMultiplier())
            .isGreaterThan(warrior.getCombat().getCriticalMultiplier());
    }
    
    @Test
    @DisplayName("mage should deal magic damage")
    void mageDealsMagicDamage() {
        assertThat(mage.getCombat().getPrimaryDamageType())
            .isEqualTo(DamageType.MAGIC);
        assertThat(warrior.getCombat().getPrimaryDamageType())
            .isEqualTo(DamageType.PHYSICAL);
    }
    
    @Test
    @DisplayName("should have player entity ID")
    void hasPlayerEntityId() {
        assertThat(warrior.getId()).isEqualTo(Player.PLAYER_ID);
        assertThat(warrior.getId().value()).isEqualTo("player");
    }
    
    @Test
    @DisplayName("should start on floor 1 at level 1")
    void startsAtFloorOneAndLevelOne() {
        assertThat(warrior.getCurrentFloor()).isEqualTo(1);
        assertThat(warrior.getLevel()).isEqualTo(1);
        assertThat(warrior.getExperience()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("should start with empty inventory")
    void startsWithEmptyInventory() {
        assertThat(warrior.getInventory().getItems()).isEmpty();
        assertThat(warrior.getInventory().getGold()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("effective attack should include equipment")
    void effectiveAttackIncludesEquipment() {
        int baseAttack = warrior.getEffectiveAttack();
        
        Item sword = Item.builder()
            .name("Test Sword")
            .type(ItemType.WEAPON)
            .attack(10)
            .build();
        
        warrior.getInventory().addItem(sword);
        warrior.getInventory().equip(sword);
        
        assertThat(warrior.getEffectiveAttack())
            .isEqualTo(baseAttack + 10);
    }
    
    @Test
    @DisplayName("should level up when gaining enough experience")
    void levelsUpWithExperience() {
        int requiredXP = warrior.getRequiredExperienceForNextLevel();
        int healthBefore = warrior.getHealth().getMaximum();
        
        boolean leveledUp = warrior.addExperience(requiredXP);
        
        assertThat(leveledUp).isTrue();
        assertThat(warrior.getLevel()).isEqualTo(2);
        assertThat(warrior.getExperience()).isEqualTo(0);
        assertThat(warrior.getHealth().getMaximum()).isGreaterThan(healthBefore);
    }
    
    @Test
    @DisplayName("should not level up with insufficient experience")
    void doesNotLevelUpWithInsufficientXP() {
        int requiredXP = warrior.getRequiredExperienceForNextLevel();
        
        boolean leveledUp = warrior.addExperience(requiredXP - 1);
        
        assertThat(leveledUp).isFalse();
        assertThat(warrior.getLevel()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("should track enemies killed")
    void tracksEnemiesKilled() {
        assertThat(warrior.getEnemiesKilled()).isEqualTo(0);
        
        warrior.incrementEnemiesKilled();
        warrior.incrementEnemiesKilled();
        
        assertThat(warrior.getEnemiesKilled()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("should descend to next floor")
    void descendsToNextFloor() {
        assertThat(warrior.getCurrentFloor()).isEqualTo(1);
        
        warrior.descendToNextFloor();
        
        assertThat(warrior.getCurrentFloor()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("should die when health reaches zero")
    void diesWhenHealthReachesZero() {
        assertThat(warrior.isAlive()).isTrue();
        
        warrior.getHealth().takeDamage(warrior.getHealth().getMaximum());
        
        assertThat(warrior.isDead()).isTrue();
        assertThat(warrior.isAlive()).isFalse();
    }
    
    @Test
    @DisplayName("weakened status should reduce attack")
    void weakenedReducesAttack() {
        int normalAttack = warrior.getEffectiveAttack();
        
        warrior.getStatuses().apply(StatusType.WEAKENED, 
            EntityId.withPrefix("test"), 3);
        
        assertThat(warrior.getEffectiveAttack())
            .isLessThan(normalAttack);
    }
    
    @Test
    @DisplayName("strengthened status should increase attack")
    void strengthenedIncreasesAttack() {
        int normalAttack = warrior.getEffectiveAttack();
        
        warrior.getStatuses().apply(StatusType.STRENGTHENED, 
            EntityId.withPrefix("test"), 3);
        
        assertThat(warrior.getEffectiveAttack())
            .isGreaterThan(normalAttack);
    }
}
