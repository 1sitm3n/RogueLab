package com.roguelab.domain;

import com.roguelab.domain.component.Combat;
import com.roguelab.domain.component.Health;
import com.roguelab.domain.component.StatusEffects;

import java.util.Objects;

/**
 * Represents an enemy entity in the game.
 * Uses composition - combines Health, Combat, and StatusEffects components.
 */
public final class Enemy {
    
    private final EntityId id;
    private final EnemyType type;
    private final int floor;
    private final Health health;
    private final Combat combat;
    private final StatusEffects statuses;
    private Position position;
    
    public Enemy(EnemyType type, int floor) {
        this.id = EntityId.withPrefix(type.name().toLowerCase());
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        this.health = new Health(type.getScaledHealth(floor));
        this.combat = new Combat(type.getScaledAttack(floor), type.getScaledDefense(floor));
        this.statuses = new StatusEffects();
        this.position = Position.ORIGIN;
    }
    
    /**
     * Create enemy with a specific ID (for testing or loading).
     */
    public Enemy(EntityId id, EnemyType type, int floor) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        this.health = new Health(type.getScaledHealth(floor));
        this.combat = new Combat(type.getScaledAttack(floor), type.getScaledDefense(floor));
        this.statuses = new StatusEffects();
        this.position = Position.ORIGIN;
    }
    
    public EntityId getId() {
        return id;
    }
    
    public EnemyType getType() {
        return type;
    }
    
    public String getName() {
        return type.getDisplayName();
    }
    
    public int getFloor() {
        return floor;
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
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position);
    }
    
    public boolean isDead() {
        return health.isDead();
    }
    
    public boolean isAlive() {
        return health.isAlive();
    }
    
    public boolean isBoss() {
        return type.isBoss();
    }
    
    /**
     * Calculate attack value considering status effects.
     */
    public int getEffectiveAttack() {
        int attack = combat.getTotalAttack();
        if (statuses.has(StatusType.WEAKENED)) {
            attack = (int) (attack * 0.7);
        }
        if (statuses.has(StatusType.STRENGTHENED)) {
            attack = (int) (attack * 1.3);
        }
        return attack;
    }
    
    /**
     * Calculate defense value considering status effects.
     */
    public int getEffectiveDefense() {
        int defense = combat.getTotalDefense();
        if (statuses.has(StatusType.SHIELDED)) {
            defense = (int) (defense * 1.5);
        }
        return defense;
    }
    
    /**
     * Calculate gold dropped on death.
     */
    public int calculateGoldDrop() {
        int baseGold = type.isBoss() ? 100 + floor * 50 : 5 + floor * 3;
        return baseGold;
    }
    
    /**
     * Calculate experience given on death.
     */
    public int calculateExperience() {
        int baseXP = type.isBoss() ? 50 + floor * 20 : 10 + floor * 2;
        return baseXP;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enemy enemy)) return false;
        return id.equals(enemy.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] HP:%s", getName(), id, health);
    }
}
