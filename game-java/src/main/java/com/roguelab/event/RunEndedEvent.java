package com.roguelab.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted once when a run concludes (victory, defeat, or abandoned).
 * Contains summary statistics for the entire run.
 */
public final class RunEndedEvent extends AbstractGameEvent {
    
    /**
     * Possible outcomes for a run.
     */
    public enum Outcome {
        VICTORY,
        DEFEAT,
        ABANDONED
    }
    
    private final Outcome outcome;
    private final int finalFloor;
    private final int finalScore;
    private final int totalGold;
    private final int itemsCollected;
    private final int enemiesDefeated;
    private final long durationSeconds;
    
    public RunEndedEvent(UUID runId, int tick, Outcome outcome, int finalFloor,
                         int finalScore, int totalGold, int itemsCollected,
                         int enemiesDefeated, long durationSeconds) {
        super(EventType.RUN_ENDED, runId, tick);
        this.outcome = outcome;
        this.finalFloor = finalFloor;
        this.finalScore = finalScore;
        this.totalGold = totalGold;
        this.itemsCollected = itemsCollected;
        this.enemiesDefeated = enemiesDefeated;
        this.durationSeconds = durationSeconds;
    }
    
    public RunEndedEvent(Instant timestamp, UUID runId, int tick, Outcome outcome, 
                         int finalFloor, int finalScore, int totalGold, int itemsCollected,
                         int enemiesDefeated, long durationSeconds) {
        super(EventType.RUN_ENDED, timestamp, runId, tick);
        this.outcome = outcome;
        this.finalFloor = finalFloor;
        this.finalScore = finalScore;
        this.totalGold = totalGold;
        this.itemsCollected = itemsCollected;
        this.enemiesDefeated = enemiesDefeated;
        this.durationSeconds = durationSeconds;
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
    
    public int getFinalFloor() {
        return finalFloor;
    }
    
    public int getFinalScore() {
        return finalScore;
    }
    
    public int getTotalGold() {
        return totalGold;
    }
    
    public int getItemsCollected() {
        return itemsCollected;
    }
    
    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }
    
    public long getDurationSeconds() {
        return durationSeconds;
    }
    
    @Override
    public String toString() {
        return String.format("RunEndedEvent[runId=%s, outcome=%s, floor=%d, score=%d]",
            getRunId(), outcome, finalFloor, finalScore);
    }
}
