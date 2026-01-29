package com.roguelab.telemetry;

import com.roguelab.event.GameEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Emits telemetry events to JSON Lines (.jsonl) files.
 * 
 * File naming convention: run_{runId}_{timestamp}.jsonl
 * Each line contains one complete JSON event.
 */
public final class FileEmitter implements TelemetryEmitter {
    
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss").withZone(ZoneOffset.UTC);
    
    private final EventSerializer serializer;
    private final Path outputDirectory;
    private final UUID runId;
    private final Path outputFile;
    private BufferedWriter writer;
    private boolean closed;
    
    /**
     * Create a file emitter for a specific run.
     * @param outputDirectory Directory to write telemetry files
     * @param runId The unique identifier for this run
     */
    public FileEmitter(Path outputDirectory, UUID runId) {
        this.serializer = new EventSerializer();
        this.outputDirectory = outputDirectory;
        this.runId = runId;
        this.outputFile = generateOutputPath();
        this.closed = false;
        
        initializeWriter();
    }
    
    /**
     * Create a file emitter with default output directory (./runs).
     */
    public FileEmitter(UUID runId) {
        this(Path.of("runs"), runId);
    }
    
    private Path generateOutputPath() {
        String timestamp = FILE_TIMESTAMP_FORMAT.format(Instant.now());
        String filename = String.format("run_%s_%s.jsonl", runId, timestamp);
        return outputDirectory.resolve(filename);
    }
    
    private void initializeWriter() {
        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(outputDirectory);
            
            // Open file for writing (append mode for safety)
            writer = Files.newBufferedWriter(
                outputFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new TelemetryException("Failed to initialize telemetry file: " + outputFile, e);
        }
    }
    
    @Override
    public void emit(GameEvent event) {
        if (closed) {
            throw new TelemetryException("Cannot emit to closed emitter");
        }
        
        try {
            String json = serializer.serialize(event);
            writer.write(json);
            writer.newLine();
        } catch (IOException e) {
            throw new TelemetryException("Failed to write event: " + event.getEventType(), e);
        }
    }
    
    @Override
    public void flush() {
        if (closed) {
            return;
        }
        
        try {
            writer.flush();
        } catch (IOException e) {
            throw new TelemetryException("Failed to flush telemetry file", e);
        }
    }
    
    @Override
    public void close() {
        if (closed) {
            return;
        }
        
        try {
            writer.close();
            closed = true;
        } catch (IOException e) {
            throw new TelemetryException("Failed to close telemetry file", e);
        }
    }
    
    /**
     * Get the path to the output file.
     */
    public Path getOutputFile() {
        return outputFile;
    }
    
    /**
     * Get the run ID this emitter is writing for.
     */
    public UUID getRunId() {
        return runId;
    }
    
    /**
     * Check if the emitter has been closed.
     */
    public boolean isClosed() {
        return closed;
    }
}
