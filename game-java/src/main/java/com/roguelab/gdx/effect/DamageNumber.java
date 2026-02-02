package com.roguelab.gdx.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A floating damage number that rises and fades out.
 */
public class DamageNumber {
    
    private float x, y;
    private final String text;
    private final Color color;
    private float lifetime;
    private final float maxLifetime;
    private float velocityY;
    
    private static final GlyphLayout layout = new GlyphLayout();
    
    public DamageNumber(float x, float y, String text, Color color) {
        this(x, y, text, color, 1.5f);
    }
    
    public DamageNumber(float x, float y, String text, Color color, float lifetime) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = new Color(color);
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.velocityY = 80f; // Initial upward velocity
    }
    
    public void update(float delta) {
        lifetime -= delta;
        
        // Rise up with deceleration
        y += velocityY * delta;
        velocityY *= 0.95f; // Slow down over time
        
        // Slight horizontal drift for variety
        x += (float)Math.sin(lifetime * 5) * delta * 10;
    }
    
    public void render(SpriteBatch batch, BitmapFont font) {
        // Calculate alpha based on remaining lifetime
        float progress = lifetime / maxLifetime;
        float alpha = progress;
        
        // Fade out more dramatically in last 30%
        if (progress < 0.3f) {
            alpha = progress / 0.3f * 0.3f;
        }
        
        // Scale effect - grow slightly at start, then shrink
        float scale;
        if (progress > 0.8f) {
            // Start: grow from 0.5 to 1.0
            scale = 0.5f + (1f - (progress - 0.8f) / 0.2f) * 0.5f;
        } else {
            scale = 1f;
        }
        
        font.setColor(color.r, color.g, color.b, alpha);
        font.getData().setScale(2f * scale);
        
        layout.setText(font, text);
        font.draw(batch, text, x - layout.width / 2f, y);
        
        // Reset font
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }
    
    public boolean isFinished() {
        return lifetime <= 0;
    }
}
