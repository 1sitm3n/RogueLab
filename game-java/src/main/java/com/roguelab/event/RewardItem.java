package com.roguelab.event;

import com.roguelab.domain.Item;
import com.roguelab.domain.Rarity;

/**
 * Snapshot of reward item info for boss defeat events.
 */
public record RewardItem(
    String itemId,
    String itemName,
    Rarity rarity
) {
    /**
     * Create from an Item entity.
     */
    public static RewardItem from(Item item) {
        return new RewardItem(
            item.getId().value(),
            item.getName(),
            item.getRarity()
        );
    }
}
