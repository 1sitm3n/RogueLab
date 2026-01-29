package com.roguelab.telemetry;

import com.roguelab.event.GameEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Emits events to multiple underlying emitters.
 * Useful for writing to both file and console, or multiple files.
 */
public final class CompositeEmitter implements TelemetryEmitter {
    
    private final List<TelemetryEmitter> emitters;
    
    public CompositeEmitter(TelemetryEmitter... emitters) {
        this.emitters = new ArrayList<>(Arrays.asList(emitters));
    }
    
    public void addEmitter(TelemetryEmitter emitter) {
        emitters.add(emitter);
    }
    
    @Override
    public void emit(GameEvent event) {
        for (TelemetryEmitter emitter : emitters) {
            emitter.emit(event);
        }
    }
    
    @Override
    public void flush() {
        for (TelemetryEmitter emitter : emitters) {
            emitter.flush();
        }
    }
    
    @Override
    public void close() {
        for (TelemetryEmitter emitter : emitters) {
            emitter.close();
        }
    }
}
