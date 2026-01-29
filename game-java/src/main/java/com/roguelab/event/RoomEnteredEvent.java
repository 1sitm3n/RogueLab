package com.roguelab.event;

import com.roguelab.domain.RoomType;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when the player enters a new room.
 */
public final class RoomEnteredEvent extends AbstractGameEvent {
    
    private final int floor;
    private final String roomId;
    private final RoomType roomType;
    private final int enemyCount;
    private final boolean hasChest;
    private final double playerHealthPercent;
    
    public RoomEnteredEvent(UUID runId, int tick, int floor, String roomId,
                            RoomType roomType, int enemyCount, boolean hasChest,
                            double playerHealthPercent) {
        super(EventType.ROOM_ENTERED, runId, tick);
        this.floor = floor;
        this.roomId = roomId;
        this.roomType = roomType;
        this.enemyCount = enemyCount;
        this.hasChest = hasChest;
        this.playerHealthPercent = playerHealthPercent;
    }
    
    public RoomEnteredEvent(Instant timestamp, UUID runId, int tick, int floor, 
                            String roomId, RoomType roomType, int enemyCount, 
                            boolean hasChest, double playerHealthPercent) {
        super(EventType.ROOM_ENTERED, timestamp, runId, tick);
        this.floor = floor;
        this.roomId = roomId;
        this.roomType = roomType;
        this.enemyCount = enemyCount;
        this.hasChest = hasChest;
        this.playerHealthPercent = playerHealthPercent;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public RoomType getRoomType() {
        return roomType;
    }
    
    public int getEnemyCount() {
        return enemyCount;
    }
    
    public boolean isHasChest() {
        return hasChest;
    }
    
    public double getPlayerHealthPercent() {
        return playerHealthPercent;
    }
    
    @Override
    public String toString() {
        return String.format("RoomEnteredEvent[room=%s, type=%s, floor=%d, enemies=%d]",
            roomId, roomType, floor, enemyCount);
    }
}
