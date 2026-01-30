package com.roguelab.dungeon;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.Room;
import com.roguelab.domain.RoomType;
import com.roguelab.util.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates complete dungeon floors with rooms and content.
 * Produces deterministic results for a given seed.
 */
public final class FloorGenerator {
    
    private final DungeonConfig config;
    private final GameRandom random;
    private final RoomGenerator roomGenerator;
    
    private int roomIdCounter = 0;
    private int floorIdCounter = 0;
    
    public FloorGenerator(DungeonConfig config, GameRandom random) {
        this.config = config;
        this.random = random;
        this.roomGenerator = new RoomGenerator(config, random);
    }
    
    /**
     * Generate a complete floor with all rooms populated.
     */
    public Floor generateFloor(int floorNumber) {
        EntityId floorId = EntityId.of("floor_" + (++floorIdCounter));
        
        List<Room> rooms = new ArrayList<>();
        int roomCount = config.getMinRoomsPerFloor() + 
            random.nextInt(config.getMaxRoomsPerFloor() - config.getMinRoomsPerFloor() + 1);
        
        boolean isBossFloor = config.isBossFloor(floorNumber);
        
        // Generate room sequence
        List<RoomType> roomTypes = planRoomTypes(roomCount, isBossFloor);
        
        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType type = roomTypes.get(i);
            Room room = createRoom(type, floorNumber, i);
            populateRoom(room);
            rooms.add(room);
        }
        
        return new Floor(floorId, floorNumber, rooms);
    }
    
    /**
     * Generate a simple test floor with specified room count.
     * Useful for testing and tutorials.
     */
    public Floor generateSimpleFloor(int floorNumber, int roomCount) {
        EntityId floorId = EntityId.of("floor_" + (++floorIdCounter));
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < roomCount; i++) {
            RoomType type = (i == roomCount - 1 && config.isBossFloor(floorNumber)) 
                ? RoomType.BOSS 
                : RoomType.COMBAT;
            Room room = createRoom(type, floorNumber, i);
            populateRoom(room);
            rooms.add(room);
        }
        
        return new Floor(floorId, floorNumber, rooms);
    }
    
    /**
     * Plan the sequence of room types for a floor.
     */
    private List<RoomType> planRoomTypes(int roomCount, boolean isBossFloor) {
        List<RoomType> types = new ArrayList<>();
        
        // First room is always combat (entrance encounter)
        types.add(RoomType.COMBAT);
        
        // Middle rooms are mixed
        for (int i = 1; i < roomCount - 1; i++) {
            RoomType type = rollRoomType();
            types.add(type);
        }
        
        // Last room: boss if boss floor, otherwise combat
        if (roomCount > 1) {
            types.add(isBossFloor ? RoomType.BOSS : RoomType.COMBAT);
        }
        
        return types;
    }
    
    /**
     * Roll for a room type based on configuration chances.
     */
    private RoomType rollRoomType() {
        double roll = random.nextDouble();
        double cumulative = 0.0;
        
        cumulative += config.getTreasureRoomChance();
        if (roll < cumulative) return RoomType.TREASURE;
        
        cumulative += config.getShopRoomChance();
        if (roll < cumulative) return RoomType.SHOP;
        
        cumulative += config.getRestSiteChance();
        if (roll < cumulative) return RoomType.REST;
        
        // Default to combat
        return RoomType.COMBAT;
    }
    
    /**
     * Create an empty room.
     */
    private Room createRoom(RoomType type, int floorNumber, int roomIndex) {
        EntityId roomId = EntityId.of("room_" + (++roomIdCounter));
        return new Room(roomId, type, floorNumber, roomIndex);
    }
    
    /**
     * Populate a room with appropriate content based on its type.
     */
    private void populateRoom(Room room) {
        switch (room.getType()) {
            case COMBAT -> roomGenerator.populateCombatRoom(room);
            case BOSS -> roomGenerator.populateBossRoom(room);
            case TREASURE -> roomGenerator.populateTreasureRoom(room);
            case SHOP -> roomGenerator.populateShopRoom(room);
            case REST, EVENT -> {} // No content generation needed
        }
    }
    
    /**
     * Reset ID counters. Useful for testing.
     */
    public void resetCounters() {
        roomIdCounter = 0;
        floorIdCounter = 0;
    }
}
