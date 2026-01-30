package com.roguelab.combat;

import com.roguelab.domain.*;
import com.roguelab.domain.component.StatusEffect;
import com.roguelab.util.GameRandom;

import java.util.List;

/**
 * Orchestrates turn-based combat encounters.
 * 
 * Combat Flow:
 * 1. For each turn:
 *    a. Process damage-over-time effects (poison, burning)
 *    b. Process healing-over-time effects (regeneration)
 *    c. Player attacks first alive enemy
 *    d. All alive enemies attack player
 *    e. Tick status effect durations
 * 2. Apply rewards on victory
 * 
 * Combat is deterministic given the same seed.
 * 
 * Note: Telemetry emission is handled separately through CombatEventListener.
 */
public final class CombatEngine {
    
    private static final int MAX_TURNS = 100; // Safety limit
    
    private final DamageCalculator damageCalculator;
    private CombatEventListener eventListener;
    
    public CombatEngine(GameRandom random) {
        this.damageCalculator = new DamageCalculator(random);
    }
    
    /**
     * Set an event listener to receive combat events.
     * This is optional - combat works without it.
     */
    public void setEventListener(CombatEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * Run a complete combat encounter.
     * 
     * @param runId Current run identifier
     * @param player The player
     * @param room Room containing enemies
     * @param random RNG for combat (for determinism, pass seeded instance)
     * @param startTick Current game tick
     * @return Combat result with statistics
     */
    public CombatResult runCombat(String runId, Player player, Room room,
                                   GameRandom random, int startTick) {
        
        CombatContext ctx = new CombatContext(runId, player, room, startTick);
        
        // Notify listener of combat start
        if (eventListener != null) {
            eventListener.onCombatStarted(ctx);
        }
        
        // Main combat loop
        while (ctx.isCombatActive() && ctx.getCurrentTurn() < MAX_TURNS) {
            ctx.nextTurn();
            executeTurn(ctx);
        }
        
        // Apply rewards if victorious
        if (player.isAlive()) {
            applyRewards(ctx);
        }
        
        // Build result
        CombatResult result = ctx.buildResult();
        
        // Notify listener of combat end
        if (eventListener != null) {
            eventListener.onCombatEnded(ctx, result);
            
            if (player.isDead()) {
                eventListener.onPlayerDied(ctx);
            }
        }
        
        return result;
    }
    
    /**
     * Execute a single combat turn.
     */
    private void executeTurn(CombatContext ctx) {
        Player player = ctx.getPlayer();
        
        // 1. Process DoT effects on player
        processPlayerDoT(ctx);
        if (player.isDead()) return;
        
        // 2. Process HoT effects on player
        processPlayerHoT(ctx);
        
        // 3. Player attacks
        if (ctx.hasAliveEnemies()) {
            Enemy target = selectTarget(ctx);
            AttackResult result = damageCalculator.calculatePlayerAttack(player, target);
            ctx.addDamageDealt(result.actualDamage());
            
            if (eventListener != null) {
                eventListener.onDamageDealt(ctx, result, true);
            }
            
            if (result.killed()) {
                ctx.recordKill(target);
            }
        }
        
        // 4. Surviving enemies attack
        for (Enemy enemy : ctx.getAliveEnemies()) {
            if (player.isDead()) break;
            
            AttackResult result = damageCalculator.calculateEnemyAttack(enemy, player);
            ctx.addDamageTaken(result.actualDamage());
            
            if (eventListener != null) {
                eventListener.onDamageDealt(ctx, result, false);
            }
        }
        
        // 5. Tick status effects
        player.getStatuses().tickAll();
        for (Enemy enemy : ctx.getAliveEnemies()) {
            enemy.getStatuses().tickAll();
        }
    }
    
    /**
     * Process damage-over-time effects on the player.
     */
    private void processPlayerDoT(CombatContext ctx) {
        Player player = ctx.getPlayer();
        
        // Poison damage
        if (player.getStatuses().hasStatus(StatusType.POISONED)) {
            StatusEffect poison = player.getStatuses().getStatus(StatusType.POISONED);
            int damage = poison.getDamagePerTick();
            player.getHealth().takeDamage(damage);
            ctx.addDamageTaken(damage);
        }
        
        // Burning damage
        if (player.getStatuses().hasStatus(StatusType.BURNING)) {
            StatusEffect burning = player.getStatuses().getStatus(StatusType.BURNING);
            int damage = burning.getDamagePerTick();
            player.getHealth().takeDamage(damage);
            ctx.addDamageTaken(damage);
        }
    }
    
    /**
     * Process healing-over-time effects on the player.
     */
    private void processPlayerHoT(CombatContext ctx) {
        Player player = ctx.getPlayer();
        
        // Regeneration healing
        if (player.getStatuses().hasStatus(StatusType.REGENERATING)) {
            StatusEffect regen = player.getStatuses().getStatus(StatusType.REGENERATING);
            int healing = regen.getHealingPerTick();
            player.getHealth().heal(healing);
        }
    }
    
    /**
     * Select the target for the player's attack.
     * Currently: first alive enemy.
     * Future: player choice, threat system, etc.
     */
    private Enemy selectTarget(CombatContext ctx) {
        List<Enemy> alive = ctx.getAliveEnemies();
        return alive.isEmpty() ? null : alive.get(0);
    }
    
    /**
     * Apply gold and XP rewards to the player.
     */
    private void applyRewards(CombatContext ctx) {
        Player player = ctx.getPlayer();
        
        // Add gold
        player.getInventory().addGold(ctx.getGoldEarned());
        
        // Add experience
        player.addExperience(ctx.getExperienceGained());
        
        // Increment kill counter
        for (int i = 0; i < ctx.getKilledEnemyIds().size(); i++) {
            player.incrementEnemiesKilled();
        }
    }
}
