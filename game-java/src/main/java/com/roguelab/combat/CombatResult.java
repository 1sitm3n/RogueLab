package com.roguelab.combat;

import java.util.List;

/**
 * Immutable result of a complete combat encounter.
 */
public record CombatResult(
    Outcome outcome,
    int turnsElapsed,
    int totalDamageDealt,
    int totalDamageTaken,
    int goldEarned,
    int experienceGained,
    int enemiesKilled,
    List<String> killedEnemyIds
) {
    
    public enum Outcome {
        VICTORY,
        DEFEAT,
        FLED,
        INTERRUPTED
    }
    
    public boolean isVictory() {
        return outcome == Outcome.VICTORY;
    }
    
    public boolean isDefeat() {
        return outcome == Outcome.DEFEAT;
    }
    
    public static CombatResult victory(int turns, int damageDealt, int damageTaken,
                                        int gold, int xp, List<String> killedIds) {
        return new CombatResult(
            Outcome.VICTORY, turns, damageDealt, damageTaken,
            gold, xp, killedIds.size(), killedIds
        );
    }
    
    public static CombatResult defeat(int turns, int damageDealt, int damageTaken,
                                       List<String> killedIds) {
        return new CombatResult(
            Outcome.DEFEAT, turns, damageDealt, damageTaken,
            0, 0, killedIds.size(), killedIds
        );
    }
}
