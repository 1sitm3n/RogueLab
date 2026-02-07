package com.roguelab.domain;

import com.badlogic.gdx.graphics.Color;

/**
 * Special abilities that enemies can use in combat.
 * Each ability has unique effects and visual indicators.
 */
public enum SpecialAbility {
    NONE("None", "No special ability.", 0, null),
    
    POISON("Poison", 
        "Inflicts poison, dealing damage over time.", 
        30, // 30% chance to trigger
        new Color(0.4f, 0.8f, 0.2f, 1f)), // Green
    
    BURN("Burn", 
        "Sets target ablaze, dealing fire damage over time.", 
        25, 
        new Color(1f, 0.5f, 0.1f, 1f)), // Orange
    
    STUN("Stun", 
        "Stuns target, causing them to lose their next attack.", 
        20, 
        new Color(1f, 1f, 0.3f, 1f)), // Yellow
    
    LIFE_DRAIN("Life Drain", 
        "Drains life from target, healing the attacker.", 
        35, 
        new Color(0.6f, 0.1f, 0.6f, 1f)), // Purple
    
    PHASE("Phase", 
        "Phases through reality, chance to avoid attacks entirely.", 
        40, 
        new Color(0.5f, 0.7f, 1f, 1f)), // Light blue
    
    CORRODE("Corrode", 
        "Corrodes armor, reducing target's defense.", 
        30, 
        new Color(0.5f, 0.9f, 0.3f, 1f)), // Acid green
    
    STEAL_GOLD("Steal Gold", 
        "Steals gold from the target on hit.", 
        25, 
        new Color(1f, 0.85f, 0.2f, 1f)), // Gold
    
    CURSE("Curse", 
        "Curses target, reducing their attack power.", 
        25, 
        new Color(0.3f, 0.1f, 0.3f, 1f)), // Dark purple
    
    CHARGE("Charge", 
        "Charges with devastating force, dealing double damage.", 
        20, 
        new Color(0.9f, 0.3f, 0.2f, 1f)), // Red
    
    ENRAGE("Enrage", 
        "Grows stronger when wounded, increasing attack.", 
        100, // Always active when conditions met
        new Color(0.8f, 0.2f, 0.2f, 1f)), // Dark red
    
    SUMMON("Summon", 
        "Summons minions to fight alongside.", 
        15, 
        new Color(0.4f, 0.4f, 0.5f, 1f)); // Gray

    private final String displayName;
    private final String description;
    private final int triggerChance; // Percentage (0-100)
    private final Color indicatorColor;

    SpecialAbility(String displayName, String description, int triggerChance, Color indicatorColor) {
        this.displayName = displayName;
        this.description = description;
        this.triggerChance = triggerChance;
        this.indicatorColor = indicatorColor;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getTriggerChance() { return triggerChance; }
    public Color getIndicatorColor() { return indicatorColor; }

    /**
     * Check if the ability triggers based on random chance.
     */
    public boolean shouldTrigger(java.util.Random random) {
        return random.nextInt(100) < triggerChance;
    }

    /**
     * Get the effect value for this ability.
     * Returns different values based on ability type.
     */
    public int getEffectValue(int attackerLevel, int floor) {
        int base = 2 + floor;
        return switch (this) {
            case POISON -> base + 2; // Damage per turn
            case BURN -> base + 3; // Damage per turn (higher than poison)
            case STUN -> 1; // Turns stunned
            case LIFE_DRAIN -> base + 4; // HP drained
            case PHASE -> 50; // % chance to dodge
            case CORRODE -> 2 + floor; // Defense reduction
            case STEAL_GOLD -> 5 + floor * 3; // Gold stolen
            case CURSE -> 2 + floor; // Attack reduction
            case CHARGE -> 2; // Damage multiplier
            case ENRAGE -> 3 + floor; // Attack bonus when low HP
            case SUMMON -> 1; // Number of minions
            default -> 0;
        };
    }

    /**
     * Get the duration in turns for effects that last multiple turns.
     */
    public int getDuration() {
        return switch (this) {
            case POISON -> 3;
            case BURN -> 2;
            case STUN -> 1;
            case CORRODE -> 4;
            case CURSE -> 3;
            default -> 0;
        };
    }

    /**
     * Get a combat message when this ability triggers.
     */
    public String getTriggerMessage(String attackerName, String targetName) {
        return switch (this) {
            case POISON -> attackerName + " poisons " + targetName + "!";
            case BURN -> attackerName + " sets " + targetName + " ablaze!";
            case STUN -> attackerName + " stuns " + targetName + "!";
            case LIFE_DRAIN -> attackerName + " drains life from " + targetName + "!";
            case PHASE -> attackerName + " phases through the attack!";
            case CORRODE -> attackerName + "'s acid corrodes " + targetName + "'s armor!";
            case STEAL_GOLD -> attackerName + " steals gold from " + targetName + "!";
            case CURSE -> attackerName + " curses " + targetName + "!";
            case CHARGE -> attackerName + " charges with devastating force!";
            case ENRAGE -> attackerName + " becomes enraged!";
            case SUMMON -> attackerName + " summons reinforcements!";
            default -> "";
        };
    }
}
