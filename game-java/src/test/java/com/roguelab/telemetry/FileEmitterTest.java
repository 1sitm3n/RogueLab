package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roguelab.domain.Difficulty;
import com.roguelab.domain.PlayerClass;
import com.roguelab.event.RunEndedEvent;
import com.roguelab.event.RunStartedEvent;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("File Emitter")
class FileEmitterTest {
    
    private static Path tempDir;
    private UUID runId;
    private ObjectMapper mapper;
    
    @BeforeAll
    static void setUpClass() throws IOException {
        tempDir = Files.createTempDirectory("roguelab-test");
    }
    
    @AfterAll
    static void tearDownClass() throws IOException {
        // Clean up temp directory
        Files.walk(tempDir)
            .sorted((a, b) -> b.compareTo(a)) // Reverse order for deletion
            .forEach(path -> {
                try { Files.deleteIfExists(path); } 
                catch (IOException ignored) {}
            });
    }
    
    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();
        mapper = new ObjectMapper();
    }
    
    @Test
    @DisplayName("should create output file in specified directory")
    void createsOutputFile() {
        FileEmitter emitter = new FileEmitter(tempDir, runId);
        
        assertThat(emitter.getOutputFile().getParent()).isEqualTo(tempDir);
        assertThat(emitter.getOutputFile().toString()).contains(runId.toString());
        assertThat(emitter.getOutputFile().toString()).endsWith(".jsonl");
        
        emitter.close();
    }
    
    @Test
    @DisplayName("should write events as JSON Lines")
    void writesJsonLines() throws IOException {
        FileEmitter emitter = new FileEmitter(tempDir, runId);
        
        RunStartedEvent startEvent = new RunStartedEvent(
            runId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        );
        
        RunEndedEvent endEvent = new RunEndedEvent(
            runId, 100, RunEndedEvent.Outcome.VICTORY,
            5, 1000, 500, 10, 25, 3600
        );
        
        emitter.emit(startEvent);
        emitter.emit(endEvent);
        emitter.close();
        
        // Read and verify
        List<String> lines = Files.readAllLines(emitter.getOutputFile());
        assertThat(lines).hasSize(2);
        
        // Verify each line is valid JSON
        JsonNode line1 = mapper.readTree(lines.get(0));
        assertThat(line1.get("eventType").asText()).isEqualTo("RUN_STARTED");
        
        JsonNode line2 = mapper.readTree(lines.get(1));
        assertThat(line2.get("eventType").asText()).isEqualTo("RUN_ENDED");
    }
    
    @Test
    @DisplayName("should flush writes to disk")
    void flushesWrites() throws IOException {
        FileEmitter emitter = new FileEmitter(tempDir, runId);
        
        emitter.emit(new RunStartedEvent(
            runId, 12345L, "0.1.0",
            PlayerClass.ROGUE, Difficulty.HARD, "Test"
        ));
        
        emitter.flush();
        
        // File should be readable even before close
        assertThat(Files.exists(emitter.getOutputFile())).isTrue();
        assertThat(Files.size(emitter.getOutputFile())).isGreaterThan(0);
        
        emitter.close();
    }
    
    @Test
    @DisplayName("should reject emit after close")
    void rejectsEmitAfterClose() {
        FileEmitter emitter = new FileEmitter(tempDir, runId);
        emitter.close();
        
        assertThat(emitter.isClosed()).isTrue();
        
        assertThatThrownBy(() -> emitter.emit(new RunStartedEvent(
            runId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        ))).isInstanceOf(TelemetryException.class);
    }
    
    @Test
    @DisplayName("should generate unique file names")
    void generatesUniqueFileNames() {
        UUID runId1 = UUID.randomUUID();
        UUID runId2 = UUID.randomUUID();
        
        FileEmitter emitter1 = new FileEmitter(tempDir, runId1);
        FileEmitter emitter2 = new FileEmitter(tempDir, runId2);
        
        assertThat(emitter1.getOutputFile())
            .isNotEqualTo(emitter2.getOutputFile());
        
        emitter1.close();
        emitter2.close();
    }
    
    @Test
    @DisplayName("should create directory if it does not exist")
    void createsDirectory() throws IOException {
        Path newDir = tempDir.resolve("subdir_" + System.currentTimeMillis());
        assertThat(Files.exists(newDir)).isFalse();
        
        FileEmitter emitter = new FileEmitter(newDir, runId);
        
        assertThat(Files.exists(newDir)).isTrue();
        
        emitter.close();
    }
}
