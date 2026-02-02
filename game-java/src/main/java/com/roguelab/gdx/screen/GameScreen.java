package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.effect.DamageNumber;
import com.roguelab.gdx.effect.EffectsManager;
import com.roguelab.gdx.render.CombatRenderer;
import com.roguelab.gdx.render.DungeonRenderer;
import com.roguelab.gdx.render.UIRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main game screen handling dungeon exploration and combat.
 * 
 * This is a standalone demo that simulates game state.
 * In full integration, this would use the existing domain classes.
 */
public class GameScreen implements Screen {
    
    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    
    // Game state (simplified for standalone demo)
    private GameState state;
    private String playerClass;
    
    // Sub-renderers
    private DungeonRenderer dungeonRenderer;
    private CombatRenderer combatRenderer;
    private UIRenderer uiRenderer;
    private EffectsManager effects;
    
    // Screen shake
    private float shakeTime = 0;
    private float shakeIntensity = 0;
    private Vector2 shakeOffset = new Vector2();
    
    // Combat state
    private boolean inCombat = false;
    private float combatTimer = 0;
    private boolean playerTurn = true;
    
    // Message log
    private List<String> messages = new ArrayList<>();
    private float messageTimer = 0;
    
    public GameScreen(RogueLabGame game, String playerClass) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.playerClass = playerClass;
        
        // Initialize game state
        this.state = new GameState(playerClass);
        
        // Initialize sub-renderers
        this.dungeonRenderer = new DungeonRenderer(game, state);
        this.combatRenderer = new CombatRenderer(game, state);
        this.uiRenderer = new UIRenderer(game, state);
        this.effects = new EffectsManager(game);
        
