package com.roguelab.core;

import com.roguelab.domain.*;

/**
 * Main entry point for RogueLab.
 * 
 * This class bootstraps the game, initializing all subsystems
 * and starting the game loop.
 */
public final class RogueLab {
    
    public static final String VERSION = "0.1.0";
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║           R O G U E L A B            ║");
        System.out.println("║              v" + VERSION + "                  ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
        
        // For now, just demonstrate that the domain model works
        demonstrateDomainModel();
        
        System.out.println();
        System.out.println("Game engine initialized successfully.");
        System.out.println("Full game loop coming in next iteration.");
    }
    
    /**
     * Temporary method to demonstrate the domain model.
     * Will be replaced by actual game loop.
     */
    private static void demonstrateDomainModel() {
        System.out.println("=== Domain Model Demo ===");
        System.out.println();
        
        // Create a player
        Player player = new Player("Hero", PlayerClass.WARRIOR);
        System.out.println("Created: " + player);
        
        // Create some enemies
        Enemy goblin = new Enemy(EnemyType.GOBLIN, 1);
        Enemy orc = new Enemy(EnemyType.ORC, 2);
        System.out.println("Created: " + goblin);
        System.out.println("Created: " + orc);
        
        // Create an item
        Item sword = Item.builder()
            .name("Iron Sword")
            .type(ItemType.WEAPON)
            .rarity(Rarity.COMMON)
            .attack(5)
            .description("A simple but reliable sword")
            .build();
        System.out.println("Created: " + sword);
        
        // Add item to player inventory and equip
        player.getInventory().addItem(sword);
        player.getInventory().equip(sword);
        System.out.println("Equipped sword. Player attack: " + player.getEffectiveAttack());
        
        // Create a room
        Room combatRoom = Room.create(RoomType.COMBAT, 1, 0);
        combatRoom.addEnemy(goblin);
        System.out.println("Created: " + combatRoom);
        
        // Simulate some damage
        System.out.println();
        System.out.println("=== Combat Simulation ===");
        int damage = player.getEffectiveAttack();
        goblin.getHealth().takeDamage(damage);
        System.out.println("Player attacks goblin for " + damage + " damage!");
        System.out.println("Goblin health: " + goblin.getHealth());
        
        if (goblin.isDead()) {
            System.out.println("Goblin defeated!");
            player.incrementEnemiesKilled();
            player.addExperience(goblin.calculateExperience());
            player.getInventory().addGold(goblin.calculateGoldDrop());
        }
        
        System.out.println();
        System.out.println("Final player state: " + player);
    }
}
