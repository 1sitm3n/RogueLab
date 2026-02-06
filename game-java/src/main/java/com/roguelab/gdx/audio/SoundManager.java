package com.roguelab.gdx.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all game sound effects.
 * Generates procedural 8-bit sounds at startup.
 */
public class SoundManager {

    public enum SoundEffect {
        // Combat
        ATTACK_SWORD,
        HIT_IMPACT,
        PLAYER_HURT,
        ENEMY_DEATH,
        BOSS_APPEAR,
        
        // Pickups
        GOLD_PICKUP,
        ITEM_PICKUP,
        LEVEL_UP,
        HEAL,
        
        // Navigation
        FOOTSTEP,
        DOOR_OPEN,
        STAIRS_DESCEND,
        
        // UI
        MENU_SELECT,
        MENU_CONFIRM,
        SHOP_BUY,
        ERROR,
        
        // Game state
        VICTORY,
        DEFEAT
    }

    private final Map<SoundEffect, Sound> sounds = new HashMap<>();
    private boolean enabled = true;
    private float masterVolume = 0.7f;

    /**
     * Load all sounds. Call during game initialization.
     */
    public void load() {
        Gdx.app.log("SoundManager", "Generating procedural sounds...");
        long startTime = System.currentTimeMillis();

        // Combat sounds
        sounds.put(SoundEffect.ATTACK_SWORD, 
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.attackSword()));
        sounds.put(SoundEffect.HIT_IMPACT,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.hitImpact()));
        sounds.put(SoundEffect.PLAYER_HURT,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.playerHurt()));
        sounds.put(SoundEffect.ENEMY_DEATH,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.enemyDeath()));
        sounds.put(SoundEffect.BOSS_APPEAR,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.bossAppear()));

        // Pickup sounds
        sounds.put(SoundEffect.GOLD_PICKUP,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.goldPickup()));
        sounds.put(SoundEffect.ITEM_PICKUP,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.itemPickup()));
        sounds.put(SoundEffect.LEVEL_UP,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.levelUp()));
        sounds.put(SoundEffect.HEAL,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.heal()));

        // Navigation sounds
        sounds.put(SoundEffect.FOOTSTEP,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.footstep()));
        sounds.put(SoundEffect.DOOR_OPEN,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.doorOpen()));
        sounds.put(SoundEffect.STAIRS_DESCEND,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.stairsDescend()));

        // UI sounds
        sounds.put(SoundEffect.MENU_SELECT,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.menuSelect()));
        sounds.put(SoundEffect.MENU_CONFIRM,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.menuConfirm()));
        sounds.put(SoundEffect.SHOP_BUY,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.shopBuy()));
        sounds.put(SoundEffect.ERROR,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.error()));

        // Game state sounds
        sounds.put(SoundEffect.VICTORY,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.victory()));
        sounds.put(SoundEffect.DEFEAT,
            ProceduralSoundGenerator.createSound(ProceduralSoundGenerator.defeat()));

        long elapsed = System.currentTimeMillis() - startTime;
        Gdx.app.log("SoundManager", "Generated " + sounds.size() + " sounds in " + elapsed + "ms");
    }

    /**
     * Play a sound effect.
     */
    public void play(SoundEffect effect) {
        play(effect, 1.0f);
    }

    /**
     * Play a sound effect with volume modifier.
     */
    public void play(SoundEffect effect, float volumeModifier) {
        if (!enabled) return;
        
        Sound sound = sounds.get(effect);
        if (sound != null) {
            sound.play(masterVolume * volumeModifier);
        }
    }

    /**
     * Play a sound with pitch variation (good for footsteps, hits).
     */
    public void playWithVariation(SoundEffect effect, float volumeModifier, float pitchVariation) {
        if (!enabled) return;
        
        Sound sound = sounds.get(effect);
        if (sound != null) {
            float pitch = 1.0f + (float)(Math.random() * 2 - 1) * pitchVariation;
            sound.play(masterVolume * volumeModifier, pitch, 0);
        }
    }

    /**
     * Enable or disable all sounds.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set master volume (0.0 to 1.0).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Toggle sound on/off.
     */
    public void toggle() {
        enabled = !enabled;
    }

    /**
     * Dispose all sounds.
     */
    public void dispose() {
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        sounds.clear();
    }
}
