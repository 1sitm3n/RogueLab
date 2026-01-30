package com.roguelab.combat;

import com.roguelab.domain.DamageType;

/**
 * Immutable result of a single attack in combat.
 */
public record AttackResult(
    String attackerId,
    String defenderId,
    int baseDamage,
    int finalDamage,
    DamageType damageType,
    boolean critical,
    int healthBefore,
    int healthAfter,
    boolean killed
) {
    
    public int actualDamage() {
        return healthBefore - healthAfter;
    }
    
    public boolean dealtDamage() {
        return actualDamage() > 0;
    }
    
    public static AttackResult miss(String attackerId, String defenderId, int defenderHealth) {
        return new AttackResult(
            attackerId, defenderId,
            0, 0, DamageType.PHYSICAL,
            false, defenderHealth, defenderHealth, false
        );
    }
}
