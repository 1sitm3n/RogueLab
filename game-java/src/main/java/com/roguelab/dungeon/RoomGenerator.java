package com.roguelab.dungeon;

import com.roguelab.domain.*;
import com.roguelab.util.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates content (enemies, items) for rooms.
 * Handles enemy selection, scaling, and loot placement.
 */
public final class RoomGenerator {
    
    private final DungeonConfig config;
    private final GameRandom random;
    
    // Enemy pools by floor tier
    private static final EnemyType[] TIER_1_ENEMIES = {
        EnemyType.RAT, EnemyType.SLIME, EnemyType.BAT
    };
    
    private static final EnemyType[] TIER_2_ENEMIES = {
        EnemyType.GOBLIN, EnemyType.SKELETON, EnemyType.SPIDER
    };
    
    private static final EnemyType[] TIER_3_ENEMIES = {
        EnemyType.ORC, EnemyType.ZOMBIE, EnemyType.WRAITH
    };
    
    private static final EnemyType[] TIER_4_ENEMIES = {
        EnemyType.TROLL, EnemyType.ELEMENTAL, EnemyType.GOLEM
    };
    
    private static final EnemyType[] BOSSES = {
        EnemyType.GOBLIN_KING, EnemyType.NECROMANCER, EnemyType.DRAGON
    };
    
    public RoomGenerator(DungeonConfig config, GameRandom random) {
        this.config = config;
        this.random = random;
    }
    
    /**
     * Populate a combat room with enemies.
     */
    public void populateCombatRoom(Room room) {
        int enemyCount = config.getMinEnemiesPerRoom() + 
            random.nextInt(config.getMaxEnemiesPerRoom() - config.getMinEnemiesPerRoom() + 1);
        int floor = room.getFloor();
        
        for (int i = 0; i < enemyCount; i++) {
            EnemyType type = selectEnemyType(floor);
            Enemy enemy = new Enemy(type, floor);
            room.addEnemy(enemy);
        }
        
        // Chance for item drop
        if (random.chance(config.getItemDropChance())) {
            Item loot = generateLoot(floor, Rarity.COMMON);
            room.addItem(loot);
        }
    }
    
    /**
     * Populate a boss room with boss enemy and guaranteed loot.
     */
    public void populateBossRoom(Room room) {
        int floor = room.getFloor();
        EnemyType bossType = selectBossType(floor);
        Enemy boss = new Enemy(bossType, floor);
        room.addEnemy(boss);
        
        // Boss rooms have guaranteed rare+ loot
        Item reward = generateLoot(floor, Rarity.RARE);
        room.addItem(reward);
    }
    
    /**
     * Populate a treasure room with items.
     */
    public void populateTreasureRoom(Room room) {
        int floor = room.getFloor();
        
        // 1-3 items depending on luck
        int itemCount = 1 + random.nextInt(3);
        for (int i = 0; i < itemCount; i++) {
            Rarity rarity = rollRarity(floor);
            Item treasure = generateLoot(floor, rarity);
            room.addItem(treasure);
        }
    }
    
    /**
     * Populate a shop room with purchasable items.
     */
    public void populateShopRoom(Room room) {
        int floor = room.getFloor();
        
        // Shops have 3-5 items for sale
        int itemCount = 3 + random.nextInt(3);
        for (int i = 0; i < itemCount; i++) {
            Rarity rarity = rollRarity(floor);
            Item shopItem = generateShopItem(floor, rarity);
            room.addItem(shopItem);
        }
    }
    
    /**
     * Select appropriate enemy type for the floor.
     */
    private EnemyType selectEnemyType(int floor) {
        EnemyType[] pool = getEnemyPool(floor);
        return random.pick(List.of(pool));
    }
    
    private EnemyType[] getEnemyPool(int floor) {
        if (floor <= 2) return TIER_1_ENEMIES;
        if (floor <= 4) return TIER_2_ENEMIES;
        if (floor <= 6) return TIER_3_ENEMIES;
        return TIER_4_ENEMIES;
    }
    
    /**
     * Select boss type based on floor.
     */
    private EnemyType selectBossType(int floor) {
        int bossIndex = Math.min((floor / 3) - 1, BOSSES.length - 1);
        bossIndex = Math.max(0, bossIndex);
        return BOSSES[bossIndex];
    }
    
