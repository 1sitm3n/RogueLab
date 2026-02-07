package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
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
import com.roguelab.gdx.audio.SoundManager;
import com.roguelab.gdx.audio.SoundManager.SoundEffect;
import com.roguelab.gdx.effect.EffectsManager;
import com.roguelab.telemetry.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Daggerfall-style game screen with proper viewport scaling.
 */
public class IntegratedGameScreen implements Screen {

    // Virtual resolution - game renders at this size and scales to fit window
    private static final float VIRTUAL_WIDTH = 1280;
    private static final float VIRTUAL_HEIGHT = 720;

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout layout;
    private final SoundManager sound;

    // Viewport for proper scaling
    private final OrthographicCamera camera;
    private final Viewport viewport;

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

    // UI Layout constants
    private static final int FRAME_BORDER = 12;
    private static final int SIDE_PANEL_WIDTH = 100;
    private static final int TOP_BAR_HEIGHT = 50;
    private static final int BOTTOM_BAR_HEIGHT = 130;

    public IntegratedGameScreen(RogueLabGame game, PlayerClass playerClass) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.layout = new GlyphLayout();
        this.effects = new EffectsManager(game);
        this.sound = game.getSoundManager();

        // Setup viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply(true);

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

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport
        viewport.apply();
        
        // Apply camera with shake
        camera.position.set(VIRTUAL_WIDTH / 2f + shakeOffset.x, VIRTUAL_HEIGHT / 2f + shakeOffset.y, 0);
        camera.update();
        
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        GameState state = session.getState();
        switch (state) {
            case IN_COMBAT -> renderCombat(delta);
            case IN_SHOP -> renderShop(delta);
            case AT_REST -> renderRest(delta);
            default -> renderExploration(delta);
        }

        renderDaggerfallUI(delta);
        
