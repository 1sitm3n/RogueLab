package com.roguelab.domain.component;

import com.roguelab.domain.DamageType;

/**
 * Combat statistics component for entities that participate in combat.
 */
public final class Combat {
    
    private int baseAttack;
    private int baseDefense;
    private int bonusAttack;
    private int bonusDefense;
    private double criticalChance;
    private double criticalMultiplier;
    private DamageType primaryDamageType;
    
    public Combat(int baseAttack, int baseDefense) {
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.bonusAttack = 0;
        this.bonusDefense = 0;
        this.criticalChance = 0.05; // 5% base crit chance
        this.criticalMultiplier = 1.5; // 150% damage on crit
        this.primaryDamageType = DamageType.PHYSICAL;
    }
    
    public int getBaseAttack() {
        return baseAttack;
    }
    
    public int getBaseDefense() {
        return baseDefense;
    }
    
    /**
     * Total attack including bonuses from items, buffs, etc.
     */
    public int getTotalAttack() {
        return baseAttack + bonusAttack;
    }
    
    /**
     * Total defense including bonuses.
     */
    public int getTotalDefense() {
        return baseDefense + bonusDefense;
    }
    
    public double getCriticalChance() {
        return criticalChance;
    }
    
    public double getCriticalMultiplier() {
        return criticalMultiplier;
    }
    
    public DamageType getPrimaryDamageType() {
        return primaryDamageType;
    }
    
    public void addBonusAttack(int amount) {
        this.bonusAttack += amount;
    }
    
    public void addBonusDefense(int amount) {
        this.bonusDefense += amount;
    }
    
    public void setCriticalChance(double chance) {
        this.criticalChance = Math.max(0, Math.min(1.0, chance));
    }
    
    public void setCriticalMultiplier(double multiplier) {
        this.criticalMultiplier = Math.max(1.0, multiplier);
    }
    
    public void setPrimaryDamageType(DamageType type) {
        this.primaryDamageType = type;
    }
    
    /**
     * Remove all temporary bonuses (e.g., at end of combat).
     */
    public void clearBonuses() {
        this.bonusAttack = 0;
        this.bonusDefense = 0;
    }
    
    @Override
    public String toString() {
        return String.format("ATK:%d DEF:%d CRIT:%.0f%%", 
            getTotalAttack(), getTotalDefense(), criticalChance * 100);
    }
}
