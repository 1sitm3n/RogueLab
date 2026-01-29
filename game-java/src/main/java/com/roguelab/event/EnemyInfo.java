package com.roguelab.event;

import com.roguelab.domain.Enemy;
import com.roguelab.domain.EnemyType;

/**
 * Snapshot of enemy state for inclusion in combat events.
 * This is a data transfer object, not a domain entity.
 */
public record EnemyInfo(
    String enemyId,
    EnemyType enemyType,
    int health,
    int attack
) {
    /**
     * Create from an Enemy entity.
     */
    public static EnemyInfo from(Enemy enemy) {
        return new EnemyInfo(
            enemy.getId().value(),
            enemy.getType(),
            enemy.getHealth().getCurrent(),
            enemy.getEffectiveAttack()
        );
    }
}
