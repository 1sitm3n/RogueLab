package com.roguelab.domain.component;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.StatusType;

import java.util.*;

/**
 * Manages the collection of status effects on an entity.
 */
public final class StatusEffects {
    
    private final Map<StatusType, StatusEffect> effects;
    
    public StatusEffects() {
        this.effects = new EnumMap<>(StatusType.class);
    }
    
    /**
     * Apply a status effect. If already present, refreshes duration and adds stacks.
     * @return The resulting status effect
     */
    public StatusEffect apply(StatusType type, EntityId sourceId, int duration, int stacks) {
        StatusEffect existing = effects.get(type);
        if (existing != null) {
            existing.refreshDuration(duration);
            existing.addStacks(stacks);
            return existing;
        } else {
            StatusEffect newEffect = new StatusEffect(type, sourceId, duration, stacks);
            effects.put(type, newEffect);
            return newEffect;
        }
    }
    
    /**
     * Apply a status effect with 1 stack.
     */
    public StatusEffect apply(StatusType type, EntityId sourceId, int duration) {
        return apply(type, sourceId, duration, 1);
    }
    
    /**
     * Check if entity has a specific status.
     */
    public boolean has(StatusType type) {
        return effects.containsKey(type);
    }
    
    /**
     * Get a specific status effect if present.
     */
    public Optional<StatusEffect> get(StatusType type) {
        return Optional.ofNullable(effects.get(type));
    }
    
    /**
     * Get all active status effects.
     */
    public Collection<StatusEffect> getAll() {
        return Collections.unmodifiableCollection(effects.values());
    }
    
    /**
     * Remove a specific status effect.
     */
    public void remove(StatusType type) {
        effects.remove(type);
    }
    
    /**
     * Clear all status effects.
     */
    public void clear() {
        effects.clear();
    }
    
    /**
     * Process a turn: tick all effects and remove expired ones.
     * @return List of effects that expired this turn
     */
    public List<StatusEffect> tick() {
        List<StatusEffect> expired = new ArrayList<>();
        Iterator<Map.Entry<StatusType, StatusEffect>> it = effects.entrySet().iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next().getValue();
            if (effect.tick()) {
                expired.add(effect);
                it.remove();
            }
        }
        return expired;
    }
    
    /**
     * Calculate total damage-over-time from all damaging effects.
     */
    public int calculateTotalTickDamage() {
        return effects.values().stream()
            .mapToInt(StatusEffect::calculateTickDamage)
            .sum();
    }
    
    /**
     * Check if entity has any debilitating status (frozen, etc.)
     */
    public boolean isIncapacitated() {
        return has(StatusType.FROZEN);
    }
    
    public boolean isEmpty() {
        return effects.isEmpty();
    }
    
    public int count() {
        return effects.size();
    }
}
