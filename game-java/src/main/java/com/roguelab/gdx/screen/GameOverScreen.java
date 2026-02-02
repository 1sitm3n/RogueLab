package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.roguelab.gdx.RogueLabGame;

/**
 * Game over screen showing victory/defeat and final stats.
 */
public class GameOverScreen implements Screen {
    
    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    
    private final boolean victory;
    private final int score;
    private final int floorsCleared;
    
    private float animTimer = 0;
    private GlyphLayout layout;
    
    public GameOverScreen(RogueLabGame game, boolean victory, int score, int floorsCleared) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.victory = victory;
        this.score = score;
        this.floorsCleared = floorsCleared;
        this.layout = new GlyphLayout();
    }
    
    @Override
    public void show() {
        Gdx.app.log("GameOverScreen", "Game Over - Victory: " + victory + ", Score: " + score);
    }
    
    @Override
    public void render(float delta) {
        animTimer += delta;
        
        // Background color based on outcome
        if (victory) {
            float pulse = (float)Math.sin(animTimer * 2) * 0.05f;
            Gdx.gl.glClearColor(0.1f + pulse, 0.15f + pulse, 0.1f + pulse, 1f);
        } else {
            Gdx.gl.glClearColor(0.15f, 0.08f, 0.08f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        
        // Draw decorative background
        drawBackground();
        
        batch.begin();
        
        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        // Main title
        if (victory) {
            titleFont.setColor(new Color(0.9f, 0.85f, 0.3f, 1f));
            layout.setText(titleFont, "VICTORY!");
        } else {
            titleFont.setColor(new Color(0.8f, 0.2f, 0.2f, 1f));
            layout.setText(titleFont, "DEFEAT");
        }
        
        // Pulsing effect
        float scale = 1f + (float)Math.sin(animTimer * 3) * 0.05f;
        titleFont.getData().setScale(3f * scale);
        titleFont.draw(batch, victory ? "VICTORY!" : "DEFEAT", centerX - layout.width * scale / 2, centerY + 150);
        titleFont.getData().setScale(3f);
        
        // Stats
        normalFont.setColor(Color.WHITE);
        
        String scoreText = "SCORE: " + score;
        layout.setText(normalFont, scoreText);
        normalFont.draw(batch, scoreText, centerX - layout.width / 2, centerY + 40);
        
        String floorsText = "FLOORS CLEARED: " + floorsCleared;
        layout.setText(normalFont, floorsText);
        normalFont.draw(batch, floorsText, centerX - layout.width / 2, centerY - 10);
        
        // Flavor text
        smallFont.setColor(Color.LIGHT_GRAY);
        String flavorText;
        if (victory) {
            flavorText = "You have conquered the dungeon!";
        } else if (floorsCleared == 0) {
            flavorText = "The dungeon claims another soul...";
        } else {
            flavorText = "You fell on floor " + (floorsCleared + 1) + ".";
        }
        layout.setText(smallFont, flavorText);
        smallFont.draw(batch, flavorText, centerX - layout.width / 2, centerY - 60);
        
        // Instructions
        smallFont.setColor(Color.GRAY);
        String instructions = "Press ENTER to play again  |  ESC for menu";
        layout.setText(smallFont, instructions);
        smallFont.draw(batch, instructions, centerX - layout.width / 2, 80);
        
        // Telemetry note
        smallFont.setColor(new Color(0.4f, 0.4f, 0.5f, 1f));
        String telemetryNote = "Run data saved to telemetry log";
        layout.setText(smallFont, telemetryNote);
        smallFont.draw(batch, telemetryNote, centerX - layout.width / 2, 40);
        
        batch.end();
        
        // Handle input
        handleInput();
    }
    
    private void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Animated particles/stars
        for (int i = 0; i < 50; i++) {
            float x = (i * 127 + animTimer * 20 * (i % 3 + 1)) % Gdx.graphics.getWidth();
            float y = (i * 73 + animTimer * 10 * (i % 2 + 1)) % Gdx.graphics.getHeight();
            float size = 2 + (i % 3);
            float alpha = 0.3f + (float)Math.sin(animTimer * 2 + i) * 0.2f;
            
            if (victory) {
                shapeRenderer.setColor(0.9f, 0.8f, 0.3f, alpha);
            } else {
                shapeRenderer.setColor(0.5f, 0.2f, 0.2f, alpha);
            }
            shapeRenderer.circle(x, y, size);
        }
        
        shapeRenderer.end();
    }
    
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.returnToMenu();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
        }
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
}
