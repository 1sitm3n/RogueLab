package com.roguelab.game;

import com.roguelab.combat.*;
import com.roguelab.domain.*;
import com.roguelab.dungeon.*;
import com.roguelab.util.GameRandom;

import java.time.Instant;
import java.util.*;

/**
 * Orchestrates a complete game run.
 * Manages the player, dungeon, combat, and state transitions.
 * 
 * This is the central coordinator that ties together all game systems.
 */
public final class GameSession {
    
    // Configuration
    private final String runId;
    private final long seed;
    private final Difficulty difficulty;
    private final DungeonConfig dungeonConfig;
    
    // Core game objects
    private final Player player;
    private final Dungeon dungeon;
    private final GameRandom random;
    private final CombatEngine combatEngine;
    
    // State
    private GameState state;
    private int currentTick;
    private final Instant startTime;
    private Instant endTime;
    private final RunStatistics statistics;
    
    // Event handling
    private GameSessionListener listener = GameSessionListener.NONE;
    
    /**
     * Create a new game session with full configuration.
     */
    public GameSession(String playerName, PlayerClass playerClass, 
                       long seed, Difficulty difficulty, DungeonConfig dungeonConfig) {
        this.runId = "run_" + System.currentTimeMillis();
        this.seed = seed;
        this.difficulty = difficulty;
        BalanceConfig.applyDifficulty(difficulty);
        this.dungeonConfig = dungeonConfig;
        this.random = new GameRandom(seed);
        
        this.player = new Player(playerName, playerClass);
        this.dungeon = new Dungeon(seed, dungeonConfig);
        this.combatEngine = new CombatEngine(random);
        
        this.state = GameState.INITIALIZING;
        this.currentTick = 0;
        this.startTime = Instant.now();
        this.statistics = new RunStatistics();
    }
    
    /**
     * Create a session with default dungeon config.
     */
    public GameSession(String playerName, PlayerClass playerClass, long seed, Difficulty difficulty) {
        this(playerName, playerClass, seed, difficulty, DungeonConfig.standard());
    }
    
    /**
     * Create a session with all defaults.
     */
    public GameSession(String playerName, PlayerClass playerClass, long seed) {
        this(playerName, playerClass, seed, Difficulty.NORMAL);
    }
    
    // === CONFIGURATION ===
    
    public void setListener(GameSessionListener listener) {
        this.listener = listener != null ? listener : GameSessionListener.NONE;
    }
    
    public void setCombatListener(CombatEventListener combatListener) {
        this.combatEngine.setEventListener(combatListener);
    }
    
    // === GETTERS ===
    
    public String getRunId() { return runId; }
    public long getSeed() { return seed; }
    public Difficulty getDifficulty() { return difficulty; }
    public Player getPlayer() { return player; }
    public Dungeon getDungeon() { return dungeon; }
    public GameRandom getRandom() { return random; }
    public GameState getState() { return state; }
    public int getCurrentTick() { return currentTick; }
    public Instant getStartTime() { return startTime; }
    public Optional<Instant> getEndTime() { return Optional.ofNullable(endTime); }
    public RunStatistics getStatistics() { return statistics; }
    
    public Room getCurrentRoom() {
        return dungeon.getCurrentRoom();
    }
    
    public Floor getCurrentFloor() {
        return dungeon.getCurrentFloor();
    }
    
    public int getCurrentFloorNumber() {
        return dungeon.getCurrentFloorNumber();
    }
    
    // === GAME FLOW ===
    
    /**
     * Start the game run.
     */
    public void start() {
        if (state != GameState.INITIALIZING) {
            throw new IllegalStateException("Game already started");
        }
        
        state = GameState.EXPLORING;
        currentTick++;
        
        // Mark first room as visited
        Room firstRoom = getCurrentRoom();
        firstRoom.visit();
        statistics.recordRoomVisited();
        
        listener.onRunStarted(this);
        listener.onFloorEntered(this, getCurrentFloor());
        listener.onRoomEntered(this, firstRoom);
        
        // If first room has enemies, trigger combat
        if (firstRoom.hasAliveEnemies()) {
            enterCombat();
        }
    }
    
    /**
     * Advance to the next room on the current floor.
     */
    public void advanceRoom() {
        validateState(GameState.EXPLORING);
        
        if (!getCurrentFloor().hasNextRoom()) {
            throw new IllegalStateException("No more rooms on this floor");
        }
        
        Room room = dungeon.advanceToNextRoom();
        room.visit();
        statistics.recordRoomVisited();
        currentTick++;
        
        listener.onRoomEntered(this, room);
        
        handleRoomEntry(room);
    }
    
    /**
     * Return to the previous room.
     */
    public void returnRoom() {
        validateState(GameState.EXPLORING);
        
        if (!getCurrentFloor().hasPreviousRoom()) {
            throw new IllegalStateException("Already at first room");
        }
        
        Room room = dungeon.returnToPreviousRoom();
        currentTick++;
        
        listener.onRoomEntered(this, room);
    }
    
    /**
     * Descend to the next floor.
     */
    public void descendFloor() {
        validateState(GameState.EXPLORING);
        
        if (!dungeon.canDescend()) {
            throw new IllegalStateException("Cannot descend - not at exit or rooms not cleared");
        }
        
        statistics.recordFloorCompleted();
        Floor newFloor = dungeon.descendToNextFloor();
        player.descendToNextFloor();
        currentTick++;
        
        listener.onFloorEntered(this, newFloor);
        
        // Visit first room of new floor
        Room firstRoom = newFloor.getCurrentRoom();
        firstRoom.visit();
        statistics.recordRoomVisited();
        
        listener.onRoomEntered(this, firstRoom);
        
        handleRoomEntry(firstRoom);
    }
    
