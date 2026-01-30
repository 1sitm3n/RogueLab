package com.roguelab.dungeon;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.Room;
import com.roguelab.domain.RoomType;

import java.util.*;

/**
 * Represents a single floor/level of the dungeon.
 * Contains a sequence of rooms the player must traverse.
 */
public final class Floor {
    
    private final EntityId id;
    private final int floorNumber;
    private final List<Room> rooms;
    private final Room entrance;
    private final Room exit;
    private final Room bossRoom; // null if not a boss floor
    
    private int currentRoomIndex;
    private boolean completed;
    
    public Floor(EntityId id, int floorNumber, List<Room> rooms) {
        this.id = Objects.requireNonNull(id);
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>(Objects.requireNonNull(rooms));
        
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Floor must have at least one room");
        }
        
        this.entrance = rooms.get(0);
        this.exit = rooms.get(rooms.size() - 1);
        this.bossRoom = findBossRoom();
        this.currentRoomIndex = 0;
        this.completed = false;
    }
    
    private Room findBossRoom() {
        return rooms.stream()
            .filter(r -> r.getType() == RoomType.BOSS)
            .findFirst()
            .orElse(null);
    }
    
    // === GETTERS ===
    
    public EntityId getId() { return id; }
    public int getFloorNumber() { return floorNumber; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public int getRoomCount() { return rooms.size(); }
    public Room getEntrance() { return entrance; }
    public Room getExit() { return exit; }
    public Optional<Room> getBossRoom() { return Optional.ofNullable(bossRoom); }
    public boolean hasBoss() { return bossRoom != null; }
    public boolean isCompleted() { return completed; }
    
    // === NAVIGATION ===
    
    public Room getCurrentRoom() {
        return rooms.get(currentRoomIndex);
    }
    
    public int getCurrentRoomIndex() {
        return currentRoomIndex;
    }
    
    public boolean hasNextRoom() {
        return currentRoomIndex < rooms.size() - 1;
    }
    
    public boolean hasPreviousRoom() {
        return currentRoomIndex > 0;
    }
    
    public Room advanceToNextRoom() {
        if (!hasNextRoom()) {
            throw new IllegalStateException("No more rooms on this floor");
        }
        currentRoomIndex++;
        return getCurrentRoom();
    }
    
    public Room returnToPreviousRoom() {
        if (!hasPreviousRoom()) {
            throw new IllegalStateException("Already at the first room");
        }
        currentRoomIndex--;
        return getCurrentRoom();
    }
    
    public Room getRoom(int index) {
        if (index < 0 || index >= rooms.size()) {
            throw new IndexOutOfBoundsException("Room index: " + index);
        }
        return rooms.get(index);
    }
    
    public Optional<Room> getRoomById(EntityId roomId) {
        return rooms.stream()
            .filter(r -> r.getId().equals(roomId))
            .findFirst();
    }
    
    // === STATE ===
    
    public void markCompleted() {
        this.completed = true;
    }
    
    public boolean isAtEntrance() {
        return currentRoomIndex == 0;
    }
    
    public boolean isAtExit() {
        return currentRoomIndex == rooms.size() - 1;
    }
    
    /**
     * Check if all combat rooms on this floor have been cleared.
     */
    public boolean allCombatRoomsCleared() {
        return rooms.stream()
            .filter(r -> r.getType() == RoomType.COMBAT || r.getType() == RoomType.BOSS)
            .allMatch(Room::isCleared);
    }
    
    /**
     * Get count of rooms by type.
     */
    public int countRoomsByType(RoomType type) {
        return (int) rooms.stream()
            .filter(r -> r.getType() == type)
            .count();
    }
    
    /**
     * Get all rooms that haven't been visited yet.
     */
    public List<Room> getUnvisitedRooms() {
        return rooms.stream()
            .filter(r -> !r.isVisited())
            .toList();
    }
    
    /**
     * Get progress through this floor as a percentage.
     */
    public double getProgressPercent() {
        if (rooms.size() == 1) return completed ? 100.0 : 0.0;
        return (currentRoomIndex / (double) (rooms.size() - 1)) * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("Floor %d [%s] - %d rooms, current: %d, %s",
            floorNumber, id.value(), rooms.size(), currentRoomIndex,
            completed ? "COMPLETED" : "IN PROGRESS");
    }
}
