package com.roguelab.domain.component;

import com.roguelab.domain.Item;
import com.roguelab.domain.ItemType;

import java.util.*;

/**
 * Manages a player's inventory, equipped items, and gold.
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
    
    public int getItemCount() {
        return items.size();
    }
    
    public void addItem(Item item) {
        items.add(Objects.requireNonNull(item));
    }
    
    public boolean removeItem(Item item) {
        if (equipped.containsValue(item)) {
            equipped.entrySet().removeIf(e -> e.getValue().equals(item));
        }
        return items.remove(item);
    }
    
    public Map<ItemType, Item> getEquipped() {
        return Collections.unmodifiableMap(equipped);
    }
    
    public Optional<Item> equip(Item item) {
        if (!items.contains(item)) {
            throw new IllegalArgumentException("Item not in inventory");
        }
        if (!item.getType().isEquippable()) {
            throw new IllegalArgumentException("Item not equippable");
        }
        Item previous = equipped.put(item.getType(), item);
        return Optional.ofNullable(previous);
    }
    
    public int getEquipmentAttackBonus() {
        return equipped.values().stream().mapToInt(Item::getAttackBonus).sum();
    }
    
    public int getEquipmentDefenseBonus() {
        return equipped.values().stream().mapToInt(Item::getDefenseBonus).sum();
    }
    
    public int getGold() {
        return gold;
    }
    
    public void addGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold cannot be negative");
        this.gold += amount;
    }
    
    public boolean spendGold(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Gold cannot be negative");
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("Inventory[items=%d, gold=%d]", items.size(), gold);
    }
}
