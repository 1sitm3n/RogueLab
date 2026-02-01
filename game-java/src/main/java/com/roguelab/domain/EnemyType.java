package com.roguelab.domain;

/**
 * Types of enemies that can appear in the game.
 * 
 * BALANCE v0.5.2:
 * - v8 was 14% win (boss ATK 10)
 * - v9 was 91% win (boss ATK 8)
 * - v10: Split the difference - boss ATK 9
 * - Target: ~50% win rate
 */
public enum EnemyType {
    
    // Floor 1-2
    RAT("Giant Rat", 12, 5, 1, 2, 1, 0, DamageType.PHYSICAL, false),
    SLIME("Slime", 16, 4, 0, 3, 1, 0, DamageType.PHYSICAL, false),
    BAT("Cave Bat", 10, 6, 0, 2, 1, 0, DamageType.PHYSICAL, false),
    GOBLIN("Goblin", 20, 7, 2, 3, 1, 1, DamageType.PHYSICAL, false),
    
    // Floor 2-3 (back to v8 levels for some attrition)
    SKELETON("Skeleton", 24, 8, 2, 4, 2, 1, DamageType.PHYSICAL, false),
    ZOMBIE("Zombie", 32, 6, 2, 5, 1, 1, DamageType.PHYSICAL, false),
    SPIDER("Giant Spider", 18, 8, 1, 3, 2, 1, DamageType.POISON, false),
    ORC("Orc Warrior", 38, 9, 4, 5, 2, 1, DamageType.PHYSICAL, false),
    
    // Floor 3+ 
    TROLL("Cave Troll", 55, 11, 5, 7, 2, 1, DamageType.PHYSICAL, false),
    WRAITH("Wraith", 26, 11, 2, 4, 2, 1, DamageType.MAGIC, false),
    ELEMENTAL("Fire Elemental", 42, 12, 4, 5, 2, 1, DamageType.FIRE, false),
    GOLEM("Stone Golem", 70, 9, 8, 8, 2, 2, DamageType.PHYSICAL, false),
    
    // Bosses (between v8 and v9)
    GOBLIN_KING("Goblin King", 60, 9, 3, 5, 1, 1, DamageType.PHYSICAL, true),
    NECROMANCER("Necromancer", 75, 13, 3, 7, 2, 1, DamageType.MAGIC, true),
    DRAGON("Ancient Dragon", 140, 17, 7, 12, 2, 1, DamageType.FIRE, true);

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
