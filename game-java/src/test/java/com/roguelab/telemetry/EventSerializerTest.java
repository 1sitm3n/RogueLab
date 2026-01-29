package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roguelab.domain.*;
import com.roguelab.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Event Serializer")
class EventSerializerTest {
    
    private EventSerializer serializer;
    private ObjectMapper mapper;
    private UUID testRunId;
    
    @BeforeEach
    void setUp() {
        serializer = new EventSerializer();
        mapper = new ObjectMapper();
        testRunId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("should serialize RunStartedEvent with all fields")
    void serializesRunStartedEvent() throws Exception {
        RunStartedEvent event = new RunStartedEvent(
            testRunId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "TestHero"
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        // Base fields
        assertThat(node.get("eventType").asText()).isEqualTo("RUN_STARTED");
        assertThat(node.get("eventVersion").asText()).isEqualTo("1.0.0");
        assertThat(node.get("runId").asText()).isEqualTo(testRunId.toString());
        assertThat(node.get("tick").asInt()).isEqualTo(0);
        assertThat(node.has("timestamp")).isTrue();
        
        // Payload
        JsonNode payload = node.get("payload");
        assertThat(payload.get("seed").asLong()).isEqualTo(12345L);
        assertThat(payload.get("gameVersion").asText()).isEqualTo("0.1.0");
        assertThat(payload.get("playerClass").asText()).isEqualTo("WARRIOR");
        assertThat(payload.get("difficulty").asText()).isEqualTo("NORMAL");
        assertThat(payload.get("playerName").asText()).isEqualTo("TestHero");
    }
    
    @Test
    @DisplayName("should serialize DamageDealtEvent with combat data")
    void serializesDamageDealtEvent() throws Exception {
        DamageDealtEvent event = new DamageDealtEvent(
            testRunId, 42, "player", DamageDealtEvent.EntityType.PLAYER,
            "goblin_123", DamageDealtEvent.EntityType.ENEMY,
            10, 15, DamageType.PHYSICAL, true,
            20, 5, false
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        assertThat(node.get("eventType").asText()).isEqualTo("DAMAGE_DEALT");
        assertThat(node.get("tick").asInt()).isEqualTo(42);
        
        JsonNode payload = node.get("payload");
        assertThat(payload.get("sourceId").asText()).isEqualTo("player");
        assertThat(payload.get("sourceType").asText()).isEqualTo("PLAYER");
        assertThat(payload.get("targetId").asText()).isEqualTo("goblin_123");
        assertThat(payload.get("baseDamage").asInt()).isEqualTo(10);
        assertThat(payload.get("finalDamage").asInt()).isEqualTo(15);
        assertThat(payload.get("isCritical").asBoolean()).isTrue();
        assertThat(payload.get("targetKilled").asBoolean()).isFalse();
    }
    
    @Test
    @DisplayName("should serialize CombatStartedEvent with enemy list")
    void serializesCombatStartedEvent() throws Exception {
        List<EnemyInfo> enemies = List.of(
            new EnemyInfo("goblin_1", EnemyType.GOBLIN, 20, 5),
            new EnemyInfo("orc_1", EnemyType.ORC, 40, 8)
        );
        
        CombatStartedEvent event = new CombatStartedEvent(
            testRunId, 100, "room_1_0", enemies, 80, 100
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        JsonNode payload = node.get("payload");
        assertThat(payload.get("roomId").asText()).isEqualTo("room_1_0");
        assertThat(payload.get("enemies").size()).isEqualTo(2);
        assertThat(payload.get("enemies").get(0).get("enemyId").asText()).isEqualTo("goblin_1");
        assertThat(payload.get("playerHealth").asInt()).isEqualTo(80);
    }
    
    @Test
    @DisplayName("should serialize ItemPickedEvent with stats map")
    void serializesItemPickedEvent() throws Exception {
        Map<String, Object> stats = Map.of("attack", 8, "bonusDamage", 3);
        
        ItemPickedEvent event = new ItemPickedEvent(
            testRunId, 200, "sword_001", ItemType.WEAPON,
            "Sword of Fire", Rarity.RARE, 2, 
            ItemPickedEvent.ItemSource.CHEST, stats
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        JsonNode payload = node.get("payload");
        assertThat(payload.get("itemName").asText()).isEqualTo("Sword of Fire");
        assertThat(payload.get("rarity").asText()).isEqualTo("RARE");
        assertThat(payload.get("stats").get("attack").asInt()).isEqualTo(8);
    }
    
    @Test
    @DisplayName("should serialize PlayerDiedEvent with items list")
    void serializesPlayerDiedEvent() throws Exception {
        PlayerDiedEvent event = new PlayerDiedEvent(
            testRunId, 500, PlayerDiedEvent.CauseType.ENEMY,
            "dragon_boss", "Ancient Dragon", DamageType.FIRE,
            50, 8, List.of("sword_001", "armor_002"), 250, 42
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        JsonNode payload = node.get("payload");
        assertThat(payload.get("causeType").asText()).isEqualTo("ENEMY");
        assertThat(payload.get("causeName").asText()).isEqualTo("Ancient Dragon");
        assertThat(payload.get("itemsHeld").size()).isEqualTo(2);
        assertThat(payload.get("enemiesKilledThisRun").asInt()).isEqualTo(42);
    }
    
    @Test
    @DisplayName("should produce valid JSON Lines output (no newlines within)")
    void producesValidJsonLines() throws Exception {
        RunStartedEvent event = new RunStartedEvent(
            testRunId, 12345L, "0.1.0",
            PlayerClass.MAGE, Difficulty.HARD, "Test\nHero" // Name with newline
        );
        
        String json = serializer.serialize(event);
        
        // JSON Lines format requires no newlines within a single record
        assertThat(json).doesNotContain("\n");
        assertThat(json).doesNotContain("\r");
        
        // Should still be valid JSON
        assertThatCode(() -> mapper.readTree(json)).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("should handle null reward item in BossDefeatedEvent")
    void handlesNullRewardItem() throws Exception {
        BossDefeatedEvent event = new BossDefeatedEvent(
            testRunId, 300, "goblin_king", "Goblin King",
            5, 20, 300, 50, 60, 100, null
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        JsonNode payload = node.get("payload");
        assertThat(payload.has("rewardItem")).isFalse();
    }
    
    @Test
    @DisplayName("should include timestamp in ISO format")
    void includesIsoTimestamp() throws Exception {
        Instant fixedTime = Instant.parse("2025-01-15T14:30:00.000Z");
        RunStartedEvent event = new RunStartedEvent(
            fixedTime, testRunId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        );
        
        String json = serializer.serialize(event);
        JsonNode node = mapper.readTree(json);
        
        String timestamp = node.get("timestamp").asText();
        assertThat(timestamp).startsWith("2025-01-15");
    }
}
