package com.roguelab.combat;

import com.roguelab.domain.*;
import com.roguelab.domain.component.StatusEffects;
import com.roguelab.util.GameRandom;

/**
 * Calculates damage for combat encounters.
 * 
 * Damage Formula:
 * 1. Start with base attack
 * 2. Apply attacker status effect modifiers (STRENGTHENED +50%, WEAKENED -25%)
 * 3. Apply critical hit multiplier (1.5x default)
 * 4. Apply defender status effect modifiers (VULNERABLE +50%, ARMORED -25%)
 * 5. Subtract defender's defense
 * 6. Minimum 1 damage guaranteed
 */
public final class DamageCalculator {
    
    public static final double CRIT_MULTIPLIER = 1.5;
    public static final int MINIMUM_DAMAGE = 1;
    
    private final GameRandom random;
    
    public DamageCalculator(GameRandom random) {
        this.random = random;
    }
    
    /**
     * Calculate damage from player attacking an enemy.
     */
    public AttackResult calculatePlayerAttack(Player player, Enemy enemy) {
        String attackerId = player.getId().value();
        String defenderId = enemy.getId().value();
        
        int baseDamage = player.getEffectiveAttack();
        DamageType damageType = player.getCombat().getPrimaryDamageType();
        
        boolean isCritical = random.chance(player.getCombat().getCriticalChance());
        
        int finalDamage = calculateFinalDamage(
            baseDamage, 
            isCritical,
            player.getCombat().getCriticalMultiplier(),
            player.getStatuses(),
            enemy.getStatuses(),
            enemy.getCombat().getTotalDefense()
        );
        
        int healthBefore = enemy.getHealth().getCurrent();
        enemy.getHealth().takeDamage(finalDamage);
        int healthAfter = enemy.getHealth().getCurrent();
        boolean killed = enemy.getHealth().isDead();
        
        return new AttackResult(
            attackerId, defenderId,
            baseDamage, finalDamage, damageType,
            isCritical, healthBefore, healthAfter, killed
        );
    }
    
    /**
     * Calculate damage from enemy attacking player.
     */
    public AttackResult calculateEnemyAttack(Enemy enemy, Player player) {
        String attackerId = enemy.getId().value();
        String defenderId = player.getId().value();
        
        int baseDamage = enemy.getEffectiveAttack();
        DamageType damageType = enemy.getDamageType();
        
        // Enemies don't crit by default
        boolean isCritical = false;
        
        int finalDamage = calculateFinalDamage(
            baseDamage,
            isCritical,
            CRIT_MULTIPLIER,
            enemy.getStatuses(),
            player.getStatuses(),
            player.getEffectiveDefense()
        );
        
        int healthBefore = player.getHealth().getCurrent();
        player.getHealth().takeDamage(finalDamage);
        int healthAfter = player.getHealth().getCurrent();
        boolean killed = player.getHealth().isDead();
        
        return new AttackResult(
            attackerId, defenderId,
            baseDamage, finalDamage, damageType,
            isCritical, healthBefore, healthAfter, killed
        );
    }
    
    private int calculateFinalDamage(
            int baseDamage,
            boolean isCritical,
            double critMultiplier,
            StatusEffects attackerStatuses,
            StatusEffects defenderStatuses,
            int defenderDefense) {
        
        double damage = baseDamage;
        
        // Apply attacker status modifiers
        damage *= attackerStatuses.getAttackModifier();
        
        // Apply critical hit
        if (isCritical) {
            damage *= critMultiplier;
        }
        
        // Apply defender status modifiers
        damage *= defenderStatuses.getDefenseModifier();
        
        // Subtract defense
        damage -= defenderDefense;
        
        return Math.max(MINIMUM_DAMAGE, (int) Math.round(damage));
    }
    
    public int calculatePoisonDamage(int stacks) {
        return 2 * stacks;
    }
    
    public int calculateBurningDamage(int stacks) {
        return 3 * stacks;
    }
    
    public int calculateRegenerationHealing(int stacks) {
        return 3 * stacks;
    }
}
