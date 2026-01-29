package com.roguelab.domain;

/**
 * Types of damage that can be dealt in combat.
 * Different damage types can have resistances/weaknesses.
 */
public enum DamageType {
    PHYSICAL,
    FIRE,
    ICE,
    POISON,
    MAGIC;
    
    /**
     * JSON serialization value (matches event schema).
     */
    public String toJsonValue() {
        return name();
    }
}
