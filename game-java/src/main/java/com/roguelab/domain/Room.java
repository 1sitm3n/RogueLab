package com.roguelab.domain;

import java.util.*;

/**
 * Represents a room/location in the dungeon.
 */
public final class Room {
    
    private final EntityId id;
    private final RoomType type;
    private final int floor;
    private final int roomNumber;
    private final List<Enemy> enemies;
    private final List<Item> items;
    private boolean visited;
    private boolean cleared;
    
    public Room(EntityId id, RoomType type, int floor, int roomNumber) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        this.visited = false;
        this.cleared = false;
    }
    
    public EntityId getId() { return id; }
    public RoomType getType() { return type; }
    public int getFloor() { return floor; }
    public int getRoomNumber() { return roomNumber; }
    public boolean isVisited() { return visited; }
    public boolean isCleared() { return cleared; }
    
    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
    
    public List<Enemy> getAliveEnemies() {
        return enemies.stream().filter(Enemy::isAlive).toList();
    }
    
    public boolean hasAliveEnemies() {
        return enemies.stream().anyMatch(Enemy::isAlive);
    }
    
    public boolean allEnemiesDefeated() {
        return enemies.stream().allMatch(Enemy::isDead);
    }
    
    public int getAliveEnemyCount() {
        return (int) enemies.stream().filter(Enemy::isAlive).count();
    }
    
    public void addEnemy(Enemy enemy) {
        enemies.add(Objects.requireNonNull(enemy));
    }
    
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public void addItem(Item item) {
        items.add(Objects.requireNonNull(item));
    }
    
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
    
    public void visit() { this.visited = true; }
    public void markCleared() { this.cleared = true; }
    
    @Override
    public String toString() {
        return String.format("Room[%s, %s, floor=%d, enemies=%d]",
            id.value(), type, floor, enemies.size());
    }
}
