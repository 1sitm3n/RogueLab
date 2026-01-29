package com.roguelab.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for game entities.
 * Using a dedicated type instead of raw String/UUID provides:
 * - Type safety (can't accidentally pass a room ID where entity ID expected)
 * - Clear intent in method signatures
 * - Centralized ID generation logic
 */
public record EntityId(String value) {
    
    public EntityId {
        Objects.requireNonNull(value, "EntityId value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("EntityId value cannot be blank");
        }
    }
    
    /**
     * Create a new unique entity ID.
     */
    public static EntityId generate() {
        return new EntityId(UUID.randomUUID().toString());
    }
    
    /**
     * Create an entity ID with a semantic prefix for readability in telemetry.
     * Example: EntityId.withPrefix("goblin") -> "goblin_abc123..."
     */
    public static EntityId withPrefix(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        return new EntityId(prefix + "_" + UUID.randomUUID().toString().substring(0, 8));
    }
    
    /**
     * Create an entity ID from an existing value (e.g., "player").
     */
    public static EntityId of(String value) {
        return new EntityId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
