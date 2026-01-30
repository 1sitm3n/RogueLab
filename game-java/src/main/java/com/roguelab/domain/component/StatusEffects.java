package com.roguelab.domain.component;

import com.roguelab.domain.EntityId;
import com.roguelab.domain.StatusType;

import java.util.*;

/**
 * Component that manages status effects on an entity.
 */
public final class StatusEffects {
    
    private final Map<StatusType, StatusEffect> effects;
    
    public StatusEffects() {
        this.effects = new EnumMap<>(StatusType.class);
    }
    
    public boolean has(StatusType type) {
        return effects.containsKey(type) && !effects.get(type).isExpired();
    }
    
    public boolean hasStatus(StatusType type) {
        return has(type);
    }
    
    public StatusEffect getStatus(StatusType type) {
        return effects.get(type);
    }
    
    public void apply(StatusType type, EntityId sourceId, int duration, int stacks) {
        if (effects.containsKey(type)) {
            StatusEffect existing = effects.get(type);
            existing.refreshDuration(duration);
            if (type.isStackable()) {
                int maxStacks = type.getMaxStacks();
                int newStacks = Math.min(existing.getStacks() + stacks, maxStacks);
                existing.addStacks(newStacks - existing.getStacks());
            }
        } else {
            effects.put(type, new StatusEffect(type, sourceId, duration, stacks));
        }
    }
    
    public void apply(StatusType type, EntityId sourceId, int duration) {
        apply(type, sourceId, duration, 1);
    }
    
    public boolean remove(StatusType type) {
        return effects.remove(type) != null;
    }
    
    public void clear() {
        effects.clear();
    }
    
    public Collection<StatusEffect> getAll() {
        return Collections.unmodifiableCollection(effects.values());
    }
    
    public int count() {
        return effects.size();
    }
    
    public boolean isEmpty() {
        return effects.isEmpty();
    }
    
    public List<StatusType> tickAll() {
        List<StatusType> expired = new ArrayList<>();
        Iterator<Map.Entry<StatusType, StatusEffect>> it = effects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StatusType, StatusEffect> entry = it.next();
            if (entry.getValue().tick()) {
                expired.add(entry.getKey());
                it.remove();
            }
        }
        return expired;
    }
    
    public double getAttackModifier() {
        double modifier = 1.0;
        if (has(StatusType.STRENGTHENED)) modifier *= 1.50;
        if (has(StatusType.WEAKENED)) modifier *= 0.75;
        return modifier;
    }
    
    public double getDefenseModifier() {
        double modifier = 1.0;
        if (has(StatusType.VULNERABLE)) modifier *= 1.50;
        if (has(StatusType.ARMORED)) modifier *= 0.75;
        if (has(StatusType.SHIELDED)) modifier *= 0.50;
        return modifier;
    }
    
    @Override
    public String toString() {
        if (effects.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (StatusEffect effect : effects.values()) {
            if (!first) sb.append(", ");
            sb.append(effect.getType().name()).append("(").append(effect.getRemainingDuration()).append("t)");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
