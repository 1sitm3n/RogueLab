package com.roguelab.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a room in the dungeon.
 * Rooms have a type and may contain enemies, items, or other features.
 */
public final class Room {
    
    private final EntityId id;
    private final RoomType type;
    private final int floor;
    private final List<Enemy> enemies;
    private final List<Item> items;
    private boolean visited;
    private boolean cleared;
    private boolean hasChest;
    
    public Room(EntityId id, RoomType type, int floor) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        this.visited = false;
        this.cleared = false;
        this.hasChest = false;
    }
    
    /**
     * Create a room with auto-generated ID.
     */
    public static Room create(RoomType type, int floor, int roomIndex) {
        EntityId id = EntityId.of(String.format("room_%d_%d", floor, roomIndex));
        return new Room(id, type, floor);
    }
    
    public EntityId getId() {
        return id;
    }
    
    public RoomType getType() {
        return type;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
    
    public List<Enemy> getLivingEnemies() {
        return enemies.stream()
            .filter(Enemy::isAlive)
            .toList();
    }
    
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public boolean isVisited() {
        return visited;
    }
    
    public boolean isCleared() {
        return cleared;
    }
    
    public boolean hasChest() {
        return hasChest;
    }
    
    public void setHasChest(boolean hasChest) {
        this.hasChest = hasChest;
    }
    
    /**
     * Add an enemy to this room.
     */
    public void addEnemy(Enemy enemy) {
        Objects.requireNonNull(enemy);
        enemies.add(enemy);
    }
    
    /**
     * Add multiple enemies to this room.
     */
    public void addEnemies(List<Enemy> newEnemies) {
        enemies.addAll(newEnemies);
    }
    
    /**
     * Add an item to this room (e.g., from a chest or drop).
     */
    public void addItem(Item item) {
        Objects.requireNonNull(item);
        items.add(item);
    }
    
    /**
     * Remove an item from this room (e.g., player picked it up).
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    /**
     * Mark room as visited (player entered).
     */
    public void markVisited() {
        this.visited = true;
    }
    
    /**
     * Mark room as cleared (all enemies defeated, if any).
     */
    public void markCleared() {
        this.cleared = true;
    }
    
    /**
     * Check if room has combat encounter.
     */
    public boolean hasCombat() {
        return type == RoomType.COMBAT || type == RoomType.BOSS;
    }
    
    /**
     * Check if all enemies in room are dead.
     */
    public boolean allEnemiesDefeated() {
        return enemies.stream().allMatch(Enemy::isDead);
    }
    
    /**
     * Get enemy count (for telemetry).
     */
    public int getEnemyCount() {
        return enemies.size();
    }
    
    /**
     * Get living enemy count.
     */
    public int getLivingEnemyCount() {
        return (int) enemies.stream().filter(Enemy::isAlive).count();
    }
    
    /**
     * Check if room is a boss room.
     */
    public boolean isBossRoom() {
        return type == RoomType.BOSS;
    }
    
    /**
     * Check if room is a shop.
     */
    public boolean isShop() {
        return type == RoomType.SHOP;
    }
    
    /**
     * Check if room is a rest site.
     */
    public boolean isRestSite() {
        return type == RoomType.REST;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        return id.equals(room.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Room[%s] %s (Floor %d) - %d enemies, %d items",
            id, type, floor, enemies.size(), items.size());
    }
}
