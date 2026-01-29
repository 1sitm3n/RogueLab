package com.roguelab.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when combat concludes.
 */
public final class CombatEndedEvent extends AbstractGameEvent {
    
    /**
     * Combat outcome from the player's perspective.
     */
    public enum CombatOutcome {
        VICTORY,  // All enemies defeated
        DEFEAT,   // Player died
        FLED      // Player escaped (if escape mechanic exists)
    }
    
    private final String roomId;
    private final CombatOutcome outcome;
    private final int turnsElapsed;
    private final int damageDealt;
    private final int damageTaken;
    private final int playerHealthRemaining;
    private final int goldDropped;
    private final int experienceGained;
    
    public CombatEndedEvent(UUID runId, int tick, String roomId, CombatOutcome outcome,
                            int turnsElapsed, int damageDealt, int damageTaken,
                            int playerHealthRemaining, int goldDropped, int experienceGained) {
        super(EventType.COMBAT_ENDED, runId, tick);
        this.roomId = roomId;
        this.outcome = outcome;
        this.turnsElapsed = turnsElapsed;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.playerHealthRemaining = playerHealthRemaining;
        this.goldDropped = goldDropped;
        this.experienceGained = experienceGained;
    }
    
    public CombatEndedEvent(Instant timestamp, UUID runId, int tick, String roomId, 
                            CombatOutcome outcome, int turnsElapsed, int damageDealt, 
                            int damageTaken, int playerHealthRemaining, int goldDropped, 
                            int experienceGained) {
        super(EventType.COMBAT_ENDED, timestamp, runId, tick);
        this.roomId = roomId;
        this.outcome = outcome;
        this.turnsElapsed = turnsElapsed;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.playerHealthRemaining = playerHealthRemaining;
        this.goldDropped = goldDropped;
        this.experienceGained = experienceGained;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public CombatOutcome getOutcome() {
        return outcome;
    }
    
    public int getTurnsElapsed() {
        return turnsElapsed;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public int getDamageTaken() {
        return damageTaken;
    }
    
    public int getPlayerHealthRemaining() {
        return playerHealthRemaining;
    }
    
    public int getGoldDropped() {
        return goldDropped;
    }
    
    public int getExperienceGained() {
        return experienceGained;
    }
    
    @Override
    public String toString() {
        return String.format("CombatEndedEvent[room=%s, outcome=%s, turns=%d, dmgDealt=%d, dmgTaken=%d]",
            roomId, outcome, turnsElapsed, damageDealt, damageTaken);
    }
}
