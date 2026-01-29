package com.roguelab.event;

import com.roguelab.domain.ItemType;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when player buys something from a shop.
 */
public final class ShopPurchasedEvent extends AbstractGameEvent {
    
    private final String itemId;
    private final String itemName;
    private final ItemType itemType;
    private final int price;
    private final int playerGoldBefore;
    private final int playerGoldAfter;
    private final int floor;
    
    public ShopPurchasedEvent(UUID runId, int tick, String itemId, String itemName,
                              ItemType itemType, int price, int playerGoldBefore,
                              int playerGoldAfter, int floor) {
        super(EventType.SHOP_PURCHASED, runId, tick);
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.price = price;
        this.playerGoldBefore = playerGoldBefore;
        this.playerGoldAfter = playerGoldAfter;
        this.floor = floor;
    }
    
    public ShopPurchasedEvent(Instant timestamp, UUID runId, int tick, String itemId, 
                              String itemName, ItemType itemType, int price, 
                              int playerGoldBefore, int playerGoldAfter, int floor) {
        super(EventType.SHOP_PURCHASED, timestamp, runId, tick);
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.price = price;
        this.playerGoldBefore = playerGoldBefore;
        this.playerGoldAfter = playerGoldAfter;
        this.floor = floor;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
    
    public int getPrice() {
        return price;
    }
    
    public int getPlayerGoldBefore() {
        return playerGoldBefore;
    }
    
    public int getPlayerGoldAfter() {
        return playerGoldAfter;
    }
    
    public int getFloor() {
        return floor;
    }
    
    @Override
    public String toString() {
        return String.format("ShopPurchasedEvent[%s for %d gold, %d->%d]",
            itemName, price, playerGoldBefore, playerGoldAfter);
    }
}