    /**
     * Roll for item rarity based on floor.
     */
    private Rarity rollRarity(int floor) {
        double roll = random.nextDouble();
        double legendaryChance = 0.01 + (floor * 0.005);
        double epicChance = 0.05 + (floor * 0.01);
        double rareChance = 0.15 + (floor * 0.02);
        double uncommonChance = 0.35 + (floor * 0.02);
        
        if (roll < legendaryChance) return Rarity.LEGENDARY;
        if (roll < legendaryChance + epicChance) return Rarity.EPIC;
        if (roll < legendaryChance + epicChance + rareChance) return Rarity.RARE;
        if (roll < legendaryChance + epicChance + rareChance + uncommonChance) return Rarity.UNCOMMON;
        return Rarity.COMMON;
    }
    
    /**
     * Generate a loot item.
     */
    private Item generateLoot(int floor, Rarity minRarity) {
        Rarity rarity = rollRarity(floor);
        if (rarity.ordinal() < minRarity.ordinal()) {
            rarity = minRarity;
        }
        
        // Decide item type
        ItemType type = rollItemType();
        
        return generateItem(type, rarity, floor);
    }
    
    /**
     * Generate a shop item with price set.
     */
    private Item generateShopItem(int floor, Rarity rarity) {
        ItemType type = rollItemType();
        Item item = generateItem(type, rarity, floor);
        return item; // Price is derived from value
    }
    
    private ItemType rollItemType() {
        double roll = random.nextDouble();
        if (roll < 0.35) return ItemType.WEAPON;
        if (roll < 0.65) return ItemType.ARMOR;
        if (roll < 0.80) return ItemType.ACCESSORY;
        if (roll < 0.95) return ItemType.CONSUMABLE;
        return ItemType.RELIC;
    }
    
    /**
     * Generate a specific item.
     */
    private Item generateItem(ItemType type, Rarity rarity, int floor) {
        int rarityBonus = rarity.ordinal() * 2;
        int floorBonus = floor;
        
        String name = generateItemName(type, rarity);
        
        Item.Builder builder = Item.builder()
            .name(name)
            .type(type)
            .rarity(rarity)
            .value(calculateItemValue(rarity, floor));
        
        switch (type) {
            case WEAPON -> builder.attackBonus(3 + rarityBonus + floorBonus);
            case ARMOR -> builder.defenseBonus(2 + rarityBonus + floorBonus);
            case HELMET -> builder.defenseBonus(1 + rarityBonus + (floorBonus / 2));
            case BOOTS -> builder.defenseBonus(1 + rarityBonus + (floorBonus / 2));
            case ACCESSORY -> {
                if (random.chance(0.5)) {
                    builder.attackBonus(1 + rarityBonus);
                } else {
                    builder.defenseBonus(1 + rarityBonus);
                }
            }
            case CONSUMABLE -> builder.healthBonus(10 + rarityBonus * 5 + floor * 2);
            case RELIC -> {
                builder.attackBonus(1 + rarityBonus);
                builder.defenseBonus(1 + rarityBonus);
            }
            default -> {} // KEY, QUEST, CURRENCY have no stats
        }
        
        return builder.build();
    }
    
    private String generateItemName(ItemType type, Rarity rarity) {
        String prefix = switch (rarity) {
            case COMMON -> "";
            case UNCOMMON -> "Fine ";
            case RARE -> "Superior ";
            case EPIC -> "Masterwork ";
            case LEGENDARY -> "Legendary ";
        };
        
        String baseName = switch (type) {
            case WEAPON -> random.pick(List.of("Sword", "Axe", "Mace", "Dagger", "Spear"));
            case ARMOR -> random.pick(List.of("Chainmail", "Plate Armor", "Leather Armor", "Robes"));
            case HELMET -> random.pick(List.of("Helmet", "Hood", "Crown", "Circlet"));
            case BOOTS -> random.pick(List.of("Boots", "Greaves", "Sandals"));
            case ACCESSORY -> random.pick(List.of("Ring", "Amulet", "Bracelet", "Cloak"));
            case CONSUMABLE -> random.pick(List.of("Health Potion", "Elixir", "Healing Salve"));
            case RELIC -> random.pick(List.of("Ancient Relic", "Mystic Orb", "Dragon Scale"));
            default -> "Item";
        };
        
        return (prefix + baseName).trim();
    }
    
    private int calculateItemValue(Rarity rarity, int floor) {
        int baseValue = switch (rarity) {
            case COMMON -> 10;
            case UNCOMMON -> 25;
            case RARE -> 50;
            case EPIC -> 100;
            case LEGENDARY -> 250;
        };
        return baseValue + (floor * 5);
    }
}
