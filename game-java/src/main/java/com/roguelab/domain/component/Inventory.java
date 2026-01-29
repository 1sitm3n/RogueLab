package com.roguelab.domain.component;

import com.roguelab.domain.Item;
import com.roguelab.domain.ItemType;

import java.util.*;

/**
 * Manages a player's inventory and equipped items.
 */
public final class Inventory {
    
    private final List<Item> items;
    private final Map<ItemType, Item> equipped;
    private int gold;
    
    public Inventory() {
        this.items = new ArrayList<>();
        this.equipped = new EnumMap<>(ItemType.class);
        this.gold = 0;
    }
    
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public Map<ItemType, Item> getEquipped() {
        return Collections.unmodifiableMap(equipped);
    }
    
    public int getGold() {
        return gold;
    }
    
    /**
     * Add an item to inventory.
     */
    public void addItem(Item item) {
        Objects.requireNonNull(item, "Item cannot be null");
        items.add(item);
    }
    
    /**
     * Remove an item from inventory.
     * @return true if item was found and removed
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    /**
     * Check if inventory contains a specific item.
     */
    public boolean hasItem(Item item) {
        return items.contains(item);
    }
    
    /**
     * Equip an item. Returns previously equipped item of same type, if any.
     * The item must be in inventory and be an equippable type.
     */
    public Optional<Item> equip(Item item) {
        Objects.requireNonNull(item, "Item cannot be null");
        
        if (!items.contains(item)) {
            throw new IllegalArgumentException("Item not in inventory: " + item);
        }
        
        ItemType type = item.getType();
        if (type == ItemType.CONSUMABLE) {
            throw new IllegalArgumentException("Cannot equip consumable items");
        }
        
        Item previous = equipped.put(type, item);
        return Optional.ofNullable(previous);
    }
    
    /**
     * Unequip an item of a specific type.
     * @return The unequipped item, if any was equipped
     */
    public Optional<Item> unequip(ItemType type) {
        return Optional.ofNullable(equipped.remove(type));
    }
    
    /**
     * Get the currently equipped item of a type.
     */
    public Optional<Item> getEquipped(ItemType type) {
        return Optional.ofNullable(equipped.get(type));
    }
    
    /**
     * Check if an item type slot is filled.
     */
    public boolean hasEquipped(ItemType type) {
        return equipped.containsKey(type);
    }
    
    /**
     * Use a consumable item, removing it from inventory.
     * @return The consumed item
     * @throws IllegalArgumentException if item not found or not consumable
     */
    public Item consume(Item item) {
        if (!items.contains(item)) {
            throw new IllegalArgumentException("Item not in inventory: " + item);
        }
        if (item.getType() != ItemType.CONSUMABLE) {
            throw new IllegalArgumentException("Item is not consumable: " + item);
        }
        items.remove(item);
        return item;
    }
    
    /**
     * Add gold to inventory.
     */
    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative gold");
        }
        this.gold += amount;
    }
    
    /**
     * Spend gold from inventory.
     * @return true if player had enough gold and it was spent
     */
    public boolean spendGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot spend negative gold");
        }
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Check if player can afford a cost.
     */
    public boolean canAfford(int cost) {
        return gold >= cost;
    }
    
    /**
     * Calculate total attack bonus from equipped items.
     */
    public int getEquippedAttackBonus() {
        return equipped.values().stream()
            .mapToInt(item -> item.getIntStat("attack", 0))
            .sum();
    }
    
    /**
     * Calculate total defense bonus from equipped items.
     */
    public int getEquippedDefenseBonus() {
        return equipped.values().stream()
            .mapToInt(item -> item.getIntStat("defense", 0))
            .sum();
    }
    
    /**
     * Calculate total health bonus from equipped items.
     */
    public int getEquippedHealthBonus() {
        return equipped.values().stream()
            .mapToInt(item -> item.getIntStat("healthBonus", 0))
            .sum();
    }
    
    /**
     * Get all consumable items in inventory.
     */
    public List<Item> getConsumables() {
        return items.stream()
            .filter(Item::isConsumable)
            .toList();
    }
    
    /**
     * Get all items of a specific type in inventory.
     */
    public List<Item> getItemsByType(ItemType type) {
        return items.stream()
            .filter(item -> item.getType() == type)
            .toList();
    }
    
    /**
     * Total number of items in inventory (not including equipped).
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Get names of all held items (for telemetry).
     */
    public List<String> getItemNames() {
        return items.stream()
            .map(Item::getName)
            .toList();
    }
    
    /**
     * Get IDs of all held items (for telemetry).
     */
    public List<String> getItemIds() {
        return items.stream()
            .map(item -> item.getId().value())
            .toList();
    }
}
