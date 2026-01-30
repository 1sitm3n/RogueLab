package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roguelab.domain.*;
import com.roguelab.dungeon.DungeonConfig;
import com.roguelab.game.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Simple Telemetry")
class SimpleTelemetryTest {
    
    private static final long SEED = 12345L;
    private Path tempFile;
    private ObjectMapper mapper;
    
    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("telemetry-test", ".jsonl");
        mapper = new ObjectMapper();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }
    
    @Test
    @DisplayName("should write RUN_STARTED event")
    void writesRunStartedEvent() throws IOException {
        String runId = "test_run";
        try (TelemetryWriter writer = new TelemetryWriter(tempFile, runId, false)) {
            GameSession session = new GameSession("Hero", PlayerClass.WARRIOR, SEED);
            SimpleTelemetrySessionListener listener = new SimpleTelemetrySessionListener(writer);
            
            listener.onRunStarted(session);
        }
        
        List<JsonNode> events = readEvents();
        assertThat(events).hasSize(1);
        
        JsonNode event = events.get(0);
        assertThat(event.get("event_type").asText()).isEqualTo("RUN_STARTED");
        assertThat(event.get("run_id").asText()).isEqualTo("test_run");
        assertThat(event.get("payload").get("player_name").asText()).isEqualTo("Hero");
        assertThat(event.get("payload").get("player_class").asText()).isEqualTo("WARRIOR");
    }
    
    @Test
    @DisplayName("should write FLOOR_ENTERED event")
    void writesFloorEnteredEvent() throws IOException {
        String runId = "test_run";
        try (TelemetryWriter writer = new TelemetryWriter(tempFile, runId, false)) {
            GameSession session = new GameSession("Hero", PlayerClass.WARRIOR, SEED, 
                Difficulty.NORMAL, DungeonConfig.easy());
            session.start();
            
            SimpleTelemetrySessionListener listener = new SimpleTelemetrySessionListener(writer);
            listener.onFloorEntered(session, session.getCurrentFloor());
        }
        
        List<JsonNode> events = readEvents();
        assertThat(events).hasSize(1);
        
        JsonNode event = events.get(0);
        assertThat(event.get("event_type").asText()).isEqualTo("FLOOR_ENTERED");
        assertThat(event.get("payload").get("floor_number").asInt()).isEqualTo(1);
        assertThat(event.get("payload").get("room_count").asInt()).isPositive();
    }
    
    @Test
    @DisplayName("should write RUN_ENDED event with statistics")
    void writesRunEndedEvent() throws IOException {
        String runId = "test_run";
        try (TelemetryWriter writer = new TelemetryWriter(tempFile, runId, false)) {
            GameSession session = new GameSession("Hero", PlayerClass.WARRIOR, SEED);
            session.start();
            
            SimpleTelemetrySessionListener listener = new SimpleTelemetrySessionListener(writer);
            listener.onRunEnded(session, GameSessionListener.RunEndReason.VICTORY);
        }
        
        List<JsonNode> events = readEvents();
        assertThat(events).hasSize(1);
        
        JsonNode event = events.get(0);
        assertThat(event.get("event_type").asText()).isEqualTo("RUN_ENDED");
        assertThat(event.get("payload").get("end_reason").asText()).isEqualTo("VICTORY");
    }
    
    @Test
    @DisplayName("TelemetryWriter should include all required fields")
    void writerIncludesRequiredFields() throws IOException {
        String runId = "test_run";
        try (TelemetryWriter writer = new TelemetryWriter(tempFile, runId, false)) {
            var payload = writer.createPayload();
            payload.put("test", "value");
            writer.write("TEST_EVENT", 42, payload);
        }
        
        List<JsonNode> events = readEvents();
        JsonNode event = events.get(0);
        
        assertThat(event.has("event_type")).isTrue();
        assertThat(event.has("event_version")).isTrue();
        assertThat(event.has("timestamp")).isTrue();
        assertThat(event.has("run_id")).isTrue();
        assertThat(event.has("tick")).isTrue();
        assertThat(event.has("payload")).isTrue();
        
        assertThat(event.get("tick").asInt()).isEqualTo(42);
    }
    
    private List<JsonNode> readEvents() throws IOException {
        List<JsonNode> events = new ArrayList<>();
        for (String line : Files.readAllLines(tempFile)) {
            if (!line.isBlank()) {
                events.add(mapper.readTree(line));
            }
        }
        return events;
    }
}
