package com.roguelab.domain;

/**
 * Game difficulty settings.
 */
public enum Difficulty {
    EASY(0.8, 1.2, 1.5),
    NORMAL(1.0, 1.0, 1.0),
    HARD(1.3, 0.8, 0.7),
    NIGHTMARE(1.6, 0.6, 0.5);
    
    private final double enemyStatMultiplier;
    private final double playerStatMultiplier;
    private final double goldMultiplier;
    
    Difficulty(double enemyStatMultiplier, double playerStatMultiplier, double goldMultiplier) {
        this.enemyStatMultiplier = enemyStatMultiplier;
        this.playerStatMultiplier = playerStatMultiplier;
        this.goldMultiplier = goldMultiplier;
    }
    
    public double getEnemyStatMultiplier() {
        return enemyStatMultiplier;
    }
    
    public double getPlayerStatMultiplier() {
        return playerStatMultiplier;
    }
    
    public double getGoldMultiplier() {
        return goldMultiplier;
    }
}
