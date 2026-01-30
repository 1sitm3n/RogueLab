package com.roguelab.game;

/**
 * Represents the current state of a game session.
 * Controls what actions are available to the player.
 */
public enum GameState {
    
    /** Initial state before game starts */
    INITIALIZING,
    
    /** Player is exploring the dungeon (moving between rooms) */
    EXPLORING,
    
    /** Player is in combat with enemies */
    IN_COMBAT,
    
    /** Player is browsing a shop */
    IN_SHOP,
    
    /** Player is at a rest site */
    AT_REST,
    
    /** Player is viewing/managing inventory */
    IN_INVENTORY,
    
    /** Player is at a special event */
    IN_EVENT,
    
    /** Run has ended (victory or defeat) */
    RUN_ENDED,
    
    /** Game is paused */
    PAUSED;
    
    /**
     * Check if player can move to another room.
     */
    public boolean canMove() {
        return this == EXPLORING;
    }
    
    /**
     * Check if player can access inventory.
     */
    public boolean canAccessInventory() {
        return this == EXPLORING || this == AT_REST || this == IN_SHOP;
    }
    
    /**
     * Check if the run is still active.
     */
    public boolean isRunActive() {
        return this != RUN_ENDED && this != INITIALIZING;
    }
}
