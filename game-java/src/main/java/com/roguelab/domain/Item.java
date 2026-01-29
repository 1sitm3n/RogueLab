package com.roguelab.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an item in the game.
 * Items are immutable - effects are applied when equipped/used.
 */
public final class Item {
    
    private final EntityId id;
    private final String name;
    private final ItemType type;
    private final Rarity rarity;
    private final Map<String, Object> stats;
    private final String description;
    
    private Item(Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.name = Objects.requireNonNull(builder.name);
        this.type = Objects.requireNonNull(builder.type);
        this.rarity = Objects.requireNonNull(builder.rarity);
        this.stats = Collections.unmodifiableMap(new HashMap<>(builder.stats));
        this.description = builder.description != null ? builder.description : "";
    }
    
    public EntityId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public Rarity getRarity() {
        return rarity;
    }
    
    public Map<String, Object> getStats() {
        return stats;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a stat value, returning default if not present.
     */
    @SuppressWarnings("unchecked")
    public <T> T getStat(String key, T defaultValue) {
        Object value = stats.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }
    
    /**
     * Convenience method for integer stats.
     */
    public int getIntStat(String key, int defaultValue) {
        Object value = stats.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    public boolean isWeapon() {
        return type == ItemType.WEAPON;
    }
    
    public boolean isArmor() {
        return type == ItemType.ARMOR;
    }
    
    public boolean isConsumable() {
        return type == ItemType.CONSUMABLE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id.equals(item.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", rarity, name, type);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private EntityId id;
        private String name;
        private ItemType type;
        private Rarity rarity = Rarity.COMMON;
        private final Map<String, Object> stats = new HashMap<>();
        private String description;
        
        public Builder id(EntityId id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(ItemType type) {
            this.type = type;
            return this;
        }
        
        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }
        
        public Builder stat(String key, Object value) {
            this.stats.put(key, value);
            return this;
        }
        
        public Builder attack(int value) {
            return stat("attack", value);
        }
        
        public Builder defense(int value) {
            return stat("defense", value);
        }
        
        public Builder healthBonus(int value) {
            return stat("healthBonus", value);
        }
        
        public Builder healAmount(int value) {
            return stat("healAmount", value);
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Item build() {
            if (id == null) {
                id = EntityId.withPrefix(type != null ? type.name().toLowerCase() : "item");
            }
            return new Item(this);
        }
    }
}
