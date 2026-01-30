package com.roguelab.domain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an item in the game.
 */
public final class Item {
    
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);
    
    private final EntityId id;
    private final String name;
    private final ItemType type;
    private final Rarity rarity;
    private final int value;
    private final int attackBonus;
    private final int defenseBonus;
    private final int healthBonus;
    
    private Item(Builder builder) {
        this.id = builder.id != null ? builder.id : 
            EntityId.of("item_" + ID_COUNTER.incrementAndGet());
        this.name = Objects.requireNonNull(builder.name);
        this.type = Objects.requireNonNull(builder.type);
        this.rarity = builder.rarity != null ? builder.rarity : Rarity.COMMON;
        this.value = builder.value;
        this.attackBonus = builder.attackBonus;
        this.defenseBonus = builder.defenseBonus;
        this.healthBonus = builder.healthBonus;
    }
    
    public EntityId getId() { return id; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public int getValue() { return value; }
    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getHealthBonus() { return healthBonus; }
    
    public boolean isEquipment() { return type.isEquippable(); }
    
    /**
     * Returns a map of stat names to values for this item.
     * Used by ItemStats for telemetry.
     */
    public java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        if (attackBonus != 0) stats.put("attack", attackBonus);
        if (defenseBonus != 0) stats.put("defense", defenseBonus);
        if (healthBonus != 0) stats.put("health", healthBonus);
        if (value != 0) stats.put("value", value);
        return stats;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id.equals(item.id);
    }
    
    @Override
    public int hashCode() { return id.hashCode(); }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(rarity.name()).append(" ").append(type.name()).append(")");
        if (attackBonus > 0) sb.append(" +").append(attackBonus).append(" ATK");
        if (defenseBonus > 0) sb.append(" +").append(defenseBonus).append(" DEF");
        return sb.toString();
    }
    
    public static Builder builder(String name, ItemType type) {
        return new Builder(name, type);
    }
    
    public static Builder builder() {
        return new Builder(null, null);
    }
    
    public static class Builder {
        private EntityId id;
        private String name;
        private ItemType type;
        private Rarity rarity = Rarity.COMMON;
        private int value = 0;
        private int attackBonus = 0;
        private int defenseBonus = 0;
        private int healthBonus = 0;
        
        private Builder(String name, ItemType type) {
            this.name = name;
            this.type = type;
        }
        
        public Builder id(EntityId id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder type(ItemType type) { this.type = type; return this; }
        public Builder rarity(Rarity rarity) { this.rarity = rarity; return this; }
        public Builder value(int value) { this.value = value; return this; }
        public Builder attack(int bonus) { this.attackBonus = bonus; return this; }
        public Builder attackBonus(int bonus) { this.attackBonus = bonus; return this; }
        public Builder defense(int bonus) { this.defenseBonus = bonus; return this; }
        public Builder defenseBonus(int bonus) { this.defenseBonus = bonus; return this; }
        public Builder health(int bonus) { this.healthBonus = bonus; return this; }
        public Builder healthBonus(int bonus) { this.healthBonus = bonus; return this; }
        public Item build() { return new Item(this); }
    }
    
    public static Item weapon(String name, Rarity rarity, int attack) {
        return builder(name, ItemType.WEAPON).rarity(rarity).attack(attack).build();
    }
    
    public static Item armor(String name, Rarity rarity, int defense) {
        return builder(name, ItemType.ARMOR).rarity(rarity).defense(defense).build();
    }
}