    /**
     * Handle entering a room based on its type.
     */
    private void handleRoomEntry(Room room) {
        switch (room.getType()) {
            case COMBAT, BOSS -> {
                if (room.hasAliveEnemies()) {
                    enterCombat();
                }
            }
            case SHOP -> state = GameState.IN_SHOP;
            case REST -> state = GameState.AT_REST;
            case TREASURE -> collectTreasure(room);
            case EVENT -> state = GameState.IN_EVENT;
        }
    }
    
    // === COMBAT ===
    
    /**
     * Enter combat in the current room.
     */
    private void enterCombat() {
        Room room = getCurrentRoom();
        if (!room.hasAliveEnemies()) {
            throw new IllegalStateException("No enemies in room");
        }
        
        state = GameState.IN_COMBAT;
    }
    
    /**
     * Execute combat in the current room.
     * Returns the combat result.
     */
    public CombatResult executeCombat() {
        validateState(GameState.IN_COMBAT);
        
        Room room = getCurrentRoom();
        CombatResult result = combatEngine.runCombat(
            runId, player, room, random, currentTick
        );
        
        currentTick += result.turnsElapsed();
        statistics.recordCombatTurns(result.turnsElapsed());
        statistics.recordDamageDealt(result.totalDamageDealt());
        statistics.recordDamageTaken(result.totalDamageTaken());
        
        if (result.isVictory()) {
            for (int i = 0; i < result.enemiesKilled(); i++) {
                statistics.recordEnemyKilled();
            }
            if (room.getType() == RoomType.BOSS) {
                statistics.recordBossKilled();
            }
            statistics.recordGoldEarned(result.goldEarned());
            
            room.markCleared();
            statistics.recordRoomCleared();
            state = GameState.EXPLORING;
            
            listener.onRoomCleared(this, room);
        } else {
            // Player died
            endRun(GameSessionListener.RunEndReason.PLAYER_DEATH);
        }
        
        listener.onCombatCompleted(this, result);
        
        return result;
    }
    
    // === SHOP ===
    
    /**
     * Purchase an item from the shop.
     */
    public boolean purchaseItem(Item item) {
        validateState(GameState.IN_SHOP);
        
        int cost = item.getValue();
        if (player.getInventory().getGold() < cost) {
            return false;
        }
        
        player.getInventory().spendGold(cost);
        player.getInventory().addItem(item);
        getCurrentRoom().removeItem(item);
        
        statistics.recordGoldSpent(cost);
        statistics.recordItemCollected();
        
        listener.onShopPurchase(this, item, cost);
        
        return true;
    }
    
    /**
     * Leave the shop.
     */
    public void leaveShop() {
        validateState(GameState.IN_SHOP);
        state = GameState.EXPLORING;
    }
    
    // === REST SITE ===
    
    /**
     * Rest at a rest site to heal.
     */
    public int rest() {
        validateState(GameState.AT_REST);
        
        // Heal 30% of max health
        int healAmount = (int)(player.getHealth().getMaximum() * BalanceConfig.REST_HEAL_PERCENT);
        int healed = player.getHealth().heal(maxHeal);
        
        statistics.recordHealing(healed);
        currentTick++;
        
        listener.onPlayerRested(this, healed);
        
        return healed;
    }
    
    /**
     * Leave the rest site.
     */
    public void leaveRest() {
        validateState(GameState.AT_REST);
        state = GameState.EXPLORING;
    }
    
    // === INVENTORY ===
    
    /**
     * Pick up an item from the current room.
     */
    public void pickUpItem(Item item) {
        Room room = getCurrentRoom();
        if (!room.getItems().contains(item)) {
            throw new IllegalArgumentException("Item not in room");
        }
        
        player.getInventory().addItem(item);
        room.removeItem(item);
        statistics.recordItemCollected();
        
        listener.onItemPicked(this, item);
    }
    
    /**
     * Use a consumable item.
     */
    public void useItem(Item item) {
        if (item.getType() != ItemType.CONSUMABLE) {
            throw new IllegalArgumentException("Item is not consumable");
        }
        
        if (!player.getInventory().getItems().contains(item)) {
            throw new IllegalArgumentException("Item not in inventory");
        }
        
        // Apply healing
        if (item.getHealthBonus() > 0) {
            int healed = player.getHealth().heal(item.getHealthBonus());
            statistics.recordHealing(healed);
        }
        
        player.getInventory().removeItem(item);
        statistics.recordItemUsed();
        
        listener.onItemUsed(this, item);
    }
    
    // === TREASURE ===
    
    /**
     * Collect all items from a treasure room.
     */
    private void collectTreasure(Room room) {
        List<Item> items = new ArrayList<>(room.getItems());
        for (Item item : items) {
            player.getInventory().addItem(item);
            room.removeItem(item);
            statistics.recordItemCollected();
            listener.onItemPicked(this, item);
        }
        
        room.markCleared();
        statistics.recordRoomCleared();
    }
    
    // === GAME END ===
    
    /**
     * End the run.
     */
    public void endRun(GameSessionListener.RunEndReason reason) {
        if (state == GameState.RUN_ENDED) {
            return; // Already ended
        }
        
        state = GameState.RUN_ENDED;
        endTime = Instant.now();
        
        listener.onRunEnded(this, reason);
    }
    
    /**
     * Check if the run is still active.
     */
    public boolean isActive() {
        return state.isRunActive();
    }
    
    // === VALIDATION ===
    
    private void validateState(GameState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                "Expected state " + expected + " but was " + state
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("GameSession[%s, %s, floor=%d, state=%s]",
            runId, player.getName(), getCurrentFloorNumber(), state);
    }
}
