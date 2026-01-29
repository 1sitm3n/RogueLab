package com.roguelab.domain;

/**
 * Status effects that can be applied to entities.
 */
public enum StatusType {
    POISON(true, "Deals damage over time"),
    BURNING(true, "Deals fire damage over time"),
    FROZEN(false, "Cannot act"),
    WEAKENED(false, "Reduced attack damage"),
    STRENGTHENED(false, "Increased attack damage"),
    SHIELDED(false, "Reduced incoming damage"),
    REGENERATING(false, "Heals over time");
    
    private final boolean damaging;
    private final String description;
    
    StatusType(boolean damaging, String description) {
        this.damaging = damaging;
        this.description = description;
    }
    
    /**
     * Returns true if this status deals damage over time.
     */
    public boolean isDamaging() {
        return damaging;
    }
    
    public String getDescription() {
        return description;
    }
}
