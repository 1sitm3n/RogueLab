package com.roguelab.event;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Emitted when combat begins in a room.
 */
public final class CombatStartedEvent extends AbstractGameEvent {
    
    private final String roomId;
    private final List<EnemyInfo> enemies;
    private final int playerHealth;
    private final int playerMaxHealth;
    
    public CombatStartedEvent(UUID runId, int tick, String roomId,
                              List<EnemyInfo> enemies, int playerHealth, int playerMaxHealth) {
        super(EventType.COMBAT_STARTED, runId, tick);
        this.roomId = roomId;
        this.enemies = Collections.unmodifiableList(enemies);
        this.playerHealth = playerHealth;
        this.playerMaxHealth = playerMaxHealth;
    }
    
    public CombatStartedEvent(Instant timestamp, UUID runId, int tick, String roomId,
                              List<EnemyInfo> enemies, int playerHealth, int playerMaxHealth) {
        super(EventType.COMBAT_STARTED, timestamp, runId, tick);
        this.roomId = roomId;
        this.enemies = Collections.unmodifiableList(enemies);
        this.playerHealth = playerHealth;
        this.playerMaxHealth = playerMaxHealth;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public List<EnemyInfo> getEnemies() {
        return enemies;
    }
    
    public int getPlayerHealth() {
        return playerHealth;
    }
    
    public int getPlayerMaxHealth() {
        return playerMaxHealth;
    }
    
    @Override
    public String toString() {
        return String.format("CombatStartedEvent[room=%s, enemies=%d, playerHP=%d/%d]",
            roomId, enemies.size(), playerHealth, playerMaxHealth);
    }
}
