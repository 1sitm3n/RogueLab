package com.roguelab.domain;

/**
 * Player character classes with different starting stats.
 */
public enum PlayerClass {
    WARRIOR("Warrior", 120, 12, 8, "High health and defense, balanced attack"),
    ROGUE("Rogue", 80, 15, 4, "High attack, low defense, critical hit bonus"),
    MAGE("Mage", 70, 18, 3, "Highest attack, lowest defense, magic damage");
    
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
