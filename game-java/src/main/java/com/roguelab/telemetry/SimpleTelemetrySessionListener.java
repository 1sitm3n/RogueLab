package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.roguelab.combat.CombatResult;
import com.roguelab.domain.*;
import com.roguelab.dungeon.Floor;
import com.roguelab.game.*;

/**
 * Session listener that writes telemetry using TelemetryWriter.
 * Simplified approach that bypasses the complex event system.
 */
public final class SimpleTelemetrySessionListener implements GameSessionListener {
    
    private final TelemetryWriter writer;
    
    public SimpleTelemetrySessionListener(TelemetryWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public void onRunStarted(GameSession session) {
        ObjectNode payload = writer.createPayload();
        payload.put("seed", session.getSeed());
        payload.put("version", "0.2.0");
        payload.put("difficulty", session.getDifficulty().name());
        payload.put("player_name", session.getPlayer().getName());
        payload.put("player_class", session.getPlayer().getPlayerClass().name());
        payload.put("starting_health", session.getPlayer().getHealth().getMaximum());
        
        writer.write("RUN_STARTED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onFloorEntered(GameSession session, Floor floor) {
        ObjectNode payload = writer.createPayload();
        payload.put("floor_number", floor.getFloorNumber());
        payload.put("room_count", floor.getRoomCount());
        payload.put("is_boss_floor", floor.hasBoss());
        
        ArrayNode roomTypes = payload.putArray("room_types");
        for (Room room : floor.getRooms()) {
            roomTypes.add(room.getType().name());
        }
        
        writer.write("FLOOR_ENTERED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onRoomEntered(GameSession session, Room room) {
        ObjectNode payload = writer.createPayload();
        payload.put("room_id", room.getId().value());
        payload.put("room_type", room.getType().name());
        payload.put("floor_number", session.getCurrentFloorNumber());
        payload.put("room_index", session.getCurrentFloor().getCurrentRoomIndex());
        payload.put("enemy_count", room.getEnemies().size());
        payload.put("item_count", room.getItems().size());
        
        // Enemy snapshots
        ArrayNode enemies = payload.putArray("enemies");
        for (Enemy enemy : room.getEnemies()) {
            ObjectNode e = enemies.addObject();
            e.put("id", enemy.getId().value());
            e.put("type", enemy.getType().name());
            e.put("health", enemy.getHealth().getCurrent());
            e.put("max_health", enemy.getHealth().getMaximum());
        }
        
        // Item snapshots
        ArrayNode items = payload.putArray("items");
        for (Item item : room.getItems()) {
            ObjectNode i = items.addObject();
            i.put("id", item.getId().value());
            i.put("name", item.getName());
            i.put("type", item.getType().name());
            i.put("rarity", item.getRarity().name());
        }
        
        writer.write("ROOM_ENTERED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onRoomCleared(GameSession session, Room room) {
        ObjectNode payload = writer.createPayload();
        payload.put("room_id", room.getId().value());
        payload.put("room_type", room.getType().name());
        payload.put("floor_number", session.getCurrentFloorNumber());
        payload.put("enemies_defeated", room.getEnemies().size());
        
        writer.write("ROOM_CLEARED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onCombatCompleted(GameSession session, CombatResult result) {
        // Combat events handled by SimpleTelemetryCombatListener
    }
    
    @Override
    public void onItemPicked(GameSession session, Item item) {
        ObjectNode payload = writer.createPayload();
        payload.put("item_id", item.getId().value());
        payload.put("item_name", item.getName());
        payload.put("item_type", item.getType().name());
        payload.put("rarity", item.getRarity().name());
        payload.put("value", item.getValue());
        payload.put("floor_number", session.getCurrentFloorNumber());
        payload.put("room_id", session.getCurrentRoom().getId().value());
        
        // Stats
        ObjectNode stats = payload.putObject("stats");
        if (item.getAttackBonus() != 0) stats.put("attack", item.getAttackBonus());
        if (item.getDefenseBonus() != 0) stats.put("defense", item.getDefenseBonus());
        if (item.getHealthBonus() != 0) stats.put("health", item.getHealthBonus());
        
        writer.write("ITEM_PICKED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onItemUsed(GameSession session, Item item) {
        ObjectNode payload = writer.createPayload();
        payload.put("item_id", item.getId().value());
        payload.put("item_name", item.getName());
        payload.put("item_type", item.getType().name());
        
        writer.write("ITEM_USED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onShopPurchase(GameSession session, Item item, int cost) {
        ObjectNode payload = writer.createPayload();
        payload.put("item_id", item.getId().value());
        payload.put("item_name", item.getName());
        payload.put("item_type", item.getType().name());
        payload.put("rarity", item.getRarity().name());
        payload.put("cost", cost);
        payload.put("gold_after", session.getPlayer().getInventory().getGold());
        payload.put("floor_number", session.getCurrentFloorNumber());
        
        writer.write("SHOP_PURCHASED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onPlayerRested(GameSession session, int healAmount) {
        Player player = session.getPlayer();
        ObjectNode payload = writer.createPayload();
        payload.put("heal_amount", healAmount);
        payload.put("health_after", player.getHealth().getCurrent());
        payload.put("max_health", player.getHealth().getMaximum());
        payload.put("floor_number", session.getCurrentFloorNumber());
        
        writer.write("PLAYER_RESTED", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onPlayerLevelUp(GameSession session, int newLevel) {
        Player player = session.getPlayer();
        ObjectNode payload = writer.createPayload();
        payload.put("previous_level", newLevel - 1);
        payload.put("new_level", newLevel);
        payload.put("total_experience", player.getExperience());
        payload.put("max_health", player.getHealth().getMaximum());
        payload.put("attack", player.getCombat().getTotalAttack());
        payload.put("defense", player.getCombat().getTotalDefense());
        
        writer.write("PLAYER_LEVEL_UP", session.getCurrentTick(), payload);
    }
    
    @Override
    public void onRunEnded(GameSession session, RunEndReason reason) {
        Player player = session.getPlayer();
        RunStatistics stats = session.getStatistics();
        
        ObjectNode payload = writer.createPayload();
        payload.put("end_reason", reason.name());
        payload.put("final_floor", session.getCurrentFloorNumber());
        payload.put("enemies_killed", stats.getEnemiesKilled());
        payload.put("bosses_killed", stats.getBossesKilled());
        payload.put("gold_earned", stats.getGoldEarned());
        payload.put("gold_spent", stats.getGoldSpent());
        payload.put("items_collected", stats.getItemsCollected());
        payload.put("damage_dealt", stats.getTotalDamageDealt());
        payload.put("damage_taken", stats.getTotalDamageTaken());
        payload.put("rooms_visited", stats.getRoomsVisited());
        payload.put("rooms_cleared", stats.getRoomsCleared());
        payload.put("player_level", player.getLevel());
        payload.put("player_alive", player.isAlive());
        
        writer.write("RUN_ENDED", session.getCurrentTick(), payload);
    }
}
