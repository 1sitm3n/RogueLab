package com.roguelab.dungeon;

/**
 * Configuration for dungeon generation.
 * Immutable - use builder to create custom configurations.
 * 
 * BALANCE v0.4.3: Added maxFloors to make game winnable
 */
public final class DungeonConfig {

    // Floor limits
    private final int maxFloors;
    
    // Room counts per floor
    private final int minRoomsPerFloor;
    private final int maxRoomsPerFloor;

    // Enemy spawning
    private final int minEnemiesPerRoom;
    private final int maxEnemiesPerRoom;
    private final double eliteSpawnChance;

    // Item spawning
    private final double itemDropChance;
    private final double treasureRoomChance;
    private final double shopRoomChance;
    private final double restSiteChance;

    // Boss configuration
    private final int bossFloorInterval; // Boss every N floors

    // Difficulty scaling
    private final double difficultyScalePerFloor;

    private DungeonConfig(Builder builder) {
        this.maxFloors = builder.maxFloors;
        this.minRoomsPerFloor = builder.minRoomsPerFloor;
        this.maxRoomsPerFloor = builder.maxRoomsPerFloor;
        this.minEnemiesPerRoom = builder.minEnemiesPerRoom;
        this.maxEnemiesPerRoom = builder.maxEnemiesPerRoom;
        this.eliteSpawnChance = builder.eliteSpawnChance;
        this.itemDropChance = builder.itemDropChance;
        this.treasureRoomChance = builder.treasureRoomChance;
        this.shopRoomChance = builder.shopRoomChance;
        this.restSiteChance = builder.restSiteChance;
        this.bossFloorInterval = builder.bossFloorInterval;
        this.difficultyScalePerFloor = builder.difficultyScalePerFloor;
    }

    // Getters
    public int getMaxFloors() { return maxFloors; }
    public int getMinRoomsPerFloor() { return minRoomsPerFloor; }
    public int getMaxRoomsPerFloor() { return maxRoomsPerFloor; }
    public int getMinEnemiesPerRoom() { return minEnemiesPerRoom; }
    public int getMaxEnemiesPerRoom() { return maxEnemiesPerRoom; }
    public double getEliteSpawnChance() { return eliteSpawnChance; }
    public double getItemDropChance() { return itemDropChance; }
    public double getTreasureRoomChance() { return treasureRoomChance; }
    public double getShopRoomChance() { return shopRoomChance; }
    public double getRestSiteChance() { return restSiteChance; }
    public int getBossFloorInterval() { return bossFloorInterval; }
    public double getDifficultyScalePerFloor() { return difficultyScalePerFloor; }

    /**
     * Check if the given floor should have a boss.
     */
    public boolean isBossFloor(int floorNumber) {
        // Boss on final floor or every interval
        return floorNumber == maxFloors || 
               (floorNumber > 0 && floorNumber % bossFloorInterval == 0);
    }
    
    /**
     * Check if the given floor is the final floor.
     */
    public boolean isFinalFloor(int floorNumber) {
        return floorNumber >= maxFloors;
    }

    /**
     * Default configuration for standard gameplay.
     * 3 floors, boss on floor 3.
     */
    public static DungeonConfig standard() {
        return new Builder()
            .maxFloors(3)
            .bossFloorInterval(3)
            .build();
    }

    /**
     * Easier configuration for testing/tutorials.
     * 2 floors, boss on floor 2, more rest sites.
     */
    public static DungeonConfig easy() {
        return new Builder()
            .maxFloors(2)
            .minRoomsPerFloor(3)
            .maxRoomsPerFloor(4)
            .minEnemiesPerRoom(1)
            .maxEnemiesPerRoom(2)
            .eliteSpawnChance(0.05)
            .restSiteChance(0.35)
            .bossFloorInterval(2)
            .build();
    }

    /**
     * Harder configuration for challenge runs.
     * 5 floors, tougher enemies.
     */
    public static DungeonConfig hard() {
        return new Builder()
            .maxFloors(5)
            .minRoomsPerFloor(5)
            .maxRoomsPerFloor(7)
            .minEnemiesPerRoom(2)
            .maxEnemiesPerRoom(4)
            .eliteSpawnChance(0.25)
            .restSiteChance(0.15)
            .bossFloorInterval(2)
            .difficultyScalePerFloor(0.2)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxFloors = 3;
        private int minRoomsPerFloor = 4;
        private int maxRoomsPerFloor = 6;
        private int minEnemiesPerRoom = 1;
        private int maxEnemiesPerRoom = 3;
        private double eliteSpawnChance = 0.15;
        private double itemDropChance = 0.3;
        private double treasureRoomChance = 0.15;
        private double shopRoomChance = 0.1;
        private double restSiteChance = 0.25;
        private int bossFloorInterval = 3;
        private double difficultyScalePerFloor = 0.1;

        public Builder maxFloors(int value) { this.maxFloors = value; return this; }
        public Builder minRoomsPerFloor(int value) { this.minRoomsPerFloor = value; return this; }
        public Builder maxRoomsPerFloor(int value) { this.maxRoomsPerFloor = value; return this; }
        public Builder minEnemiesPerRoom(int value) { this.minEnemiesPerRoom = value; return this; }
        public Builder maxEnemiesPerRoom(int value) { this.maxEnemiesPerRoom = value; return this; }
        public Builder eliteSpawnChance(double value) { this.eliteSpawnChance = value; return this; }
        public Builder itemDropChance(double value) { this.itemDropChance = value; return this; }
        public Builder treasureRoomChance(double value) { this.treasureRoomChance = value; return this; }
        public Builder shopRoomChance(double value) { this.shopRoomChance = value; return this; }
        public Builder restSiteChance(double value) { this.restSiteChance = value; return this; }
        public Builder bossFloorInterval(int value) { this.bossFloorInterval = value; return this; }
        public Builder difficultyScalePerFloor(double value) { this.difficultyScalePerFloor = value; return this; }

        public DungeonConfig build() {
            if (minRoomsPerFloor > maxRoomsPerFloor) {
                throw new IllegalArgumentException("minRoomsPerFloor cannot exceed maxRoomsPerFloor");
            }
            if (minEnemiesPerRoom > maxEnemiesPerRoom) {
                throw new IllegalArgumentException("minEnemiesPerRoom cannot exceed maxEnemiesPerRoom");
            }
            if (maxFloors < 1) {
                throw new IllegalArgumentException("maxFloors must be at least 1");
            }
            return new DungeonConfig(this);
        }
    }
}
