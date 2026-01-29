package com.roguelab.domain;

/**
 * Types of rooms the player can encounter.
 */
public enum RoomType {
    COMBAT("Fight enemies"),
    TREASURE("Find items"),
    SHOP("Buy and sell"),
    REST("Heal and upgrade"),
    BOSS("Face a powerful enemy"),
    EVENT("Random encounter");
    
    private final String description;
    
    RoomType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
