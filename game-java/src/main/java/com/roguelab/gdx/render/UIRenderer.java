package com.roguelab.gdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.screen.GameScreen.GameState;

import java.util.List;

/**
 * Renders the UI overlay - health bars, gold, stats, message log.
 */
public class UIRenderer {
    
    private final RogueLabGame game;
    private final GameState state;
    private final GlyphLayout layout;
    
    // Panel dimensions
    private static final int PANEL_PADDING = 10;
    private static final int STATS_PANEL_WIDTH = 220;
    private static final int STATS_PANEL_HEIGHT = 140;
    private static final int LOG_PANEL_HEIGHT = 150;
    
    public UIRenderer(RogueLabGame game, GameState state) {
        this.game = game;
        this.state = state;
        this.layout = new GlyphLayout();
    }
    
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float delta, List<String> messages) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Draw stat panel (top left)
        drawStatsPanel(batch, shapeRenderer, PANEL_PADDING, screenHeight - STATS_PANEL_HEIGHT - PANEL_PADDING);
        
        // Draw message log (bottom)
        drawMessageLog(batch, shapeRenderer, PANEL_PADDING, PANEL_PADDING, messages);
        
        // Draw gold counter (top right)
        drawGoldCounter(batch, screenWidth - 150, screenHeight - 40);
    }
    
    private void drawStatsPanel(SpriteBatch batch, ShapeRenderer shapeRenderer, float x, float y) {
        // Panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.85f);
        shapeRenderer.rect(x, y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);
        shapeRenderer.end();
        
        // Panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.4f, 0.4f, 0.5f, 1f);
        shapeRenderer.rect(x, y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);
        shapeRenderer.end();
        
        batch.begin();
        
        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        float textX = x + 15;
        float textY = y + STATS_PANEL_HEIGHT - 15;
        float lineHeight = 28;
        
        // Class name
        normalFont.setColor(getClassColor(state.playerClass));
        normalFont.draw(batch, state.playerClass, textX, textY);
        textY -= lineHeight + 5;
        
        // Health bar
        drawMiniHealthBar(batch, shapeRenderer, textX, textY - 5, 
            STATS_PANEL_WIDTH - 30, 14,
            state.playerHealth, state.playerMaxHealth, Color.GREEN);
        textY -= lineHeight;
        
        // Stats
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "ATK: " + state.playerAttack, textX, textY);
        textY -= 22;
        smallFont.draw(batch, "DEF: " + state.playerDefense, textX, textY);
        textY -= 22;
        smallFont.draw(batch, "Floor: " + state.floor + "/" + state.maxFloors, textX, textY);
        
        batch.end();
    }
    
    private void drawMiniHealthBar(SpriteBatch batch, ShapeRenderer shapeRenderer,
                                   float x, float y, float width, float height,
                                   int current, int max, Color color) {
        batch.end();
        
        float percent = Math.max(0, (float) current / max);
        
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width, height);
        
        // Fill
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        batch.begin();
        
        // Text
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.WHITE);
        String text = "HP: " + current + "/" + max;
        layout.setText(smallFont, text);
        smallFont.draw(batch, text, x + width / 2f - layout.width / 2f, y + height / 2f + layout.height / 2f);
    }
    
    private void drawMessageLog(SpriteBatch batch, ShapeRenderer shapeRenderer, 
                                float x, float y, List<String> messages) {
        float logWidth = Gdx.graphics.getWidth() - PANEL_PADDING * 2;
        
        // Panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.8f);
        shapeRenderer.rect(x, y, logWidth, LOG_PANEL_HEIGHT);
        shapeRenderer.end();
        
        // Panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(x, y, logWidth, LOG_PANEL_HEIGHT);
        shapeRenderer.end();
        
        batch.begin();
        
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        float textX = x + 15;
        float textY = y + LOG_PANEL_HEIGHT - 20;
        float lineHeight = 20;
        
        // Draw messages (most recent at top)
        for (int i = messages.size() - 1; i >= 0 && textY > y + 10; i--) {
            // Fade older messages
            float alpha = 1f - (messages.size() - 1 - i) * 0.15f;
            alpha = Math.max(0.3f, alpha);
            
            // Color based on message content
            String msg = messages.get(i);
            if (msg.contains("damage") || msg.contains("CRITICAL")) {
                smallFont.setColor(new Color(1f, 0.5f, 0.5f, alpha));
            } else if (msg.contains("Victory") || msg.contains("gold") || msg.contains("HP")) {
                smallFont.setColor(new Color(0.5f, 1f, 0.5f, alpha));
            } else if (msg.contains("Boss") || msg.contains("Welcome")) {
                smallFont.setColor(new Color(1f, 0.8f, 0.3f, alpha));
            } else {
                smallFont.setColor(new Color(0.8f, 0.8f, 0.8f, alpha));
            }
            
            smallFont.draw(batch, "> " + msg, textX, textY);
            textY -= lineHeight;
        }
        
        batch.end();
    }
    
    private void drawGoldCounter(SpriteBatch batch, float x, float y) {
        batch.begin();
        
        BitmapFont normalFont = game.getAssets().getNormalFont();
        normalFont.setColor(Color.GOLD);
        
        String goldText = "$ " + state.gold;
        layout.setText(normalFont, goldText);
        normalFont.draw(batch, goldText, x, y);
        
        batch.end();
    }
    
    private Color getClassColor(String playerClass) {
        switch (playerClass) {
            case "WARRIOR": return new Color(0.3f, 0.5f, 0.9f, 1f);
            case "ROGUE": return new Color(0.4f, 0.7f, 0.4f, 1f);
            case "MAGE": return new Color(0.7f, 0.3f, 0.7f, 1f);
            default: return Color.WHITE;
        }
    }
}
