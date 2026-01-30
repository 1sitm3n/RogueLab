package com.roguelab.dungeon;

import com.roguelab.domain.Room;
import com.roguelab.domain.RoomType;
import com.roguelab.util.GameRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FloorGenerator")
class FloorGeneratorTest {
    
    private static final long SEED = 12345L;
    private DungeonConfig config;
    private FloorGenerator generator;
    
    @BeforeEach
    void setUp() {
        config = DungeonConfig.standard();
        generator = new FloorGenerator(config, new GameRandom(SEED));
    }
    
    @Nested
    @DisplayName("Floor generation")
    class FloorGeneration {
        
        @Test
        @DisplayName("should generate floor with correct floor number")
        void generatesFloorWithCorrectNumber() {
            Floor floor = generator.generateFloor(1);
            
            assertThat(floor.getFloorNumber()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("should generate floor with rooms within config bounds")
        void generatesRoomsWithinBounds() {
            Floor floor = generator.generateFloor(1);
            
            assertThat(floor.getRoomCount())
                .isBetween(config.getMinRoomsPerFloor(), config.getMaxRoomsPerFloor());
        }
        
        @Test
        @DisplayName("should generate boss room on boss floor")
        void generatesBossRoomOnBossFloor() {
            int bossFloor = config.getBossFloorInterval();
            Floor floor = generator.generateFloor(bossFloor);
            
            assertThat(floor.hasBoss()).isTrue();
            assertThat(floor.getBossRoom()).isPresent();
            assertThat(floor.getBossRoom().get().getType()).isEqualTo(RoomType.BOSS);
        }
        
        @Test
        @DisplayName("should not generate boss room on non-boss floor")
        void noBossRoomOnNormalFloor() {
            Floor floor = generator.generateFloor(1);
            
            assertThat(floor.hasBoss()).isFalse();
        }
        
        @Test
        @DisplayName("should populate combat rooms with enemies")
        void populatesCombatRoomsWithEnemies() {
            Floor floor = generator.generateFloor(1);
            
            Room combatRoom = floor.getRooms().stream()
                .filter(r -> r.getType() == RoomType.COMBAT)
                .findFirst()
                .orElseThrow();
            
            assertThat(combatRoom.getEnemies()).isNotEmpty();
        }
    }
    
    @Nested
    @DisplayName("Determinism")
    class Determinism {
        
        @Test
        @DisplayName("should produce same floor with same seed")
        void sameResultWithSameSeed() {
            FloorGenerator gen1 = new FloorGenerator(config, new GameRandom(SEED));
            FloorGenerator gen2 = new FloorGenerator(config, new GameRandom(SEED));
            
            Floor floor1 = gen1.generateFloor(1);
            Floor floor2 = gen2.generateFloor(1);
            
            assertThat(floor1.getRoomCount()).isEqualTo(floor2.getRoomCount());
            
            for (int i = 0; i < floor1.getRoomCount(); i++) {
                Room r1 = floor1.getRoom(i);
                Room r2 = floor2.getRoom(i);
                assertThat(r1.getType()).isEqualTo(r2.getType());
                assertThat(r1.getEnemies().size()).isEqualTo(r2.getEnemies().size());
            }
        }
        
        @Test
        @DisplayName("should produce different floor with different seed")
        void differentResultWithDifferentSeed() {
            FloorGenerator gen1 = new FloorGenerator(config, new GameRandom(SEED));
            FloorGenerator gen2 = new FloorGenerator(config, new GameRandom(SEED + 1));
            
            Floor floor1 = gen1.generateFloor(1);
            Floor floor2 = gen2.generateFloor(1);
            
            // With different seeds, at least something should differ
            // (room count, room types, or enemy counts)
            boolean different = floor1.getRoomCount() != floor2.getRoomCount();
            
            if (!different) {
                for (int i = 0; i < floor1.getRoomCount() && !different; i++) {
                    Room r1 = floor1.getRoom(i);
                    Room r2 = floor2.getRoom(i);
                    different = r1.getType() != r2.getType() ||
                               r1.getEnemies().size() != r2.getEnemies().size();
                }
            }
            
            assertThat(different).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Simple floor generation")
    class SimpleFloorGeneration {
        
        @Test
        @DisplayName("should generate floor with exact room count")
        void generatesExactRoomCount() {
            Floor floor = generator.generateSimpleFloor(1, 5);
            
            assertThat(floor.getRoomCount()).isEqualTo(5);
        }
        
        @Test
        @DisplayName("should generate boss as last room on boss floor")
        void bossAsLastRoomOnBossFloor() {
            Floor floor = generator.generateSimpleFloor(3, 5);
            
            Room lastRoom = floor.getRoom(4);
            assertThat(lastRoom.getType()).isEqualTo(RoomType.BOSS);
        }
    }
}
