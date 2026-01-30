package com.roguelab.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;

/**
 * Simple telemetry writer that outputs JSON Lines directly.
 * Avoids complex event system dependencies.
 */
public final class TelemetryWriter implements AutoCloseable {
    
    private final ObjectMapper mapper;
    private final BufferedWriter writer;
    private final String runId;
    private final boolean consoleOutput;
    
    public TelemetryWriter(Path outputFile, String runId, boolean consoleOutput) throws IOException {
        this.mapper = new ObjectMapper();
        this.writer = Files.newBufferedWriter(outputFile, 
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        this.runId = runId;
        this.consoleOutput = consoleOutput;
    }
    
    /**
     * Write an event to the telemetry file.
     */
    public void write(String eventType, int tick, ObjectNode payload) {
        try {
            ObjectNode event = mapper.createObjectNode();
            event.put("event_type", eventType);
            event.put("event_version", "1");
            event.put("timestamp", Instant.now().toString());
            event.put("run_id", runId);
            event.put("tick", tick);
            event.set("payload", payload);
            
            String json = mapper.writeValueAsString(event);
            writer.write(json);
            writer.newLine();
            writer.flush();
            
            if (consoleOutput) {
                System.out.println("[TELEMETRY] " + eventType);
            }
        } catch (IOException e) {
            System.err.println("Telemetry write error: " + e.getMessage());
        }
    }
    
    /**
     * Create a payload builder.
     */
    public ObjectNode createPayload() {
        return mapper.createObjectNode();
    }
    
    public String getRunId() {
        return runId;
    }
    
    @Override
    public void close() throws IOException {
        writer.close();
    }
}
