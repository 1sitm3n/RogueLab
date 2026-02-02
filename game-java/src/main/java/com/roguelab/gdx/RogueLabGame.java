package com.roguelab.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.roguelab.domain.PlayerClass;
import com.roguelab.gdx.screen.GameOverScreen;
import com.roguelab.gdx.screen.IntegratedGameScreen;
import com.roguelab.gdx.screen.MenuScreen;

/**
 * Main game class for RogueLab.
 * Manages screens, shared resources, and game state transitions.
 */
public class RogueLabGame extends Game {

    // Shared rendering resources
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Assets assets;

    // Game configuration
    public static final int VIRTUAL_WIDTH = 1280;
    public static final int VIRTUAL_HEIGHT = 720;
    public static final int TILE_SIZE = 32;

    @Override
    public void create() {
        // Initialize shared resources
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assets = new Assets();
        assets.load();

        // Start at menu screen
        setScreen(new MenuScreen(this));

        Gdx.app.log("RogueLab", "Game initialized - LibGDX version " + com.badlogic.gdx.Version.VERSION);
    }

    /**
     * Start a new game with the selected player class.
     */
    public void startGame(PlayerClass playerClass) {
        setScreen(new IntegratedGameScreen(this, playerClass));
    }

    /**
     * Start a new game with the selected player class (string version for compatibility).
     */
    public void startGame(String playerClassName) {
        PlayerClass playerClass = PlayerClass.valueOf(playerClassName);
        startGame(playerClass);
    }

    /**
     * Return to the main menu.
     */
    public void returnToMenu() {
        setScreen(new MenuScreen(this));
    }

    /**
     * Show game over screen.
     */
    public void gameOver(boolean victory, int score, int floorsCleared) {
        setScreen(new GameOverScreen(this, victory, score, floorsCleared));
    }

    // Getters for shared resources
    public SpriteBatch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public Assets getAssets() { return assets; }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        assets.dispose();
        Gdx.app.log("RogueLab", "Game disposed");
    }
}
