package com.roguelab.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all game assets: textures, fonts, and sounds.
 * 
 * For now, generates procedural placeholder graphics.
 * Can be replaced with loaded assets later.
 */
public class Assets implements Disposable {
    
    // Fonts
    private BitmapFont titleFont;
    private BitmapFont normalFont;
    private BitmapFont smallFont;
    private BitmapFont damageFont;
    
    // Tile textures (32x32)
    private Map<String, TextureRegion> tiles;
    
    // Entity textures
    private Map<String, TextureRegion> playerSprites;
    private Map<String, TextureRegion> enemySprites;
    private Map<String, TextureRegion> itemSprites;
    
    // UI textures
    private Texture whitePixel;
    private TextureRegion healthBarBg;
    private TextureRegion healthBarFill;
    private TextureRegion panelBackground;
    
    // Effect textures
    private TextureRegion slashEffect;
    private TextureRegion hitEffect;
    
    public void load() {
        loadFonts();
        loadTiles();
        loadEntities();
        loadUI();
        loadEffects();
        
        Gdx.app.log("Assets", "All assets loaded");
    }
    
    private void loadFonts() {
        // Use default bitmap font for now
        // Can be replaced with FreeType fonts later
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        
        normalFont = new BitmapFont();
        normalFont.getData().setScale(1.5f);
        normalFont.setColor(Color.WHITE);
        
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1f);
        smallFont.setColor(Color.LIGHT_GRAY);
        
