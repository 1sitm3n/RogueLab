package com.roguelab.event;

import com.roguelab.domain.Difficulty;
import com.roguelab.domain.PlayerClass;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted once at the beginning of each run.
 * Contains all the initial configuration for the run.
 */
public final class RunStartedEvent extends AbstractGameEvent {
    
    private final long seed;
    private final String gameVersion;
    private final PlayerClass playerClass;
    private final Difficulty difficulty;
    private final String playerName;
    
    public RunStartedEvent(UUID runId, long seed, String gameVersion, 
                           PlayerClass playerClass, Difficulty difficulty, String playerName) {
        super(EventType.RUN_STARTED, runId, 0);
        this.seed = seed;
        this.gameVersion = gameVersion;
        this.playerClass = playerClass;
        this.difficulty = difficulty;
        this.playerName = playerName;
    }
    
    public RunStartedEvent(Instant timestamp, UUID runId, long seed, String gameVersion,
                           PlayerClass playerClass, Difficulty difficulty, String playerName) {
        super(EventType.RUN_STARTED, timestamp, runId, 0);
        this.seed = seed;
        this.gameVersion = gameVersion;
        this.playerClass = playerClass;
        this.difficulty = difficulty;
        this.playerName = playerName;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public String getGameVersion() {
        return gameVersion;
    }
    
    public PlayerClass getPlayerClass() {
        return playerClass;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    @Override
    public String toString() {
        return String.format("RunStartedEvent[runId=%s, seed=%d, player=%s (%s), difficulty=%s]",
            getRunId(), seed, playerName, playerClass, difficulty);
    }
}
