package com.roguelab.event;

import com.roguelab.domain.ItemType;
import com.roguelab.domain.Rarity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Emitted when the player picks up an item.
 */
public final class ItemPickedEvent extends AbstractGameEvent {
    
    /**
     * Where the item came from.
     */
    public enum ItemSource {
        CHEST,
        DROP,
        GROUND,
        REWARD
    }
    
    private final String itemId;
    private final ItemType itemType;
    private final String itemName;
    private final Rarity rarity;
    private final int floor;
    private final ItemSource source;
    private final Map<String, Object> stats;
    
    public ItemPickedEvent(UUID runId, int tick, String itemId, ItemType itemType,
                           String itemName, Rarity rarity, int floor, ItemSource source,
                           Map<String, Object> stats) {
        super(EventType.ITEM_PICKED, runId, tick);
        this.itemId = itemId;
        this.itemType = itemType;
        this.itemName = itemName;
        this.rarity = rarity;
        this.floor = floor;
        this.source = source;
        this.stats = stats;
    }
    
    public ItemPickedEvent(Instant timestamp, UUID runId, int tick, String itemId, 
                           ItemType itemType, String itemName, Rarity rarity, int floor, 
                           ItemSource source, Map<String, Object> stats) {
        super(EventType.ITEM_PICKED, timestamp, runId, tick);
        this.itemId = itemId;
        this.itemType = itemType;
        this.itemName = itemName;
        this.rarity = rarity;
        this.floor = floor;
        this.source = source;
        this.stats = stats;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public Rarity getRarity() {
        return rarity;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public ItemSource getSource() {
        return source;
    }
    
    public Map<String, Object> getStats() {
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("ItemPickedEvent[%s %s (%s) from %s on floor %d]",
            rarity, itemName, itemType, source, floor);
    }
}
