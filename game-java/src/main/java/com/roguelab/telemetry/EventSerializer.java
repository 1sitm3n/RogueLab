package com.roguelab.telemetry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roguelab.event.*;

import java.time.format.DateTimeFormatter;

/**
 * Serializes GameEvent objects to JSON format matching the event schema.
 * 
 * Output format:
 * {
 *   "eventType": "EVENT_TYPE",
 *   "eventVersion": "1.0.0",
 *   "timestamp": "2025-01-15T14:30:00.000Z",
 *   "runId": "uuid-string",
 *   "tick": 42,
 *   "payload": { ... event-specific fields ... }
 * }
 */
public final class EventSerializer {
    
    private final ObjectMapper mapper;
    
    public EventSerializer() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    /**
     * Serialize an event to JSON string.
     * @throws TelemetryException if serialization fails
     */
    public String serialize(GameEvent event) {
        try {
            ObjectNode root = mapper.createObjectNode();
            
            // Base event fields
            root.put("eventType", event.getEventType());
            root.put("eventVersion", event.getEventVersion());
            root.put("timestamp", event.getTimestamp().toString());
            root.put("runId", event.getRunId().toString());
            root.put("tick", event.getTick());
            
            // Payload - event-specific fields
            ObjectNode payload = serializePayload(event);
            root.set("payload", payload);
            
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new TelemetryException("Failed to serialize event: " + event.getEventType(), e);
        }
    }
    
    /**
     * Serialize the event-specific payload fields.
     */
    private ObjectNode serializePayload(GameEvent event) {
        ObjectNode payload = mapper.createObjectNode();
        
        switch (event) {
            case RunStartedEvent e -> {
                payload.put("seed", e.getSeed());
                payload.put("gameVersion", e.getGameVersion());
                payload.put("playerClass", e.getPlayerClass().name());
                payload.put("difficulty", e.getDifficulty().name());
                payload.put("playerName", e.getPlayerName());
            }
            case RunEndedEvent e -> {
                payload.put("outcome", e.getOutcome().name());
                payload.put("finalFloor", e.getFinalFloor());
                payload.put("finalScore", e.getFinalScore());
                payload.put("totalGold", e.getTotalGold());
                payload.put("itemsCollected", e.getItemsCollected());
                payload.put("enemiesDefeated", e.getEnemiesDefeated());
                payload.put("durationSeconds", e.getDurationSeconds());
            }
            case RoomEnteredEvent e -> {
                payload.put("floor", e.getFloor());
                payload.put("roomId", e.getRoomId());
                payload.put("roomType", e.getRoomType().name());
                payload.put("enemyCount", e.getEnemyCount());
                payload.put("hasChest", e.isHasChest());
                payload.put("playerHealthPercent", e.getPlayerHealthPercent());
            }
            case CombatStartedEvent e -> {
                payload.put("roomId", e.getRoomId());
                payload.set("enemies", mapper.valueToTree(e.getEnemies()));
                payload.put("playerHealth", e.getPlayerHealth());
                payload.put("playerMaxHealth", e.getPlayerMaxHealth());
            }
            case CombatEndedEvent e -> {
                payload.put("roomId", e.getRoomId());
                payload.put("outcome", e.getOutcome().name());
                payload.put("turnsElapsed", e.getTurnsElapsed());
                payload.put("damageDealt", e.getDamageDealt());
                payload.put("damageTaken", e.getDamageTaken());
                payload.put("playerHealthRemaining", e.getPlayerHealthRemaining());
                payload.put("goldDropped", e.getGoldDropped());
                payload.put("experienceGained", e.getExperienceGained());
            }
            case DamageDealtEvent e -> {
                payload.put("sourceId", e.getSourceId());
                payload.put("sourceType", e.getSourceType().name());
                payload.put("targetId", e.getTargetId());
                payload.put("targetType", e.getTargetType().name());
                payload.put("baseDamage", e.getBaseDamage());
                payload.put("finalDamage", e.getFinalDamage());
                payload.put("damageType", e.getDamageType().name());
                payload.put("isCritical", e.isCritical());
                payload.put("targetHealthBefore", e.getTargetHealthBefore());
                payload.put("targetHealthAfter", e.getTargetHealthAfter());
                payload.put("targetKilled", e.isTargetKilled());
            }
            case StatusAppliedEvent e -> {
                payload.put("targetId", e.getTargetId());
                payload.put("targetType", e.getTargetType().name());
                payload.put("statusType", e.getStatusType().name());
                payload.put("duration", e.getDuration());
                payload.put("stacks", e.getStacks());
                payload.put("sourceId", e.getSourceId());
                payload.put("sourceType", e.getSourceType().name());
            }
            case ItemPickedEvent e -> {
                payload.put("itemId", e.getItemId());
                payload.put("itemType", e.getItemType().name());
                payload.put("itemName", e.getItemName());
                payload.put("rarity", e.getRarity().name());
                payload.put("floor", e.getFloor());
                payload.put("source", e.getSource().name());
                payload.set("stats", mapper.valueToTree(e.getStats()));
            }
            case ShopPurchasedEvent e -> {
                payload.put("itemId", e.getItemId());
                payload.put("itemName", e.getItemName());
                payload.put("itemType", e.getItemType().name());
                payload.put("price", e.getPrice());
                payload.put("playerGoldBefore", e.getPlayerGoldBefore());
                payload.put("playerGoldAfter", e.getPlayerGoldAfter());
                payload.put("floor", e.getFloor());
            }
            case BossDefeatedEvent e -> {
                payload.put("bossId", e.getBossId());
                payload.put("bossName", e.getBossName());
                payload.put("floor", e.getFloor());
                payload.put("combatTurns", e.getCombatTurns());
                payload.put("damageDealt", e.getDamageDealt());
                payload.put("damageTaken", e.getDamageTaken());
                payload.put("playerHealthRemaining", e.getPlayerHealthRemaining());
                payload.put("rewardGold", e.getRewardGold());
                if (e.getRewardItem() != null) {
                    payload.set("rewardItem", mapper.valueToTree(e.getRewardItem()));
                }
            }
            case PlayerDiedEvent e -> {
                payload.put("causeType", e.getCauseType().name());
                payload.put("causeId", e.getCauseId());
                payload.put("causeName", e.getCauseName());
                payload.put("damageType", e.getDamageType().name());
                payload.put("finalBlow", e.getFinalBlow());
                payload.put("floor", e.getFloor());
                payload.set("itemsHeld", mapper.valueToTree(e.getItemsHeld()));
                payload.put("gold", e.getGold());
                payload.put("enemiesKilledThisRun", e.getEnemiesKilledThisRun());
            }
            case PlayerHealedEvent e -> {
                payload.put("source", e.getSource().name());
                payload.put("sourceId", e.getSourceId());
                payload.put("amount", e.getAmount());
                payload.put("healthBefore", e.getHealthBefore());
                payload.put("healthAfter", e.getHealthAfter());
                payload.put("maxHealth", e.getMaxHealth());
                payload.put("overheal", e.getOverheal());
            }
            default -> throw new TelemetryException("Unknown event type: " + event.getClass().getName());
        }
        
        return payload;
    }
    
    /**
     * Get the underlying ObjectMapper for advanced usage.
     */
    public ObjectMapper getMapper() {
        return mapper;
    }
}
