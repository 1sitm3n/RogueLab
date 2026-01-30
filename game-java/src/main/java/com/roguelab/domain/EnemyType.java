package com.roguelab.domain;

/**
 * Types of enemies that can appear in the game.
 */
public enum EnemyType {
    
    // Floor 1-2
    RAT("Giant Rat", 12, 4, 1, 2, 1, 0, DamageType.PHYSICAL, false),
    SLIME("Slime", 15, 3, 0, 3, 1, 0, DamageType.PHYSICAL, false),
    BAT("Cave Bat", 8, 5, 0, 2, 1, 0, DamageType.PHYSICAL, false),
    GOBLIN("Goblin", 18, 6, 2, 3, 1, 1, DamageType.PHYSICAL, false),
    
    // Floor 3-4
    SKELETON("Skeleton", 25, 8, 3, 4, 2, 1, DamageType.PHYSICAL, false),
    ZOMBIE("Zombie", 35, 6, 2, 5, 1, 1, DamageType.PHYSICAL, false),
    SPIDER("Giant Spider", 20, 9, 2, 3, 2, 1, DamageType.POISON, false),
    ORC("Orc Warrior", 40, 10, 4, 6, 2, 1, DamageType.PHYSICAL, false),
    
    // Floor 5-6
    TROLL("Cave Troll", 60, 12, 6, 8, 3, 2, DamageType.PHYSICAL, false),
    WRAITH("Wraith", 30, 15, 2, 5, 3, 1, DamageType.MAGIC, false),
    ELEMENTAL("Fire Elemental", 45, 14, 4, 6, 3, 1, DamageType.FIRE, false),
    GOLEM("Stone Golem", 80, 10, 10, 10, 2, 3, DamageType.PHYSICAL, false),
    
    // Bosses
    GOBLIN_KING("Goblin King", 80, 15, 8, 10, 3, 2, DamageType.PHYSICAL, true),
    NECROMANCER("Necromancer", 100, 20, 5, 15, 4, 2, DamageType.MAGIC, true),
    DRAGON("Ancient Dragon", 200, 30, 15, 25, 5, 3, DamageType.FIRE, true);
    
    private final String displayName;
    private final int baseHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final int healthPerFloor;
    private final int attackPerFloor;
    private final int defensePerFloor;
    private final DamageType damageType;
    private final boolean boss;
    
    EnemyType(String displayName, int baseHealth, int baseAttack, int baseDefense,
              int healthPerFloor, int attackPerFloor, int defensePerFloor,
              DamageType damageType, boolean boss) {
        this.displayName = displayName;
        this.baseHealth = baseHealth;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.healthPerFloor = healthPerFloor;
        this.attackPerFloor = attackPerFloor;
        this.defensePerFloor = defensePerFloor;
        this.damageType = damageType;
        this.boss = boss;
    }
    
    public String getDisplayName() { return displayName; }
    public int getBaseHealth() { return baseHealth; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getHealthPerFloor() { return healthPerFloor; }
    public int getAttackPerFloor() { return attackPerFloor; }
    public int getDefensePerFloor() { return defensePerFloor; }
    public DamageType getDamageType() { return damageType; }
    public boolean isBoss() { return boss; }
    
    public int getHealthForFloor(int floor) {
        return baseHealth + (floor - 1) * healthPerFloor;
    }
    
    public int getAttackForFloor(int floor) {
        return baseAttack + (floor - 1) * attackPerFloor;
    }
    
    public int getDefenseForFloor(int floor) {
        return baseDefense + (floor - 1) * defensePerFloor;
    }
}
