package com.roguelab.event;

import com.roguelab.domain.Item;

import java.util.Collections;
import java.util.Map;

/**
 * Snapshot of item stats for inclusion in events.
 */
public record ItemStats(Map<String, Object> stats) {
    
    public ItemStats {
        stats = Collections.unmodifiableMap(stats);
    }
    
    /**
     * Create from an Item entity.
     */
    public static ItemStats from(Item item) {
        return new ItemStats(item.getStats());
    }
}
