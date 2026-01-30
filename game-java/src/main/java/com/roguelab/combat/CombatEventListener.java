package com.roguelab.combat;

/**
 * Listener interface for combat events.
 * 
 * This decouples the combat engine from the specific telemetry/event system,
 * allowing different implementations for testing, logging, or full telemetry.
 */
public interface CombatEventListener {
    
    /**
     * Called when combat begins.
     */
    void onCombatStarted(CombatContext ctx);
    
    /**
     * Called when damage is dealt.
     * @param ctx Combat context
     * @param result The attack result
     * @param playerAttack true if player attacked, false if enemy attacked
     */
    void onDamageDealt(CombatContext ctx, AttackResult result, boolean playerAttack);
    
    /**
     * Called when combat ends.
     */
    void onCombatEnded(CombatContext ctx, CombatResult result);
    
    /**
     * Called when the player dies in combat.
     */
    void onPlayerDied(CombatContext ctx);
    
    /**
     * A no-op implementation for testing or when telemetry is disabled.
     */
    CombatEventListener NONE = new CombatEventListener() {
        @Override public void onCombatStarted(CombatContext ctx) {}
        @Override public void onDamageDealt(CombatContext ctx, AttackResult result, boolean playerAttack) {}
        @Override public void onCombatEnded(CombatContext ctx, CombatResult result) {}
        @Override public void onPlayerDied(CombatContext ctx) {}
    };
}
