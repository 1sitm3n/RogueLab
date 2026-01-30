package com.roguelab.game;

import com.roguelab.combat.CombatResult;
import com.roguelab.domain.*;
import com.roguelab.dungeon.DungeonConfig;
import com.roguelab.dungeon.Floor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameSession")
class GameSessionTest {
    
    private static final long SEED = 12345L;
    private GameSession session;
    private TestListener listener;
    
    @BeforeEach
    void setUp() {
        session = new GameSession("Hero", PlayerClass.WARRIOR, SEED, Difficulty.NORMAL,
            DungeonConfig.easy());
        listener = new TestListener();
        session.setListener(listener);
    }
    
    @Nested
    @DisplayName("Initialization")
    class Initialization {
        
        @Test
        @DisplayName("should create session in INITIALIZING state")
        void createsInInitializingState() {
            assertThat(session.getState()).isEqualTo(GameState.INITIALIZING);
        }
        
        @Test
        @DisplayName("should have correct run configuration")
        void hasCorrectConfig() {
            assertThat(session.getSeed()).isEqualTo(SEED);
            assertThat(session.getDifficulty()).isEqualTo(Difficulty.NORMAL);
            assertThat(session.getRunId()).startsWith("run_");
        }
        
        @Test
        @DisplayName("should have player with correct class")
        void hasPlayerWithCorrectClass() {
            assertThat(session.getPlayer().getName()).isEqualTo("Hero");
            assertThat(session.getPlayer().getPlayerClass()).isEqualTo(PlayerClass.WARRIOR);
        }
        
        @Test
        @DisplayName("should have dungeon on floor 1")
        void hasDungeonOnFloor1() {
            assertThat(session.getCurrentFloorNumber()).isEqualTo(1);
        }
    }
    
    @Nested
    @DisplayName("Starting the game")
    class StartingGame {
        
        @Test
        @DisplayName("should transition to EXPLORING or IN_COMBAT state")
        void transitionsToPlayState() {
            session.start();
            
            assertThat(session.getState())
                .isIn(GameState.EXPLORING, GameState.IN_COMBAT);
        }
        
        @Test
        @DisplayName("should emit run started event")
        void emitsRunStartedEvent() {
            session.start();
            
            assertThat(listener.runStarted).isTrue();
        }
        
        @Test
        @DisplayName("should emit floor entered event")
        void emitsFloorEnteredEvent() {
            session.start();
            
            assertThat(listener.floorsEntered).hasSize(1);
        }
        
        @Test
        @DisplayName("should emit room entered event")
        void emitsRoomEnteredEvent() {
            session.start();
            
            assertThat(listener.roomsEntered).hasSize(1);
        }
        
        @Test
        @DisplayName("should throw if already started")
        void throwsIfAlreadyStarted() {
            session.start();
            
            assertThatThrownBy(() -> session.start())
                .isInstanceOf(IllegalStateException.class);
        }
    }
    
    @Nested
    @DisplayName("Room navigation")
    class RoomNavigation {
        
        @BeforeEach
        void startGame() {
            session.start();
            // Clear any combat first
            clearCombatIfNeeded();
        }
        
        private void clearCombatIfNeeded() {
            if (session.getState() == GameState.IN_COMBAT) {
                session.executeCombat();
            }
        }
        
        @Test
        @DisplayName("should advance to next room")
        void advancesToNextRoom() {
            if (!session.getCurrentFloor().hasNextRoom()) return; // Skip if only one room
            
            int initialRoomIndex = session.getCurrentFloor().getCurrentRoomIndex();
            session.advanceRoom();
            
            assertThat(session.getCurrentFloor().getCurrentRoomIndex())
                .isEqualTo(initialRoomIndex + 1);
        }
        
        @Test
        @DisplayName("should record room visited in statistics")
        void recordsRoomVisited() {
            int initialVisits = session.getStatistics().getRoomsVisited();
            
            if (session.getCurrentFloor().hasNextRoom()) {
                session.advanceRoom();
                assertThat(session.getStatistics().getRoomsVisited())
                    .isEqualTo(initialVisits + 1);
            }
        }
    }
    
    @Nested
    @DisplayName("Combat")
    class Combat {
        
        @Test
        @DisplayName("should execute combat and return result")
        void executesCombatAndReturnsResult() {
            session.start();
            
            if (session.getState() == GameState.IN_COMBAT) {
                CombatResult result = session.executeCombat();
                
                assertThat(result).isNotNull();
                assertThat(result.turnsElapsed()).isPositive();
            }
        }
        
        @Test
        @DisplayName("should record combat statistics")
        void recordsCombatStatistics() {
            session.start();
            
            if (session.getState() == GameState.IN_COMBAT) {
                CombatResult result = session.executeCombat();
                
                if (result.isVictory()) {
                    assertThat(session.getStatistics().getEnemiesKilled())
                        .isEqualTo(result.enemiesKilled());
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Determinism")
    class Determinism {
        
        @Test
        @DisplayName("should produce same dungeon with same seed")
        void sameResultWithSameSeed() {
            GameSession session1 = new GameSession("Hero", PlayerClass.WARRIOR, SEED);
            GameSession session2 = new GameSession("Hero", PlayerClass.WARRIOR, SEED);
            
            session1.start();
            session2.start();
            
            // Same floor layout
            assertThat(session1.getCurrentFloor().getRoomCount())
                .isEqualTo(session2.getCurrentFloor().getRoomCount());
        }
    }
    
    @Nested
    @DisplayName("Run ending")
    class RunEnding {
        
        @Test
        @DisplayName("should end run and emit event")
        void endsRunAndEmitsEvent() {
            session.start();
            session.endRun(GameSessionListener.RunEndReason.ABANDONED);
            
            assertThat(session.getState()).isEqualTo(GameState.RUN_ENDED);
            assertThat(listener.runEnded).isTrue();
            assertThat(listener.endReason).isEqualTo(GameSessionListener.RunEndReason.ABANDONED);
        }
        
        @Test
        @DisplayName("should not be active after ending")
        void notActiveAfterEnding() {
            session.start();
            session.endRun(GameSessionListener.RunEndReason.VICTORY);
            
            assertThat(session.isActive()).isFalse();
        }
    }
    
    /**
     * Test listener to capture events.
     */
    static class TestListener implements GameSessionListener {
        boolean runStarted = false;
        List<Floor> floorsEntered = new ArrayList<>();
        List<Room> roomsEntered = new ArrayList<>();
        List<Room> roomsCleared = new ArrayList<>();
        List<CombatResult> combatResults = new ArrayList<>();
        List<Item> itemsPicked = new ArrayList<>();
        boolean runEnded = false;
        RunEndReason endReason = null;
        
        @Override public void onRunStarted(GameSession s) { runStarted = true; }
        @Override public void onFloorEntered(GameSession s, Floor f) { floorsEntered.add(f); }
        @Override public void onRoomEntered(GameSession s, Room r) { roomsEntered.add(r); }
        @Override public void onRoomCleared(GameSession s, Room r) { roomsCleared.add(r); }
        @Override public void onCombatCompleted(GameSession s, CombatResult r) { combatResults.add(r); }
        @Override public void onItemPicked(GameSession s, Item i) { itemsPicked.add(i); }
        @Override public void onItemUsed(GameSession s, Item i) {}
        @Override public void onShopPurchase(GameSession s, Item i, int c) {}
        @Override public void onPlayerRested(GameSession s, int h) {}
        @Override public void onPlayerLevelUp(GameSession s, int l) {}
        @Override public void onRunEnded(GameSession s, RunEndReason r) { runEnded = true; endReason = r; }
    }
}
