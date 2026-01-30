package com.roguelab.domain;

import com.roguelab.domain.component.Combat;
import com.roguelab.domain.component.Health;
import com.roguelab.domain.component.StatusEffects;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an enemy in the game.
 */
public final class Enemy {
    
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);
    
    private final EntityId id;
    private final EnemyType type;
    private final Health health;
    private final Combat combat;
    private final StatusEffects statuses;
    private final int floor;
    
    public Enemy(EnemyType type, int floor) {
        this.id = EntityId.of(type.name().toLowerCase() + "_" + ID_COUNTER.incrementAndGet());
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        
        int scaledHealth = type.getBaseHealth() + (floor - 1) * type.getHealthPerFloor();
        int scaledAttack = type.getBaseAttack() + (floor - 1) * type.getAttackPerFloor();
        int scaledDefense = type.getBaseDefense() + (floor - 1) * type.getDefensePerFloor();
        
        this.health = new Health(scaledHealth);
        this.combat = new Combat(scaledAttack, scaledDefense);
        this.statuses = new StatusEffects();
    }
    
    public Enemy(EntityId id, EnemyType type, int floor, int health, int attack, int defense) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        
        this.health = new Health(health);
        this.combat = new Combat(attack, defense);
        this.statuses = new StatusEffects();
    }
    
    public EntityId getId() { return id; }
    public EnemyType getType() { return type; }
    public String getName() { return type.getDisplayName(); }
    public Health getHealth() { return health; }
    public Combat getCombat() { return combat; }
    public StatusEffects getStatuses() { return statuses; }
    public int getFloor() { return floor; }
    
    public boolean isAlive() { return health.isAlive(); }
    public boolean isDead() { return health.isDead(); }
    public boolean isBoss() { return type.isBoss(); }
    
    public int getEffectiveAttack() {
        int attack = combat.getTotalAttack();
        double modifier = statuses.getAttackModifier();
        return (int) Math.round(attack * modifier);
    }
    
    public int getEffectiveDefense() {
        int defense = combat.getTotalDefense();
        if (statuses.has(StatusType.SHIELDED)) {
            defense = (int) (defense * 1.5);
        }
        return defense;
    }
    
    public DamageType getDamageType() {
        return type.getDamageType();
    }
    
    public int calculateGoldDrop() {
        return type.isBoss() ? 100 + floor * 50 : 5 + floor * 3;
    }
    
    public int calculateExperience() {
        return type.isBoss() ? 50 + floor * 20 : 10 + floor * 2;
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
        return String.format("%s [%s] HP:%d/%d", 
            getName(), id.value(), health.getCurrent(), health.getMaximum());
    }
}
