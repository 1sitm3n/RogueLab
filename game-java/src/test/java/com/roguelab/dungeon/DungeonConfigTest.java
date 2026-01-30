package com.roguelab.dungeon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DungeonConfig")
class DungeonConfigTest {
    
    @Nested
    @DisplayName("Presets")
    class Presets {
        
        @Test
        @DisplayName("standard config should have sensible defaults")
        void standardConfigDefaults() {
            DungeonConfig config = DungeonConfig.standard();
            
            assertThat(config.getMinRoomsPerFloor()).isPositive();
            assertThat(config.getMaxRoomsPerFloor()).isGreaterThanOrEqualTo(config.getMinRoomsPerFloor());
            assertThat(config.getBossFloorInterval()).isPositive();
        }
        
        @Test
        @DisplayName("easy config should have fewer enemies")
        void easyConfigFewerEnemies() {
            DungeonConfig easy = DungeonConfig.easy();
            DungeonConfig standard = DungeonConfig.standard();
            
            assertThat(easy.getMaxEnemiesPerRoom())
                .isLessThanOrEqualTo(standard.getMaxEnemiesPerRoom());
        }
        
        @Test
        @DisplayName("hard config should have more enemies")
        void hardConfigMoreEnemies() {
            DungeonConfig hard = DungeonConfig.hard();
            DungeonConfig standard = DungeonConfig.standard();
            
            assertThat(hard.getMaxEnemiesPerRoom())
                .isGreaterThanOrEqualTo(standard.getMaxEnemiesPerRoom());
        }
    }
    
    @Nested
    @DisplayName("Boss floor detection")
    class BossFloorDetection {
        
        @Test
        @DisplayName("should identify boss floors correctly")
        void identifiesBossFloors() {
            DungeonConfig config = DungeonConfig.builder()
                .bossFloorInterval(3)
                .build();
            
            assertThat(config.isBossFloor(1)).isFalse();
            assertThat(config.isBossFloor(2)).isFalse();
            assertThat(config.isBossFloor(3)).isTrue();
            assertThat(config.isBossFloor(4)).isFalse();
            assertThat(config.isBossFloor(6)).isTrue();
            assertThat(config.isBossFloor(9)).isTrue();
        }
        
        @Test
        @DisplayName("floor 0 should never be boss floor")
        void floor0NeverBoss() {
            DungeonConfig config = DungeonConfig.standard();
            
            assertThat(config.isBossFloor(0)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Builder validation")
    class BuilderValidation {
        
        @Test
        @DisplayName("should reject minRooms > maxRooms")
        void rejectsInvalidRoomRange() {
            assertThatThrownBy(() -> 
                DungeonConfig.builder()
                    .minRoomsPerFloor(10)
                    .maxRoomsPerFloor(5)
                    .build()
            ).isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("should reject minEnemies > maxEnemies")
        void rejectsInvalidEnemyRange() {
            assertThatThrownBy(() -> 
                DungeonConfig.builder()
                    .minEnemiesPerRoom(5)
                    .maxEnemiesPerRoom(2)
                    .build()
            ).isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        @DisplayName("should allow custom configuration")
        void allowsCustomConfig() {
            DungeonConfig config = DungeonConfig.builder()
                .minRoomsPerFloor(2)
                .maxRoomsPerFloor(3)
                .bossFloorInterval(5)
                .eliteSpawnChance(0.5)
                .build();
            
            assertThat(config.getMinRoomsPerFloor()).isEqualTo(2);
            assertThat(config.getMaxRoomsPerFloor()).isEqualTo(3);
            assertThat(config.getBossFloorInterval()).isEqualTo(5);
            assertThat(config.getEliteSpawnChance()).isEqualTo(0.5);
        }
    }
}
