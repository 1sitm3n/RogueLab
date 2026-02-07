package com.roguelab.domain;

/**
 * Enemy types with stats, scaling, and special abilities.
 */
public enum EnemyType {
    // === FLOOR 1 ENEMIES ===
    RAT("Giant Rat", 15, 5, 1, 5, 8, false, SpecialAbility.NONE, DamageType.PHYSICAL),
    BAT("Cave Bat", 12, 4, 0, 4, 6, false, SpecialAbility.NONE, DamageType.PHYSICAL),
    SPIDER("Cave Spider", 18, 6, 1, 6, 10, false, SpecialAbility.POISON, DamageType.POISON),
    SKELETON("Skeleton", 22, 7, 2, 8, 12, false, SpecialAbility.NONE, DamageType.PHYSICAL),
    SLIME("Acid Slime", 20, 5, 3, 5, 8, false, SpecialAbility.CORRODE, DamageType.POISON),
    GOBLIN("Goblin", 20, 8, 1, 7, 10, false, SpecialAbility.STEAL_GOLD, DamageType.PHYSICAL),

    // === FLOOR 2 ENEMIES ===
    ZOMBIE("Zombie", 35, 9, 3, 10, 15, false, SpecialAbility.LIFE_DRAIN, DamageType.PHYSICAL),
    ORC("Orc Warrior", 40, 12, 4, 12, 18, false, SpecialAbility.NONE, DamageType.PHYSICAL),
    GHOST("Vengeful Ghost", 25, 10, 0, 10, 14, false, SpecialAbility.PHASE, DamageType.MAGIC),
    WRAITH("Soul Wraith", 30, 11, 1, 12, 16, false, SpecialAbility.LIFE_DRAIN, DamageType.MAGIC),
    CULTIST("Dark Cultist", 28, 11, 2, 11, 16, false, SpecialAbility.CURSE, DamageType.MAGIC),

    // === FLOOR 3 ENEMIES ===
    TROLL("Cave Troll", 60, 14, 6, 15, 20, false, SpecialAbility.NONE, DamageType.PHYSICAL),
    ELEMENTAL("Fire Elemental", 45, 15, 3, 14, 18, false, SpecialAbility.BURN, DamageType.FIRE),
    GOLEM("Stone Golem", 70, 10, 10, 12, 18, false, SpecialAbility.STUN, DamageType.PHYSICAL),
    DEMON("Lesser Demon", 50, 14, 5, 15, 22, false, SpecialAbility.BURN, DamageType.FIRE),
    VAMPIRE("Vampire Spawn", 45, 13, 4, 16, 24, false, SpecialAbility.LIFE_DRAIN, DamageType.PHYSICAL),
    MINOTAUR("Minotaur", 55, 16, 6, 18, 28, false, SpecialAbility.CHARGE, DamageType.PHYSICAL),

    // === BOSSES ===
    GOBLIN_KING("Goblin King", 80, 14, 5, 30, 0, true, SpecialAbility.SUMMON, DamageType.PHYSICAL),
    NECROMANCER("Necromancer", 70, 16, 3, 40, 0, true, SpecialAbility.CURSE, DamageType.MAGIC),
    SKELETON_LORD("Skeleton Lord", 90, 15, 6, 35, 0, true, SpecialAbility.SUMMON, DamageType.PHYSICAL),
    ORC_CHIEFTAIN("Orc Chieftain", 100, 18, 8, 40, 0, true, SpecialAbility.ENRAGE, DamageType.PHYSICAL),
    LICH("The Lich", 90, 20, 5, 50, 0, true, SpecialAbility.CURSE, DamageType.MAGIC),
    DRAGON("Ancient Dragon", 150, 22, 12, 75, 0, true, SpecialAbility.BURN, DamageType.FIRE),
    DEMON_LORD("Demon Lord", 120, 25, 10, 60, 0, true, SpecialAbility.LIFE_DRAIN, DamageType.FIRE);

    private final String displayName;
    private final int baseHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final int goldReward;
    private final int expReward;
    private final boolean boss;
    private final SpecialAbility specialAbility;
    private final DamageType damageType;

    private static final int HEALTH_PER_FLOOR = 5;
    private static final int ATTACK_PER_FLOOR = 2;
    private static final int DEFENSE_PER_FLOOR = 1;

    EnemyType(String displayName, int baseHealth, int baseAttack, int baseDefense,
              int goldReward, int expReward, boolean boss, SpecialAbility specialAbility,
              DamageType damageType) {
        this.displayName = displayName;
        this.baseHealth = baseHealth;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.goldReward = goldReward;
        this.expReward = expReward;
        this.boss = boss;
        this.specialAbility = specialAbility;
        this.damageType = damageType;
    }

    public String getDisplayName() { return displayName; }
    public int getBaseHealth() { return baseHealth; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getGoldReward() { return goldReward; }
    public int getExpReward() { return expReward; }
    public boolean isBoss() { return boss; }
    public SpecialAbility getSpecialAbility() { return specialAbility; }
    public DamageType getDamageType() { return damageType; }

    public int getHealthPerFloor() { return HEALTH_PER_FLOOR; }
    public int getAttackPerFloor() { return ATTACK_PER_FLOOR; }
    public int getDefensePerFloor() { return DEFENSE_PER_FLOOR; }

    public boolean hasSpecialAbility() {
        return specialAbility != SpecialAbility.NONE;
    }

    public static EnemyType[] getEnemiesForFloor(int floor) {
        return switch (floor) {
            case 1 -> new EnemyType[]{RAT, BAT, SPIDER, SKELETON, SLIME, GOBLIN};
            case 2 -> new EnemyType[]{GOBLIN, SKELETON, ZOMBIE, ORC, GHOST, WRAITH, CULTIST};
            case 3 -> new EnemyType[]{ORC, TROLL, ELEMENTAL, GOLEM, DEMON, VAMPIRE, MINOTAUR};
            default -> new EnemyType[]{DEMON, VAMPIRE, MINOTAUR};
        };
    }

    public static EnemyType getBossForFloor(int floor) {
        return switch (floor) {
            case 1 -> GOBLIN_KING;
            case 2 -> NECROMANCER;
            case 3 -> LICH;
            default -> DRAGON;
        };
    }
}
