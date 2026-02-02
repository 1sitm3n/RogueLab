package com.roguelab.gdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.screen.GameScreen.GameState;

/**
 * Renders the combat view - player vs enemy with health bars and animations.
 */
public class CombatRenderer {
    
    private final RogueLabGame game;
    private final GameState state;
    private final GlyphLayout layout;
    
    // Animation state
    private float playerBob = 0;
    private float enemyBob = 0;
    private float turnIndicatorPulse = 0;
    
    // Entity sizes
    private static final int ENTITY_SIZE = 128;
    private static final int BOSS_SIZE = 180;
    
    public CombatRenderer(RogueLabGame game, GameState state) {
        this.game = game;
        this.state = state;
        this.layout = new GlyphLayout();
    }
    
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float delta) {
        // Update animations
        playerBob += delta * 3;
        enemyBob += delta * 2.5f;
        turnIndicatorPulse += delta * 5;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerY = screenHeight / 2f;
        
        // Positions
        float playerX = screenWidth * 0.25f;
        float enemyX = screenWidth * 0.75f;
        
        // Draw combat arena background
        drawArenaBackground(shapeRenderer);
        
        batch.begin();
        
        // Draw entities
        drawPlayer(batch, playerX, centerY, delta);
        drawEnemy(batch, enemyX, centerY, delta);
        
        // Draw health bars
        drawHealthBar(batch, shapeRenderer, playerX, centerY - 100, 
            state.playerHealth, state.playerMaxHealth, Color.GREEN, state.playerClass);
        drawHealthBar(batch, shapeRenderer, enemyX, centerY - 100,
            state.enemyHealth, state.enemyMaxHealth, 
            state.enemyIsBoss ? Color.ORANGE : Color.RED, state.enemyName);
        
        // Draw turn indicator
        drawTurnIndicator(batch, playerX, enemyX, centerY);
        
        // Draw combat UI
        drawCombatUI(batch);
        
        batch.end();
    }
    
    private void drawArenaBackground(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Ground
        shapeRenderer.setColor(0.2f, 0.18f, 0.15f, 1f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.35f);
        
        // Arena circle
        shapeRenderer.setColor(0.15f, 0.13f, 0.12f, 1f);
        shapeRenderer.ellipse(
            Gdx.graphics.getWidth() * 0.1f,
            Gdx.graphics.getHeight() * 0.1f,
            Gdx.graphics.getWidth() * 0.8f,
            Gdx.graphics.getHeight() * 0.3f
        );
        
        shapeRenderer.end();
        
        // Arena border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.4f, 0.35f, 0.3f, 1f);
        shapeRenderer.ellipse(
            Gdx.graphics.getWidth() * 0.1f,
            Gdx.graphics.getHeight() * 0.1f,
            Gdx.graphics.getWidth() * 0.8f,
            Gdx.graphics.getHeight() * 0.3f
        );
        shapeRenderer.end();
    }
    
    private void drawPlayer(SpriteBatch batch, float x, float y, float delta) {
        TextureRegion sprite = game.getAssets().getPlayerSprite(state.playerClass);
        
        // Bobbing animation
        float bobOffset = MathUtils.sin(playerBob) * 5;
        
        // Scale for visual impact
        float size = ENTITY_SIZE;
        
        batch.setColor(Color.WHITE);
        batch.draw(sprite, 
            x - size / 2f, 
            y - size / 2f + bobOffset,
            size, size);
        
        // Draw class letter on sprite
        BitmapFont font = game.getAssets().getNormalFont();
        font.setColor(Color.WHITE);
        String letter = state.playerClass.substring(0, 1);
        layout.setText(font, letter);
        font.draw(batch, letter, x - layout.width / 2f, y + layout.height / 2f + bobOffset);
    }
    
    private void drawEnemy(SpriteBatch batch, float x, float y, float delta) {
        // Determine enemy sprite based on name
        String enemyType = state.enemyName.toUpperCase().replace(" ", "_");
        TextureRegion sprite = game.getAssets().getEnemySprite(enemyType);
        
        // Bobbing animation (slightly different frequency)
        float bobOffset = MathUtils.sin(enemyBob) * 4;
        
        // Boss is larger
        float size = state.enemyIsBoss ? BOSS_SIZE : ENTITY_SIZE;
        
        // Tint red if low health
        float healthPercent = (float) state.enemyHealth / state.enemyMaxHealth;
        if (healthPercent < 0.3f) {
            float flash = MathUtils.sin(enemyBob * 3) * 0.3f + 0.7f;
            batch.setColor(1f, flash, flash, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        
        batch.draw(sprite,
            x - size / 2f,
            y - size / 2f + bobOffset,
            size, size);
        
        batch.setColor(Color.WHITE);
        
        // Draw enemy letter
        BitmapFont font = game.getAssets().getNormalFont();
        font.setColor(Color.WHITE);
        String letter = state.enemyName.substring(0, 1);
        layout.setText(font, letter);
        font.draw(batch, letter, x - layout.width / 2f, y + layout.height / 2f + bobOffset);
    }
    
    private void drawHealthBar(SpriteBatch batch, ShapeRenderer shapeRenderer, 
                               float x, float y, int current, int max, Color color, String name) {
        batch.end();
        
        float barWidth = 150;
        float barHeight = 16;
        float healthPercent = Math.max(0, (float) current / max);
        
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x - barWidth / 2f, y, barWidth, barHeight);
        
        // Health fill
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x - barWidth / 2f, y, barWidth * healthPercent, barHeight);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(x - barWidth / 2f, y, barWidth, barHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Health text
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.WHITE);
        String healthText = current + "/" + max;
        layout.setText(smallFont, healthText);
        smallFont.draw(batch, healthText, x - layout.width / 2f, y + barHeight / 2f + layout.height / 2f);
        
        // Name above health bar
        BitmapFont normalFont = game.getAssets().getNormalFont();
        normalFont.setColor(color);
        layout.setText(normalFont, name);
        normalFont.draw(batch, name, x - layout.width / 2f, y + barHeight + 30);
    }
    
    private void drawTurnIndicator(SpriteBatch batch, float playerX, float enemyX, float centerY) {
        BitmapFont font = game.getAssets().getNormalFont();
        
        float alpha = 0.5f + MathUtils.sin(turnIndicatorPulse) * 0.3f;
        
        // Draw arrow pointing to current turn
        String indicator = ">>> YOUR TURN <<<";
        font.setColor(new Color(0.3f, 0.9f, 0.3f, alpha));
        
        layout.setText(font, indicator);
        font.draw(batch, indicator, playerX - layout.width / 2f, centerY + 150);
        
        // Instructions
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(new Color(1f, 1f, 1f, alpha));
        String actionText = "Press SPACE or ENTER to attack!";
        layout.setText(smallFont, actionText);
        smallFont.draw(batch, actionText, playerX - layout.width / 2f, centerY + 110);
    }
    
    private void drawCombatUI(SpriteBatch batch) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        // Combat title
        titleFont.setColor(Color.RED);
        titleFont.getData().setScale(2f);
        String combatTitle = state.enemyIsBoss ? "BOSS BATTLE!" : "COMBAT";
        layout.setText(titleFont, combatTitle);
        titleFont.draw(batch, combatTitle, screenWidth / 2f - layout.width / 2f, screenHeight - 30);
        titleFont.getData().setScale(3f);
        
        // Floor info
        smallFont.setColor(Color.GRAY);
        smallFont.draw(batch, "Floor " + state.floor, 20, screenHeight - 20);
    }
}