        damageFont = new BitmapFont();
        damageFont.getData().setScale(2f);
        damageFont.setColor(Color.RED);
    }
    
    private void loadTiles() {
        tiles = new HashMap<>();
        
        // Generate procedural tiles
        tiles.put("floor", createColoredTile(new Color(0.3f, 0.25f, 0.2f, 1f), true));
        tiles.put("wall", createColoredTile(new Color(0.4f, 0.35f, 0.3f, 1f), false));
        tiles.put("door", createColoredTile(new Color(0.5f, 0.35f, 0.2f, 1f), false));
        tiles.put("stairs_down", createStairsTile(false));
        tiles.put("stairs_up", createStairsTile(true));
        tiles.put("chest", createChestTile());
        tiles.put("rest", createRestTile());
        tiles.put("shop", createShopTile());
        tiles.put("combat", createCombatTile());
        tiles.put("boss", createBossTile());
        tiles.put("fog", createColoredTile(new Color(0f, 0f, 0f, 0.8f), false));
    }
    
    private void loadEntities() {
        playerSprites = new HashMap<>();
        enemySprites = new HashMap<>();
        itemSprites = new HashMap<>();
        
        // Player sprites by class
        playerSprites.put("WARRIOR", createCharacterSprite(new Color(0.2f, 0.4f, 0.8f, 1f), "W"));
        playerSprites.put("ROGUE", createCharacterSprite(new Color(0.3f, 0.6f, 0.3f, 1f), "R"));
        playerSprites.put("MAGE", createCharacterSprite(new Color(0.6f, 0.2f, 0.6f, 1f), "M"));
        
        // Enemy sprites by type
        enemySprites.put("RAT", createCharacterSprite(new Color(0.5f, 0.4f, 0.3f, 1f), "r"));
        enemySprites.put("SLIME", createCharacterSprite(new Color(0.2f, 0.7f, 0.2f, 1f), "s"));
        enemySprites.put("BAT", createCharacterSprite(new Color(0.3f, 0.2f, 0.3f, 1f), "b"));
        enemySprites.put("GOBLIN", createCharacterSprite(new Color(0.3f, 0.5f, 0.2f, 1f), "g"));
        enemySprites.put("SKELETON", createCharacterSprite(new Color(0.9f, 0.9f, 0.85f, 1f), "S"));
        enemySprites.put("ZOMBIE", createCharacterSprite(new Color(0.4f, 0.5f, 0.3f, 1f), "Z"));
        enemySprites.put("SPIDER", createCharacterSprite(new Color(0.2f, 0.2f, 0.2f, 1f), "X"));
        enemySprites.put("ORC", createCharacterSprite(new Color(0.3f, 0.4f, 0.2f, 1f), "O"));
        enemySprites.put("TROLL", createCharacterSprite(new Color(0.4f, 0.5f, 0.4f, 1f), "T"));
        enemySprites.put("WRAITH", createCharacterSprite(new Color(0.6f, 0.6f, 0.8f, 1f), "W"));
        enemySprites.put("ELEMENTAL", createCharacterSprite(new Color(0.9f, 0.4f, 0.1f, 1f), "E"));
        enemySprites.put("GOLEM", createCharacterSprite(new Color(0.5f, 0.5f, 0.5f, 1f), "G"));
        enemySprites.put("GOBLIN_KING", createCharacterSprite(new Color(0.8f, 0.6f, 0.1f, 1f), "K"));
        enemySprites.put("NECROMANCER", createCharacterSprite(new Color(0.3f, 0.1f, 0.4f, 1f), "N"));
        enemySprites.put("DRAGON", createCharacterSprite(new Color(0.8f, 0.2f, 0.1f, 1f), "D"));
        
        // Item sprites
        itemSprites.put("WEAPON", createItemSprite(new Color(0.7f, 0.7f, 0.7f, 1f), "/"));
        itemSprites.put("ARMOR", createItemSprite(new Color(0.5f, 0.5f, 0.6f, 1f), "["));
        itemSprites.put("POTION", createItemSprite(new Color(0.8f, 0.2f, 0.2f, 1f), "!"));
        itemSprites.put("RING", createItemSprite(new Color(0.9f, 0.8f, 0.2f, 1f), "o"));
        itemSprites.put("AMULET", createItemSprite(new Color(0.2f, 0.8f, 0.8f, 1f), "\""));
        itemSprites.put("GOLD", createItemSprite(new Color(1f, 0.85f, 0f, 1f), "$"));
    }
    
    private void loadUI() {
        // Create a 1x1 white pixel for drawing colored rectangles
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
        
        // Health bar components
        healthBarBg = createColoredRegion(new Color(0.2f, 0.2f, 0.2f, 1f), 100, 12);
        healthBarFill = createColoredRegion(new Color(0.8f, 0.2f, 0.2f, 1f), 100, 12);
        
        // Panel background
        panelBackground = createPanelTexture();
    }
    
    private void loadEffects() {
        slashEffect = createSlashTexture();
        hitEffect = createHitTexture();
    }
    
    // === Procedural texture generation ===
    
    private TextureRegion createColoredTile(Color color, boolean addNoise) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                float noise = addNoise ? (float)(Math.random() * 0.1 - 0.05) : 0;
                pixmap.setColor(
                    Math.max(0, Math.min(1, color.r + noise)),
                    Math.max(0, Math.min(1, color.g + noise)),
                    Math.max(0, Math.min(1, color.b + noise)),
                    color.a
                );
                pixmap.drawPixel(x, y);
            }
        }
        
        // Add border
        pixmap.setColor(new Color(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f, 1f));
        pixmap.drawRectangle(0, 0, 32, 32);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createStairsTile(boolean up) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        pixmap.fill();
        
        // Draw stairs pattern
        pixmap.setColor(new Color(0.5f, 0.45f, 0.4f, 1f));
        for (int i = 0; i < 4; i++) {
            int y = up ? (24 - i * 6) : (6 + i * 6);
            pixmap.fillRectangle(4 + i * 2, y, 24 - i * 4, 4);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createChestTile() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        pixmap.fill();
        
        // Chest body
        pixmap.setColor(new Color(0.6f, 0.4f, 0.2f, 1f));
        pixmap.fillRectangle(6, 10, 20, 14);
        
        // Chest lid
        pixmap.setColor(new Color(0.5f, 0.35f, 0.15f, 1f));
        pixmap.fillRectangle(6, 8, 20, 6);
        
        // Lock
        pixmap.setColor(new Color(0.9f, 0.8f, 0.2f, 1f));
        pixmap.fillRectangle(14, 14, 4, 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createRestTile() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        pixmap.fill();
        
        // Campfire
        pixmap.setColor(new Color(0.4f, 0.25f, 0.1f, 1f));
        pixmap.fillRectangle(10, 20, 12, 4); // logs
        
        pixmap.setColor(new Color(0.9f, 0.5f, 0.1f, 1f));
        pixmap.fillTriangle(16, 8, 10, 20, 22, 20); // flame
        
        pixmap.setColor(new Color(1f, 0.8f, 0.2f, 1f));
        pixmap.fillTriangle(16, 12, 13, 18, 19, 18); // inner flame
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createShopTile() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        pixmap.fill();
        
        // Shop counter
        pixmap.setColor(new Color(0.5f, 0.35f, 0.2f, 1f));
        pixmap.fillRectangle(4, 16, 24, 10);
        
        // Coin symbol
        pixmap.setColor(new Color(1f, 0.85f, 0f, 1f));
        pixmap.fillCircle(16, 10, 6);
        pixmap.setColor(new Color(0.8f, 0.65f, 0f, 1f));
        pixmap.fillCircle(16, 10, 3);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createCombatTile() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.35f, 0.25f, 0.2f, 1f));
        pixmap.fill();
        
        // Skull symbol
        pixmap.setColor(new Color(0.9f, 0.1f, 0.1f, 1f));
        pixmap.fillCircle(16, 14, 8);
        pixmap.setColor(new Color(0.35f, 0.25f, 0.2f, 1f));
        pixmap.fillCircle(12, 12, 2); // eye
        pixmap.fillCircle(20, 12, 2); // eye
        pixmap.fillRectangle(14, 18, 4, 4); // jaw
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createBossTile() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.4f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        
        // Crown symbol
        pixmap.setColor(new Color(1f, 0.85f, 0f, 1f));
        pixmap.fillRectangle(8, 16, 16, 8);
        pixmap.fillTriangle(8, 16, 8, 8, 12, 16);
        pixmap.fillTriangle(16, 16, 16, 6, 16, 16);
        pixmap.fillTriangle(24, 16, 24, 8, 20, 16);
        
        // Gems
        pixmap.setColor(new Color(0.8f, 0.1f, 0.1f, 1f));
        pixmap.fillCircle(12, 20, 2);
        pixmap.fillCircle(20, 20, 2);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createCharacterSprite(Color color, String symbol) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        
        // Body circle
        pixmap.setColor(color);
        pixmap.fillCircle(16, 16, 12);
        
        // Outline
        pixmap.setColor(new Color(color.r * 0.6f, color.g * 0.6f, color.b * 0.6f, 1f));
        pixmap.drawCircle(16, 16, 12);
        
        // The symbol will be drawn by the font, this is just the background
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createItemSprite(Color color, String symbol) {
        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        
        // Item background
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        pixmap.fillCircle(12, 12, 10);
        
        // Item color
        pixmap.setColor(color);
        pixmap.fillCircle(12, 12, 8);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createColoredRegion(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createPanelTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        pixmap.fill();
        
        // Border
        pixmap.setColor(new Color(0.4f, 0.4f, 0.5f, 1f));
        pixmap.drawRectangle(0, 0, 32, 32);
        pixmap.drawRectangle(1, 1, 30, 30);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createSlashTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        
        // Slash arc
        pixmap.setColor(new Color(1f, 1f, 1f, 0.8f));
        for (int i = 0; i < 3; i++) {
            pixmap.drawLine(10 + i, 54 - i, 54 - i, 10 + i);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    private TextureRegion createHitTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        
        // Star burst
        pixmap.setColor(new Color(1f, 0.8f, 0.2f, 0.9f));
        pixmap.fillCircle(16, 16, 8);
        
        pixmap.setColor(new Color(1f, 1f, 1f, 1f));
        pixmap.fillCircle(16, 16, 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
    
    // === Getters ===
    
    public BitmapFont getTitleFont() { return titleFont; }
    public BitmapFont getNormalFont() { return normalFont; }
    public BitmapFont getSmallFont() { return smallFont; }
    public BitmapFont getDamageFont() { return damageFont; }
    
    public TextureRegion getTile(String name) {
        return tiles.getOrDefault(name, tiles.get("floor"));
    }
    
    public TextureRegion getPlayerSprite(String playerClass) {
        return playerSprites.getOrDefault(playerClass, playerSprites.get("WARRIOR"));
    }
    
    public TextureRegion getEnemySprite(String enemyType) {
        return enemySprites.getOrDefault(enemyType, enemySprites.get("RAT"));
    }
    
    public TextureRegion getItemSprite(String itemType) {
        return itemSprites.getOrDefault(itemType, itemSprites.get("GOLD"));
    }
    
    public Texture getWhitePixel() { return whitePixel; }
    public TextureRegion getHealthBarBg() { return healthBarBg; }
    public TextureRegion getHealthBarFill() { return healthBarFill; }
    public TextureRegion getPanelBackground() { return panelBackground; }
    public TextureRegion getSlashEffect() { return slashEffect; }
    public TextureRegion getHitEffect() { return hitEffect; }
    
    @Override
    public void dispose() {
        titleFont.dispose();
        normalFont.dispose();
        smallFont.dispose();
        damageFont.dispose();
        whitePixel.dispose();
        
        // Dispose all texture regions
        for (TextureRegion region : tiles.values()) {
            region.getTexture().dispose();
        }
        for (TextureRegion region : playerSprites.values()) {
            region.getTexture().dispose();
        }
        for (TextureRegion region : enemySprites.values()) {
            region.getTexture().dispose();
        }
        for (TextureRegion region : itemSprites.values()) {
            region.getTexture().dispose();
        }
        
        healthBarBg.getTexture().dispose();
        healthBarFill.getTexture().dispose();
        panelBackground.getTexture().dispose();
        slashEffect.getTexture().dispose();
        hitEffect.getTexture().dispose();
    }
}
