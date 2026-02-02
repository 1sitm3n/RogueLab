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
import com.roguelab.combat.CombatResult;
import com.roguelab.domain.*;
import com.roguelab.dungeon.Dungeon;
import com.roguelab.dungeon.DungeonConfig;
import com.roguelab.dungeon.Floor;
import com.roguelab.game.GameSession;
import com.roguelab.game.GameSessionListener;
import com.roguelab.game.GameState;
import com.roguelab.gdx.Assets;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.effect.EffectsManager;
import com.roguelab.telemetry.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game screen that integrates with the real domain model.
 * Uses GameSession to manage game state and CombatEngine for battles.
 */
public class IntegratedGameScreen implements Screen {

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout layout;

    // Real game objects
    private final GameSession session;
    private final Player player;
    private final Dungeon dungeon;

    // Effects
    private final EffectsManager effects;

    // Screen shake
    private float shakeTime = 0;
    private float shakeIntensity = 0;
    private final Vector2 shakeOffset = new Vector2();

    // Message log
    private final List<String> messages = new ArrayList<>();

    // Animation timers
    private float animTimer = 0;

    // Combat state (for visual display)
    private Enemy currentEnemy = null;
    private boolean awaitingCombatInput = false;

    // Telemetry
    private TelemetryWriter telemetryWriter;

    // Room display constants
    private static final int ROOM_SIZE = 80;
    private static final int ROOM_SPACING = 100;

    public IntegratedGameScreen(RogueLabGame game, PlayerClass playerClass) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.layout = new GlyphLayout();
        this.effects = new EffectsManager(game);

        // Create game session with random seed
        long seed = System.currentTimeMillis();
        this.session = new GameSession(
            playerClass.getDisplayName(),
            playerClass,
            seed,
            Difficulty.NORMAL,
            DungeonConfig.standard()
        );

        this.player = session.getPlayer();
        this.dungeon = session.getDungeon();

        // Setup telemetry
        setupTelemetry();

        // Setup session listener for messages
        session.setListener(new GameScreenListener());

        // Start the game
        session.start();
        
        // Check if first room has combat
        checkForCombat();

