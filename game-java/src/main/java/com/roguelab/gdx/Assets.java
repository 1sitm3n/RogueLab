package com.roguelab.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Daggerfall-style procedural assets.
 * Creates stone textures, parchment backgrounds, and gothic UI elements.
 */
public class Assets {

    // Textures
    private Texture tilesetTexture;
    private Texture uiTexture;
    private Texture portraitTexture;
    
    private Map<String, TextureRegion> tiles;
    private Map<String, TextureRegion> uiElements;
    private Map<String, TextureRegion> playerSprites;
    private Map<String, TextureRegion> enemySprites;
    private Map<String, TextureRegion> portraits;
    
    private TextureRegion whitePixel;
    
    // Fonts
    private BitmapFont titleFont;
    private BitmapFont normalFont;
    private BitmapFont smallFont;

    // Daggerfall color palette
    public static final Color STONE_DARK = new Color(0.15f, 0.13f, 0.12f, 1f);
    public static final Color STONE_MID = new Color(0.25f, 0.22f, 0.2f, 1f);
    public static final Color STONE_LIGHT = new Color(0.35f, 0.32f, 0.28f, 1f);
    public static final Color STONE_HIGHLIGHT = new Color(0.45f, 0.4f, 0.35f, 1f);
    
    public static final Color PARCHMENT_DARK = new Color(0.55f, 0.45f, 0.3f, 1f);
    public static final Color PARCHMENT_MID = new Color(0.7f, 0.6f, 0.4f, 1f);
    public static final Color PARCHMENT_LIGHT = new Color(0.85f, 0.75f, 0.55f, 1f);
    
    public static final Color GOLD_DARK = new Color(0.5f, 0.35f, 0.1f, 1f);
    public static final Color GOLD_MID = new Color(0.7f, 0.55f, 0.2f, 1f);
    public static final Color GOLD_LIGHT = new Color(0.9f, 0.75f, 0.3f, 1f);
    
    public static final Color BLOOD_RED = new Color(0.6f, 0.1f, 0.1f, 1f);
    public static final Color HEALTH_GREEN = new Color(0.2f, 0.5f, 0.2f, 1f);
    public static final Color MANA_BLUE = new Color(0.15f, 0.2f, 0.5f, 1f);
    
    public static final Color TORCH_ORANGE = new Color(1f, 0.7f, 0.3f, 1f);
    public static final Color TORCH_AMBIENT = new Color(0.3f, 0.2f, 0.1f, 1f);

    public void load() {
        tiles = new HashMap<>();
        uiElements = new HashMap<>();
        playerSprites = new HashMap<>();
        enemySprites = new HashMap<>();
        portraits = new HashMap<>();

        generateTileset();
        generateUIElements();
        generateCharacterSprites();
        generatePortraits();
        generateFonts();
        
        // White pixel for drawing
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        whitePixel = new TextureRegion(new Texture(px));
        px.dispose();
    }

