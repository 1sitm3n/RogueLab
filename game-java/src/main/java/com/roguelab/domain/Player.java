package com.roguelab.domain;

import com.roguelab.domain.component.*;

import java.util.Objects;

/**
 * Represents the player character.
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
        
        if (playerClass == PlayerClass.ROGUE) {
            combat.setCriticalChance(0.15);
            combat.setCriticalMultiplier(2.0);
        }
        
        if (playerClass == PlayerClass.MAGE) {
            combat.setPrimaryDamageType(DamageType.MAGIC);
        }
    }
    
    // Getters
    public EntityId getId() { return id; }
    public String getName() { return name; }
    public PlayerClass getPlayerClass() { return playerClass; }
    public Health getHealth() { return health; }
    public Combat getCombat() { return combat; }
    public StatusEffects getStatuses() { return statuses; }
    public Inventory getInventory() { return inventory; }
    public Position getPosition() { return position; }
    public int getCurrentFloor() { return currentFloor; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public int getEnemiesKilled() { return enemiesKilled; }
    
    public void setPosition(Position position) { this.position = Objects.requireNonNull(position); }
    public void setCurrentFloor(int floor) { this.currentFloor = floor; }
    public void incrementEnemiesKilled() { this.enemiesKilled++; }
    
    public boolean isDead() { return health.isDead(); }
    public boolean isAlive() { return health.isAlive(); }
    
    public int getEffectiveAttack() {
        int attack = combat.getTotalAttack();
        attack += inventory.getEquipmentAttackBonus();
        double modifier = statuses.getAttackModifier();
        return (int) Math.round(attack * modifier);
    }
    
    public int getEffectiveDefense() {
        int defense = combat.getTotalDefense();
        defense += inventory.getEquipmentDefenseBonus();
        if (statuses.has(StatusType.SHIELDED)) {
            defense = (int) (defense * 1.5);
        }
        return defense;
    }
    
    public boolean addExperience(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Experience cannot be negative");
        int oldLevel = level;
        experience += amount;
        while (experience >= getExperienceForNextLevel()) {
            experience -= getExperienceForNextLevel();
            levelUp();
        }
        return level > oldLevel;
    }
    
    public int getExperienceForNextLevel() {
        return (int) (100 * Math.pow(level, 1.5));
    }
    
    public int getRequiredExperienceForNextLevel() {
        return getExperienceForNextLevel();
    }
    
    public void descendToNextFloor() {
        currentFloor++;
    }
    
    private void levelUp() {
        level++;
        int healthIncrease = switch (playerClass) {
            case WARRIOR -> 15;
            case ROGUE -> 10;
            case MAGE -> 8;
        };
        int attackIncrease = switch (playerClass) {
            case WARRIOR -> 2;
            case ROGUE -> 3;
            case MAGE -> 1;
        };
        int defenseIncrease = switch (playerClass) {
            case WARRIOR -> 2;
            case ROGUE -> 1;
            case MAGE -> 1;
        };
        
        health.increaseMaximum(healthIncrease);
        health.heal(health.getMaximum()); // Heal to full on level up
        combat.addBonusAttack(attackIncrease);
        combat.addBonusDefense(defenseIncrease);
    }
    
    @Override
    public String toString() {
        return String.format("%s the %s [Lv.%d] HP:%d/%d ATK:%d DEF:%d Gold:%d",
            name, playerClass.getDisplayName(), level,
            health.getCurrent(), health.getMaximum(),
            getEffectiveAttack(), getEffectiveDefense(),
            inventory.getGold());
    }
}