        addMessage("Welcome, " + player.getName() + "!");
        addMessage("Seed: " + seed);
    }

    private void setupTelemetry() {
        try {
            java.nio.file.Files.createDirectories(Path.of("runs"));
            Path outputFile = Path.of("runs", session.getRunId() + ".jsonl");
            telemetryWriter = new TelemetryWriter(outputFile, session.getRunId(), true);
            
            // Create telemetry listeners
            SimpleTelemetrySessionListener sessionListener = new SimpleTelemetrySessionListener(telemetryWriter);
            SimpleTelemetryCombatListener combatListener = new SimpleTelemetryCombatListener(telemetryWriter);
            
            session.setListener(new CompositeSessionListener(sessionListener, new GameScreenListener()));
            session.setCombatListener(combatListener);
        } catch (Exception e) {
            Gdx.app.error("Telemetry", "Failed to setup telemetry: " + e.getMessage());
        }
    }

    @Override
    public void show() {
        Gdx.app.log("IntegratedGameScreen", "Starting game with " + player.getPlayerClass());
    }

    @Override
    public void render(float delta) {
        // Update
        update(delta);
        updateShake(delta);

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply screen shake
        batch.getProjectionMatrix().translate(shakeOffset.x, shakeOffset.y, 0);

        // Render based on game state
        GameState state = session.getState();
        switch (state) {
            case IN_COMBAT -> renderCombat(delta);
            case IN_SHOP -> renderShop(delta);
            case AT_REST -> renderRest(delta);
            default -> renderExploration(delta);
        }

        // Reset shake for UI
        batch.getProjectionMatrix().translate(-shakeOffset.x, -shakeOffset.y, 0);

        // Render UI overlay
        renderUI(delta);

        // Render effects
        effects.render(batch, delta);
    }

    private void update(float delta) {
        animTimer += delta;
        effects.update(delta);

        // Check for game end conditions
        if (!session.isActive()) {
            boolean victory = session.getState() == GameState.RUN_ENDED && 
                              player.isAlive() && 
                              dungeon.isOnFinalFloor();
            game.gameOver(victory, player.getInventory().getGold(), dungeon.getCurrentFloorNumber());
            return;
        }

        // Handle input based on state
        GameState state = session.getState();
        switch (state) {
            case EXPLORING -> handleExplorationInput();
            case IN_COMBAT -> handleCombatInput();
            case IN_SHOP -> handleShopInput();
            case AT_REST -> handleRestInput();
        }

        // ESC to quit
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            session.endRun(GameSessionListener.RunEndReason.ABANDONED);
            game.returnToMenu();
        }
    }

    // === INPUT HANDLERS ===

    private void handleExplorationInput() {
        Floor floor = dungeon.getCurrentFloor();

        // Move forward
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (floor.hasNextRoom()) {
                session.advanceRoom();
                checkForCombat();
            }
        }

        // Move backward
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (floor.hasPreviousRoom()) {
                session.returnRoom();
            }
        }

        // Interact with room
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            RoomType type = room.getType();
            
            if (type == RoomType.REST && !room.isCleared()) {
                int healed = session.rest();
                addMessage("Rested and recovered " + healed + " HP!");
                effects.addDamageNumber(
                    Gdx.graphics.getWidth() / 2f,
                    Gdx.graphics.getHeight() / 2f,
                    "+" + healed + " HP",
                    Color.GREEN
                );
                session.leaveRest();
                room.markCleared();
            } else if (floor.isAtExit()) {
                // At the last room - try to descend or win
                if (dungeon.canDescend()) {
                    session.descendFloor();
                    addMessage("Descended to floor " + dungeon.getCurrentFloorNumber());
                    triggerShake(0.3f, 5f);
                    checkForCombat();
                } else if (dungeon.isOnFinalFloor() && floor.allCombatRoomsCleared()) {
                    // Victory!
                    addMessage("Victory! You conquered the dungeon!");
                    session.endRun(GameSessionListener.RunEndReason.VICTORY);
                } else {
                    addMessage("Clear all combat rooms first!");
                }
            }
        }
    }

    private void checkForCombat() {
        Room room = session.getCurrentRoom();
        if (room.hasAliveEnemies() && !room.isCleared()) {
            // Find first alive enemy
            for (Enemy enemy : room.getEnemies()) {
                if (enemy.isAlive()) {
                    currentEnemy = enemy;
                    awaitingCombatInput = true;
                    addMessage("Combat started with " + enemy.getName() + "!");
                    break;
                }
            }
        }
    }

    private void handleCombatInput() {
        if (awaitingCombatInput) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                // Execute combat
                CombatResult result = session.executeCombat();

                // Show effects
                float enemyX = Gdx.graphics.getWidth() * 0.7f;
                float enemyY = Gdx.graphics.getHeight() * 0.5f;
                float playerX = Gdx.graphics.getWidth() * 0.3f;
                float playerY = Gdx.graphics.getHeight() * 0.5f;

                effects.addDamageNumber(enemyX, enemyY + 50, 
                    "-" + result.totalDamageDealt(), Color.WHITE);
                effects.addSlash(enemyX, enemyY);

                if (result.totalDamageTaken() > 0) {
                    effects.addDamageNumber(playerX, playerY + 50,
                        "-" + result.totalDamageTaken(), Color.RED);
                    effects.addHit(playerX, playerY);
                }

                triggerShake(0.15f, 5f);

                if (result.isVictory()) {
                    addMessage("Victory! Gained " + result.goldEarned() + " gold!");
                    effects.addDamageNumber(
                        Gdx.graphics.getWidth() / 2f,
                        Gdx.graphics.getHeight() / 2f + 100,
                        "+" + result.goldEarned() + " gold",
                        Color.GOLD
                    );
                    awaitingCombatInput = false;
                    currentEnemy = null;
                } else {
                    addMessage("You were defeated...");
                }
            }
        }
    }

    private void handleShopInput() {
        Room room = session.getCurrentRoom();
        List<Item> items = room.getItems();

        // Number keys to buy items
        for (int i = 0; i < Math.min(items.size(), 9); i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                Item item = items.get(i);
                if (session.purchaseItem(item)) {
                    addMessage("Purchased " + item.getName() + "!");
                } else {
                    addMessage("Not enough gold!");
                }
            }
        }

        // Leave shop
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            session.leaveShop();
        }
    }

    private void handleRestInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            if (!room.isCleared()) {
                int healed = session.rest();
                addMessage("Rested and recovered " + healed + " HP!");
                effects.addDamageNumber(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, "+" + healed + " HP", Color.GREEN);
                room.markCleared();
            }
            session.leaveRest();
        }
    }

    // === RENDERING ===

    private void renderExploration(float delta) {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        Floor floor = dungeon.getCurrentFloor();
        List<Room> rooms = floor.getRooms();
        int currentRoomIndex = floor.getCurrentRoomIndex();

        // Calculate map offset
        float mapWidth = rooms.size() * ROOM_SPACING;
        float mapStartX = centerX - mapWidth / 2f + ROOM_SPACING / 2f;
        float mapY = centerY + 50;

        // Draw connections
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
        for (int i = 0; i < rooms.size() - 1; i++) {
            float x1 = mapStartX + i * ROOM_SPACING;
            float x2 = mapStartX + (i + 1) * ROOM_SPACING;
            shapeRenderer.rectLine(x1, mapY, x2, mapY, 4);
        }
        shapeRenderer.end();

        // Draw rooms
        batch.begin();

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            float roomX = mapStartX + i * ROOM_SPACING - ROOM_SIZE / 2f;
            float roomY = mapY - ROOM_SIZE / 2f;

            boolean visited = room.isVisited();
            boolean current = (i == currentRoomIndex);
            boolean cleared = room.isCleared();

            // Get room tile
            TextureRegion tile = getRoomTile(room.getType(), visited);

            // Draw room
            if (visited || current) {
                batch.setColor(current ? Color.WHITE : new Color(0.7f, 0.7f, 0.7f, 1f));
            } else {
                batch.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
            }
            batch.draw(tile, roomX, roomY, ROOM_SIZE, ROOM_SIZE);

            // Draw cleared marker
            if (cleared) {
                batch.setColor(new Color(0.2f, 0.8f, 0.2f, 0.3f));
                batch.draw(game.getAssets().getWhitePixel(), roomX, roomY, ROOM_SIZE, ROOM_SIZE);
            }

            // Draw player on current room
            if (current) {
                TextureRegion playerSprite = game.getAssets().getPlayerSprite(
                    player.getPlayerClass().name()
                );
                batch.setColor(Color.WHITE);
                batch.draw(playerSprite, roomX + ROOM_SIZE / 2f - 16, roomY + ROOM_SIZE / 2f - 16, 32, 32);
            }

            batch.setColor(Color.WHITE);
        }

        // Floor indicator
        BitmapFont normalFont = game.getAssets().getNormalFont();
        normalFont.setColor(Color.WHITE);
        String floorText = "FLOOR " + dungeon.getCurrentFloorNumber() + "/" + dungeon.getMaxFloors();
        layout.setText(normalFont, floorText);
        normalFont.draw(batch, floorText, centerX - layout.width / 2, mapY + ROOM_SIZE / 2f + 60);

        // Room info
        drawRoomInfo(centerY - 120);

        // Controls
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.GRAY);
        String controls = "A/D to move | SPACE to interact | ESC for menu";
        layout.setText(smallFont, controls);
        smallFont.draw(batch, controls, centerX - layout.width / 2, 40);

        batch.end();
    }

    private void renderCombat(float delta) {
        if (currentEnemy == null) return;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerY = screenHeight / 2f;

        float playerX = screenWidth * 0.25f;
        float enemyX = screenWidth * 0.75f;

        // Arena background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.18f, 0.15f, 1f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight * 0.35f);
        shapeRenderer.setColor(0.15f, 0.13f, 0.12f, 1f);
        shapeRenderer.ellipse(screenWidth * 0.1f, screenHeight * 0.1f, 
            screenWidth * 0.8f, screenHeight * 0.3f);
        shapeRenderer.end();

        batch.begin();

        // Draw player
        float bobOffset = MathUtils.sin(animTimer * 3) * 5;
        TextureRegion playerSprite = game.getAssets().getPlayerSprite(player.getPlayerClass().name());
        batch.draw(playerSprite, playerX - 64, centerY - 64 + bobOffset, 128, 128);

        // Draw enemy
        float enemyBob = MathUtils.sin(animTimer * 2.5f) * 4;
        TextureRegion enemySprite = game.getAssets().getEnemySprite(currentEnemy.getType().name());
        int enemySize = currentEnemy.getType().isBoss() ? 180 : 128;
        batch.draw(enemySprite, enemyX - enemySize / 2f, centerY - enemySize / 2f + enemyBob, 
            enemySize, enemySize);

        batch.end();

        // Health bars
        drawHealthBar(playerX, centerY - 100, 
            player.getHealth().getCurrent(), player.getHealth().getMaximum(),
            Color.GREEN, player.getName());
        drawHealthBar(enemyX, centerY - 100,
            currentEnemy.getHealth().getCurrent(), currentEnemy.getHealth().getMaximum(),
            currentEnemy.getType().isBoss() ? Color.ORANGE : Color.RED, 
            currentEnemy.getName());

        batch.begin();

        // Turn indicator
        BitmapFont normalFont = game.getAssets().getNormalFont();
        float alpha = 0.5f + MathUtils.sin(animTimer * 5) * 0.3f;
        normalFont.setColor(new Color(0.3f, 0.9f, 0.3f, alpha));
        layout.setText(normalFont, ">>> PRESS SPACE TO ATTACK <<<");
        normalFont.draw(batch, ">>> PRESS SPACE TO ATTACK <<<", 
            playerX - layout.width / 2f, centerY + 150);

        // Combat title
        BitmapFont titleFont = game.getAssets().getTitleFont();
        titleFont.setColor(Color.RED);
        titleFont.getData().setScale(2f);
        String title = currentEnemy.getType().isBoss() ? "BOSS BATTLE!" : "COMBAT";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, screenWidth / 2f - layout.width / 2f, screenHeight - 30);
        titleFont.getData().setScale(3f);

        batch.end();
    }

    private void renderShop(float delta) {
        batch.begin();
        
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        
        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        titleFont.setColor(Color.GOLD);
        titleFont.getData().setScale(2f);
        layout.setText(titleFont, "SHOP");
        titleFont.draw(batch, "SHOP", centerX - layout.width / 2, centerY + 200);
        titleFont.getData().setScale(3f);
        
        Room room = session.getCurrentRoom();
        List<Item> items = room.getItems();
        
        float y = centerY + 100;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean canAfford = player.getInventory().getGold() >= item.getValue();
            
            normalFont.setColor(canAfford ? Color.WHITE : Color.GRAY);
            String line = (i + 1) + ". " + item.getName() + " - " + item.getValue() + " gold";
            layout.setText(normalFont, line);
            normalFont.draw(batch, line, centerX - layout.width / 2, y);
            y -= 40;
        }
        
        smallFont.setColor(Color.GRAY);
        layout.setText(smallFont, "Press number to buy | SPACE to leave");
        smallFont.draw(batch, "Press number to buy | SPACE to leave", 
            centerX - layout.width / 2, 60);
        
        batch.end();
    }

    private void renderRest(float delta) {
        // Rest is now handled immediately in exploration
        renderExploration(delta);
    }

    private void renderUI(float delta) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Stats panel (top left)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.85f);
        shapeRenderer.rect(10, screenHeight - 150, 220, 140);
        shapeRenderer.end();

        batch.begin();

        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        float textX = 25;
        float textY = screenHeight - 25;

        // Class name
        normalFont.setColor(getClassColor(player.getPlayerClass()));
        normalFont.draw(batch, player.getPlayerClass().getDisplayName(), textX, textY);
        textY -= 35;

        // Health
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, "HP: " + player.getHealth().getCurrent() + "/" + 
            player.getHealth().getMaximum(), textX, textY);
        textY -= 22;

        // Stats
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "ATK: " + player.getEffectiveAttack(), textX, textY);
        textY -= 22;
        smallFont.draw(batch, "DEF: " + player.getEffectiveDefense(), textX, textY);
        textY -= 22;
        smallFont.draw(batch, "Floor: " + dungeon.getCurrentFloorNumber() + "/" + 
            dungeon.getMaxFloors(), textX, textY);

        // Gold (top right)
        normalFont.setColor(Color.GOLD);
        String goldText = "$ " + player.getInventory().getGold();
        layout.setText(normalFont, goldText);
        normalFont.draw(batch, goldText, screenWidth - layout.width - 20, screenHeight - 25);

        batch.end();

        // Message log (bottom)
        renderMessageLog();
    }

    private void renderMessageLog() {
        float logWidth = Gdx.graphics.getWidth() - 20;
        float logHeight = 120;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.8f);
        shapeRenderer.rect(10, 10, logWidth, logHeight);
        shapeRenderer.end();

        batch.begin();

        BitmapFont smallFont = game.getAssets().getSmallFont();
        float textY = logHeight - 5;

        for (int i = messages.size() - 1; i >= 0 && textY > 15; i--) {
            String msg = messages.get(i);
            float alpha = 1f - (messages.size() - 1 - i) * 0.15f;
            alpha = Math.max(0.3f, alpha);

            if (msg.contains("damage") || msg.contains("defeated")) {
                smallFont.setColor(new Color(1f, 0.5f, 0.5f, alpha));
            } else if (msg.contains("Victory") || msg.contains("gold") || msg.contains("HP")) {
                smallFont.setColor(new Color(0.5f, 1f, 0.5f, alpha));
            } else {
                smallFont.setColor(new Color(0.8f, 0.8f, 0.8f, alpha));
            }

            smallFont.draw(batch, "> " + msg, 25, textY);
            textY -= 20;
        }

        batch.end();
    }

    private void drawHealthBar(float x, float y, int current, int max, Color color, String name) {
        float barWidth = 150;
        float barHeight = 16;
        float percent = Math.max(0, (float) current / max);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x - barWidth / 2f, y, barWidth, barHeight);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x - barWidth / 2f, y, barWidth * percent, barHeight);
        shapeRenderer.end();

        batch.begin();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.WHITE);
        String text = current + "/" + max;
        layout.setText(smallFont, text);
        smallFont.draw(batch, text, x - layout.width / 2f, y + barHeight / 2f + layout.height / 2f);

        BitmapFont normalFont = game.getAssets().getNormalFont();
        normalFont.setColor(color);
        layout.setText(normalFont, name);
        normalFont.draw(batch, name, x - layout.width / 2f, y + barHeight + 30);
        batch.end();
    }

    private void drawRoomInfo(float y) {
        float centerX = Gdx.graphics.getWidth() / 2f;
        Room room = session.getCurrentRoom();
        Floor floor = dungeon.getCurrentFloor();

        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        normalFont.setColor(getRoomColor(room.getType()));
        String roomName = getRoomName(room.getType());
        if (room.isCleared()) roomName += " (Cleared)";
        if (floor.isAtExit()) roomName = "Exit - " + roomName;
        layout.setText(normalFont, roomName);
        normalFont.draw(batch, roomName, centerX - layout.width / 2, y);

        smallFont.setColor(Color.LIGHT_GRAY);
        String desc = getRoomDescription(room, floor);
        layout.setText(smallFont, desc);
        smallFont.draw(batch, desc, centerX - layout.width / 2, y - 35);
    }

    // === HELPERS ===

    private TextureRegion getRoomTile(RoomType type, boolean visited) {
        if (!visited) return game.getAssets().getTile("fog");

        return switch (type) {
            case COMBAT -> game.getAssets().getTile("combat");
            case BOSS -> game.getAssets().getTile("boss");
            case TREASURE -> game.getAssets().getTile("chest");
            case SHOP -> game.getAssets().getTile("shop");
            case REST -> game.getAssets().getTile("rest");
            case EVENT -> game.getAssets().getTile("floor");
        };
    }

    private Color getRoomColor(RoomType type) {
        return switch (type) {
            case COMBAT -> new Color(0.9f, 0.3f, 0.3f, 1f);
            case BOSS -> new Color(1f, 0.5f, 0.1f, 1f);
            case TREASURE -> new Color(1f, 0.85f, 0.2f, 1f);
            case SHOP -> new Color(0.3f, 0.8f, 0.9f, 1f);
            case REST -> new Color(0.3f, 0.9f, 0.4f, 1f);
            case EVENT -> Color.LIGHT_GRAY;
        };
    }

    private String getRoomName(RoomType type) {
        return switch (type) {
            case COMBAT -> "Monster Den";
            case BOSS -> "Boss Chamber";
            case TREASURE -> "Treasure Room";
            case SHOP -> "Merchant";
            case REST -> "Rest Site";
            case EVENT -> "Strange Room";
        };
    }

    private String getRoomDescription(Room room, Floor floor) {
        if (room.isCleared()) return "Nothing left here.";

        // Check if this is the exit room
        if (floor.isAtExit()) {
            if (dungeon.isOnFinalFloor()) {
                return floor.allCombatRoomsCleared() ? 
                    "Victory awaits! Press SPACE!" : "Clear all rooms first!";
            } else {
                return dungeon.canDescend() ? 
                    "Press SPACE to descend." : "Clear all combat rooms first!";
            }
        }

        return switch (room.getType()) {
            case COMBAT -> "Enemies: " + room.getEnemies().size();
            case BOSS -> "A powerful foe awaits!";
            case TREASURE -> "Press SPACE to open.";
            case SHOP -> "Browse the wares.";
            case REST -> "Press SPACE to rest (heal 30%).";
            case EVENT -> "Something strange...";
        };
    }

    private Color getClassColor(PlayerClass pc) {
        return switch (pc) {
            case WARRIOR -> new Color(0.3f, 0.5f, 0.9f, 1f);
            case ROGUE -> new Color(0.4f, 0.7f, 0.4f, 1f);
            case MAGE -> new Color(0.7f, 0.3f, 0.7f, 1f);
        };
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

    private void addMessage(String msg) {
        messages.add(msg);
        if (messages.size() > 6) messages.remove(0);
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
    public void dispose() {
        if (telemetryWriter != null) {
            try { telemetryWriter.close(); } catch (Exception e) { /* ignore */ }
        }
    }

    // === INNER CLASSES ===

    /**
     * Listener for game events to update UI messages.
     */
    private class GameScreenListener implements GameSessionListener {
        @Override
        public void onRunStarted(GameSession session) {
            addMessage("Run started!");
        }

        @Override
        public void onFloorEntered(GameSession session, Floor floor) {
            addMessage("Entered floor " + floor.getFloorNumber());
        }

        @Override
        public void onRoomEntered(GameSession session, Room room) {
            if (room.getType() == RoomType.COMBAT && room.hasAliveEnemies()) {
                addMessage("Enemies ahead!");
            }
        }

        @Override
        public void onRoomCleared(GameSession session, Room room) {
            addMessage("Room cleared!");
        }

        @Override
        public void onCombatCompleted(GameSession session, CombatResult result) {
            if (result.isVictory()) {
                addMessage("Combat won! +" + result.goldEarned() + " gold");
            }
        }

        @Override
        public void onItemPicked(GameSession session, Item item) {
            addMessage("Found: " + item.getName());
        }

        @Override
        public void onItemUsed(GameSession session, Item item) {
            addMessage("Used: " + item.getName());
        }

        @Override
        public void onShopPurchase(GameSession session, Item item, int cost) {
            addMessage("Bought: " + item.getName());
        }

        @Override
        public void onPlayerRested(GameSession session, int healed) {
            addMessage("Rested, healed " + healed + " HP");
        }

        @Override
        public void onPlayerLevelUp(GameSession session, int newLevel) {
            addMessage("Level up! Now level " + newLevel);
        }

        @Override
        public void onRunEnded(GameSession session, RunEndReason reason) {
            addMessage("Run ended: " + reason);
        }
    }

    /**
     * Combines multiple listeners.
     */
    private static class CompositeSessionListener implements GameSessionListener {
        private final GameSessionListener[] listeners;

        public CompositeSessionListener(GameSessionListener... listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onRunStarted(GameSession s) {
            for (var l : listeners) l.onRunStarted(s);
        }

        @Override
        public void onFloorEntered(GameSession s, Floor f) {
            for (var l : listeners) l.onFloorEntered(s, f);
        }

        @Override
        public void onRoomEntered(GameSession s, Room r) {
            for (var l : listeners) l.onRoomEntered(s, r);
        }

        @Override
        public void onRoomCleared(GameSession s, Room r) {
            for (var l : listeners) l.onRoomCleared(s, r);
        }

        @Override
        public void onCombatCompleted(GameSession s, CombatResult r) {
            for (var l : listeners) l.onCombatCompleted(s, r);
        }

        @Override
        public void onItemPicked(GameSession s, Item i) {
            for (var l : listeners) l.onItemPicked(s, i);
        }

        @Override
        public void onItemUsed(GameSession s, Item i) {
            for (var l : listeners) l.onItemUsed(s, i);
        }

        @Override
        public void onShopPurchase(GameSession s, Item i, int c) {
            for (var l : listeners) l.onShopPurchase(s, i, c);
        }

        @Override
        public void onPlayerRested(GameSession s, int h) {
            for (var l : listeners) l.onPlayerRested(s, h);
        }

        @Override
        public void onPlayerLevelUp(GameSession s, int newLevel) {
            for (var l : listeners) l.onPlayerLevelUp(s, newLevel);
        }

        @Override
        public void onRunEnded(GameSession s, RunEndReason r) {
            for (var l : listeners) l.onRunEnded(s, r);
        }
    }
}