    private void generateTileset() {
        int tileSize = 64;
        int cols = 8;
        int rows = 8;
        
        Pixmap pixmap = new Pixmap(cols * tileSize, rows * tileSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(42);

        // Row 0: Floor tiles (stone brick patterns)
        generateStoneBrickTile(pixmap, 0, 0, tileSize, rand, STONE_MID, false);
        generateStoneBrickTile(pixmap, tileSize, 0, tileSize, rand, STONE_DARK, false);
        generateStoneBrickTile(pixmap, tileSize * 2, 0, tileSize, rand, STONE_LIGHT, false);
        generateMossyStoneTile(pixmap, tileSize * 3, 0, tileSize, rand);

        // Row 1: Wall tiles
        generateStoneBrickTile(pixmap, 0, tileSize, tileSize, rand, STONE_DARK, true);
        generateStoneBrickTile(pixmap, tileSize, tileSize, tileSize, rand, STONE_MID, true);
        generateTorchTile(pixmap, tileSize * 2, tileSize, tileSize, rand);
        generateDoorTile(pixmap, tileSize * 3, tileSize, tileSize, rand);

        // Row 2: Room type icons (Daggerfall style)
        generateCombatIcon(pixmap, 0, tileSize * 2, tileSize, rand);
        generateBossIcon(pixmap, tileSize, tileSize * 2, tileSize, rand);
        generateTreasureIcon(pixmap, tileSize * 2, tileSize * 2, tileSize, rand);
        generateShopIcon(pixmap, tileSize * 3, tileSize * 2, tileSize, rand);
        generateRestIcon(pixmap, tileSize * 4, tileSize * 2, tileSize, rand);
        generateStairsIcon(pixmap, tileSize * 5, tileSize * 2, tileSize, rand);
        generateFogTile(pixmap, tileSize * 6, tileSize * 2, tileSize, rand);

        tilesetTexture = new Texture(pixmap);
        pixmap.dispose();

        // Map tile names
        tiles.put("floor", new TextureRegion(tilesetTexture, 0, 0, tileSize, tileSize));
        tiles.put("floor_dark", new TextureRegion(tilesetTexture, tileSize, 0, tileSize, tileSize));
        tiles.put("floor_light", new TextureRegion(tilesetTexture, tileSize * 2, 0, tileSize, tileSize));
        tiles.put("floor_mossy", new TextureRegion(tilesetTexture, tileSize * 3, 0, tileSize, tileSize));
        
        tiles.put("wall", new TextureRegion(tilesetTexture, 0, tileSize, tileSize, tileSize));
        tiles.put("wall_light", new TextureRegion(tilesetTexture, tileSize, tileSize, tileSize, tileSize));
        tiles.put("torch", new TextureRegion(tilesetTexture, tileSize * 2, tileSize, tileSize, tileSize));
        tiles.put("door", new TextureRegion(tilesetTexture, tileSize * 3, tileSize, tileSize, tileSize));
        
        tiles.put("combat", new TextureRegion(tilesetTexture, 0, tileSize * 2, tileSize, tileSize));
        tiles.put("boss", new TextureRegion(tilesetTexture, tileSize, tileSize * 2, tileSize, tileSize));
        tiles.put("chest", new TextureRegion(tilesetTexture, tileSize * 2, tileSize * 2, tileSize, tileSize));
        tiles.put("shop", new TextureRegion(tilesetTexture, tileSize * 3, tileSize * 2, tileSize, tileSize));
        tiles.put("rest", new TextureRegion(tilesetTexture, tileSize * 4, tileSize * 2, tileSize, tileSize));
        tiles.put("stairs_down", new TextureRegion(tilesetTexture, tileSize * 5, tileSize * 2, tileSize, tileSize));
        tiles.put("fog", new TextureRegion(tilesetTexture, tileSize * 6, tileSize * 2, tileSize, tileSize));
    }

    private void generateStoneBrickTile(Pixmap pm, int x, int y, int size, Random rand, Color baseColor, boolean isWall) {
        // Fill with base
        pm.setColor(baseColor);
        pm.fillRectangle(x, y, size, size);

        // Draw brick pattern
        int brickH = size / 4;
        int brickW = size / 2;
        
        for (int row = 0; row < 4; row++) {
            int offset = (row % 2 == 0) ? 0 : brickW / 2;
            for (int col = -1; col < 3; col++) {
                int bx = x + col * brickW + offset;
                int by = y + row * brickH;
                
                // Brick variation
                float variation = 0.9f + rand.nextFloat() * 0.2f;
                pm.setColor(baseColor.r * variation, baseColor.g * variation, baseColor.b * variation, 1f);
                pm.fillRectangle(bx + 1, by + 1, brickW - 2, brickH - 2);
                
                // Mortar lines (darker)
                pm.setColor(baseColor.r * 0.6f, baseColor.g * 0.6f, baseColor.b * 0.6f, 1f);
                pm.drawRectangle(bx, by, brickW, brickH);
            }
        }

        // Add some noise/wear
        for (int i = 0; i < size * 2; i++) {
            int px = x + rand.nextInt(size);
            int py = y + rand.nextInt(size);
            float shade = 0.8f + rand.nextFloat() * 0.4f;
            pm.setColor(baseColor.r * shade, baseColor.g * shade, baseColor.b * shade, 0.5f);
            pm.drawPixel(px, py);
        }

        // Wall gets darker at bottom (shadow)
        if (isWall) {
            for (int i = 0; i < size / 4; i++) {
                float alpha = (float) i / (size / 4) * 0.3f;
                pm.setColor(0, 0, 0, alpha);
                pm.drawLine(x, y + size - i - 1, x + size - 1, y + size - i - 1);
            }
        }
    }

    private void generateMossyStoneTile(Pixmap pm, int x, int y, int size, Random rand) {
        // Base stone
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        
        // Add moss patches
        Color moss = new Color(0.2f, 0.35f, 0.15f, 1f);
        for (int i = 0; i < 20; i++) {
            int px = x + rand.nextInt(size);
            int py = y + rand.nextInt(size);
            int patchSize = 2 + rand.nextInt(4);
            float alpha = 0.3f + rand.nextFloat() * 0.4f;
            pm.setColor(moss.r, moss.g, moss.b, alpha);
            pm.fillCircle(px, py, patchSize);
        }
    }

    private void generateTorchTile(Pixmap pm, int x, int y, int size, Random rand) {
        // Wall background
        generateStoneBrickTile(pm, x, y, size, rand, STONE_DARK, true);
        
        // Torch bracket
        pm.setColor(0.3f, 0.2f, 0.1f, 1f);
        pm.fillRectangle(x + size/2 - 3, y + size/2, 6, size/3);
        
        // Torch head
        pm.setColor(0.4f, 0.25f, 0.1f, 1f);
        pm.fillRectangle(x + size/2 - 4, y + size/3, 8, 12);
        
        // Flame
        pm.setColor(TORCH_ORANGE);
        pm.fillCircle(x + size/2, y + size/4, 8);
        pm.setColor(1f, 0.9f, 0.5f, 1f);
        pm.fillCircle(x + size/2, y + size/4, 4);
        
        // Glow effect
        for (int r = 20; r > 5; r -= 3) {
            float alpha = 0.05f * (20 - r) / 15f;
            pm.setColor(1f, 0.6f, 0.2f, alpha);
            pm.fillCircle(x + size/2, y + size/3, r);
        }
    }

    private void generateDoorTile(Pixmap pm, int x, int y, int size, Random rand) {
        // Stone frame
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        
        // Door wood
        pm.setColor(0.35f, 0.2f, 0.1f, 1f);
        pm.fillRectangle(x + 8, y + 4, size - 16, size - 8);
        
        // Wood grain
        pm.setColor(0.25f, 0.15f, 0.08f, 1f);
        for (int i = 0; i < 5; i++) {
            int lx = x + 12 + i * 8;
            pm.drawLine(lx, y + 8, lx, y + size - 8);
        }
        
        // Door handle
        pm.setColor(GOLD_MID);
        pm.fillCircle(x + size - 16, y + size/2, 4);
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size - 16, y + size/2 - 1, 2);
        
        // Iron bands
        pm.setColor(0.3f, 0.3f, 0.35f, 1f);
        pm.fillRectangle(x + 6, y + 12, size - 12, 4);
        pm.fillRectangle(x + 6, y + size - 16, size - 12, 4);
    }

