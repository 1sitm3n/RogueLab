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
 * Daggerfall-style game screen with classic dungeon crawler aesthetics.
 * Features stone frame UI, character portrait, gothic styling.
 */
public class IntegratedGameScreen implements Screen {

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout layout;

    // Game objects
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

    // Animation
    private float animTimer = 0;
    private float displayedPlayerHealth;
    private float displayedEnemyHealth;

    // Combat state
    private Enemy currentEnemy = null;
    private boolean awaitingCombatInput = false;
    private int combatTurn = 0;

    // Telemetry
    private TelemetryWriter telemetryWriter;

    // Daggerfall UI colors
    private static final Color STONE_DARK = Assets.STONE_DARK;
    private static final Color STONE_MID = Assets.STONE_MID;
    private static final Color STONE_LIGHT = Assets.STONE_LIGHT;
    private static final Color PARCHMENT = Assets.PARCHMENT_MID;
    private static final Color GOLD = Assets.GOLD_MID;
    private static final Color BLOOD = Assets.BLOOD_RED;
    private static final Color TORCH = Assets.TORCH_ORANGE;

    // UI Layout
    private static final int PORTRAIT_SIZE = 80;
    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 20;
    private static final int FRAME_BORDER = 8;

    public IntegratedGameScreen(RogueLabGame game, PlayerClass playerClass) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.layout = new GlyphLayout();
        this.effects = new EffectsManager(game);

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
        this.displayedPlayerHealth = player.getHealth().getCurrent();

        setupTelemetry();
        session.setListener(new GameScreenListener());
        session.start();
        checkForCombat();

