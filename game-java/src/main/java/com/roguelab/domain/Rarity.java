package com.roguelab.domain;

/**
 * Item rarity tiers.
 */
public enum Rarity {
    COMMON(1.0, "#FFFFFF"),
    UNCOMMON(0.5, "#00FF00"),
    RARE(0.2, "#0080FF"),
    EPIC(0.08, "#A020F0"),
    LEGENDARY(0.02, "#FFA500");
    
    private final double dropWeight;
    private final String displayColor;
    
    Rarity(double dropWeight, String displayColor) {
        this.dropWeight = dropWeight;
        this.displayColor = displayColor;
    }
    
    /**
     * Relative probability weight for random drops.
     */
    public double getDropWeight() {
        return dropWeight;
    }
    
    /**
     * Hex color for UI display.
     */
    public String getDisplayColor() {
        return displayColor;
    }
}
