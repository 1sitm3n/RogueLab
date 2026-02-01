package com.roguelab.dungeon;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.Room;
import com.roguelab.util.GameRandom;

import java.util.*;

/**
 * Represents a complete dungeon with multiple floors.
 * Manages floor progression and state.
 * 
 * BALANCE v0.4.3: Added max floor limit for winnable games
 */
public final class Dungeon {

    private final EntityId id;
    private final long seed;
    private final DungeonConfig config;
    private final FloorGenerator floorGenerator;
    private final Map<Integer, Floor> floors;

    private int currentFloorNumber;
    private int deepestFloorReached;

    public Dungeon(long seed, DungeonConfig config) {
        this.id = EntityId.of("dungeon_" + seed);
        this.seed = seed;
        this.config = config;
        this.floorGenerator = new FloorGenerator(config, new GameRandom(seed));
        this.floors = new HashMap<>();
        this.currentFloorNumber = 1;
        this.deepestFloorReached = 1;

        // Generate first floor
        generateFloor(1);
    }

    public Dungeon(long seed) {
        this(seed, DungeonConfig.standard());
    }

    // === GETTERS ===

    public EntityId getId() { return id; }
    public long getSeed() { return seed; }
    public DungeonConfig getConfig() { return config; }
    public int getCurrentFloorNumber() { return currentFloorNumber; }
    public int getDeepestFloorReached() { return deepestFloorReached; }
    public int getMaxFloors() { return config.getMaxFloors(); }

    public Floor getCurrentFloor() {
        return floors.get(currentFloorNumber);
    }

    public Room getCurrentRoom() {
        return getCurrentFloor().getCurrentRoom();
    }

    public Optional<Floor> getFloor(int floorNumber) {
        return Optional.ofNullable(floors.get(floorNumber));
    }

    public int getFloorsGenerated() {
        return floors.size();
    }

    // === FLOOR MANAGEMENT ===

    /**
     * Generate a floor if it doesn't exist.
     */
    private Floor generateFloor(int floorNumber) {
        if (floors.containsKey(floorNumber)) {
            return floors.get(floorNumber);
        }

        Floor floor = floorGenerator.generateFloor(floorNumber);
        floors.put(floorNumber, floor);
        return floor;
    }

    /**
     * Descend to the next floor.
     * Generates the floor if needed.
     */
    public Floor descendToNextFloor() {
        Floor currentFloor = getCurrentFloor();

        if (!currentFloor.isAtExit()) {
            throw new IllegalStateException("Must be at floor exit to descend");
        }
        
        if (isOnFinalFloor()) {
            throw new IllegalStateException("Already on final floor - cannot descend");
        }

        currentFloor.markCompleted();
        currentFloorNumber++;
        deepestFloorReached = Math.max(deepestFloorReached, currentFloorNumber);

        return generateFloor(currentFloorNumber);
    }

    /**
     * Ascend to the previous floor.
     * Only allowed if the floor was already visited.
     */
    public Floor ascendToPreviousFloor() {
        if (currentFloorNumber <= 1) {
            throw new IllegalStateException("Already at the top floor");
        }

        currentFloorNumber--;
        Floor floor = floors.get(currentFloorNumber);

        if (floor == null) {
            throw new IllegalStateException("Cannot ascend to unvisited floor");
        }

        return floor;
    }

    // === NAVIGATION ===

    /**
     * Move to the next room on the current floor.
     */
    public Room advanceToNextRoom() {
        return getCurrentFloor().advanceToNextRoom();
    }

    /**
     * Move to the previous room on the current floor.
     */
    public Room returnToPreviousRoom() {
        return getCurrentFloor().returnToPreviousRoom();
    }

    /**
     * Check if player can descend to next floor.
     * Returns false if on final floor (victory condition).
     */
    public boolean canDescend() {
        Floor floor = getCurrentFloor();
        
        // Can't descend past max floors
        if (isOnFinalFloor()) {
            return false;
        }
        
        return floor.isAtExit() && floor.allCombatRoomsCleared();
    }

    /**
     * Check if player can ascend to previous floor.
     */
    public boolean canAscend() {
        return currentFloorNumber > 1 && floors.containsKey(currentFloorNumber - 1);
    }
    
    /**
     * Check if currently on the final floor.
     */
    public boolean isOnFinalFloor() {
        return currentFloorNumber >= config.getMaxFloors();
    }

    // === STATE ===

    /**
     * Check if this is a boss floor.
     */
    public boolean isCurrentFloorBossFloor() {
        return config.isBossFloor(currentFloorNumber);
    }

    /**
     * Get total rooms visited across all floors.
     */
    public int getTotalRoomsVisited() {
        return floors.values().stream()
            .flatMap(f -> f.getRooms().stream())
            .filter(Room::isVisited)
            .mapToInt(r -> 1)
            .sum();
    }

    /**
     * Get total enemies defeated across all floors.
     */
    public int getTotalRoomsCleared() {
        return floors.values().stream()
            .flatMap(f -> f.getRooms().stream())
            .filter(Room::isCleared)
            .mapToInt(r -> 1)
            .sum();
    }

    @Override
    public String toString() {
        return String.format("Dungeon[seed=%d, floor=%d/%d, deepest=%d]",
            seed, currentFloorNumber, config.getMaxFloors(), deepestFloorReached);
    }
}
