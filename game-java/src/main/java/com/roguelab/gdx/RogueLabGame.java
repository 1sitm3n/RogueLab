package com.roguelab.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.roguelab.domain.PlayerClass;
import com.roguelab.gdx.audio.SoundManager;
import com.roguelab.gdx.screen.GameOverScreen;
import com.roguelab.gdx.screen.IntegratedGameScreen;
import com.roguelab.gdx.screen.MenuScreen;

/**
 * Main game class managing screens, assets, and audio.
 */
public class RogueLabGame extends Game {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Assets assets;
    private SoundManager soundManager;

    @Override
    public void create() {
        Gdx.app.log("RogueLabGame", "Initializing...");

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Load assets
        assets = new Assets();
        assets.load();

        // Initialize sound
        soundManager = new SoundManager();
        soundManager.load();

        // Start at menu
        setScreen(new MenuScreen(this));
        
        Gdx.app.log("RogueLabGame", "Initialization complete");
    }

    public void startGame(PlayerClass playerClass) {
        Gdx.app.log("RogueLabGame", "Starting game with class: " + playerClass);
        setScreen(new IntegratedGameScreen(this, playerClass));
    }

    public void gameOver(boolean victory, int goldEarned, int floorsReached) {
        Gdx.app.log("RogueLabGame", "Game over - Victory: " + victory);
        setScreen(new GameOverScreen(this, victory, goldEarned, floorsReached));
    }

    public void returnToMenu() {
        setScreen(new MenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public Assets getAssets() {
        return assets;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    public void dispose() {
        Gdx.app.log("RogueLabGame", "Disposing resources...");
        
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (assets != null) assets.dispose();
        if (soundManager != null) soundManager.dispose();
    }
}
