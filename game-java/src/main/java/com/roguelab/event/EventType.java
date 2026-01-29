package com.roguelab.event;

/**
 * Enumeration of all event types in the game.
 * This serves as a registry of valid event types and their current versions.
 */
public enum EventType {
    RUN_STARTED("1.0.0"),
    RUN_ENDED("1.0.0"),
    ROOM_ENTERED("1.0.0"),
    COMBAT_STARTED("1.0.0"),
    COMBAT_ENDED("1.0.0"),
    DAMAGE_DEALT("1.0.0"),
    STATUS_APPLIED("1.0.0"),
    ITEM_PICKED("1.0.0"),
    SHOP_PURCHASED("1.0.0"),
    BOSS_DEFEATED("1.0.0"),
    PLAYER_DIED("1.0.0"),
    PLAYER_HEALED("1.0.0");
    
    private final String currentVersion;
    
    EventType(String currentVersion) {
        this.currentVersion = currentVersion;
    }
    
    /**
     * Get the current schema version for this event type.
     */
    public String getCurrentVersion() {
        return currentVersion;
    }
}
