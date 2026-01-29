package com.roguelab.event;

import com.roguelab.domain.Difficulty;
import com.roguelab.domain.PlayerClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Game Events")
class GameEventTest {
    
    private final UUID testRunId = UUID.randomUUID();
    
    @Test
    @DisplayName("RunStartedEvent should have tick 0")
    void runStartedHasTickZero() {
        RunStartedEvent event = new RunStartedEvent(
            testRunId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        );
        
        assertThat(event.getTick()).isEqualTo(0);
        assertThat(event.getEventType()).isEqualTo("RUN_STARTED");
        assertThat(event.getEventVersion()).isEqualTo("1.0.0");
    }
    
    @Test
    @DisplayName("events should capture timestamp")
    void capturesTimestamp() {
        Instant before = Instant.now();
        
        RunStartedEvent event = new RunStartedEvent(
            testRunId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        );
        
        Instant after = Instant.now();
        
        assertThat(event.getTimestamp())
            .isAfterOrEqualTo(before)
            .isBeforeOrEqualTo(after);
    }
    
    @Test
    @DisplayName("events should reject negative tick")
    void rejectsNegativeTick() {
        assertThatThrownBy(() -> new RunEndedEvent(
            testRunId, -1, RunEndedEvent.Outcome.VICTORY,
            5, 1000, 500, 10, 25, 3600
        )).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("events should reject null runId")
    void rejectsNullRunId() {
        assertThatThrownBy(() -> new RunStartedEvent(
            null, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        )).isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("DamageDealtEvent should capture combat details")
    void damageDealtCapturesCombat() {
        DamageDealtEvent event = new DamageDealtEvent(
            testRunId, 42,
            "player", DamageDealtEvent.EntityType.PLAYER,
            "goblin_1", DamageDealtEvent.EntityType.ENEMY,
            10, 15, com.roguelab.domain.DamageType.PHYSICAL,
            true, 20, 5, false
        );
        
        assertThat(event.getSourceId()).isEqualTo("player");
        assertThat(event.getTargetId()).isEqualTo("goblin_1");
        assertThat(event.getBaseDamage()).isEqualTo(10);
        assertThat(event.getFinalDamage()).isEqualTo(15);
        assertThat(event.isCritical()).isTrue();
        assertThat(event.isTargetKilled()).isFalse();
    }
    
    @Test
    @DisplayName("EventType enum should have current versions")
    void eventTypeHasVersions() {
        for (EventType type : EventType.values()) {
            assertThat(type.getCurrentVersion())
                .isNotNull()
                .matches("\\d+\\.\\d+\\.\\d+");
        }
    }
    
    @Test
    @DisplayName("should allow explicit timestamp for testing")
    void allowsExplicitTimestamp() {
        Instant fixedTime = Instant.parse("2025-01-15T14:30:00.000Z");
        
        RunStartedEvent event = new RunStartedEvent(
            fixedTime, testRunId, 12345L, "0.1.0",
            PlayerClass.WARRIOR, Difficulty.NORMAL, "Hero"
        );
        
        assertThat(event.getTimestamp()).isEqualTo(fixedTime);
    }
}