        addMessage("Welcome to RogueLab, " + playerClass + "!");
        addMessage("Use WASD or Arrow keys to move. SPACE to interact.");
    }
    
    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Starting game as " + playerClass);
    }
    
    @Override
    public void render(float delta) {
        // Update
        update(delta);
        
        // Calculate shake offset
        updateShake(delta);
        
        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Apply screen shake
        batch.getProjectionMatrix().translate(shakeOffset.x, shakeOffset.y, 0);
        
        // Render based on current mode
        if (inCombat) {
            combatRenderer.render(batch, shapeRenderer, delta);
        } else {
            dungeonRenderer.render(batch, shapeRenderer, delta);
        }
        
        // Reset shake for UI (UI shouldn't shake)
        batch.getProjectionMatrix().translate(-shakeOffset.x, -shakeOffset.y, 0);
        
        // Render UI overlay
        uiRenderer.render(batch, shapeRenderer, delta, messages);
        
        // Render effects on top
        effects.render(batch, delta);
        
        // Debug info
        if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            renderDebug();
        }
    }
    
    private void update(float delta) {
        // Update effects
        effects.update(delta);
        
        // Update message timer
        messageTimer += delta;
        if (messageTimer > 5f && messages.size() > 3) {
            messages.remove(0);
            messageTimer = 0;
        }
        
        // Handle input based on mode
        if (inCombat) {
            updateCombat(delta);
        } else {
            updateExploration(delta);
        }
        
        // Check for game over
        if (state.playerHealth <= 0) {
            game.gameOver(false, state.gold, state.floor);
        }
        
        // Check for victory
        if (state.floor > state.maxFloors && !inCombat) {
            game.gameOver(true, state.gold + state.playerHealth * 10, state.floor - 1);
        }
        
        // Escape to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
        }
    }
    
    private void updateExploration(float delta) {
        // Movement
        boolean moved = false;
        int dx = 0, dy = 0;
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            dy = 1; moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            dy = -1; moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            dx = -1; moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            dx = 1; moved = true;
        }
        
        if (moved) {
            movePlayer(dx, dy);
        }
        
        // Interact
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            interactWithRoom();
        }
    }
    
    private void movePlayer(int dx, int dy) {
        int newRoom = state.currentRoom + dx;
        
        // Bounds check
        if (newRoom < 0 || newRoom >= state.roomCount) {
            return;
        }
        
        state.currentRoom = newRoom;
        state.visitedRooms[newRoom] = true;
        
        // Check what's in the new room
        RoomType type = state.rooms[newRoom];
        switch (type) {
            case COMBAT:
                startCombat(false);
                break;
            case BOSS:
                startCombat(true);
                break;
            case TREASURE:
                addMessage("You found a treasure chest!");
                break;
            case SHOP:
                addMessage("A merchant offers their wares.");
                break;
            case REST:
                addMessage("A peaceful campfire. Press SPACE to rest.");
                break;
            case STAIRS:
                addMessage("Stairs leading down. Press SPACE to descend.");
                break;
            default:
                break;
        }
    }
    
    private void interactWithRoom() {
        RoomType type = state.rooms[state.currentRoom];
        
        switch (type) {
            case TREASURE:
                if (!state.roomsCleared[state.currentRoom]) {
                    int gold = 20 + state.random.nextInt(30) * state.floor;
                    state.gold += gold;
                    state.roomsCleared[state.currentRoom] = true;
                    addMessage("Found " + gold + " gold!");
                    effects.addDamageNumber(
                        Gdx.graphics.getWidth() / 2f,
                        Gdx.graphics.getHeight() / 2f,
                        "+" + gold + " gold",
                        Color.GOLD
                    );
                }
                break;
                
            case REST:
                if (!state.roomsCleared[state.currentRoom]) {
                    int heal = (int)(state.playerMaxHealth * 0.3f);
                    state.playerHealth = Math.min(state.playerMaxHealth, state.playerHealth + heal);
                    state.roomsCleared[state.currentRoom] = true;
                    addMessage("Rested and recovered " + heal + " HP!");
                    effects.addDamageNumber(
                        Gdx.graphics.getWidth() / 2f,
                        Gdx.graphics.getHeight() / 2f,
                        "+" + heal + " HP",
                        Color.GREEN
                    );
                }
                break;
                
            case SHOP:
                if (state.gold >= 50) {
                    state.gold -= 50;
                    state.playerAttack += 2;
                    addMessage("Bought a weapon upgrade! (+2 ATK)");
                } else {
                    addMessage("Not enough gold! (Need 50)");
                }
                break;
                
            case STAIRS:
                if (state.bossDefeated) {
                    state.floor++;
                    state.currentRoom = 0;
                    state.bossDefeated = false;
                    generateFloor();
                    addMessage("Descended to floor " + state.floor);
                    triggerShake(0.3f, 5f);
                } else {
                    addMessage("Defeat the boss first!");
                }
                break;
                
            default:
                break;
        }
    }
    
    private void startCombat(boolean boss) {
        inCombat = true;
        combatTimer = 0;
        playerTurn = true;
        
        // Generate enemy
        if (boss) {
            state.enemyName = getBossName();
            state.enemyMaxHealth = 60 + state.floor * 15;
            state.enemyAttack = 9 + state.floor * 2;
            state.enemyDefense = 3 + state.floor;
        } else {
            state.enemyName = getEnemyName();
            state.enemyMaxHealth = 15 + state.floor * 8;
            state.enemyAttack = 5 + state.floor * 2;
            state.enemyDefense = 1 + state.floor / 2;
        }
        state.enemyHealth = state.enemyMaxHealth;
        state.enemyIsBoss = boss;
        
        addMessage("Combat started with " + state.enemyName + "!");
    }
    
    private void updateCombat(float delta) {
        combatTimer += delta;
        
        // Player turn
        if (playerTurn) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || 
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                playerAttack();
            }
        } else {
            // Enemy turn (auto after delay)
            if (combatTimer > 0.8f) {
                enemyAttack();
            }
        }
    }
    
    private void playerAttack() {
        int damage = calculateDamage(state.playerAttack, state.enemyDefense);
        boolean crit = state.random.nextFloat() < 0.1f;
        if (crit) damage = (int)(damage * 1.5f);
        
        state.enemyHealth -= damage;
        
        // Visual effects
        float enemyX = Gdx.graphics.getWidth() * 0.7f;
        float enemyY = Gdx.graphics.getHeight() * 0.5f;
        
        effects.addDamageNumber(enemyX, enemyY + 50, "-" + damage, crit ? Color.YELLOW : Color.WHITE);
        effects.addSlash(enemyX, enemyY);
        triggerShake(0.1f, 3f);
        
        addMessage("You deal " + damage + " damage!" + (crit ? " CRITICAL!" : ""));
        
        // Check if enemy died
        if (state.enemyHealth <= 0) {
            endCombat(true);
        } else {
            playerTurn = false;
            combatTimer = 0;
        }
    }
    
    private void enemyAttack() {
        int damage = calculateDamage(state.enemyAttack, state.playerDefense);
        state.playerHealth -= damage;
        
        // Visual effects
        float playerX = Gdx.graphics.getWidth() * 0.3f;
        float playerY = Gdx.graphics.getHeight() * 0.5f;
        
        effects.addDamageNumber(playerX, playerY + 50, "-" + damage, Color.RED);
        effects.addHit(playerX, playerY);
        triggerShake(0.15f, 5f);
        
        addMessage(state.enemyName + " deals " + damage + " damage!");
        
        playerTurn = true;
        combatTimer = 0;
    }
    
    private void endCombat(boolean victory) {
        inCombat = false;
        state.roomsCleared[state.currentRoom] = true;
        
        if (victory) {
            int goldReward = 10 + state.floor * 5 + (state.enemyIsBoss ? 50 : 0);
            state.gold += goldReward;
            addMessage("Victory! Gained " + goldReward + " gold.");
            
            if (state.enemyIsBoss) {
                state.bossDefeated = true;
                addMessage("Boss defeated! Stairs are now accessible.");
            }
            
            effects.addDamageNumber(
                Gdx.graphics.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f + 100,
                "VICTORY!",
                Color.GOLD
            );
        }
    }
    
    private int calculateDamage(int attack, int defense) {
        int baseDamage = Math.max(1, attack - defense);
        float variance = 0.8f + state.random.nextFloat() * 0.4f;
        return Math.max(1, (int)(baseDamage * variance));
    }
    
    private void generateFloor() {
        state.roomCount = 5 + state.floor;
        state.rooms = new RoomType[state.roomCount];
        state.visitedRooms = new boolean[state.roomCount];
        state.roomsCleared = new boolean[state.roomCount];
        
        // Generate room types
        state.rooms[0] = RoomType.EMPTY; // Start room
        state.visitedRooms[0] = true;
        
        for (int i = 1; i < state.roomCount - 1; i++) {
            float roll = state.random.nextFloat();
            if (roll < 0.4f) state.rooms[i] = RoomType.COMBAT;
            else if (roll < 0.55f) state.rooms[i] = RoomType.TREASURE;
            else if (roll < 0.65f) state.rooms[i] = RoomType.SHOP;
            else if (roll < 0.75f) state.rooms[i] = RoomType.REST;
            else state.rooms[i] = RoomType.EMPTY;
        }
        
        // Last room is always boss + stairs
        state.rooms[state.roomCount - 1] = RoomType.BOSS;
    }
    
    private String getEnemyName() {
        String[] names = {"Rat", "Slime", "Bat", "Goblin", "Skeleton", "Spider"};
        return names[state.random.nextInt(names.length)];
    }
    
    private String getBossName() {
        String[] names = {"Goblin King", "Giant Troll", "Dark Wizard"};
        return names[Math.min(state.floor - 1, names.length - 1)];
    }
    
    private void triggerShake(float duration, float intensity) {
        shakeTime = duration;
        shakeIntensity = intensity;
    }
    
    private void updateShake(float delta) {
        if (shakeTime > 0) {
            shakeTime -= delta;
            shakeOffset.x = (MathUtils.random() - 0.5f) * 2 * shakeIntensity;
            shakeOffset.y = (MathUtils.random() - 0.5f) * 2 * shakeIntensity;
        } else {
            shakeOffset.set(0, 0);
        }
    }
    
    private void addMessage(String message) {
        messages.add(message);
        if (messages.size() > 6) {
            messages.remove(0);
        }
        messageTimer = 0;
    }
    
    private void renderDebug() {
        batch.begin();
        BitmapFont font = game.getAssets().getSmallFont();
        font.setColor(Color.YELLOW);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, Gdx.graphics.getHeight() - 10);
        font.draw(batch, "Floor: " + state.floor + " Room: " + state.currentRoom, 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "InCombat: " + inCombat, 10, Gdx.graphics.getHeight() - 50);
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {}
    
    // === Inner Classes ===
    
    public enum RoomType {
        EMPTY, COMBAT, BOSS, TREASURE, SHOP, REST, STAIRS
    }
    
    /**
     * Simplified game state for standalone demo.
     * In full integration, this would wrap the existing domain classes.
     */
    public static class GameState {
        public Random random = new Random();
        
        // Player stats
        public String playerClass;
        public int playerHealth;
        public int playerMaxHealth;
        public int playerAttack;
        public int playerDefense;
        public int gold = 0;
        
        // Dungeon state
        public int floor = 1;
        public int maxFloors = 3;
        public int roomCount;
        public int currentRoom = 0;
        public RoomType[] rooms;
        public boolean[] visitedRooms;
        public boolean[] roomsCleared;
        public boolean bossDefeated = false;
        
        // Enemy state (during combat)
        public String enemyName;
        public String enemyType;
        public int enemyHealth;
        public int enemyMaxHealth;
        public int enemyAttack;
        public int enemyDefense;
        public boolean enemyIsBoss;
        
        public GameState(String playerClass) {
            this.playerClass = playerClass;
            
            switch (playerClass) {
                case "WARRIOR":
                    playerMaxHealth = 100;
                    playerAttack = 12;
                    playerDefense = 5;
                    break;
                case "ROGUE":
                    playerMaxHealth = 75;
                    playerAttack = 14;
                    playerDefense = 3;
                    break;
                case "MAGE":
                    playerMaxHealth = 65;
                    playerAttack = 16;
                    playerDefense = 2;
                    break;
                default:
                    playerMaxHealth = 100;
                    playerAttack = 10;
                    playerDefense = 5;
            }
            playerHealth = playerMaxHealth;
            
            // Generate first floor
            roomCount = 6;
            rooms = new RoomType[roomCount];
            visitedRooms = new boolean[roomCount];
            roomsCleared = new boolean[roomCount];
            
            rooms[0] = RoomType.EMPTY;
            visitedRooms[0] = true;
            rooms[1] = RoomType.COMBAT;
            rooms[2] = RoomType.TREASURE;
            rooms[3] = RoomType.REST;
            rooms[4] = RoomType.COMBAT;
            rooms[5] = RoomType.BOSS;
        }
    }
}
