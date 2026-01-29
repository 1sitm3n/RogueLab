package com.roguelab.telemetry;

import com.roguelab.event.GameEvent;

import java.io.PrintStream;

/**
 * Emits events to console (stdout).
 * Useful for debugging and development.
 */
public final class ConsoleEmitter implements TelemetryEmitter {
    
    private final EventSerializer serializer;
    private final PrintStream out;
    private final boolean prettyPrint;
    
    public ConsoleEmitter() {
        this(System.out, false);
    }
    
    public ConsoleEmitter(boolean prettyPrint) {
        this(System.out, prettyPrint);
    }
    
    public ConsoleEmitter(PrintStream out, boolean prettyPrint) {
        this.serializer = new EventSerializer();
        this.out = out;
        this.prettyPrint = prettyPrint;
    }
    
    @Override
    public void emit(GameEvent event) {
        String json = serializer.serialize(event);
        
        if (prettyPrint) {
            out.println("[TELEMETRY] " + event.getEventType());
            out.println(json);
            out.println();
        } else {
            out.println(json);
        }
    }
    
    @Override
    public void flush() {
        out.flush();
    }
    
    @Override
    public void close() {
        // Don't close stdout
    }
}