    private void generateCombatIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Stone background
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        
        // Crossed swords
        pm.setColor(0.6f, 0.6f, 0.65f, 1f);
        // Sword 1
        pm.fillRectangle(x + 12, y + 8, 4, 40);
        pm.fillRectangle(x + 8, y + 38, 12, 4);
        // Sword 2
        pm.fillRectangle(x + size - 16, y + 8, 4, 40);
        pm.fillRectangle(x + size - 20, y + 38, 12, 4);
        
        // Skull in center
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillCircle(x + size/2, y + size/2, 12);
        pm.setColor(STONE_DARK);
        pm.fillCircle(x + size/2 - 4, y + size/2 - 2, 3);
        pm.fillCircle(x + size/2 + 4, y + size/2 - 2, 3);
        pm.fillRectangle(x + size/2 - 4, y + size/2 + 4, 8, 4);
    }

    private void generateBossIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Dark red background
        pm.setColor(0.25f, 0.08f, 0.08f, 1f);
        pm.fillRectangle(x, y, size, size);
        
        // Stone border
        pm.setColor(STONE_DARK);
        pm.drawRectangle(x, y, size, size);
        pm.drawRectangle(x + 1, y + 1, size - 2, size - 2);
        
        // Demon skull
        pm.setColor(0.8f, 0.75f, 0.7f, 1f);
        pm.fillCircle(x + size/2, y + size/2 + 4, 16);
        
        // Horns
        pm.setColor(0.5f, 0.4f, 0.35f, 1f);
        for (int i = 0; i < 12; i++) {
            pm.drawPixel(x + size/2 - 14 + i/2, y + size/2 - 8 - i);
            pm.drawPixel(x + size/2 + 14 - i/2, y + size/2 - 8 - i);
        }
        
        // Eyes (glowing red)
        pm.setColor(BLOOD_RED);
        pm.fillCircle(x + size/2 - 6, y + size/2, 4);
        pm.fillCircle(x + size/2 + 6, y + size/2, 4);
        pm.setColor(1f, 0.3f, 0.2f, 1f);
        pm.fillCircle(x + size/2 - 6, y + size/2, 2);
        pm.fillCircle(x + size/2 + 6, y + size/2, 2);
    }

    private void generateTreasureIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Stone background
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        
        // Chest body
        pm.setColor(0.4f, 0.25f, 0.1f, 1f);
        pm.fillRectangle(x + 10, y + 20, size - 20, size - 30);
        
        // Chest lid
        pm.setColor(0.45f, 0.3f, 0.12f, 1f);
        pm.fillRectangle(x + 8, y + 12, size - 16, 12);
        
        // Gold trim
        pm.setColor(GOLD_MID);
        pm.fillRectangle(x + 10, y + 18, size - 20, 3);
        pm.fillRectangle(x + size/2 - 6, y + 20, 12, 20);
        
        // Lock
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size/2, y + 30, 5);
        pm.setColor(GOLD_DARK);
        pm.fillCircle(x + size/2, y + 30, 2);
        
        // Gold coins peeking out
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size/2 - 8, y + 8, 4);
        pm.fillCircle(x + size/2, y + 6, 4);
        pm.fillCircle(x + size/2 + 8, y + 8, 4);
    }

    private void generateShopIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Parchment background
        pm.setColor(PARCHMENT_MID);
        pm.fillRectangle(x, y, size, size);
        
        // Border
        pm.setColor(STONE_DARK);
        pm.drawRectangle(x, y, size, size);
        
        // Scales of commerce
        pm.setColor(GOLD_MID);
        // Center post
        pm.fillRectangle(x + size/2 - 2, y + 16, 4, 32);
        // Top bar
        pm.fillRectangle(x + 12, y + 14, size - 24, 4);
        // Pans
        pm.setColor(GOLD_DARK);
        pm.fillRectangle(x + 10, y + 40, 16, 3);
        pm.fillRectangle(x + size - 26, y + 36, 16, 3);
        // Chains
        pm.setColor(0.5f, 0.45f, 0.3f, 1f);
        pm.drawLine(x + 18, y + 18, x + 18, y + 40);
        pm.drawLine(x + size - 18, y + 18, x + size - 18, y + 36);
        
        // Coin
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size/2, y + size - 14, 8);
        pm.setColor(GOLD_DARK);
        pm.drawCircle(x + size/2, y + size - 14, 8);
    }

    private void generateRestIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Dark stone background
        generateStoneBrickTile(pm, x, y, size, rand, STONE_DARK, false);
        
        // Campfire logs
        pm.setColor(0.35f, 0.2f, 0.1f, 1f);
        pm.fillRectangle(x + 12, y + size - 20, 20, 6);
        pm.fillRectangle(x + size - 32, y + size - 18, 20, 6);
        
        // Fire
        pm.setColor(BLOOD_RED);
        pm.fillCircle(x + size/2, y + size/2 + 8, 10);
        pm.setColor(TORCH_ORANGE);
        pm.fillCircle(x + size/2, y + size/2 + 4, 8);
        pm.setColor(1f, 0.9f, 0.4f, 1f);
        pm.fillCircle(x + size/2, y + size/2, 5);
        
        // Glow
        for (int r = 18; r > 8; r -= 2) {
            float alpha = 0.1f * (18 - r) / 10f;
            pm.setColor(1f, 0.5f, 0.2f, alpha);
            pm.fillCircle(x + size/2, y + size/2 + 4, r);
        }
    }

    private void generateStairsIcon(Pixmap pm, int x, int y, int size, Random rand) {
        // Stone background
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        
        // Stairs going down
        int steps = 5;
        int stepH = size / steps;
        int stepW = (size - 16) / steps;
        
        for (int i = 0; i < steps; i++) {
            float shade = 0.35f - i * 0.05f;
            pm.setColor(shade, shade * 0.9f, shade * 0.85f, 1f);
            int sx = x + 8 + i * stepW;
            int sy = y + 8 + i * stepH;
            int sw = size - 16 - i * stepW;
            int sh = size - 8 - i * stepH;
            pm.fillRectangle(sx, sy, sw, sh);
            
            // Step edge highlight
            pm.setColor(shade + 0.1f, shade + 0.08f, shade + 0.06f, 1f);
            pm.fillRectangle(sx, sy, sw, 3);
        }
        
        // Arrow pointing down
        pm.setColor(GOLD_MID);
        int cx = x + size/2;
        int cy = y + size/2;
        pm.fillTriangle(cx - 10, cy - 8, cx + 10, cy - 8, cx, cy + 10);
    }

    private void generateFogTile(Pixmap pm, int x, int y, int size, Random rand) {
        // Dark unknown
        pm.setColor(0.05f, 0.05f, 0.08f, 1f);
        pm.fillRectangle(x, y, size, size);
        
        // Question mark
        pm.setColor(0.2f, 0.2f, 0.25f, 1f);
        pm.fillCircle(x + size/2, y + size/3, 12);
        pm.setColor(0.05f, 0.05f, 0.08f, 1f);
        pm.fillCircle(x + size/2, y + size/3, 6);
        pm.fillRectangle(x + size/2 - 8, y + size/3, 16, 12);
        pm.setColor(0.2f, 0.2f, 0.25f, 1f);
        pm.fillRectangle(x + size/2 - 3, y + size/3 + 4, 6, 16);
        pm.fillCircle(x + size/2, y + size - 16, 4);
    }

    private void generateUIElements() {
        int uiSize = 256;
        Pixmap pm = new Pixmap(uiSize, uiSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(123);

        // Stone panel corner (0,0) - 32x32
        generateStoneCorner(pm, 0, 0, 32, rand);
        
        // Stone panel edge horizontal (32,0) - 64x32
        generateStoneEdgeH(pm, 32, 0, 64, 32, rand);
        
        // Stone panel edge vertical (0,32) - 32x64
        generateStoneEdgeV(pm, 0, 32, 32, 64, rand);
        
        // Parchment background (96,0) - 64x64
        generateParchment(pm, 96, 0, 64, 64, rand);
        
        // Health bar frame (0,96) - 128x24
        generateBarFrame(pm, 0, 96, 128, 24, rand, BLOOD_RED);
        
        // Mana bar frame (0,120) - 128x24
        generateBarFrame(pm, 0, 120, 128, 24, rand, MANA_BLUE);

        uiTexture = new Texture(pm);
        pm.dispose();

        uiElements.put("corner", new TextureRegion(uiTexture, 0, 0, 32, 32));
        uiElements.put("edge_h", new TextureRegion(uiTexture, 32, 0, 64, 32));
        uiElements.put("edge_v", new TextureRegion(uiTexture, 0, 32, 32, 64));
        uiElements.put("parchment", new TextureRegion(uiTexture, 96, 0, 64, 64));
        uiElements.put("health_frame", new TextureRegion(uiTexture, 0, 96, 128, 24));
        uiElements.put("mana_frame", new TextureRegion(uiTexture, 0, 120, 128, 24));
    }

    private void generateStoneCorner(Pixmap pm, int x, int y, int size, Random rand) {
        // Outer dark
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        
        // Inner lighter
        pm.setColor(STONE_MID);
        pm.fillRectangle(x + 4, y + 4, size - 8, size - 8);
        
        // Bevel highlight
        pm.setColor(STONE_HIGHLIGHT);
        pm.drawLine(x + 2, y + 2, x + size - 3, y + 2);
        pm.drawLine(x + 2, y + 2, x + 2, y + size - 3);
        
        // Bevel shadow
        pm.setColor(0.1f, 0.08f, 0.06f, 1f);
        pm.drawLine(x + size - 3, y + 3, x + size - 3, y + size - 3);
        pm.drawLine(x + 3, y + size - 3, x + size - 3, y + size - 3);
    }

    private void generateStoneEdgeH(Pixmap pm, int x, int y, int w, int h, Random rand) {
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, w, h);
        pm.setColor(STONE_MID);
        pm.fillRectangle(x, y + 4, w, h - 8);
        
        // Texture variation
        for (int i = 0; i < w; i += 8) {
            float v = 0.95f + rand.nextFloat() * 0.1f;
            pm.setColor(STONE_MID.r * v, STONE_MID.g * v, STONE_MID.b * v, 1f);
            pm.fillRectangle(x + i, y + 4, 8, h - 8);
        }
        
        // Highlights
        pm.setColor(STONE_HIGHLIGHT);
        pm.drawLine(x, y + 2, x + w, y + 2);
        pm.setColor(0.1f, 0.08f, 0.06f, 1f);
        pm.drawLine(x, y + h - 3, x + w, y + h - 3);
    }

    private void generateStoneEdgeV(Pixmap pm, int x, int y, int w, int h, Random rand) {
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, w, h);
        pm.setColor(STONE_MID);
        pm.fillRectangle(x + 4, y, w - 8, h);
        
        pm.setColor(STONE_HIGHLIGHT);
        pm.drawLine(x + 2, y, x + 2, y + h);
        pm.setColor(0.1f, 0.08f, 0.06f, 1f);
        pm.drawLine(x + w - 3, y, x + w - 3, y + h);
    }

    private void generateParchment(Pixmap pm, int x, int y, int w, int h, Random rand) {
        // Base parchment color
        pm.setColor(PARCHMENT_MID);
        pm.fillRectangle(x, y, w, h);
        
        // Age spots and variation
        for (int i = 0; i < 50; i++) {
            int px = x + rand.nextInt(w);
            int py = y + rand.nextInt(h);
            float v = 0.85f + rand.nextFloat() * 0.3f;
            pm.setColor(PARCHMENT_MID.r * v, PARCHMENT_MID.g * v, PARCHMENT_MID.b * v, 0.5f);
            pm.fillCircle(px, py, 1 + rand.nextInt(3));
        }
        
        // Darker edges
        for (int i = 0; i < 8; i++) {
            float alpha = 0.1f * (8 - i) / 8f;
            pm.setColor(0.3f, 0.2f, 0.1f, alpha);
            pm.drawRectangle(x + i, y + i, w - i * 2, h - i * 2);
        }
    }

    private void generateBarFrame(Pixmap pm, int x, int y, int w, int h, Random rand, Color accentColor) {
        // Outer frame
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, w, h);
        
        // Inner area
        pm.setColor(0.08f, 0.08f, 0.1f, 1f);
        pm.fillRectangle(x + 3, y + 3, w - 6, h - 6);
        
        // Accent color border
        pm.setColor(accentColor.r * 0.5f, accentColor.g * 0.5f, accentColor.b * 0.5f, 1f);
        pm.drawRectangle(x + 2, y + 2, w - 4, h - 4);
        
        // Corner accents
        pm.setColor(accentColor);
        pm.fillRectangle(x, y, 4, 4);
        pm.fillRectangle(x + w - 4, y, 4, 4);
        pm.fillRectangle(x, y + h - 4, 4, 4);
        pm.fillRectangle(x + w - 4, y + h - 4, 4, 4);
    }

    private void generateCharacterSprites() {
        int spriteSize = 64;
        Pixmap pm = new Pixmap(spriteSize * 6, spriteSize * 4, Pixmap.Format.RGBA8888);
        Random rand = new Random(456);

        // Row 0: Player classes
        generateWarriorSprite(pm, 0, 0, spriteSize, rand);
        generateRogueSprite(pm, spriteSize, 0, spriteSize, rand);
        generateMageSprite(pm, spriteSize * 2, 0, spriteSize, rand);

        // Row 1-3: Enemies (Daggerfall style)
        generateSkeletonSprite(pm, 0, spriteSize, spriteSize, rand);
        generateRatSprite(pm, spriteSize, spriteSize, spriteSize, rand);
        generateGoblinSprite(pm, spriteSize * 2, spriteSize, spriteSize, rand);
        generateOrcSprite(pm, spriteSize * 3, spriteSize, spriteSize, rand);
        generateGhostSprite(pm, spriteSize * 4, spriteSize, spriteSize, rand);
        generateDemonSprite(pm, spriteSize * 5, spriteSize, spriteSize, rand);
        
        // Bosses
        generateDragonSprite(pm, 0, spriteSize * 2, spriteSize, rand);
        generateLichSprite(pm, spriteSize, spriteSize * 2, spriteSize, rand);

        Texture spriteTex = new Texture(pm);
        pm.dispose();

        playerSprites.put("WARRIOR", new TextureRegion(spriteTex, 0, 0, spriteSize, spriteSize));
        playerSprites.put("ROGUE", new TextureRegion(spriteTex, spriteSize, 0, spriteSize, spriteSize));
        playerSprites.put("MAGE", new TextureRegion(spriteTex, spriteSize * 2, 0, spriteSize, spriteSize));

        enemySprites.put("SKELETON", new TextureRegion(spriteTex, 0, spriteSize, spriteSize, spriteSize));
        enemySprites.put("RAT", new TextureRegion(spriteTex, spriteSize, spriteSize, spriteSize, spriteSize));
        enemySprites.put("GOBLIN", new TextureRegion(spriteTex, spriteSize * 2, spriteSize, spriteSize, spriteSize));
        enemySprites.put("ORC", new TextureRegion(spriteTex, spriteSize * 3, spriteSize, spriteSize, spriteSize));
        enemySprites.put("GHOST", new TextureRegion(spriteTex, spriteSize * 4, spriteSize, spriteSize, spriteSize));
        enemySprites.put("DEMON", new TextureRegion(spriteTex, spriteSize * 5, spriteSize, spriteSize, spriteSize));
        enemySprites.put("DRAGON", new TextureRegion(spriteTex, 0, spriteSize * 2, spriteSize, spriteSize));
        enemySprites.put("LICH", new TextureRegion(spriteTex, spriteSize, spriteSize * 2, spriteSize, spriteSize));
        
        // Fallback
        enemySprites.put("BOSS", enemySprites.get("DEMON"));
    }

    private void generateWarriorSprite(Pixmap pm, int x, int y, int size, Random rand) {
        // Daggerfall-style front-facing warrior
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body (chainmail)
        pm.setColor(0.4f, 0.4f, 0.45f, 1f);
        pm.fillRectangle(cx - 12, cy - 4, 24, 28);
        
        // Head
        pm.setColor(0.8f, 0.65f, 0.5f, 1f);
        pm.fillCircle(cx, cy - 14, 10);
        
        // Helmet
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(cx - 11, cy - 26, 22, 14);
        pm.fillRectangle(cx - 2, cy - 18, 4, 10);
        
        // Eyes
        pm.setColor(0.2f, 0.2f, 0.2f, 1f);
        pm.fillRectangle(cx - 5, cy - 14, 3, 2);
        pm.fillRectangle(cx + 2, cy - 14, 3, 2);
        
        // Shield (left)
        pm.setColor(0.35f, 0.25f, 0.15f, 1f);
        pm.fillRectangle(x + 4, cy - 8, 14, 24);
        pm.setColor(GOLD_MID);
        pm.drawRectangle(x + 5, cy - 7, 12, 22);
        
        // Sword (right)
        pm.setColor(0.6f, 0.6f, 0.65f, 1f);
        pm.fillRectangle(x + size - 10, cy - 20, 4, 36);
        pm.setColor(0.4f, 0.3f, 0.2f, 1f);
        pm.fillRectangle(x + size - 12, cy + 10, 8, 6);
    }

    private void generateRogueSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body (leather)
        pm.setColor(0.3f, 0.25f, 0.2f, 1f);
        pm.fillRectangle(cx - 10, cy - 4, 20, 26);
        
        // Hood
        pm.setColor(0.25f, 0.2f, 0.15f, 1f);
        pm.fillCircle(cx, cy - 12, 12);
        pm.fillRectangle(cx - 12, cy - 16, 24, 8);
        
        // Face in shadow
        pm.setColor(0.15f, 0.12f, 0.1f, 1f);
        pm.fillCircle(cx, cy - 10, 8);
        
        // Eyes (glinting)
        pm.setColor(0.6f, 0.6f, 0.5f, 1f);
        pm.fillRectangle(cx - 4, cy - 12, 2, 2);
        pm.fillRectangle(cx + 2, cy - 12, 2, 2);
        
        // Daggers
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(x + 6, cy, 3, 20);
        pm.fillRectangle(x + size - 9, cy, 3, 20);
    }

    private void generateMageSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Robe
        pm.setColor(0.2f, 0.15f, 0.35f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 32);
        
        // Hood
        pm.setColor(0.25f, 0.2f, 0.4f, 1f);
        pm.fillCircle(cx, cy - 14, 12);
        
        // Face
        pm.setColor(0.75f, 0.6f, 0.5f, 1f);
        pm.fillCircle(cx, cy - 12, 8);
        
        // Eyes
        pm.setColor(0.4f, 0.3f, 0.6f, 1f);
        pm.fillCircle(cx - 3, cy - 13, 2);
        pm.fillCircle(cx + 3, cy - 13, 2);
        
        // Staff
        pm.setColor(0.4f, 0.3f, 0.2f, 1f);
        pm.fillRectangle(x + size - 12, y + 4, 4, size - 8);
        
        // Staff orb
        pm.setColor(0.6f, 0.3f, 0.8f, 1f);
        pm.fillCircle(x + size - 10, y + 8, 6);
        pm.setColor(0.8f, 0.5f, 1f, 1f);
        pm.fillCircle(x + size - 11, y + 7, 2);
    }

    private void generateSkeletonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Skull
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillCircle(cx, cy - 14, 12);
        
        // Eye sockets
        pm.setColor(0.1f, 0.05f, 0.05f, 1f);
        pm.fillCircle(cx - 4, cy - 14, 4);
        pm.fillCircle(cx + 4, cy - 14, 4);
        
        // Jaw
        pm.setColor(0.85f, 0.8f, 0.75f, 1f);
        pm.fillRectangle(cx - 8, cy - 6, 16, 6);
        
        // Ribs
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        for (int i = 0; i < 4; i++) {
            pm.fillRectangle(cx - 10, cy + 2 + i * 5, 20, 3);
        }
        
        // Arms
        pm.fillRectangle(cx - 16, cy, 4, 20);
        pm.fillRectangle(cx + 12, cy, 4, 20);
        
        // Sword
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(cx + 14, cy - 10, 3, 30);
    }

    private void generateRatSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2 + 8;
        
        // Body
        pm.setColor(0.35f, 0.25f, 0.2f, 1f);
        pm.fillCircle(cx, cy, 14);
        pm.fillCircle(cx - 8, cy - 4, 8);
        
        // Ears
        pm.setColor(0.4f, 0.3f, 0.25f, 1f);
        pm.fillCircle(cx - 14, cy - 12, 5);
        pm.fillCircle(cx - 6, cy - 14, 5);
        
        // Eyes
        pm.setColor(0.8f, 0.2f, 0.2f, 1f);
        pm.fillCircle(cx - 12, cy - 6, 3);
        
        // Tail
        pm.setColor(0.5f, 0.35f, 0.3f, 1f);
        for (int i = 0; i < 20; i++) {
            pm.fillCircle(cx + 8 + i, cy + 6 - i/3, 2);
        }
    }

    private void generateGoblinSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body
        pm.setColor(0.3f, 0.45f, 0.25f, 1f);
        pm.fillRectangle(cx - 10, cy - 2, 20, 24);
        
        // Head
        pm.setColor(0.35f, 0.5f, 0.3f, 1f);
        pm.fillCircle(cx, cy - 12, 11);
        
        // Ears
        pm.setColor(0.4f, 0.55f, 0.35f, 1f);
        pm.fillCircle(cx - 14, cy - 12, 6);
        pm.fillCircle(cx + 14, cy - 12, 6);
        
        // Eyes
        pm.setColor(0.9f, 0.7f, 0.2f, 1f);
        pm.fillCircle(cx - 4, cy - 12, 3);
        pm.fillCircle(cx + 4, cy - 12, 3);
        pm.setColor(0.1f, 0.1f, 0.1f, 1f);
        pm.fillCircle(cx - 4, cy - 12, 1);
        pm.fillCircle(cx + 4, cy - 12, 1);
        
        // Club
        pm.setColor(0.35f, 0.25f, 0.15f, 1f);
        pm.fillRectangle(x + size - 14, cy - 8, 8, 28);
    }

    private void generateOrcSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body (muscular)
        pm.setColor(0.35f, 0.45f, 0.3f, 1f);
        pm.fillRectangle(cx - 14, cy - 4, 28, 28);
        
        // Head
        pm.setColor(0.4f, 0.5f, 0.35f, 1f);
        pm.fillCircle(cx, cy - 14, 13);
        
        // Tusks
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillRectangle(cx - 8, cy - 6, 3, 8);
        pm.fillRectangle(cx + 5, cy - 6, 3, 8);
        
        // Eyes
        pm.setColor(0.8f, 0.3f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 16, 3);
        pm.fillCircle(cx + 5, cy - 16, 3);
        
        // Axe
        pm.setColor(0.4f, 0.35f, 0.3f, 1f);
        pm.fillRectangle(x + 4, cy - 14, 5, 36);
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(x + 2, cy - 14, 12, 14);
    }

    private void generateGhostSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Ethereal body
        pm.setColor(0.7f, 0.75f, 0.8f, 0.7f);
        pm.fillCircle(cx, cy - 8, 16);
        
        // Flowing bottom
        for (int i = 0; i < 4; i++) {
            int bx = cx - 12 + i * 8;
            pm.fillCircle(bx, cy + 12 + rand.nextInt(6), 6);
        }
        
        // Face
        pm.setColor(0.1f, 0.1f, 0.15f, 1f);
        pm.fillCircle(cx - 5, cy - 10, 4);
        pm.fillCircle(cx + 5, cy - 10, 4);
        pm.fillCircle(cx, cy, 5);
        
        // Inner glow
        pm.setColor(0.9f, 0.95f, 1f, 0.3f);
        pm.fillCircle(cx, cy - 6, 8);
    }

    private void generateDemonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body
        pm.setColor(0.5f, 0.15f, 0.1f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 30);
        
        // Head
        pm.setColor(0.55f, 0.2f, 0.15f, 1f);
        pm.fillCircle(cx, cy - 14, 12);
        
        // Horns
        pm.setColor(0.3f, 0.15f, 0.1f, 1f);
        for (int i = 0; i < 10; i++) {
            pm.fillCircle(cx - 12 + i/2, cy - 22 - i, 3);
            pm.fillCircle(cx + 12 - i/2, cy - 22 - i, 3);
        }
        
        // Eyes (glowing)
        pm.setColor(1f, 0.8f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 14, 4);
        pm.fillCircle(cx + 5, cy - 14, 4);
        pm.setColor(1f, 0.3f, 0.1f, 1f);
        pm.fillCircle(cx - 5, cy - 14, 2);
        pm.fillCircle(cx + 5, cy - 14, 2);
        
        // Wings
        pm.setColor(0.4f, 0.1f, 0.08f, 1f);
        pm.fillTriangle(cx - 14, cy, x, cy - 16, x, cy + 10);
        pm.fillTriangle(cx + 14, cy, x + size, cy - 16, x + size, cy + 10);
    }

    private void generateDragonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body
        pm.setColor(0.3f, 0.5f, 0.25f, 1f);
        pm.fillCircle(cx, cy + 4, 18);
        
        // Head
        pm.setColor(0.35f, 0.55f, 0.3f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        pm.fillRectangle(cx - 8, cy - 20, 16, 12);
        
        // Horns
        pm.setColor(0.4f, 0.35f, 0.25f, 1f);
        pm.fillTriangle(cx - 10, cy - 24, cx - 14, cy - 32, cx - 6, cy - 28);
        pm.fillTriangle(cx + 10, cy - 24, cx + 14, cy - 32, cx + 6, cy - 28);
        
        // Eyes
        pm.setColor(1f, 0.6f, 0.1f, 1f);
        pm.fillCircle(cx - 5, cy - 18, 4);
        pm.fillCircle(cx + 5, cy - 18, 4);
        
        // Nostrils (breathing fire)
        pm.setColor(1f, 0.4f, 0.1f, 1f);
        pm.fillCircle(cx - 3, cy - 10, 2);
        pm.fillCircle(cx + 3, cy - 10, 2);
        
        // Wings
        pm.setColor(0.25f, 0.4f, 0.2f, 1f);
        pm.fillTriangle(cx - 16, cy - 4, x - 4, cy - 20, x - 4, cy + 12);
        pm.fillTriangle(cx + 16, cy - 4, x + size + 4, cy - 20, x + size + 4, cy + 12);
    }

    private void generateLichSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Robes
        pm.setColor(0.15f, 0.1f, 0.2f, 1f);
        pm.fillRectangle(cx - 14, cy - 8, 28, 34);
        
        // Hood
        pm.setColor(0.12f, 0.08f, 0.18f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        
        // Skull face
        pm.setColor(0.85f, 0.8f, 0.75f, 1f);
        pm.fillCircle(cx, cy - 14, 10);
        
        // Eye sockets (glowing)
        pm.setColor(0.1f, 0.8f, 0.3f, 1f);
        pm.fillCircle(cx - 4, cy - 15, 3);
        pm.fillCircle(cx + 4, cy - 15, 3);
        pm.setColor(0.3f, 1f, 0.5f, 1f);
        pm.fillCircle(cx - 4, cy - 15, 1);
        pm.fillCircle(cx + 4, cy - 15, 1);
        
        // Staff
        pm.setColor(0.3f, 0.25f, 0.2f, 1f);
        pm.fillRectangle(x + size - 10, y + 2, 4, size - 4);
        
        // Staff skull
        pm.setColor(0.8f, 0.75f, 0.7f, 1f);
        pm.fillCircle(x + size - 8, y + 6, 5);
        pm.setColor(0.1f, 0.8f, 0.3f, 1f);
        pm.fillCircle(x + size - 9, y + 5, 2);
        pm.fillCircle(x + size - 7, y + 5, 2);
    }

    private void generatePortraits() {
        int portSize = 64;
        Pixmap pm = new Pixmap(portSize * 3, portSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(789);

        // Warrior portrait
        generatePortrait(pm, 0, 0, portSize, new Color(0.3f, 0.4f, 0.8f, 1f), "W", rand);
        // Rogue portrait  
        generatePortrait(pm, portSize, 0, portSize, new Color(0.3f, 0.6f, 0.3f, 1f), "R", rand);
        // Mage portrait
        generatePortrait(pm, portSize * 2, 0, portSize, new Color(0.6f, 0.3f, 0.7f, 1f), "M", rand);

        portraitTexture = new Texture(pm);
        pm.dispose();

        portraits.put("WARRIOR", new TextureRegion(portraitTexture, 0, 0, portSize, portSize));
        portraits.put("ROGUE", new TextureRegion(portraitTexture, portSize, 0, portSize, portSize));
        portraits.put("MAGE", new TextureRegion(portraitTexture, portSize * 2, 0, portSize, portSize));
    }

    private void generatePortrait(Pixmap pm, int x, int y, int size, Color frameColor, String initial, Random rand) {
        // Stone frame
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(STONE_MID);
        pm.fillRectangle(x + 4, y + 4, size - 8, size - 8);
        
        // Inner area (face placeholder)
        pm.setColor(0.15f, 0.12f, 0.1f, 1f);
        pm.fillRectangle(x + 8, y + 8, size - 16, size - 16);
        
        // Frame accent
        pm.setColor(frameColor);
        pm.drawRectangle(x + 6, y + 6, size - 12, size - 12);
        
        // Corners
        pm.fillRectangle(x + 2, y + 2, 6, 6);
        pm.fillRectangle(x + size - 8, y + 2, 6, 6);
        pm.fillRectangle(x + 2, y + size - 8, 6, 6);
        pm.fillRectangle(x + size - 8, y + size - 8, 6, 6);
    }

    private void generateFonts() {
        // Using default fonts with gothic-ish styling via color
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(GOLD_LIGHT);

        normalFont = new BitmapFont();
        normalFont.getData().setScale(1.5f);
        normalFont.setColor(PARCHMENT_LIGHT);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.0f);
        smallFont.setColor(PARCHMENT_MID);
    }

    // === GETTERS ===

    public TextureRegion getTile(String name) {
        return tiles.getOrDefault(name, tiles.get("floor"));
    }

    public TextureRegion getUIElement(String name) {
        return uiElements.get(name);
    }

    public TextureRegion getPlayerSprite(String className) {
        return playerSprites.getOrDefault(className, playerSprites.get("WARRIOR"));
    }

    public TextureRegion getEnemySprite(String enemyType) {
        return enemySprites.getOrDefault(enemyType, enemySprites.get("SKELETON"));
    }

    public TextureRegion getPortrait(String className) {
        return portraits.getOrDefault(className, portraits.get("WARRIOR"));
    }

    public TextureRegion getWhitePixel() {
        return whitePixel;
    }

    public BitmapFont getTitleFont() { return titleFont; }
    public BitmapFont getNormalFont() { return normalFont; }
    public BitmapFont getSmallFont() { return smallFont; }

    public BitmapFont getDamageFont() { return normalFont; }
    
    public TextureRegion getSlashEffect() { return whitePixel; }
    
    public TextureRegion getHitEffect() { return whitePixel; }

        public void dispose() {
        if (tilesetTexture != null) tilesetTexture.dispose();
        if (uiTexture != null) uiTexture.dispose();
        if (portraitTexture != null) portraitTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (normalFont != null) normalFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}

