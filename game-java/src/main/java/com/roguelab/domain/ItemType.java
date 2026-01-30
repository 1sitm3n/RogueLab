package com.roguelab.domain;

/**
 * Types of items that can exist in the game.
 */
public enum ItemType {
    
    WEAPON(true),
    ARMOR(true),
    ACCESSORY(true),
    HELMET(true),
    BOOTS(true),
    CONSUMABLE(false),
    KEY(false),
    QUEST(false),
    RELIC(false),
    CURRENCY(false);
    
    private final boolean equippable;
    
    ItemType(boolean equippable) {
        this.equippable = equippable;
    }
    
    public boolean isEquippable() {
        return equippable;
    }
}
