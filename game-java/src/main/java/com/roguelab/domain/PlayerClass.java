package com.roguelab.domain;

/**
 * Player character classes with different starting stats.
 * 
 * BALANCE v0.5.0:
 * - Nerfed WARRIOR defense 8->5 (was negating most damage)
 * - Nerfed WARRIOR health 120->100
 * - Slight nerfs to other classes for consistency
 */
public enum PlayerClass {
    
    WARRIOR("Warrior", 100, 12, 5, "Balanced stats, good survivability"),
    ROGUE("Rogue", 75, 14, 3, "High attack, low defense, critical hit bonus"),
    MAGE("Mage", 65, 16, 2, "Highest attack, lowest defense, magic damage");

    private final String displayName;
    private final int startingHealth;
    private final int startingAttack;
    private final int startingDefense;
    private final String description;

    PlayerClass(String displayName, int startingHealth, int startingAttack,
                int startingDefense, String description) {
        this.displayName = displayName;
        this.startingHealth = startingHealth;
        this.startingAttack = startingAttack;
        this.startingDefense = startingDefense;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStartingHealth() {
        return startingHealth;
    }

    public int getStartingAttack() {
        return startingAttack;
    }

    public int getStartingDefense() {
        return startingDefense;
    }

    public String getDescription() {
        return description;
    }
}