        // Reset batch projection for effects (screen space)
        batch.setProjectionMatrix(camera.combined);
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
            if (victory) {
                sound.play(SoundEffect.VICTORY);
            } else {
                sound.play(SoundEffect.DEFEAT);
            }
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
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            sound.toggle();
            addMessage("Sound: " + (sound.isEnabled() ? "ON" : "OFF"));
        }
    }

    // === INPUT HANDLERS ===

    private void handleExplorationInput() {
        Floor floor = dungeon.getCurrentFloor();

        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (floor.hasNextRoom()) {
                sound.playWithVariation(SoundEffect.FOOTSTEP, 1.0f, 0.2f);
                session.advanceRoom();
                addMessage("You move deeper into the dungeon...");
                checkForCombat();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (floor.hasPreviousRoom()) {
                sound.playWithVariation(SoundEffect.FOOTSTEP, 1.0f, 0.2f);
                session.returnRoom();
                addMessage("You retrace your steps...");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            RoomType type = room.getType();
            
            if (type == RoomType.REST && !room.isCleared()) {
                int healed = session.rest();
                sound.play(SoundEffect.HEAL);
                addMessage("You rest by the fire and recover " + healed + " health.");
                effects.addDamageNumber(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, "+" + healed, Color.GREEN);
                session.leaveRest();
                room.markCleared();
            } else if (floor.isAtExit()) {
                if (dungeon.canDescend()) {
                    sound.play(SoundEffect.STAIRS_DESCEND);
                    session.descendFloor();
                    addMessage("You descend to level " + dungeon.getCurrentFloorNumber() + "...");
                    triggerShake(0.4f, 6f);
                    checkForCombat();
                } else if (dungeon.isOnFinalFloor() && floor.allCombatRoomsCleared()) {
                    addMessage("VICTORY! You have conquered the dungeon!");
                    session.endRun(GameSessionListener.RunEndReason.VICTORY);
                } else {
                    sound.play(SoundEffect.ERROR);
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
                    
                    if (enemy.getType().isBoss()) {
                        sound.play(SoundEffect.BOSS_APPEAR);
                    } else {
                        sound.play(SoundEffect.DOOR_OPEN);
                    }
                    
                    addMessage("A " + enemy.getName() + " blocks your path!");
                    break;
                }
            }
        }
    }

    private void handleCombatInput() {
        if (awaitingCombatInput) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                sound.play(SoundEffect.ATTACK_SWORD);
                
                CombatResult result = session.executeCombat();

                float enemyX = VIRTUAL_WIDTH / 2f;
                float enemyY = VIRTUAL_HEIGHT / 2f;

                sound.play(SoundEffect.HIT_IMPACT, 0.8f);
                
                effects.addDamageNumber(enemyX + 50, enemyY, "-" + result.totalDamageDealt(), Color.WHITE);
                effects.addSlash(enemyX, enemyY);

                if (result.totalDamageTaken() > 0) {
                    sound.play(SoundEffect.PLAYER_HURT);
                    effects.addDamageNumber(enemyX - 50, enemyY - 50, "-" + result.totalDamageTaken(), Color.RED);
                    addMessage("The " + currentEnemy.getName() + " strikes you for " + result.totalDamageTaken() + " damage!");
                }

                triggerShake(0.2f, 8f);
                combatTurn++;

                if (result.isVictory()) {
                    sound.play(SoundEffect.ENEMY_DEATH);
                    sound.play(SoundEffect.GOLD_PICKUP, 0.7f);
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
                    sound.play(SoundEffect.SHOP_BUY);
                    sound.play(SoundEffect.ITEM_PICKUP, 0.6f);
                    addMessage("You purchase " + item.getName() + " for " + item.getValue() + " gold.");
                } else {
                    sound.play(SoundEffect.ERROR);
                    addMessage("You cannot afford that.");
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            sound.play(SoundEffect.DOOR_OPEN);
            session.leaveShop();
            addMessage("You leave the merchant.");
        }
    }

    private void handleRestInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Room room = session.getCurrentRoom();
            if (!room.isCleared()) {
                int healed = session.rest();
                sound.play(SoundEffect.HEAL);
                addMessage("You rest and recover " + healed + " health.");
                effects.addDamageNumber(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, "+" + healed, Color.GREEN);
                room.markCleared();
            }
            session.leaveRest();
        }
    }

    // === RENDERING ===

    private void renderExploration(float delta) {
        float viewX = SIDE_PANEL_WIDTH + FRAME_BORDER;
        float viewY = BOTTOM_BAR_HEIGHT;
        float viewW = VIRTUAL_WIDTH - (SIDE_PANEL_WIDTH * 2) - (FRAME_BORDER * 2);
        float viewH = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT;

        renderDungeonCorridor(viewX, viewY, viewW, viewH, delta);
        renderRoomMinimap(VIRTUAL_WIDTH / 2f, viewY + 40, delta);
    }

    private void renderDungeonCorridor(float x, float y, float w, float h, float delta) {
        Room room = session.getCurrentRoom();
        Floor floor = dungeon.getCurrentFloor();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Ceiling
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(x, y + h * 0.6f, w, h * 0.4f);
        
        // Floor
        shapeRenderer.setColor(STONE_MID.r * 0.8f, STONE_MID.g * 0.8f, STONE_MID.b * 0.8f, 1f);
        shapeRenderer.rect(x, y, w, h * 0.4f);
        
        // Perspective walls
        float perspDepth = 0.25f;
        
        // Left wall
        shapeRenderer.setColor(STONE_MID.r * 0.6f, STONE_MID.g * 0.6f, STONE_MID.b * 0.6f, 1f);
        shapeRenderer.triangle(x, y, x, y + h, x + w * perspDepth, y + h * (1 - perspDepth));
        shapeRenderer.triangle(x, y, x + w * perspDepth, y + h * perspDepth, x + w * perspDepth, y + h * (1 - perspDepth));
        
        // Right wall
        shapeRenderer.setColor(STONE_MID.r * 0.5f, STONE_MID.g * 0.5f, STONE_MID.b * 0.5f, 1f);
        shapeRenderer.triangle(x + w, y, x + w, y + h, x + w * (1 - perspDepth), y + h * (1 - perspDepth));
        shapeRenderer.triangle(x + w, y, x + w * (1 - perspDepth), y + h * perspDepth, x + w * (1 - perspDepth), y + h * (1 - perspDepth));
        
        // Back wall
        float backX = x + w * perspDepth;
        float backY = y + h * perspDepth;
        float backW = w * (1 - 2 * perspDepth);
        float backH = h * (1 - 2 * perspDepth);
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(backX, backY, backW, backH);
        
        shapeRenderer.end();

        batch.begin();
        
        float centerX = x + w / 2f;
        float centerY = y + h / 2f;
        
        // Room icon
        TextureRegion roomTile = game.getAssets().getTile(getRoomTileKey(room.getType()));
        float iconSize = Math.min(backW, backH) * 0.5f;
        batch.draw(roomTile, centerX - iconSize / 2f, centerY - iconSize / 2f, iconSize, iconSize);

        // Torchlight effect
        float flicker = 0.9f + MathUtils.sin(animTimer * 8) * 0.1f;
        batch.setColor(TORCH.r * flicker, TORCH.g * flicker, TORCH.b * 0.5f, 0.12f);
        batch.draw(game.getAssets().getWhitePixel(), x, y, w, h);
        batch.setColor(Color.WHITE);

        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        // Room name
        font.setColor(PARCHMENT);
        String roomName = getRoomDisplayName(room.getType());
        layout.setText(font, roomName);
        font.draw(batch, roomName, centerX - layout.width / 2f, y + h - 30);
        
        // Room status
        smallFont.setColor(room.isCleared() ? Color.GREEN : Color.LIGHT_GRAY);
        String status = room.isCleared() ? "[CLEARED]" : getRoomStatus(room, floor);
        layout.setText(smallFont, status);
        smallFont.draw(batch, status, centerX - layout.width / 2f, y + 50);

        batch.end();
    }

    private void renderRoomMinimap(float centerX, float y, float delta) {
        Floor floor = dungeon.getCurrentFloor();
        List<Room> rooms = floor.getRooms();
        int currentIdx = floor.getCurrentRoomIndex();
        
        int roomSize = 28;
        int spacing = 36;
        float mapWidth = rooms.size() * spacing;
        float startX = centerX - mapWidth / 2f + spacing / 2f;
        
        // Connection lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(STONE_MID);
        for (int i = 0; i < rooms.size() - 1; i++) {
            float x1 = startX + i * spacing;
            float x2 = startX + (i + 1) * spacing;
            shapeRenderer.rectLine(x1, y, x2, y, 3);
        }
        shapeRenderer.end();
        
        batch.begin();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            float rx = startX + i * spacing - roomSize / 2f;
            float ry = y - roomSize / 2f;
            
            boolean current = (i == currentIdx);
            boolean visited = room.isVisited();
            
            TextureRegion tile = game.getAssets().getTile(visited ? getRoomTileKey(room.getType()) : "fog");
            
            // Highlight current room
            if (current) {
                batch.end();
                float pulse = 0.4f + MathUtils.sin(animTimer * 4) * 0.2f;
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
                shapeRenderer.rect(rx - 4, ry - 4, roomSize + 8, roomSize + 8);
                shapeRenderer.end();
                batch.begin();
            }
            
            batch.setColor(visited || current ? Color.WHITE : new Color(0.3f, 0.3f, 0.3f, 1f));
            batch.draw(tile, rx, ry, roomSize, roomSize);
            
            // Cleared overlay
            if (room.isCleared()) {
                batch.setColor(0.3f, 0.8f, 0.3f, 0.5f);
                batch.draw(game.getAssets().getWhitePixel(), rx, ry, roomSize, roomSize);
            }
            
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void renderCombat(float delta) {
        if (currentEnemy == null) return;

        float viewX = SIDE_PANEL_WIDTH + FRAME_BORDER;
        float viewY = BOTTOM_BAR_HEIGHT;
        float viewW = VIRTUAL_WIDTH - (SIDE_PANEL_WIDTH * 2) - (FRAME_BORDER * 2);
        float viewH = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT;
        
        // Combat background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.05f, 0.03f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
        
        // Boss aura
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
        
        // Enemy sprite with bob animation
        float bob = MathUtils.sin(animTimer * 2) * 8;
        TextureRegion enemySprite = game.getAssets().getEnemySprite(currentEnemy.getType().name());
        int spriteSize = currentEnemy.getType().isBoss() ? 180 : 140;
        
        // Flash when low health
        float healthPct = (float) currentEnemy.getHealth().getCurrent() / currentEnemy.getHealth().getMaximum();
        if (healthPct < 0.3f) {
            float flash = MathUtils.sin(animTimer * 10) * 0.4f + 0.6f;
            batch.setColor(1f, flash, flash, 1f);
        }
        
        batch.draw(enemySprite, centerX - spriteSize / 2f, centerY - spriteSize / 2f + bob, spriteSize, spriteSize);
        batch.setColor(Color.WHITE);
        
        // Enemy name
        BitmapFont font = game.getAssets().getNormalFont();
        font.setColor(currentEnemy.getType().isBoss() ? TORCH : PARCHMENT);
        String enemyName = currentEnemy.getName().toUpperCase();
        layout.setText(font, enemyName);
        font.draw(batch, enemyName, centerX - layout.width / 2f, viewY + viewH - 25);
        
        // Special ability indicator
        SpecialAbility ability = currentEnemy.getType().getSpecialAbility();
        if (ability != SpecialAbility.NONE) {
            BitmapFont smallFont = game.getAssets().getSmallFont();
            Color abilityColor = ability.getIndicatorColor();
            smallFont.setColor(abilityColor != null ? abilityColor : GOLD);
            String abilityText = "[" + ability.getDisplayName() + "]";
            layout.setText(smallFont, abilityText);
            smallFont.draw(batch, abilityText, centerX - layout.width / 2f, viewY + viewH - 50);
        }
        
        batch.end();
        
        // Enemy health bar
        drawHealthBar(centerX - 120, viewY + viewH - 80, 240, 20, 
            displayedEnemyHealth, currentEnemy.getHealth().getMaximum(),
            currentEnemy.getType().isBoss() ? TORCH : BLOOD);
        
        batch.begin();
        
        // Turn counter
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(STONE_LIGHT);
        String turnText = "Turn " + combatTurn;
        layout.setText(smallFont, turnText);
        smallFont.draw(batch, turnText, centerX - layout.width / 2f, viewY + 40);
        
        // Attack prompt
        float pulse = 0.6f + MathUtils.sin(animTimer * 5) * 0.4f;
        font.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
        String prompt = "[ PRESS SPACE TO ATTACK ]";
        layout.setText(font, prompt);
        font.draw(batch, prompt, centerX - layout.width / 2f, viewY + 70);
        
        batch.end();
    }

    private void renderShop(float delta) {
        float viewX = SIDE_PANEL_WIDTH + FRAME_BORDER;
        float viewY = BOTTOM_BAR_HEIGHT;
        float viewW = VIRTUAL_WIDTH - (SIDE_PANEL_WIDTH * 2) - (FRAME_BORDER * 2);
        float viewH = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT;

        // Parchment background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(PARCHMENT.r * 0.7f, PARCHMENT.g * 0.7f, PARCHMENT.b * 0.7f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
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
        titleFont.draw(batch, "MERCHANT", centerX - layout.width / 2f, viewY + viewH - 25);
        titleFont.getData().setScale(3f);

        // Items
        Room room = session.getCurrentRoom();
        List<Item> items = room.getItems();
        
        float itemY = viewY + viewH - 90;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean canAfford = player.getInventory().getGold() >= item.getValue();
            
            font.setColor(canAfford ? STONE_DARK : STONE_LIGHT);
            font.draw(batch, "[" + (i + 1) + "]", viewX + 40, itemY);
            font.draw(batch, item.getName(), viewX + 90, itemY);
            
            smallFont.setColor(canAfford ? new Color(0.2f, 0.4f, 0.2f, 1f) : STONE_LIGHT);
            String stats = "";
            if (item.getAttackBonus() > 0) stats += "+" + item.getAttackBonus() + " ATK ";
            if (item.getDefenseBonus() > 0) stats += "+" + item.getDefenseBonus() + " DEF ";
            if (item.getHealthBonus() > 0) stats += "+" + item.getHealthBonus() + " HP";
            smallFont.draw(batch, stats, viewX + 300, itemY);
            
            font.setColor(canAfford ? GOLD : BLOOD);
            layout.setText(font, item.getValue() + " gold");
            font.draw(batch, item.getValue() + " gold", viewX + viewW - layout.width - 50, itemY);
            
            itemY -= 45;
        }

        // Player gold
        font.setColor(GOLD);
        String goldText = "Your Gold: " + player.getInventory().getGold();
        layout.setText(font, goldText);
        font.draw(batch, goldText, centerX - layout.width / 2f, viewY + 55);

        // Controls
        smallFont.setColor(STONE_MID);
        layout.setText(smallFont, "[1-9] Purchase   [SPACE] Leave");
        smallFont.draw(batch, "[1-9] Purchase   [SPACE] Leave", centerX - layout.width / 2f, viewY + 30);

        batch.end();
    }

    private void renderRest(float delta) {
        float viewX = SIDE_PANEL_WIDTH + FRAME_BORDER;
        float viewY = BOTTOM_BAR_HEIGHT;
        float viewW = VIRTUAL_WIDTH - (SIDE_PANEL_WIDTH * 2) - (FRAME_BORDER * 2);
        float viewH = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT;

        // Dark background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.08f, 0.05f, 1f);
        shapeRenderer.rect(viewX, viewY, viewW, viewH);
        
        // Fire glow
        float glowPulse = 0.8f + MathUtils.sin(animTimer * 3) * 0.2f;
        float centerX = viewX + viewW / 2f;
        float centerY = viewY + viewH / 2f;
        for (int i = 0; i < 8; i++) {
            float alpha = 0.15f * glowPulse - i * 0.015f;
            shapeRenderer.setColor(1f, 0.5f, 0.2f, alpha);
            float r = 80 + i * 25;
            shapeRenderer.circle(centerX, centerY - 20, r);
        }
        shapeRenderer.end();

        batch.begin();

        // Campfire icon
        TextureRegion fireTile = game.getAssets().getTile("rest");
        batch.draw(fireTile, centerX - 56, centerY - 56, 112, 112);

        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Title
        font.setColor(PARCHMENT);
        layout.setText(font, "REST SITE");
        font.draw(batch, "REST SITE", centerX - layout.width / 2f, viewY + viewH - 35);

        // Heal amount
        int maxHeal = (int)(player.getHealth().getMaximum() * 0.30);
        int actualHeal = Math.min(maxHeal, player.getHealth().getMaximum() - player.getHealth().getCurrent());
        
        smallFont.setColor(Color.GREEN);
        String healText = "Recover " + actualHeal + " health (30%)";
        layout.setText(smallFont, healText);
        smallFont.draw(batch, healText, centerX - layout.width / 2f, centerY + 90);

        // Prompt
        float pulse = 0.6f + MathUtils.sin(animTimer * 4) * 0.4f;
        font.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
        layout.setText(font, "[ PRESS SPACE TO REST ]");
        font.draw(batch, "[ PRESS SPACE TO REST ]", centerX - layout.width / 2f, viewY + 50);

        batch.end();
    }

    // === DAGGERFALL UI FRAME ===

    private void renderDaggerfallUI(float delta) {
        // Main stone frame
        drawStoneFrame(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, FRAME_BORDER);
        
        // Top bar
        drawStonePanel(FRAME_BORDER, VIRTUAL_HEIGHT - TOP_BAR_HEIGHT, VIRTUAL_WIDTH - FRAME_BORDER * 2, TOP_BAR_HEIGHT - FRAME_BORDER);
        
        // Bottom message panel
        drawStonePanel(FRAME_BORDER, FRAME_BORDER, VIRTUAL_WIDTH - FRAME_BORDER * 2, BOTTOM_BAR_HEIGHT - FRAME_BORDER);
        
        // Left stats panel
        drawStonePanel(FRAME_BORDER, BOTTOM_BAR_HEIGHT, SIDE_PANEL_WIDTH - FRAME_BORDER, VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT);
        
        // Right stats panel
        drawStonePanel(VIRTUAL_WIDTH - SIDE_PANEL_WIDTH, BOTTOM_BAR_HEIGHT, SIDE_PANEL_WIDTH - FRAME_BORDER, VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT);

        batch.begin();

        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // === TOP BAR ===
        float topY = VIRTUAL_HEIGHT - 15;
        
        font.setColor(PARCHMENT);
        String dungeonInfo = "FLOOR " + dungeon.getCurrentFloorNumber() + " OF " + dungeon.getMaxFloors();
        layout.setText(font, dungeonInfo);
        font.draw(batch, dungeonInfo, VIRTUAL_WIDTH / 2f - layout.width / 2f, topY);
        
        // Sound indicator
        smallFont.setColor(sound.isEnabled() ? GOLD : STONE_MID);
        String soundText = sound.isEnabled() ? "[M] Sound ON" : "[M] Sound OFF";
        layout.setText(smallFont, soundText);
        smallFont.draw(batch, soundText, VIRTUAL_WIDTH - layout.width - 25, topY - 5);

        // === LEFT PANEL (Character) ===
        float leftX = 22;
        float leftY = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - 20;

        // Portrait
        TextureRegion portrait = game.getAssets().getPortrait(player.getPlayerClass().name());
        batch.draw(portrait, leftX, leftY - 60, 56, 56);

        // Class abbreviation
        smallFont.setColor(getClassColor(player.getPlayerClass()));
        String classAbbr = player.getPlayerClass().name().substring(0, 3);
        layout.setText(smallFont, classAbbr);
        smallFont.draw(batch, classAbbr, leftX + 28 - layout.width / 2f, leftY - 68);

        // Level
        smallFont.setColor(GOLD);
        smallFont.draw(batch, "Lv" + player.getLevel(), leftX + 8, leftY - 85);

        batch.end();

        // Vertical HP bar
        drawVerticalBar(leftX + 8, BOTTOM_BAR_HEIGHT + 20, 24, 120, 
            displayedPlayerHealth, player.getHealth().getMaximum(), 
            new Color(0.2f, 0.6f, 0.2f, 1f), BLOOD);

        batch.begin();

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "HP", leftX + 40, BOTTOM_BAR_HEIGHT + 145);
        smallFont.draw(batch, (int)displayedPlayerHealth + "/" + player.getHealth().getMaximum(), leftX + 40, BOTTOM_BAR_HEIGHT + 125);

        // === RIGHT PANEL (Stats) ===
        float rightX = VIRTUAL_WIDTH - SIDE_PANEL_WIDTH + 15;
        float rightY = VIRTUAL_HEIGHT - TOP_BAR_HEIGHT - 25;

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "ATK", rightX, rightY);
        font.setColor(TORCH);
        font.draw(batch, "" + player.getEffectiveAttack(), rightX, rightY - 22);

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "DEF", rightX, rightY - 55);
        font.setColor(new Color(0.4f, 0.6f, 0.9f, 1f));
        font.draw(batch, "" + player.getEffectiveDefense(), rightX, rightY - 77);

        smallFont.setColor(PARCHMENT);
        smallFont.draw(batch, "GOLD", rightX, rightY - 110);
        font.setColor(GOLD);
        font.draw(batch, "" + player.getInventory().getGold(), rightX, rightY - 132);

        int itemCount = player.getInventory().getItems().size();
        if (itemCount > 0) {
            smallFont.setColor(PARCHMENT);
            smallFont.draw(batch, "ITEMS", rightX, rightY - 165);
            font.setColor(PARCHMENT);
            font.draw(batch, "" + itemCount, rightX, rightY - 187);
        }

        // === BOTTOM MESSAGE LOG ===
        float msgX = 30;
        float msgY = BOTTOM_BAR_HEIGHT - 20;
        
        for (int i = messages.size() - 1; i >= 0 && msgY > 20; i--) {
            String msg = messages.get(i);
            float alpha = 1f - (messages.size() - 1 - i) * 0.18f;
            alpha = Math.max(0.3f, alpha);

            smallFont.setColor(getMessageColor(msg, alpha));
            smallFont.draw(batch, "> " + msg, msgX, msgY);
            msgY -= 20;
        }

        // Controls hint
        smallFont.setColor(STONE_LIGHT);
        String controls = "[A/D] Move   [SPACE] Action   [M] Sound   [ESC] Quit";
        layout.setText(smallFont, controls);
        smallFont.draw(batch, controls, VIRTUAL_WIDTH / 2f - layout.width / 2f, 18);

        batch.end();
    }

    private void drawStoneFrame(float x, float y, float w, float h, float thickness) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer dark border
        shapeRenderer.setColor(STONE_DARK);
        shapeRenderer.rect(x, y, w, thickness);
        shapeRenderer.rect(x, y + h - thickness, w, thickness);
        shapeRenderer.rect(x, y, thickness, h);
        shapeRenderer.rect(x + w - thickness, y, thickness, h);
        
        // Inner highlight
        shapeRenderer.setColor(STONE_MID);
        float t2 = thickness * 0.6f;
        shapeRenderer.rect(x + t2, y + t2, w - t2 * 2, thickness - t2);
        shapeRenderer.rect(x + t2, y + h - thickness, w - t2 * 2, thickness - t2);
        
        // Bevel effect
        shapeRenderer.setColor(STONE_LIGHT);
        shapeRenderer.rect(x + thickness * 0.3f, y + h - thickness * 0.5f, w - thickness * 0.6f, 2);
        shapeRenderer.rect(x + thickness * 0.3f, y + thickness * 0.3f, 2, h - thickness * 0.6f);
        
        shapeRenderer.end();
    }

    private void drawStonePanel(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Panel background
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
        
        // Border
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
        
        // Fill with color based on health level
        Color fillColor = pct > 0.3f ? fullColor : (pct > 0.15f ? TORCH : emptyColor);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x + 2, y + 2, w - 4, (h - 4) * pct);
        
        // Border
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

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
            sound.play(SoundEffect.ITEM_PICKUP);
            addMessage("You found: " + item.getName()); 
        }
        @Override public void onItemUsed(GameSession session, Item item) {}
        @Override public void onShopPurchase(GameSession session, Item item, int cost) {}
        @Override public void onPlayerRested(GameSession session, int healed) {}
        @Override public void onPlayerLevelUp(GameSession session, int newLevel) { 
            sound.play(SoundEffect.LEVEL_UP);
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
