package com.roguelab.domain;

import com.roguelab.domain.component.*;

import java.util.Objects;

/**
 * Represents the player character.
 * Uses composition - combines Health, Combat, StatusEffects, and Inventory components.
 */
public final class Player {
    
    public static final EntityId PLAYER_ID = EntityId.of("player");
    
    private final EntityId id;
    private final String name;
    private final PlayerClass playerClass;
    private final Health health;
    private final Combat combat;
    private final StatusEffects statuses;
    private final Inventory inventory;
    private Position position;
    private int currentFloor;
    private int experience;
    private int level;
    private int enemiesKilled;
    
    public Player(String name, PlayerClass playerClass) {
        this.id = PLAYER_ID;
        this.name = Objects.requireNonNull(name);
        this.playerClass = Objects.requireNonNull(playerClass);
        this.health = new Health(playerClass.getStartingHealth());
        this.combat = new Combat(playerClass.getStartingAttack(), playerClass.getStartingDefense());
        this.statuses = new StatusEffects();
        this.inventory = new Inventory();
        this.position = Position.ORIGIN;
        this.currentFloor = 1;
        this.experience = 0;
        this.level = 1;
        this.enemiesKilled = 0;
        
        // Rogues get higher crit chance
        if (playerClass == PlayerClass.ROGUE) {
            combat.setCriticalChance(0.15);
            combat.setCriticalMultiplier(2.0);
        }
        
        // Mages deal magic damage
        if (playerClass == PlayerClass.MAGE) {
            combat.setPrimaryDamageType(DamageType.MAGIC);
        }
    }
    
    public EntityId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public PlayerClass getPlayerClass() {
        return playerClass;
    }
    
    public Health getHealth() {
        return health;
    }
    
    public Combat getCombat() {
        return combat;
    }
    
    public StatusEffects getStatuses() {
        return statuses;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position);
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getEnemiesKilled() {
        return enemiesKilled;
    }
    
    public void incrementEnemiesKilled() {
        this.enemiesKilled++;
    }
    
    public boolean isDead() {
        return health.isDead();
    }
    
    public boolean isAlive() {
        return health.isAlive();
    }
    
    /**
     * Calculate effective attack including equipment and status effects.
     */
    public int getEffectiveAttack() {
        int attack = combat.getTotalAttack() + inventory.getEquippedAttackBonus();
        if (statuses.has(StatusType.WEAKENED)) {
            attack = (int) (attack * 0.7);
        }
        if (statuses.has(StatusType.STRENGTHENED)) {
            attack = (int) (attack * 1.3);
        }
        return Math.max(1, attack);
    }
    
    /**
     * Calculate effective defense including equipment and status effects.
     */
    public int getEffectiveDefense() {
        int defense = combat.getTotalDefense() + inventory.getEquippedDefenseBonus();
        if (statuses.has(StatusType.SHIELDED)) {
            defense = (int) (defense * 1.5);
        }
        return Math.max(0, defense);
    }
    
    /**
     * Calculate maximum health including equipment bonuses.
     */
    public int getEffectiveMaxHealth() {
        return health.getMaximum() + inventory.getEquippedHealthBonus();
    }
    
    /**
     * Add experience and check for level up.
     * @return true if player leveled up
     */
    public boolean addExperience(int amount) {
        if (amount <= 0) return false;
        
        experience += amount;
        int requiredXP = getRequiredExperienceForNextLevel();
        
        if (experience >= requiredXP) {
            levelUp();
            return true;
        }
        return false;
    }
    
    /**
     * Get XP required for next level.
     */
    public int getRequiredExperienceForNextLevel() {
        return level * 100;
    }
    
    private void levelUp() {
        level++;
        experience = 0;
        
        // Stat increases on level up
        health.increaseMaximum(10);
        combat.addBonusAttack(2);
        combat.addBonusDefense(1);
        
        // Full heal on level up
        health.heal(health.getMaximum());
    }
    
    /**
     * Advance to next floor.
     */
    public void descendToNextFloor() {
        currentFloor++;
    }
    
    /**
     * Get gold from inventory (convenience method).
     */
    public int getGold() {
        return inventory.getGold();
    }
    
    @Override
    public String toString() {
        return String.format("%s the %s [Lv.%d] HP:%s ATK:%d DEF:%d Gold:%d",
            name, playerClass.getDisplayName(), level,
            health, getEffectiveAttack(), getEffectiveDefense(), getGold());
    }
}
