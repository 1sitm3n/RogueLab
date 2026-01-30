package com.roguelab.domain;

/**
 * Types of status effects that can be applied to entities.
 */
public enum StatusType {
    
    // Damaging debuffs
    POISONED(false, true, 5, true, false),
    BURNING(false, true, 3, true, false),
    BLEEDING(false, true, 5, true, false),
    
    // Non-damaging debuffs
    WEAKENED(false, false, 1, false, false),
    VULNERABLE(false, false, 1, false, false),
    SLOWED(false, false, 1, false, false),
    FROZEN(false, false, 1, false, false),
    STUNNED(false, false, 1, false, false),
    BLINDED(false, false, 1, false, false),
    
    // Healing buffs
    REGENERATING(true, false, 3, false, true),
    
    // Defensive buffs
    SHIELDED(true, false, 1, false, false),
    ARMORED(true, false, 1, false, false),
    
    // Offensive buffs
    STRENGTHENED(true, false, 1, false, false),
    ENRAGED(true, false, 1, false, false),
    FOCUSED(true, false, 1, false, false),
    
    // Utility buffs
    HASTED(true, false, 1, false, false),
    INVISIBLE(true, false, 1, false, false);
    
    private final boolean buff;
    private final boolean damaging;
    private final int maxStacks;
    private final boolean stackable;
    private final boolean healing;
    
    StatusType(boolean buff, boolean damaging, int maxStacks, boolean stackable, boolean healing) {
        this.buff = buff;
        this.damaging = damaging;
        this.maxStacks = maxStacks;
        this.stackable = stackable;
        this.healing = healing;
    }
    
    public boolean isBuff() { return buff; }
    public boolean isDebuff() { return !buff; }
    public boolean isDamaging() { return damaging; }
    public boolean isHealing() { return healing; }
    public int getMaxStacks() { return maxStacks; }
    public boolean isStackable() { return stackable; }
    
    public String getDisplayName() {
        String name = name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
