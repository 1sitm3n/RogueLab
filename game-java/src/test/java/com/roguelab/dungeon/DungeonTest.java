package com.roguelab.dungeon;

import com.roguelab.domain.Room;
import com.roguelab.domain.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Dungeon")
class DungeonTest {
    
    private static final long SEED = 12345L;
    private Dungeon dungeon;
    
    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(SEED);
    }
    
    @Nested
    @DisplayName("Initialization")
    class Initialization {
        
        @Test
        @DisplayName("should start on floor 1")
        void startsOnFloor1() {
            assertThat(dungeon.getCurrentFloorNumber()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("should have first floor generated")
        void hasFirstFloorGenerated() {
            assertThat(dungeon.getCurrentFloor()).isNotNull();
            assertThat(dungeon.getFloorsGenerated()).isEqualTo(1);
        }
        
        @Test
        @DisplayName("should store seed")
        void storesSeed() {
            assertThat(dungeon.getSeed()).isEqualTo(SEED);
        }
        
        @Test
        @DisplayName("should have current room")
        void hasCurrentRoom() {
            assertThat(dungeon.getCurrentRoom()).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("Room navigation")
    class RoomNavigation {
        
        @Test
        @DisplayName("should advance to next room")
        void advancesToNextRoom() {
            Room first = dungeon.getCurrentRoom();
            Room second = dungeon.advanceToNextRoom();
            
            assertThat(second).isNotEqualTo(first);
            assertThat(dungeon.getCurrentRoom()).isEqualTo(second);
        }
        
        @Test
        @DisplayName("should return to previous room")
        void returnsToPreviousRoom() {
            Room first = dungeon.getCurrentRoom();
            dungeon.advanceToNextRoom();
            Room returned = dungeon.returnToPreviousRoom();
            
            assertThat(returned).isEqualTo(first);
        }
        
        @Test
        @DisplayName("should throw when no next room")
        void throwsWhenNoNextRoom() {
            // Advance to end of floor
            while (dungeon.getCurrentFloor().hasNextRoom()) {
                dungeon.advanceToNextRoom();
            }
            
            assertThatThrownBy(() -> dungeon.advanceToNextRoom())
                .isInstanceOf(IllegalStateException.class);
        }
    }
    
    @Nested
    @DisplayName("Floor navigation")
    class FloorNavigation {
        
        @Test
        @DisplayName("should descend to next floor when at exit")
        void descendsToNextFloor() {
            // Clear all combat rooms and move to exit
            Floor floor = dungeon.getCurrentFloor();
            for (Room room : floor.getRooms()) {
                room.visit();
                room.markCleared();
            }
            while (floor.hasNextRoom()) {
                dungeon.advanceToNextRoom();
            }
            
            Floor nextFloor = dungeon.descendToNextFloor();
            
            assertThat(dungeon.getCurrentFloorNumber()).isEqualTo(2);
            assertThat(nextFloor.getFloorNumber()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("should throw when descending not at exit")
        void throwsWhenNotAtExit() {
            assertThatThrownBy(() -> dungeon.descendToNextFloor())
                .isInstanceOf(IllegalStateException.class);
        }
        
        @Test
        @DisplayName("should throw when ascending from floor 1")
        void throwsWhenAscendingFromFloor1() {
            assertThatThrownBy(() -> dungeon.ascendToPreviousFloor())
                .isInstanceOf(IllegalStateException.class);
        }
        
        @Test
        @DisplayName("should track deepest floor reached")
        void tracksDeepestFloor() {
            // Prepare to descend
            Floor floor = dungeon.getCurrentFloor();
            for (Room room : floor.getRooms()) {
                room.visit();
                room.markCleared();
            }
            while (floor.hasNextRoom()) {
                dungeon.advanceToNextRoom();
            }
            
            dungeon.descendToNextFloor();
            
            assertThat(dungeon.getDeepestFloorReached()).isEqualTo(2);
        }
    }
    
    @Nested
    @DisplayName("Determinism")
    class Determinism {
        
        @Test
        @DisplayName("should produce same dungeon with same seed")
        void sameResultWithSameSeed() {
            Dungeon d1 = new Dungeon(SEED);
            Dungeon d2 = new Dungeon(SEED);
            
            Floor f1 = d1.getCurrentFloor();
            Floor f2 = d2.getCurrentFloor();
            
            assertThat(f1.getRoomCount()).isEqualTo(f2.getRoomCount());
            
            for (int i = 0; i < f1.getRoomCount(); i++) {
                assertThat(f1.getRoom(i).getType())
                    .isEqualTo(f2.getRoom(i).getType());
            }
        }
    }
    
    @Nested
    @DisplayName("State queries")
    class StateQueries {
        
        @Test
        @DisplayName("should report boss floor status")
        void reportsBossFloorStatus() {
            // Floor 3 should be boss floor with standard config
            // Navigate to floor 3
            for (int i = 0; i < 2; i++) {
                Floor floor = dungeon.getCurrentFloor();
                for (Room room : floor.getRooms()) {
                    room.visit();
                    room.markCleared();
                }
                while (floor.hasNextRoom()) {
                    dungeon.advanceToNextRoom();
                }
                dungeon.descendToNextFloor();
            }
            
            assertThat(dungeon.getCurrentFloorNumber()).isEqualTo(3);
            assertThat(dungeon.isCurrentFloorBossFloor()).isTrue();
        }
        
        @Test
        @DisplayName("canDescend should be false initially")
        void canDescendFalseInitially() {
            assertThat(dungeon.canDescend()).isFalse();
        }
        
        @Test
        @DisplayName("canAscend should be false on floor 1")
        void canAscendFalseOnFloor1() {
            assertThat(dungeon.canAscend()).isFalse();
        }
    }
}
