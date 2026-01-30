package com.roguelab.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RunStatistics")
class RunStatisticsTest {
    
    private RunStatistics stats;
    
    @BeforeEach
    void setUp() {
        stats = new RunStatistics();
    }
    
    @Test
    @DisplayName("should start with all zeros")
    void startsWithAllZeros() {
        assertThat(stats.getRoomsVisited()).isZero();
        assertThat(stats.getEnemiesKilled()).isZero();
        assertThat(stats.getGoldEarned()).isZero();
        assertThat(stats.getTotalTicks()).isZero();
    }
    
    @Test
    @DisplayName("should track room statistics")
    void tracksRoomStatistics() {
        stats.recordRoomVisited();
        stats.recordRoomVisited();
        stats.recordRoomCleared();
        
        assertThat(stats.getRoomsVisited()).isEqualTo(2);
        assertThat(stats.getRoomsCleared()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("should track combat statistics")
    void tracksCombatStatistics() {
        stats.recordDamageDealt(100);
        stats.recordDamageTaken(50);
        stats.recordEnemyKilled();
        stats.recordEnemyKilled();
        stats.recordBossKilled();
        
        assertThat(stats.getTotalDamageDealt()).isEqualTo(100);
        assertThat(stats.getTotalDamageTaken()).isEqualTo(50);
        assertThat(stats.getEnemiesKilled()).isEqualTo(2);
        assertThat(stats.getBossesKilled()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("should track gold statistics")
    void tracksGoldStatistics() {
        stats.recordGoldEarned(100);
        stats.recordGoldSpent(30);
        
        assertThat(stats.getGoldEarned()).isEqualTo(100);
        assertThat(stats.getGoldSpent()).isEqualTo(30);
        assertThat(stats.getNetGold()).isEqualTo(70);
    }
    
    @Test
    @DisplayName("should calculate damage efficiency")
    void calculatesDamageEfficiency() {
        stats.recordDamageDealt(100);
        stats.recordDamageTaken(50);
        
        assertThat(stats.getDamageEfficiency()).isEqualTo(2.0);
    }
    
    @Test
    @DisplayName("should handle zero damage taken for efficiency")
    void handlesZeroDamageTaken() {
        stats.recordDamageDealt(100);
        
        assertThat(stats.getDamageEfficiency()).isEqualTo(Double.MAX_VALUE);
    }
    
    @Test
    @DisplayName("should calculate room clear rate")
    void calculatesRoomClearRate() {
        stats.recordRoomVisited();
        stats.recordRoomVisited();
        stats.recordRoomCleared();
        
        assertThat(stats.getAverageRoomClearRate()).isEqualTo(0.5);
    }
}
