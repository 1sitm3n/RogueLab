package com.roguelab.domain.component;

/**
 * Health component for entities that can take damage and heal.
 * Encapsulates all health-related state and logic.
 */
public final class Health {
    
    private int current;
    private int maximum;
    
    public Health(int maximum) {
        if (maximum <= 0) {
            throw new IllegalArgumentException("Maximum health must be positive");
        }
        this.maximum = maximum;
        this.current = maximum;
    }
    
    public Health(int current, int maximum) {
        if (maximum <= 0) {
            throw new IllegalArgumentException("Maximum health must be positive");
        }
        if (current < 0) {
            throw new IllegalArgumentException("Current health cannot be negative");
        }
        this.maximum = maximum;
        this.current = Math.min(current, maximum);
    }
    
    public int getCurrent() {
        return current;
    }
    
    public int getMaximum() {
        return maximum;
    }
    
    public boolean isDead() {
        return current <= 0;
    }
    
    public boolean isAlive() {
        return current > 0;
    }
    
    public boolean isFullHealth() {
        return current >= maximum;
    }
    
    /**
     * Returns current health as a percentage (0.0 to 1.0).
     */
    public double getPercent() {
        return (double) current / maximum;
    }
    
    /**
     * Apply damage to this entity.
     * @param amount Raw damage amount (must be non-negative)
     * @return Actual damage dealt (may differ if health reaches 0)
     */
    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount cannot be negative");
        }
        int actualDamage = Math.min(amount, current);
        current -= actualDamage;
        return actualDamage;
    }
    
    /**
     * Heal this entity.
     * @param amount Heal amount (must be non-negative)
     * @return Actual amount healed (capped by max health)
     */
    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative");
        }
        int actualHeal = Math.min(amount, maximum - current);
        current += actualHeal;
        return actualHeal;
    }
    
    /**
     * Increase maximum health (e.g., from items or level up).
     * Current health increases by the same amount.
     */
    public void increaseMaximum(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        maximum += amount;
        current += amount;
    }
    
    /**
     * Set current health directly. Used for loading saves or special effects.
     */
    public void setCurrent(int value) {
        this.current = Math.max(0, Math.min(value, maximum));
    }
    
    @Override
    public String toString() {
        return current + "/" + maximum;
    }
}
