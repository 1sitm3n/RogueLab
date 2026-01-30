package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.roguelab.combat.*;
import com.roguelab.domain.*;

/**
 * Combat listener that writes telemetry using TelemetryWriter.
 */
public final class SimpleTelemetryCombatListener implements CombatEventListener {
    
    private final TelemetryWriter writer;
    
    public SimpleTelemetryCombatListener(TelemetryWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public void onCombatStarted(CombatContext ctx) {
        ObjectNode payload = writer.createPayload();
        payload.put("room_id", ctx.getRoom().getId().value());
        payload.put("player_health", ctx.getPlayer().getHealth().getCurrent());
        payload.put("player_max_health", ctx.getPlayer().getHealth().getMaximum());
        
        ArrayNode enemies = payload.putArray("enemies");
        for (Enemy enemy : ctx.getRoom().getEnemies()) {
            ObjectNode e = enemies.addObject();
            e.put("id", enemy.getId().value());
            e.put("type", enemy.getType().name());
            e.put("health", enemy.getHealth().getCurrent());
            e.put("max_health", enemy.getHealth().getMaximum());
            e.put("attack", enemy.getCombat().getTotalAttack());
            e.put("defense", enemy.getCombat().getTotalDefense());
        }
        
        writer.write("COMBAT_STARTED", ctx.getCurrentTick(), payload);
    }
    
    @Override
    public void onDamageDealt(CombatContext ctx, AttackResult result, boolean playerAttack) {
        ObjectNode payload = writer.createPayload();
        
        if (playerAttack) {
            payload.put("source_id", "player");
            payload.put("source_type", "PLAYER");
            payload.put("target_id", result.defenderId());
            payload.put("target_type", getEnemyType(ctx, result.defenderId()));
        } else {
            payload.put("source_id", result.attackerId());
            payload.put("source_type", getEnemyType(ctx, result.attackerId()));
            payload.put("target_id", "player");
            payload.put("target_type", "PLAYER");
        }
        
        payload.put("base_damage", result.baseDamage());
        payload.put("final_damage", result.finalDamage());
        payload.put("damage_type", result.damageType().name());
        payload.put("critical", result.critical());
        payload.put("health_before", result.healthBefore());
        payload.put("health_after", result.healthAfter());
        payload.put("killed", result.killed());
        
        writer.write("DAMAGE_DEALT", ctx.getCurrentTick(), payload);
    }
    
    @Override
    public void onCombatEnded(CombatContext ctx, CombatResult result) {
        ObjectNode payload = writer.createPayload();
        payload.put("room_id", ctx.getRoom().getId().value());
        payload.put("outcome", result.outcome().name());
        payload.put("turns_elapsed", result.turnsElapsed());
        payload.put("total_damage_dealt", result.totalDamageDealt());
        payload.put("total_damage_taken", result.totalDamageTaken());
        payload.put("enemies_killed", result.enemiesKilled());
        payload.put("gold_earned", result.goldEarned());
        payload.put("experience_gained", result.experienceGained());
        payload.put("player_health_after", ctx.getPlayer().getHealth().getCurrent());
        payload.put("player_max_health", ctx.getPlayer().getHealth().getMaximum());
        
        writer.write("COMBAT_ENDED", ctx.getCurrentTick(), payload);
    }
    
    @Override
    public void onPlayerDied(CombatContext ctx) {
        Player player = ctx.getPlayer();
        ObjectNode payload = writer.createPayload();
        
        // Find killing enemy
        java.util.List<Enemy> enemies = ctx.getAliveEnemies();
        String causeId = enemies.isEmpty() ? "unknown" : enemies.get(0).getId().value();
        String causeName = enemies.isEmpty() ? "Unknown" : enemies.get(0).getType().name();
        
        payload.put("cause_type", "ENEMY");
        payload.put("cause_id", causeId);
        payload.put("cause_name", causeName);
        payload.put("total_damage_taken", ctx.getTotalDamageTaken());
        payload.put("floor", player.getCurrentFloor());
        payload.put("gold", player.getInventory().getGold());
        payload.put("enemies_killed", player.getEnemiesKilled());
        
        ArrayNode items = payload.putArray("items");
        for (Item item : player.getInventory().getItems()) {
            items.add(item.getId().value());
        }
        
        writer.write("PLAYER_DIED", ctx.getCurrentTick(), payload);
    }
    
    private String getEnemyType(CombatContext ctx, String enemyId) {
        return ctx.getRoom().getEnemies().stream()
            .filter(e -> e.getId().value().equals(enemyId))
            .map(e -> e.getType().name())
            .findFirst()
            .orElse("UNKNOWN");
    }
}
