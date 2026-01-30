package com.roguelab.game;

import com.roguelab.combat.CombatResult;
import com.roguelab.domain.Item;
import com.roguelab.domain.Player;
import com.roguelab.domain.Room;
import com.roguelab.dungeon.Floor;

/**
 * Listener interface for game session events.
 * Allows decoupled telemetry emission and UI updates.
 */
public interface GameSessionListener {
    
    /**
     * Called when a new run starts.
     */
    void onRunStarted(GameSession session);
    
    /**
     * Called when player enters a new floor.
     */
    void onFloorEntered(GameSession session, Floor floor);
    
    /**
     * Called when player enters a new room.
     */
    void onRoomEntered(GameSession session, Room room);
    
    /**
     * Called when a room is cleared (all enemies defeated).
     */
    void onRoomCleared(GameSession session, Room room);
    
    /**
     * Called when combat ends.
     */
    void onCombatCompleted(GameSession session, CombatResult result);
    
    /**
     * Called when player picks up an item.
     */
    void onItemPicked(GameSession session, Item item);
    
    /**
     * Called when player uses an item.
     */
    void onItemUsed(GameSession session, Item item);
    
    /**
     * Called when player makes a shop purchase.
     */
    void onShopPurchase(GameSession session, Item item, int cost);
    
    /**
     * Called when player rests and heals.
     */
    void onPlayerRested(GameSession session, int healAmount);
    
    /**
     * Called when player levels up.
     */
    void onPlayerLevelUp(GameSession session, int newLevel);
    
    /**
     * Called when the run ends (victory or defeat).
     */
    void onRunEnded(GameSession session, RunEndReason reason);
    
    /**
     * Reasons why a run can end.
     */
    enum RunEndReason {
        VICTORY,        // Defeated final boss
        PLAYER_DEATH,   // Player HP reached 0
        ABANDONED,      // Player quit
        ERROR           // Technical issue
    }
    
    /**
     * No-op implementation for testing.
     */
    GameSessionListener NONE = new GameSessionListener() {
        @Override public void onRunStarted(GameSession session) {}
        @Override public void onFloorEntered(GameSession session, Floor floor) {}
        @Override public void onRoomEntered(GameSession session, Room room) {}
        @Override public void onRoomCleared(GameSession session, Room room) {}
        @Override public void onCombatCompleted(GameSession session, CombatResult result) {}
        @Override public void onItemPicked(GameSession session, Item item) {}
        @Override public void onItemUsed(GameSession session, Item item) {}
        @Override public void onShopPurchase(GameSession session, Item item, int cost) {}
        @Override public void onPlayerRested(GameSession session, int healAmount) {}
        @Override public void onPlayerLevelUp(GameSession session, int newLevel) {}
        @Override public void onRunEnded(GameSession session, RunEndReason reason) {}
    };
}
