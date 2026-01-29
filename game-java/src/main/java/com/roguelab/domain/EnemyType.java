package com.roguelab.domain;

/**
 * Types of enemies in the game.
 * Each type has base stats that can be scaled by floor.
 */
public enum EnemyType {
    // Basic enemies (floors 1-3)
    GOBLIN("Goblin", 20, 5, 3, false),
    RAT("Giant Rat", 15, 4, 2, false),
    SLIME("Slime", 25, 3, 1, false),
    
    // Mid-tier enemies (floors 3-6)
    ORC("Orc", 40, 8, 5, false),
    SKELETON("Skeleton", 30, 7, 4, false),
    SPIDER("Giant Spider", 25, 6, 3, false),
    
    // High-tier enemies (floors 6+)
    DEMON("Demon", 60, 12, 8, false),
    GOLEM("Stone Golem", 80, 10, 12, false),
    WRAITH("Wraith", 45, 15, 4, false),
    
    // Bosses
    GOBLIN_KING("Goblin King", 100, 12, 8, true),
    DRAGON("Ancient Dragon", 200, 20, 15, true),
    DEMON_LORD("Demon Lord", 250, 25, 12, true);
    
    private final String displayName;
    private final int baseHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final boolean isBoss;
    
    EnemyType(String displayName, int baseHealth, int baseAttack, int baseDefense, boolean isBoss) {
        this.displayName = displayName;
        this.baseHealth = baseHealth;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.isBoss = isBoss;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getBaseHealth() {
        return baseHealth;
    }
    
    public int getBaseAttack() {
        return baseAttack;
    }
    
    public int getBaseDefense() {
        return baseDefense;
    }
    
    public boolean isBoss() {
        return isBoss;
    }
    
    /**
     * Calculate scaled stats for a given floor.
     * Enemies get ~10% stronger per floor.
     */
    public int getScaledHealth(int floor) {
        return (int) (baseHealth * (1.0 + (floor - 1) * 0.1));
    }
    
    public int getScaledAttack(int floor) {
        return (int) (baseAttack * (1.0 + (floor - 1) * 0.1));
    }
    
    public int getScaledDefense(int floor) {
        return (int) (baseDefense * (1.0 + (floor - 1) * 0.1));
    }
}