        addMessage("You enter the dungeon...");
        addMessage("Seed: " + seed);
    }

    private void setupTelemetry() {
        try {
            java.nio.file.Files.createDirectories(Path.of("runs"));
            Path outputFile = Path.of("runs", session.getRunId() + ".jsonl");
            telemetryWriter = new TelemetryWriter(outputFile, session.getRunId(), true);
            
            SimpleTelemetrySessionListener sessionListener = new SimpleTelemetrySessionListener(telemetryWriter);
            SimpleTelemetryCombatListener combatListener = new SimpleTelemetryCombatListener(telemetryWriter);
            
            session.setListener(new CompositeSessionListener(sessionListener, new GameScreenListener()));
            session.setCombatListener(combatListener);
        } catch (Exception e) {
            Gdx.app.error("Telemetry", "Failed to setup telemetry: " + e.getMessage());
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        update(delta);
        updateShake(delta);
        updateAnimatedHealth(delta);

        // Dark dungeon background
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.getProjectionMatrix().translate(shakeOffset.x, shakeOffset.y, 0);

        GameState state = session.getState();
        switch (state) {
            case IN_COMBAT -> renderCombat(delta);
            case IN_SHOP -> renderShop(delta);
            case AT_REST -> renderRest(delta);
            default -> renderExploration(delta);
        }

        batch.getProjectionMatrix().translate(-shakeOffset.x, -shakeOffset.y, 0);

        // Daggerfall-style UI frame (always on top)
        renderDaggerfallUI(delta);
        
        effects.render(batch, delta);
    }

    private void updateAnimatedHealth(float delta) {
        float targetPlayerHealth = player.getHealth().getCurrent();
        displayedPlayerHealth = MathUtils.lerp(displayedPlayerHealth, targetPlayerHealth, 5f * delta);
        
        if (currentEnemy != null) {
            float targetEnemyHealth = currentEnemy.getHealth().getCurrent();
            displayedEnemyHealth = MathUtils.lerp(displayedEnemyHealth, targetEnemyHealth, 5f * delta);
        }
    }

    private void update(float delta) {
        animTimer += delta;
        effects.update(delta);

        if (!session.isActive()) {
            boolean victory = session.getState() == GameState.RUN_ENDED && 
                              player.isAlive() && 
                              dungeon.isOnFinalFloor();
            game.gameOver(victory, player.getInventory().getGold(), dungeon.getCurrentFloorNumber());
            return;
        }

        GameState state = session.getState();
        switch (state) {
            case EXPLORING -> handleExplorationInput();
            case IN_COMBAT -> handleCombatInput();
            case IN_SHOP -> handleShopInput();
            case AT_REST -> handleRestInput();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            session.endRun(GameSessionListener.RunEndReason.ABANDONED);
            game.returnToMenu();
        }
    }

    // === INPUT HANDLERS ===

    private void handleExplorationInput() {
        Floor floor = dungeon.getCurrentFloor();

        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (floor.hasNextRoom()) {
                session.advanceRoom();
                addMessage("You move deeper into the dungeon...");
                checkForCombat();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (floor.hasPreviousRoom()) {
                session.returnRoom();
                addMessage("You retrace your steps...");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            RoomType type = room.getType();
            
            if (type == RoomType.REST && !room.isCleared()) {
                int healed = session.rest();
                addMessage("You rest by the fire and recover " + healed + " health.");
                effects.addDamageNumber(
                    Gdx.graphics.getWidth() / 2f,
                    Gdx.graphics.getHeight() / 2f,
                    "+" + healed,
                    Color.GREEN
                );
                session.leaveRest();
                room.markCleared();
            } else if (floor.isAtExit()) {
                if (dungeon.canDescend()) {
                    session.descendFloor();
                    addMessage("You descend to level " + dungeon.getCurrentFloorNumber() + "...");
                    triggerShake(0.4f, 6f);
                    checkForCombat();
                } else if (dungeon.isOnFinalFloor() && floor.allCombatRoomsCleared()) {
                    addMessage("VICTORY! You have conquered the dungeon!");
                    session.endRun(GameSessionListener.RunEndReason.VICTORY);
                } else {
                    addMessage("You must clear all rooms before descending.");
                }
            }
        }
    }

    private void checkForCombat() {
        Room room = session.getCurrentRoom();
        if (room.hasAliveEnemies() && !room.isCleared()) {
            for (Enemy enemy : room.getEnemies()) {
                if (enemy.isAlive()) {
                    currentEnemy = enemy;
                    displayedEnemyHealth = enemy.getHealth().getCurrent();
                    awaitingCombatInput = true;
                    combatTurn = 1;
                    addMessage("A " + enemy.getName() + " blocks your path!");
                    break;
                }
            }
        }
    }

    private void handleCombatInput() {
        if (awaitingCombatInput) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                CombatResult result = session.executeCombat();

                float enemyX = Gdx.graphics.getWidth() / 2f;
                float enemyY = Gdx.graphics.getHeight() / 2f;

                effects.addDamageNumber(enemyX + 50, enemyY, "-" + result.totalDamageDealt(), Color.WHITE);
                effects.addSlash(enemyX, enemyY);

                if (result.totalDamageTaken() > 0) {
                    effects.addDamageNumber(enemyX - 50, enemyY - 50, "-" + result.totalDamageTaken(), Color.RED);
                    addMessage("The " + currentEnemy.getName() + " strikes you for " + result.totalDamageTaken() + " damage!");
                }

                triggerShake(0.2f, 8f);
                combatTurn++;

                if (result.isVictory()) {
                    addMessage("The " + currentEnemy.getName() + " is slain! You find " + result.goldEarned() + " gold.");
                    effects.addDamageNumber(enemyX, enemyY + 50, "+" + result.goldEarned() + " GOLD", GOLD);
                    awaitingCombatInput = false;
                    currentEnemy = null;
                } else if (player.isDead()) {
                    addMessage("You have been slain...");
                }
            }
        }
    }

    private void handleShopInput() {
        Room room = session.getCurrentRoom();
        List<Item> items = room.getItems();

        for (int i = 0; i < Math.min(items.size(), 9); i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                Item item = items.get(i);
                if (session.purchaseItem(item)) {
                    addMessage("You purchase " + item.getName() + " for " + item.getValue() + " gold.");
                } else {
                    addMessage("You cannot afford that.");
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || 
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            session.leaveShop();
            addMessage("You leave the merchant.");
        }
    }

    private void handleRestInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            if (!room.isCleared()) {
                int healed = session.rest();
                addMessage("You rest and recover " + healed + " health.");
                effects.addDamageNumber(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, "+" + healed, Color.GREEN);
                room.markCleared();
            }
            session.leaveRest();
        }
    }

    // === RENDERING ===

    private void renderExploration(float delta) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Main viewport area (inside UI frame)
        float viewX = 100;
        float viewY = 140;
        float viewW = screenWidth - 200;
        float viewH = screenHeight - 280;

        // Draw dungeon corridor view (Daggerfall-style)
        renderDungeonCorridor(viewX, viewY, viewW, viewH, delta);
        
        // Room minimap in center-bottom
        renderRoomMinimap(screenWidth / 2f, viewY + 30, delta);
    }

    private void renderDungeonCorridor(float x, float y, float w, float h, float delta) {
        Room room = session.getCurrentRoom();
        Floor floor = dungeon.getCurrentFloor();
        
        // Draw corridor walls (perspective)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Ceiling
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(x, y + h * 0.6f, w, h * 0.4f);
        
        // Floor
        shapeRenderer.setColor(STONE_MID.r * 0.8f, STONE_MID.g * 0.8f, STONE_MID.b * 0.8f, 1f);
        shapeRenderer.rect(x, y, w, h * 0.4f);
        
        // Left wall with perspective
        float perspDepth = 0.3f;
        shapeRenderer.setColor(STONE_MID.r * 0.6f, STONE_MID.g * 0.6f, STONE_MID.b * 0.6f, 1f);
        shapeRenderer.triangle(
            x, y,
            x, y + h,
            x + w * perspDepth, y + h * (1 - perspDepth)
        );
        shapeRenderer.triangle(
            x, y,
            x + w * perspDepth, y + h * perspDepth,
            x + w * perspDepth, y + h * (1 - perspDepth)
        );
        
        // Right wall
        shapeRenderer.setColor(STONE_MID.r * 0.5f, STONE_MID.g * 0.5f, STONE_MID.b * 0.5f, 1f);
        shapeRenderer.triangle(
            x + w, y,
            x + w, y + h,
            x + w * (1 - perspDepth), y + h * (1 - perspDepth)
        );
        shapeRenderer.triangle(
            x + w, y,
            x + w * (1 - perspDepth), y + h * perspDepth,
            x + w * (1 - perspDepth), y + h * (1 - perspDepth)
        );
        
        // Back wall
        float backX = x + w * perspDepth;
        float backY = y + h * perspDepth;
        float backW = w * (1 - 2 * perspDepth);
        float backH = h * (1 - 2 * perspDepth);
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(backX, backY, backW, backH);
        
        shapeRenderer.end();

        // Draw room content
        batch.begin();
        
        float centerX = x + w / 2f;
        float centerY = y + h / 2f;
        
        // Room type icon/content
        TextureRegion roomTile = game.getAssets().getTile(getRoomTileKey(room.getType()));
        float iconSize = Math.min(backW, backH) * 0.6f;
        batch.draw(roomTile, centerX - iconSize / 2f, centerY - iconSize / 2f, iconSize, iconSize);

        // Torchlight flicker effect
        float flicker = 0.9f + MathUtils.sin(animTimer * 8) * 0.1f;
        batch.setColor(TORCH.r * flicker, TORCH.g * flicker, TORCH.b * 0.5f, 0.15f);
        batch.draw(game.getAssets().getWhitePixel(), x, y, w, h);
        batch.setColor(Color.WHITE);

        BitmapFont font = game.getAssets().getNormalFont();
        
        // Room name
        font.setColor(PARCHMENT);
        String roomName = getRoomDisplayName(room.getType());
        layout.setText(font, roomName);
        font.draw(batch, roomName, centerX - layout.width / 2f, y + h - 20);
        
        // Room status
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(room.isCleared() ? Color.GREEN : Color.LIGHT_GRAY);
        String status = room.isCleared() ? "[CLEARED]" : getRoomStatus(room, floor);
        layout.setText(smallFont, status);
        smallFont.draw(batch, status, centerX - layout.width / 2f, y + 40);

        batch.end();
    }

    private void renderRoomMinimap(float centerX, float y, float delta) {
        Floor floor = dungeon.getCurrentFloor();
        List<Room> rooms = floor.getRooms();
        int currentIdx = floor.getCurrentRoomIndex();
        
        int roomSize = 24;
        int spacing = 32;
        float mapWidth = rooms.size() * spacing;
        float startX = centerX - mapWidth / 2f + spacing / 2f;
        
        // Draw connections
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(STONE_MID);
        for (int i = 0; i < rooms.size() - 1; i++) {
            float x1 = startX + i * spacing;
            float x2 = startX + (i + 1) * spacing;
            shapeRenderer.rectLine(x1, y, x2, y, 2);
        }
        shapeRenderer.end();
        
        batch.begin();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            float rx = startX + i * spacing - roomSize / 2f;
            float ry = y - roomSize / 2f;
            
            boolean current = (i == currentIdx);
            boolean visited = room.isVisited();
            
            // Room tile
            TextureRegion tile = game.getAssets().getTile(visited ? getRoomTileKey(room.getType()) : "fog");
            
            if (current) {
                // Highlight current room
                batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.5f + MathUtils.sin(animTimer * 4) * 0.3f);
                batch.draw(game.getAssets().getWhitePixel(), rx - 4, ry - 4, roomSize + 8, roomSize + 8);
            }
            
            batch.setColor(visited || current ? Color.WHITE : new Color(0.3f, 0.3f, 0.3f, 1f));
            batch.draw(tile, rx, ry, roomSize, roomSize);
            
            // Cleared checkmark
            if (room.isCleared()) {
                batch.setColor(0.3f, 0.8f, 0.3f, 0.7f);
                batch.draw(game.getAssets().getWhitePixel(), rx, ry, roomSize, roomSize);
            }
            
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void renderCombat(float delta) {
        if (currentEnemy == null) return;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        float viewX = 100;
        float viewY = 140;
        float viewW = screenWidth - 200;
        float viewH = screenHeight - 280;
        
        // Dark combat arena
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.05f, 0.03f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
        
        // Blood-red vignette for boss
        if (currentEnemy.getType().isBoss()) {
            for (int i = 0; i < 5; i++) {
                float alpha = 0.1f - i * 0.02f;
                shapeRenderer.setColor(0.5f, 0.1f, 0.05f, alpha);
                shapeRenderer.rect(viewX + i * 20, viewY + i * 20, viewW - i * 40, viewH - i * 40);
            }
        }
        shapeRenderer.end();

        batch.begin();
        
        float centerX = viewX + viewW / 2f;
        float centerY = viewY + viewH / 2f;
        
        // Enemy sprite (large, centered, Daggerfall-style)
        float bob = MathUtils.sin(animTimer * 2) * 8;
        TextureRegion enemySprite = game.getAssets().getEnemySprite(currentEnemy.getType().name());
        int spriteSize = currentEnemy.getType().isBoss() ? 200 : 160;
        
        // Damage flash
        float healthPct = (float) currentEnemy.getHealth().getCurrent() / currentEnemy.getHealth().getMaximum();
        if (healthPct < 0.3f) {
            float flash = MathUtils.sin(animTimer * 10) * 0.4f + 0.6f;
            batch.setColor(1f, flash, flash, 1f);
        }
        
        batch.draw(enemySprite, centerX - spriteSize / 2f, centerY - spriteSize / 2f + bob, spriteSize, spriteSize);
        batch.setColor(Color.WHITE);
        
        // Enemy name plate
        BitmapFont font = game.getAssets().getNormalFont();
        font.setColor(currentEnemy.getType().isBoss() ? TORCH : PARCHMENT);
        String enemyName = currentEnemy.getName().toUpperCase();
        layout.setText(font, enemyName);
        font.draw(batch, enemyName, centerX - layout.width / 2f, viewY + viewH - 20);
        
        // Enemy health bar (under name)
        batch.end();
        drawHealthBar(centerX - 100, viewY + viewH - 55, 200, 18, 
            displayedEnemyHealth, currentEnemy.getHealth().getMaximum(),
            currentEnemy.getType().isBoss() ? TORCH : BLOOD);
        batch.begin();
        
        // Combat turn indicator
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(STONE_LIGHT);
        String turnText = "Turn " + combatTurn;
        layout.setText(smallFont, turnText);
        smallFont.draw(batch, turnText, centerX - layout.width / 2f, viewY + 30);
        
        // Attack prompt (pulsing)
        float pulse = 0.6f + MathUtils.sin(animTimer * 5) * 0.4f;
        font.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
        String prompt = "[ PRESS SPACE TO ATTACK ]";
        layout.setText(font, prompt);
        font.draw(batch, prompt, centerX - layout.width / 2f, viewY + 60);
        
        batch.end();
    }

    private void renderShop(float delta) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        float viewX = 100;
        float viewY = 140;
        float viewW = screenWidth - 200;
        float viewH = screenHeight - 280;

        // Parchment background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(PARCHMENT.r * 0.7f, PARCHMENT.g * 0.7f, PARCHMENT.b * 0.7f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
        
        // Darker border
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(viewX, viewY, viewW, 4);
        shapeRenderer.rect(viewX, viewY + viewH - 4, viewW, 4);
        shapeRenderer.rect(viewX, viewY, 4, viewH);
        shapeRenderer.rect(viewX + viewW - 4, viewY, 4, viewH);
        shapeRenderer.end();

        batch.begin();

        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        float centerX = viewX + viewW / 2f;
        
        // Title
        titleFont.setColor(STONE_DARK);
        titleFont.getData().setScale(2f);
        layout.setText(titleFont, "MERCHANT");
        titleFont.draw(batch, "MERCHANT", centerX - layout.width / 2f, viewY + viewH - 20);
        titleFont.getData().setScale(3f);

        // Items
        Room room = session.getCurrentRoom();
        List<Item> items = room.getItems();
        
        float itemY = viewY + viewH - 80;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean canAfford = player.getInventory().getGold() >= item.getValue();
            
            // Item number
            font.setColor(canAfford ? STONE_DARK : STONE_LIGHT);
            font.draw(batch, "[" + (i + 1) + "]", viewX + 30, itemY);
            
            // Item name
            font.draw(batch, item.getName(), viewX + 80, itemY);
            
            // Stats
            smallFont.setColor(canAfford ? new Color(0.2f, 0.4f, 0.2f, 1f) : STONE_LIGHT);
            String stats = "";
            if (item.getAttackBonus() > 0) stats += "+" + item.getAttackBonus() + " ATK ";
            if (item.getDefenseBonus() > 0) stats += "+" + item.getDefenseBonus() + " DEF ";
            if (item.getHealthBonus() > 0) stats += "+" + item.getHealthBonus() + " HP";
            smallFont.draw(batch, stats, viewX + 280, itemY);
            
            // Price
            font.setColor(canAfford ? GOLD : BLOOD);
            layout.setText(font, item.getValue() + " gold");
            font.draw(batch, item.getValue() + " gold", viewX + viewW - layout.width - 40, itemY);
            
            itemY -= 40;
        }

        // Your gold
        font.setColor(GOLD);
        String goldText = "Your Gold: " + player.getInventory().getGold();
        layout.setText(font, goldText);
        font.draw(batch, goldText, centerX - layout.width / 2f, viewY + 50);

        // Instructions
        smallFont.setColor(STONE_MID);
        layout.setText(smallFont, "[1-9] Purchase   [SPACE] Leave");
        smallFont.draw(batch, "[1-9] Purchase   [SPACE] Leave", centerX - layout.width / 2f, viewY + 25);

        batch.end();
    }

    private void renderRest(float delta) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        float viewX = 100;
        float viewY = 140;
        float viewW = screenWidth - 200;
        float viewH = screenHeight - 280;

        // Dark background with fire glow
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.08f, 0.05f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
        
        // Fire glow
        float glowPulse = 0.8f + MathUtils.sin(animTimer * 3) * 0.2f;
        for (int i = 0; i < 8; i++) {
            float alpha = 0.15f * glowPulse - i * 0.015f;
            shapeRenderer.setColor(1f, 0.5f, 0.2f, alpha);
            float r = 80 + i * 20;
            shapeRenderer.circle(viewX + viewW / 2f, viewY + viewH / 2f - 30, r);
        }
        shapeRenderer.end();

        batch.begin();

        float centerX = viewX + viewW / 2f;
        float centerY = viewY + viewH / 2f;

        // Campfire sprite
        TextureRegion fireTile = game.getAssets().getTile("rest");
        batch.draw(fireTile, centerX - 48, centerY - 60, 96, 96);

        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Rest text
        font.setColor(PARCHMENT);
        layout.setText(font, "REST SITE");
        font.draw(batch, "REST SITE", centerX - layout.width / 2f, viewY + viewH - 30);

        // Heal amount preview
        int maxHeal = (int)(player.getHealth().getMaximum() * 0.30);
        int actualHeal = Math.min(maxHeal, player.getHealth().getMaximum() - player.getHealth().getCurrent());
        
        smallFont.setColor(Color.GREEN);
        String healText = "Recover " + actualHeal + " health (30%)";
        layout.setText(smallFont, healText);
        smallFont.draw(batch, healText, centerX - layout.width / 2f, centerY + 80);

        // Prompt
        float pulse = 0.6f + MathUtils.sin(animTimer * 4) * 0.4f;
        font.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
        layout.setText(font, "[ PRESS SPACE TO REST ]");
        font.draw(batch, "[ PRESS SPACE TO REST ]", centerX - layout.width / 2f, viewY + 40);

        batch.end();
    }

    // === DAGGERFALL UI FRAME ===

    private void renderDaggerfallUI(float delta) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Stone frame around entire screen
        drawStoneFrame(0, 0, screenWidth, screenHeight, 12);

        // Top bar (dungeon info)
        drawStonePanel(12, screenHeight - 50, screenWidth - 24, 38);
        
        // Bottom bar (message log)
        drawStonePanel(12, 12, screenWidth - 24, 120);
        
        // Left panel (character)
        drawStonePanel(12, 140, 80, screenHeight - 200);
        
        // Right panel (stats)
        drawStonePanel(screenWidth - 92, 140, 80, screenHeight - 200);

        batch.begin();

        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // === TOP BAR ===
        font.setColor(PARCHMENT);
        String dungeonInfo = "FLOOR " + dungeon.getCurrentFloorNumber() + " OF " + dungeon.getMaxFloors();
        layout.setText(font, dungeonInfo);
        font.draw(batch, dungeonInfo, screenWidth / 2f - layout.width / 2f, screenHeight - 22);

        // === LEFT PANEL (Character) ===
        float leftX = 20;
        float leftY = screenHeight - 80;

        // Portrait
        TextureRegion portrait = game.getAssets().getPortrait(player.getPlayerClass().name());
        batch.draw(portrait, leftX, leftY - 60, 64, 64);

        // Class name below
        smallFont.setColor(getClassColor(player.getPlayerClass()));
        layout.setText(smallFont, player.getPlayerClass().name().substring(0, 3));
        smallFont.draw(batch, player.getPlayerClass().name().substring(0, 3), leftX + 32 - layout.width / 2f, leftY - 70);

        // Level
        smallFont.setColor(GOLD);
        smallFont.draw(batch, "Lv" + player.getLevel(), leftX + 10, leftY - 90);

        batch.end();

        // Health bar (vertical style for left panel)
        drawVerticalBar(leftX + 10, 150, 20, 150, 
            displayedPlayerHealth, player.getHealth().getMaximum(), 
            new Color(0.2f, 0.6f, 0.2f, 1f), BLOOD);

        batch.begin();

        // HP label
        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "HP", leftX + 40, 310);
        smallFont.draw(batch, (int)displayedPlayerHealth + "", leftX + 40, 290);

        // === RIGHT PANEL (Stats) ===
        float rightX = screenWidth - 85;
        float rightY = screenHeight - 80;

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "ATK", rightX + 5, rightY);
        font.setColor(TORCH);
        font.draw(batch, "" + player.getEffectiveAttack(), rightX + 5, rightY - 20);

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "DEF", rightX + 5, rightY - 50);
        font.setColor(new Color(0.4f, 0.6f, 0.9f, 1f));
        font.draw(batch, "" + player.getEffectiveDefense(), rightX + 5, rightY - 70);

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "GOLD", rightX + 5, rightY - 100);
        font.setColor(GOLD);
        font.draw(batch, "" + player.getInventory().getGold(), rightX + 5, rightY - 120);

        // Items count
        int items = player.getInventory().getItems().size();
        if (items > 0) {
            smallFont.setColor(PARCHMENT);
            smallFont.draw(batch, "ITEMS", rightX + 5, rightY - 150);
            font.setColor(PARCHMENT);
            font.draw(batch, "" + items, rightX + 5, rightY - 170);
        }

        // === BOTTOM PANEL (Message Log) ===
        float msgY = 115;
        for (int i = messages.size() - 1; i >= 0 && msgY > 25; i--) {
            String msg = messages.get(i);
            float alpha = 1f - (messages.size() - 1 - i) * 0.2f;
            alpha = Math.max(0.3f, alpha);

            smallFont.setColor(getMessageColor(msg, alpha));
            smallFont.draw(batch, "> " + msg, 30, msgY);
            msgY -= 18;
        }

        // Controls hint
        smallFont.setColor(STONE_LIGHT);
        String controls = "[A/D] Move   [SPACE] Action   [ESC] Quit";
        layout.setText(smallFont, controls);
        smallFont.draw(batch, controls, screenWidth / 2f - layout.width / 2f, 18);

        batch.end();
    }

    private void drawStoneFrame(float x, float y, float w, float h, float thickness) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer dark
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(x, y, w, thickness); // Bottom
        shapeRenderer.rect(x, y + h - thickness, w, thickness); // Top
        shapeRenderer.rect(x, y, thickness, h); // Left
        shapeRenderer.rect(x + w - thickness, y, thickness, h); // Right
        
        // Inner bevel (lighter)
        shapeRenderer.setColor(STONE_MID);
        float t2 = thickness * 0.6f;
        shapeRenderer.rect(x + t2, y + t2, w - t2 * 2, thickness - t2); // Bottom inner
        shapeRenderer.rect(x + t2, y + h - thickness, w - t2 * 2, thickness - t2); // Top inner
        
        // Highlight
        shapeRenderer.setColor(STONE_LIGHT);
        shapeRenderer.rect(x + thickness * 0.3f, y + h - thickness * 0.5f, w - thickness * 0.6f, 2);
        shapeRenderer.rect(x + thickness * 0.3f, y + thickness * 0.3f, 2, h - thickness * 0.6f);
        
        shapeRenderer.end();
    }

    private void drawStonePanel(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(STONE_DARK.r * 0.7f, STONE_DARK.g * 0.7f, STONE_DARK.b * 0.7f, 0.95f);
        shapeRenderer.rect(x, y, w, h);
        
        // Border
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x, y, w, 3);
        shapeRenderer.rect(x, y + h - 3, w, 3);
        shapeRenderer.rect(x, y, 3, h);
        shapeRenderer.rect(x + w - 3, y, 3, h);
        
        // Inner highlight
        shapeRenderer.setColor(STONE_LIGHT.r, STONE_LIGHT.g, STONE_LIGHT.b, 0.3f);
        shapeRenderer.rect(x + 3, y + h - 4, w - 6, 1);
        shapeRenderer.rect(x + 3, y + 3, 1, h - 6);
        
        shapeRenderer.end();
    }

    private void drawHealthBar(float x, float y, float w, float h, float current, float max, Color color) {
        float pct = Math.max(0, current / max);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(0.1f, 0.08f, 0.06f, 1f);
        shapeRenderer.rect(x, y, w, h);
        
        // Fill
        shapeRenderer.setColor(color.r * 0.6f, color.g * 0.6f, color.b * 0.6f, 1f);
        shapeRenderer.rect(x + 2, y + 2, (w - 4) * pct, h - 4);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x + 2, y + h * 0.4f, (w - 4) * pct, h * 0.4f);
        
        // Frame
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x, y, w, 2);
        shapeRenderer.rect(x, y + h - 2, w, 2);
        shapeRenderer.rect(x, y, 2, h);
        shapeRenderer.rect(x + w - 2, y, 2, h);
        
        shapeRenderer.end();

        // Text
        batch.begin();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.WHITE);
        String text = (int) current + "/" + (int) max;
        layout.setText(smallFont, text);
        smallFont.draw(batch, text, x + w / 2f - layout.width / 2f, y + h / 2f + layout.height / 2f);
        batch.end();
    }

    private void drawVerticalBar(float x, float y, float w, float h, float current, float max, Color fullColor, Color emptyColor) {
        float pct = Math.max(0, current / max);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(emptyColor.r * 0.3f, emptyColor.g * 0.3f, emptyColor.b * 0.3f, 1f);
        shapeRenderer.rect(x, y, w, h);
        
        // Fill from bottom
        Color fillColor = pct > 0.3f ? fullColor : (pct > 0.15f ? TORCH : emptyColor);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x + 2, y + 2, w - 4, (h - 4) * pct);
        
        // Frame
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x, y, w, 2);
        shapeRenderer.rect(x, y + h - 2, w, 2);
        shapeRenderer.rect(x, y, 2, h);
        shapeRenderer.rect(x + w - 2, y, 2, h);
        
        shapeRenderer.end();
    }

    // === HELPERS ===

    private String getRoomTileKey(RoomType type) {
        return switch (type) {
            case COMBAT -> "combat";
            case BOSS -> "boss";
            case TREASURE -> "chest";
            case SHOP -> "shop";
            case REST -> "rest";
            case EVENT -> "floor";
        };
    }

    private String getRoomDisplayName(RoomType type) {
        return switch (type) {
            case COMBAT -> "MONSTER LAIR";
            case BOSS -> "BOSS CHAMBER";
            case TREASURE -> "TREASURE ROOM";
            case SHOP -> "MERCHANT";
            case REST -> "CAMPFIRE";
            case EVENT -> "MYSTERIOUS CHAMBER";
        };
    }

    private String getRoomStatus(Room room, Floor floor) {
        if (floor.isAtExit()) {
            return dungeon.isOnFinalFloor() ? "Exit to Victory" : "Stairs Down";
        }
        return switch (room.getType()) {
            case COMBAT -> room.getEnemies().size() + " enemies";
            case BOSS -> "Powerful foe ahead";
            case TREASURE -> "Loot awaits";
            case SHOP -> room.getItems().size() + " items";
            case REST -> "Rest here";
            default -> "";
        };
    }

    private Color getClassColor(PlayerClass pc) {
        return switch (pc) {
            case WARRIOR -> new Color(0.4f, 0.5f, 0.9f, 1f);
            case ROGUE -> new Color(0.4f, 0.7f, 0.4f, 1f);
            case MAGE -> new Color(0.7f, 0.4f, 0.8f, 1f);
        };
    }

    private Color getMessageColor(String msg, float alpha) {
        if (msg.contains("slain") || msg.contains("damage") || msg.contains("struck")) {
            return new Color(0.9f, 0.4f, 0.3f, alpha);
        } else if (msg.contains("gold") || msg.contains("purchase")) {
            return new Color(GOLD.r, GOLD.g, GOLD.b, alpha);
        } else if (msg.contains("recover") || msg.contains("health")) {
            return new Color(0.4f, 0.8f, 0.4f, alpha);
        } else if (msg.contains("VICTORY")) {
            return new Color(1f, 0.9f, 0.3f, alpha);
        } else {
            return new Color(PARCHMENT.r, PARCHMENT.g, PARCHMENT.b, alpha);
        }
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
        if (messages.size() > 5) messages.remove(0);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (telemetryWriter != null) {
            try { telemetryWriter.close(); } catch (Exception e) { /* ignore */ }
        }
    }

    // === LISTENERS ===

    private class GameScreenListener implements GameSessionListener {
        @Override public void onRunStarted(GameSession session) {}
        @Override public void onFloorEntered(GameSession session, Floor floor) { 
            addMessage("You enter dungeon level " + floor.getFloorNumber() + "..."); 
        }
        @Override public void onRoomEntered(GameSession session, Room room) {}
        @Override public void onRoomCleared(GameSession session, Room room) {}
        @Override public void onCombatCompleted(GameSession session, CombatResult result) {}
        @Override public void onItemPicked(GameSession session, Item item) { 
            addMessage("You found: " + item.getName()); 
        }
        @Override public void onItemUsed(GameSession session, Item item) {}
        @Override public void onShopPurchase(GameSession session, Item item, int cost) {}
        @Override public void onPlayerRested(GameSession session, int healed) {}
        @Override public void onPlayerLevelUp(GameSession session, int newLevel) { 
            addMessage("You have reached level " + newLevel + "!"); 
        }
        @Override public void onRunEnded(GameSession session, RunEndReason reason) {}
    }

    private static class CompositeSessionListener implements GameSessionListener {
        private final GameSessionListener[] listeners;
        public CompositeSessionListener(GameSessionListener... listeners) { this.listeners = listeners; }
        @Override public void onRunStarted(GameSession s) { for (var l : listeners) l.onRunStarted(s); }
        @Override public void onFloorEntered(GameSession s, Floor f) { for (var l : listeners) l.onFloorEntered(s, f); }
        @Override public void onRoomEntered(GameSession s, Room r) { for (var l : listeners) l.onRoomEntered(s, r); }
        @Override public void onRoomCleared(GameSession s, Room r) { for (var l : listeners) l.onRoomCleared(s, r); }
        @Override public void onCombatCompleted(GameSession s, CombatResult r) { for (var l : listeners) l.onCombatCompleted(s, r); }
        @Override public void onItemPicked(GameSession s, Item i) { for (var l : listeners) l.onItemPicked(s, i); }
        @Override public void onItemUsed(GameSession s, Item i) { for (var l : listeners) l.onItemUsed(s, i); }
        @Override public void onShopPurchase(GameSession s, Item i, int c) { for (var l : listeners) l.onShopPurchase(s, i, c); }
        @Override public void onPlayerRested(GameSession s, int h) { for (var l : listeners) l.onPlayerRested(s, h); }
        @Override public void onPlayerLevelUp(GameSession s, int n) { for (var l : listeners) l.onPlayerLevelUp(s, n); }
        @Override public void onRunEnded(GameSession s, RunEndReason r) { for (var l : listeners) l.onRunEnded(s, r); }
    }
}
