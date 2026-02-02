package com.roguelab.gdx.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.roguelab.gdx.RogueLabGame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all visual effects: damage numbers, slashes, hits, etc.
 */
public class EffectsManager {
    
    private final RogueLabGame game;
    private final List<DamageNumber> damageNumbers;
    private final List<Effect> effects;
    
    public EffectsManager(RogueLabGame game) {
        this.game = game;
        this.damageNumbers = new ArrayList<>();
        this.effects = new ArrayList<>();
    }
    
    public void update(float delta) {
        // Update damage numbers
        Iterator<DamageNumber> dnIter = damageNumbers.iterator();
        while (dnIter.hasNext()) {
            DamageNumber dn = dnIter.next();
            dn.update(delta);
            if (dn.isFinished()) {
                dnIter.remove();
            }
        }
        
        // Update effects
        Iterator<Effect> effectIter = effects.iterator();
        while (effectIter.hasNext()) {
            Effect effect = effectIter.next();
            effect.update(delta);
            if (effect.isFinished()) {
                effectIter.remove();
            }
        }
    }
    
    public void render(SpriteBatch batch, float delta) {
        batch.begin();
        
        // Render effects
        for (Effect effect : effects) {
            effect.render(batch, game.getAssets());
        }
        
        // Render damage numbers
        for (DamageNumber dn : damageNumbers) {
            dn.render(batch, game.getAssets().getDamageFont());
        }
        
        batch.end();
    }
    
    /**
     * Add a floating damage number.
     */
    public void addDamageNumber(float x, float y, String text, Color color) {
        damageNumbers.add(new DamageNumber(x, y, text, color));
    }
    
    /**
     * Add a slash effect at the given position.
     */
    public void addSlash(float x, float y) {
        effects.add(new SlashEffect(x, y, game.getAssets().getSlashEffect()));
    }
    
    /**
     * Add a hit effect at the given position.
     */
    public void addHit(float x, float y) {
        effects.add(new HitEffect(x, y, game.getAssets().getHitEffect()));
    }
    
    // === Inner Effect Classes ===
    
    private static abstract class Effect {
        protected float x, y;
        protected float lifetime;
        protected float maxLifetime;
        protected TextureRegion texture;
        
        public Effect(float x, float y, TextureRegion texture, float lifetime) {
            this.x = x;
            this.y = y;
            this.texture = texture;
            this.lifetime = lifetime;
            this.maxLifetime = lifetime;
        }
        
        public void update(float delta) {
            lifetime -= delta;
        }
        
        public boolean isFinished() {
            return lifetime <= 0;
        }
        
        public abstract void render(SpriteBatch batch, com.roguelab.gdx.Assets assets);
        
        protected float getProgress() {
            return 1f - (lifetime / maxLifetime);
        }
    }
    
    private static class SlashEffect extends Effect {
        private float rotation;
        
        public SlashEffect(float x, float y, TextureRegion texture) {
            super(x, y, texture, 0.3f);
            this.rotation = -45f;
        }
        
        @Override
        public void update(float delta) {
            super.update(delta);
            rotation += delta * 180f; // Rotate during animation
        }
        
        @Override
        public void render(SpriteBatch batch, com.roguelab.gdx.Assets assets) {
            float progress = getProgress();
            float alpha = 1f - progress;
            float scale = 0.5f + progress * 0.5f;
            
            batch.setColor(1f, 1f, 1f, alpha);
            
            float width = texture.getRegionWidth() * scale;
            float height = texture.getRegionHeight() * scale;
            
            batch.draw(texture,
                x - width / 2f, y - height / 2f,
                width / 2f, height / 2f,
                width, height,
                1f, 1f,
                rotation);
            
            batch.setColor(Color.WHITE);
        }
    }
    
    private static class HitEffect extends Effect {
        
        public HitEffect(float x, float y, TextureRegion texture) {
            super(x, y, texture, 0.2f);
        }
        
        @Override
        public void render(SpriteBatch batch, com.roguelab.gdx.Assets assets) {
            float progress = getProgress();
            float alpha = 1f - progress;
            float scale = 1f + progress * 0.5f;
            
            batch.setColor(1f, 1f, 1f, alpha);
            
            float width = texture.getRegionWidth() * scale;
            float height = texture.getRegionHeight() * scale;
            
            batch.draw(texture,
                x - width / 2f, y - height / 2f,
                width, height);
            
            batch.setColor(Color.WHITE);
        }
    }
}
