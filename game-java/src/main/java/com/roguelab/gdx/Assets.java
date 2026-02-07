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
 * Daggerfall-style procedural assets with expanded enemy sprites.
 */
public class Assets {

    // Textures
    private Texture tilesetTexture;
    private Texture uiTexture;
    private Texture portraitTexture;
    private Texture enemyTexture;
    
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
        generateEnemySprites();
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
        int rows = 4;
        
        Pixmap pixmap = new Pixmap(cols * tileSize, rows * tileSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(42);

        // Floor tiles
        generateStoneBrickTile(pixmap, 0, 0, tileSize, rand, STONE_MID, false);
        generateStoneBrickTile(pixmap, tileSize, 0, tileSize, rand, STONE_DARK, false);
        generateStoneBrickTile(pixmap, tileSize * 2, 0, tileSize, rand, STONE_LIGHT, false);
        generateMossyStoneTile(pixmap, tileSize * 3, 0, tileSize, rand);

        // Wall tiles
        generateStoneBrickTile(pixmap, 0, tileSize, tileSize, rand, STONE_DARK, true);
        generateStoneBrickTile(pixmap, tileSize, tileSize, tileSize, rand, STONE_MID, true);
        generateTorchTile(pixmap, tileSize * 2, tileSize, tileSize, rand);
        generateDoorTile(pixmap, tileSize * 3, tileSize, tileSize, rand);

        // Room icons
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

    private void generateEnemySprites() {
        int spriteSize = 64;
        int cols = 8;
        int rows = 5; // Expanded for more enemies
        
        Pixmap pm = new Pixmap(cols * spriteSize, rows * spriteSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(789);

        // Row 0: Basic enemies
        generateRatSprite(pm, 0, 0, spriteSize, rand);
        generateSpiderSprite(pm, spriteSize, 0, spriteSize, rand);
        generateSkeletonSprite(pm, spriteSize * 2, 0, spriteSize, rand);
        generateGoblinSprite(pm, spriteSize * 3, 0, spriteSize, rand);
        generateBatSprite(pm, spriteSize * 4, 0, spriteSize, rand);

        // Row 1: Medium enemies
        generateZombieSprite(pm, 0, spriteSize, spriteSize, rand);
        generateOrcSprite(pm, spriteSize, spriteSize, spriteSize, rand);
        generateGhostSprite(pm, spriteSize * 2, spriteSize, spriteSize, rand);
        generateSlimeSprite(pm, spriteSize * 3, spriteSize, spriteSize, rand);
        generateCultistSprite(pm, spriteSize * 4, spriteSize, spriteSize, rand);
        generateTrollSprite(pm, spriteSize * 5, spriteSize, spriteSize, rand);
        generateElementalSprite(pm, spriteSize * 6, spriteSize, spriteSize, rand);

        // Row 2: Strong enemies
        generateDemonSprite(pm, 0, spriteSize * 2, spriteSize, rand);
        generateWraithSprite(pm, spriteSize, spriteSize * 2, spriteSize, rand);
        generateGolemSprite(pm, spriteSize * 2, spriteSize * 2, spriteSize, rand);
        generateVampireSprite(pm, spriteSize * 3, spriteSize * 2, spriteSize, rand);
        generateMinotaurSprite(pm, spriteSize * 4, spriteSize * 2, spriteSize, rand);

        // Row 3: Bosses
        generateSkeletonLordSprite(pm, 0, spriteSize * 3, spriteSize, rand);
        generateOrcChieftainSprite(pm, spriteSize, spriteSize * 3, spriteSize, rand);
        generateLichSprite(pm, spriteSize * 2, spriteSize * 3, spriteSize, rand);
        generateDragonSprite(pm, spriteSize * 3, spriteSize * 3, spriteSize, rand);
        generateDemonLordSprite(pm, spriteSize * 4, spriteSize * 3, spriteSize, rand);
        generateGoblinKingSprite(pm, spriteSize * 5, spriteSize * 3, spriteSize, rand);
        generateNecromancerSprite(pm, spriteSize * 6, spriteSize * 3, spriteSize, rand);

        enemyTexture = new Texture(pm);
        pm.dispose();

        // Map enemy types to sprites
        enemySprites.put("RAT", new TextureRegion(enemyTexture, 0, 0, spriteSize, spriteSize));
        enemySprites.put("BAT", new TextureRegion(enemyTexture, spriteSize * 4, 0, spriteSize, spriteSize));
        enemySprites.put("SPIDER", new TextureRegion(enemyTexture, spriteSize, 0, spriteSize, spriteSize));
        enemySprites.put("SKELETON", new TextureRegion(enemyTexture, spriteSize * 2, 0, spriteSize, spriteSize));
        enemySprites.put("GOBLIN", new TextureRegion(enemyTexture, spriteSize * 3, 0, spriteSize, spriteSize));

        enemySprites.put("ZOMBIE", new TextureRegion(enemyTexture, 0, spriteSize, spriteSize, spriteSize));
        enemySprites.put("ORC", new TextureRegion(enemyTexture, spriteSize, spriteSize, spriteSize, spriteSize));
        enemySprites.put("GHOST", new TextureRegion(enemyTexture, spriteSize * 2, spriteSize, spriteSize, spriteSize));
        enemySprites.put("SLIME", new TextureRegion(enemyTexture, spriteSize * 3, spriteSize, spriteSize, spriteSize));
        enemySprites.put("CULTIST", new TextureRegion(enemyTexture, spriteSize * 4, spriteSize, spriteSize, spriteSize));
        enemySprites.put("TROLL", new TextureRegion(enemyTexture, spriteSize * 5, spriteSize, spriteSize, spriteSize));
        enemySprites.put("ELEMENTAL", new TextureRegion(enemyTexture, spriteSize * 6, spriteSize, spriteSize, spriteSize));

        enemySprites.put("DEMON", new TextureRegion(enemyTexture, 0, spriteSize * 2, spriteSize, spriteSize));
        enemySprites.put("WRAITH", new TextureRegion(enemyTexture, spriteSize, spriteSize * 2, spriteSize, spriteSize));
        enemySprites.put("GOLEM", new TextureRegion(enemyTexture, spriteSize * 2, spriteSize * 2, spriteSize, spriteSize));
        enemySprites.put("VAMPIRE", new TextureRegion(enemyTexture, spriteSize * 3, spriteSize * 2, spriteSize, spriteSize));
        enemySprites.put("MINOTAUR", new TextureRegion(enemyTexture, spriteSize * 4, spriteSize * 2, spriteSize, spriteSize));

        enemySprites.put("SKELETON_LORD", new TextureRegion(enemyTexture, 0, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("ORC_CHIEFTAIN", new TextureRegion(enemyTexture, spriteSize, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("GOBLIN_KING", new TextureRegion(enemyTexture, spriteSize * 5, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("NECROMANCER", new TextureRegion(enemyTexture, spriteSize * 6, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("LICH", new TextureRegion(enemyTexture, spriteSize * 2, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("DRAGON", new TextureRegion(enemyTexture, spriteSize * 3, spriteSize * 3, spriteSize, spriteSize));
        enemySprites.put("DEMON_LORD", new TextureRegion(enemyTexture, spriteSize * 4, spriteSize * 3, spriteSize, spriteSize));
        
        // Fallback
        enemySprites.put("BOSS", enemySprites.get("DEMON_LORD"));
    }

    // === NEW ENEMY SPRITE GENERATORS ===

    private void generateSpiderSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Body
        pm.setColor(0.2f, 0.15f, 0.1f, 1f);
        pm.fillCircle(cx, cy, 12);
        pm.fillCircle(cx, cy + 10, 8);
        
        // Legs (8 of them)
        pm.setColor(0.25f, 0.2f, 0.15f, 1f);
        for (int i = 0; i < 4; i++) {
            int legY = cy - 4 + i * 4;
            // Left legs
            pm.drawLine(cx - 10, legY, cx - 22, legY - 8 + i * 4);
            pm.drawLine(cx - 22, legY - 8 + i * 4, cx - 26, legY + 10);
            // Right legs
            pm.drawLine(cx + 10, legY, cx + 22, legY - 8 + i * 4);
            pm.drawLine(cx + 22, legY - 8 + i * 4, cx + 26, legY + 10);
        }
        
        // Eyes (multiple, creepy)
        pm.setColor(0.8f, 0.2f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 6, 3);
        pm.fillCircle(cx + 5, cy - 6, 3);
        pm.fillCircle(cx - 3, cy - 2, 2);
        pm.fillCircle(cx + 3, cy - 2, 2);
        
        // Fangs
        pm.setColor(0.9f, 0.9f, 0.8f, 1f);
        pm.fillRectangle(cx - 4, cy + 4, 2, 6);
        pm.fillRectangle(cx + 2, cy + 4, 2, 6);
        
        // Poison drip (green)
        pm.setColor(0.3f, 0.8f, 0.2f, 0.7f);
        pm.fillCircle(cx - 3, cy + 12, 2);
        pm.fillCircle(cx + 3, cy + 14, 2);
    }

    private void generateZombieSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Tattered clothes
        pm.setColor(0.25f, 0.2f, 0.15f, 1f);
        pm.fillRectangle(cx - 12, cy - 4, 24, 28);
        
        // Rotting flesh
        pm.setColor(0.4f, 0.5f, 0.35f, 1f);
        pm.fillCircle(cx, cy - 14, 11);
        
        // Exposed bone patches
        pm.setColor(0.8f, 0.75f, 0.7f, 1f);
        pm.fillCircle(cx - 5, cy - 12, 3);
        pm.fillCircle(cx + 7, cy - 10, 2);
        
        // Sunken eyes
        pm.setColor(0.1f, 0.1f, 0.1f, 1f);
        pm.fillCircle(cx - 4, cy - 14, 3);
        pm.fillCircle(cx + 4, cy - 14, 3);
        pm.setColor(0.6f, 0.2f, 0.1f, 1f);
        pm.fillCircle(cx - 4, cy - 14, 1);
        pm.fillCircle(cx + 4, cy - 14, 1);
        
        // Gaping mouth
        pm.setColor(0.2f, 0.1f, 0.1f, 1f);
        pm.fillRectangle(cx - 5, cy - 6, 10, 4);
        
        // Reaching arms
        pm.setColor(0.4f, 0.5f, 0.35f, 1f);
        pm.fillRectangle(cx - 18, cy - 2, 6, 20);
        pm.fillRectangle(cx + 12, cy, 6, 18);
    }

    private void generateSlimeSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2 + 8;
        
        // Main blob (acid green)
        pm.setColor(0.3f, 0.7f, 0.2f, 0.9f);
        pm.fillCircle(cx, cy, 18);
        pm.fillCircle(cx - 8, cy - 6, 10);
        pm.fillCircle(cx + 10, cy - 4, 8);
        
        // Darker core
        pm.setColor(0.2f, 0.5f, 0.15f, 0.8f);
        pm.fillCircle(cx, cy + 4, 10);
        
        // Shiny highlights
        pm.setColor(0.5f, 0.9f, 0.4f, 0.6f);
        pm.fillCircle(cx - 6, cy - 10, 4);
        pm.fillCircle(cx + 8, cy - 8, 3);
        
        // Eyes floating in the goo
        pm.setColor(1f, 1f, 0.8f, 1f);
        pm.fillCircle(cx - 5, cy - 4, 4);
        pm.fillCircle(cx + 6, cy - 2, 3);
        pm.setColor(0.1f, 0.1f, 0.1f, 1f);
        pm.fillCircle(cx - 5, cy - 4, 2);
        pm.fillCircle(cx + 6, cy - 2, 1);
        
        // Acid drips
        pm.setColor(0.3f, 0.7f, 0.2f, 0.7f);
        pm.fillCircle(cx - 12, cy + 16, 3);
        pm.fillCircle(cx + 8, cy + 18, 2);
    }

    private void generateCultistSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Dark robes
        pm.setColor(0.15f, 0.1f, 0.15f, 1f);
        pm.fillRectangle(cx - 14, cy - 8, 28, 34);
        
        // Hood
        pm.setColor(0.12f, 0.08f, 0.12f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        pm.fillRectangle(cx - 14, cy - 20, 28, 10);
        
        // Shadowed face
        pm.setColor(0.08f, 0.05f, 0.08f, 1f);
        pm.fillCircle(cx, cy - 14, 8);
        
        // Glowing eyes
        pm.setColor(0.8f, 0.2f, 0.8f, 1f);
        pm.fillCircle(cx - 3, cy - 15, 2);
        pm.fillCircle(cx + 3, cy - 15, 2);
        
        // Ritual dagger
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(x + size - 14, cy - 12, 3, 20);
        pm.setColor(0.6f, 0.1f, 0.1f, 1f); // Blood on blade
        pm.fillRectangle(x + size - 14, cy - 8, 3, 8);
        
        // Dark magic aura
        pm.setColor(0.5f, 0.2f, 0.5f, 0.3f);
        pm.fillCircle(cx, cy, 22);
    }

    private void generateWraithSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Ethereal body (dark, flowing)
        pm.setColor(0.15f, 0.1f, 0.2f, 0.8f);
        pm.fillCircle(cx, cy - 8, 16);
        
        // Flowing bottom
        for (int i = 0; i < 5; i++) {
            int bx = cx - 16 + i * 8;
            int by = cy + 8 + rand.nextInt(8);
            pm.fillCircle(bx, by, 6 + rand.nextInt(4));
        }
        
        // Skull face
        pm.setColor(0.7f, 0.65f, 0.6f, 0.9f);
        pm.fillCircle(cx, cy - 12, 10);
        
        // Dark eye sockets
        pm.setColor(0.1f, 0.05f, 0.1f, 1f);
        pm.fillCircle(cx - 4, cy - 13, 4);
        pm.fillCircle(cx + 4, cy - 13, 4);
        
        // Soul fire eyes
        pm.setColor(0.4f, 0.8f, 1f, 1f);
        pm.fillCircle(cx - 4, cy - 13, 2);
        pm.fillCircle(cx + 4, cy - 13, 2);
        
        // Spectral glow
        pm.setColor(0.3f, 0.4f, 0.6f, 0.2f);
        pm.fillCircle(cx, cy - 4, 24);
    }

    private void generateGolemSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Massive stone body
        pm.setColor(0.4f, 0.38f, 0.35f, 1f);
        pm.fillRectangle(cx - 16, cy - 10, 32, 36);
        
        // Stone head
        pm.setColor(0.45f, 0.42f, 0.38f, 1f);
        pm.fillRectangle(cx - 12, cy - 26, 24, 18);
        
        // Cracks
        pm.setColor(0.25f, 0.22f, 0.2f, 1f);
        pm.drawLine(cx - 8, cy - 24, cx - 4, cy - 18);
        pm.drawLine(cx + 6, cy - 22, cx + 10, cy - 14);
        pm.drawLine(cx - 10, cy, cx + 8, cy + 12);
        
        // Glowing rune eyes
        pm.setColor(0.9f, 0.6f, 0.1f, 1f);
        pm.fillRectangle(cx - 8, cy - 22, 6, 4);
        pm.fillRectangle(cx + 2, cy - 22, 6, 4);
        
        // Stone fists
        pm.setColor(0.42f, 0.4f, 0.36f, 1f);
        pm.fillRectangle(cx - 24, cy, 10, 14);
        pm.fillRectangle(cx + 14, cy, 10, 14);
    }

    private void generateVampireSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Noble attire
        pm.setColor(0.15f, 0.05f, 0.1f, 1f);
        pm.fillRectangle(cx - 12, cy - 6, 24, 30);
        
        // High collar / cape
        pm.setColor(0.5f, 0.1f, 0.15f, 1f);
        pm.fillRectangle(cx - 16, cy - 10, 32, 12);
        
        // Pale face
        pm.setColor(0.85f, 0.8f, 0.85f, 1f);
        pm.fillCircle(cx, cy - 14, 10);
        
        // Slicked back hair
        pm.setColor(0.1f, 0.08f, 0.1f, 1f);
        pm.fillRectangle(cx - 10, cy - 26, 20, 10);
        
        // Red eyes
        pm.setColor(0.9f, 0.15f, 0.1f, 1f);
        pm.fillCircle(cx - 4, cy - 15, 2);
        pm.fillCircle(cx + 4, cy - 15, 2);
        
        // Fangs
        pm.setColor(1f, 1f, 0.95f, 1f);
        pm.fillRectangle(cx - 4, cy - 8, 2, 5);
        pm.fillRectangle(cx + 2, cy - 8, 2, 5);
        
        // Blood drip
        pm.setColor(0.7f, 0.1f, 0.1f, 1f);
        pm.fillCircle(cx - 3, cy - 2, 2);
    }

    private void generateMinotaurSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Massive muscular body
        pm.setColor(0.45f, 0.3f, 0.2f, 1f);
        pm.fillRectangle(cx - 16, cy - 8, 32, 32);
        
        // Bull head
        pm.setColor(0.5f, 0.35f, 0.25f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        pm.fillRectangle(cx - 10, cy - 14, 20, 10); // Snout
        
        // Horns
        pm.setColor(0.35f, 0.3f, 0.25f, 1f);
        for (int i = 0; i < 10; i++) {
            pm.fillCircle(cx - 14 - i, cy - 20 - i/2, 3);
            pm.fillCircle(cx + 14 + i, cy - 20 - i/2, 3);
        }
        
        // Angry eyes
        pm.setColor(0.9f, 0.3f, 0.2f, 1f);
        pm.fillCircle(cx - 6, cy - 18, 3);
        pm.fillCircle(cx + 6, cy - 18, 3);
        
        // Nose ring
        pm.setColor(0.7f, 0.6f, 0.2f, 1f);
        pm.fillCircle(cx, cy - 8, 3);
        pm.setColor(0.5f, 0.35f, 0.25f, 1f);
        pm.fillCircle(cx, cy - 8, 1);
        
        // Battle axe
        pm.setColor(0.35f, 0.3f, 0.25f, 1f);
        pm.fillRectangle(x + 4, cy - 20, 5, 40);
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(x + 2, cy - 20, 14, 16);
    }

    // === BOSS SPRITES ===

    private void generateSkeletonLordSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Royal robes (tattered)
        pm.setColor(0.2f, 0.15f, 0.25f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 32);
        
        // Crown
        pm.setColor(0.7f, 0.6f, 0.2f, 1f);
        pm.fillRectangle(cx - 10, cy - 28, 20, 6);
        for (int i = 0; i < 5; i++) {
            pm.fillRectangle(cx - 10 + i * 5, cy - 34, 4, 8);
        }
        
        // Skull
        pm.setColor(0.85f, 0.8f, 0.75f, 1f);
        pm.fillCircle(cx, cy - 16, 12);
        
        // Empty eye sockets with glow
        pm.setColor(0.1f, 0.05f, 0.05f, 1f);
        pm.fillCircle(cx - 4, cy - 17, 4);
        pm.fillCircle(cx + 4, cy - 17, 4);
        pm.setColor(0.8f, 0.2f, 0.8f, 1f);
        pm.fillCircle(cx - 4, cy - 17, 2);
        pm.fillCircle(cx + 4, cy - 17, 2);
        
        // Scepter
        pm.setColor(0.6f, 0.5f, 0.2f, 1f);
        pm.fillRectangle(x + size - 12, cy - 16, 4, 36);
        pm.setColor(0.8f, 0.3f, 0.8f, 1f);
        pm.fillCircle(x + size - 10, cy - 20, 6);
    }

    private void generateOrcChieftainSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Massive armored body
        pm.setColor(0.3f, 0.3f, 0.35f, 1f);
        pm.fillRectangle(cx - 16, cy - 8, 32, 32);
        
        // Orc head
        pm.setColor(0.35f, 0.5f, 0.3f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        
        // War helmet
        pm.setColor(0.4f, 0.35f, 0.3f, 1f);
        pm.fillRectangle(cx - 14, cy - 28, 28, 14);
        pm.fillRectangle(cx - 2, cy - 20, 4, 12);
        
        // Tusks
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillRectangle(cx - 10, cy - 10, 4, 10);
        pm.fillRectangle(cx + 6, cy - 10, 4, 10);
        
        // Rage eyes
        pm.setColor(1f, 0.3f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 18, 3);
        pm.fillCircle(cx + 5, cy - 18, 3);
        
        // War banner
        pm.setColor(0.6f, 0.15f, 0.1f, 1f);
        pm.fillRectangle(x + 4, cy - 26, 12, 16);
        pm.setColor(0.3f, 0.25f, 0.2f, 1f);
        pm.fillRectangle(x + 8, cy - 28, 4, 44);
    }

    private void generateDragonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Massive body
        pm.setColor(0.25f, 0.45f, 0.2f, 1f);
        pm.fillCircle(cx, cy + 4, 20);
        
        // Dragon head
        pm.setColor(0.3f, 0.5f, 0.25f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        pm.fillRectangle(cx - 6, cy - 18, 12, 14);
        
        // Horns
        pm.setColor(0.35f, 0.3f, 0.2f, 1f);
        for (int i = 0; i < 8; i++) {
            pm.fillCircle(cx - 12 - i/2, cy - 24 - i, 3);
            pm.fillCircle(cx + 12 + i/2, cy - 24 - i, 3);
        }
        
        // Fire eyes
        pm.setColor(1f, 0.6f, 0.1f, 1f);
        pm.fillCircle(cx - 5, cy - 18, 4);
        pm.fillCircle(cx + 5, cy - 18, 4);
        pm.setColor(1f, 0.9f, 0.3f, 1f);
        pm.fillCircle(cx - 5, cy - 18, 2);
        pm.fillCircle(cx + 5, cy - 18, 2);
        
        // Fire breath
        pm.setColor(1f, 0.5f, 0.1f, 0.8f);
        pm.fillCircle(cx, cy - 6, 5);
        pm.setColor(1f, 0.8f, 0.2f, 0.6f);
        pm.fillCircle(cx, cy - 4, 3);
        
        // Wings
        pm.setColor(0.2f, 0.35f, 0.15f, 1f);
        pm.fillTriangle(cx - 18, cy - 4, x - 6, cy - 24, x - 6, cy + 16);
        pm.fillTriangle(cx + 18, cy - 4, x + size + 6, cy - 24, x + size + 6, cy + 16);
    }

    private void generateDemonLordSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        
        // Massive demonic body
        pm.setColor(0.5f, 0.12f, 0.08f, 1f);
        pm.fillRectangle(cx - 18, cy - 8, 36, 34);
        
        // Horned head
        pm.setColor(0.55f, 0.15f, 0.1f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        
        // Massive horns
        pm.setColor(0.25f, 0.1f, 0.08f, 1f);
        for (int i = 0; i < 14; i++) {
            pm.fillCircle(cx - 14 + i/3, cy - 24 - i, 4);
            pm.fillCircle(cx + 14 - i/3, cy - 24 - i, 4);
        }
        
        // Burning eyes
        pm.setColor(1f, 0.8f, 0.2f, 1f);
        pm.fillCircle(cx - 6, cy - 16, 5);
        pm.fillCircle(cx + 6, cy - 16, 5);
        pm.setColor(1f, 0.4f, 0.1f, 1f);
        pm.fillCircle(cx - 6, cy - 16, 3);
        pm.fillCircle(cx + 6, cy - 16, 3);
        
        // Fanged maw
        pm.setColor(0.2f, 0.05f, 0.05f, 1f);
        pm.fillRectangle(cx - 8, cy - 6, 16, 8);
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        for (int i = 0; i < 4; i++) {
            pm.fillRectangle(cx - 7 + i * 4, cy - 6, 2, 5);
        }
        
        // Demonic wings
        pm.setColor(0.35f, 0.08f, 0.05f, 1f);
        pm.fillTriangle(cx - 16, cy - 2, x - 8, cy - 22, x - 8, cy + 18);
        pm.fillTriangle(cx + 16, cy - 2, x + size + 8, cy - 22, x + size + 8, cy + 18);
        
        // Hellfire aura
        pm.setColor(1f, 0.3f, 0.1f, 0.2f);
        pm.fillCircle(cx, cy, 28);
    }

    // === EXISTING GENERATORS (abbreviated) ===

    private void generateStoneBrickTile(Pixmap pm, int x, int y, int size, Random rand, Color baseColor, boolean isWall) {
        pm.setColor(baseColor);
        pm.fillRectangle(x, y, size, size);
        int brickH = size / 4;
        int brickW = size / 2;
        for (int row = 0; row < 4; row++) {
            int offset = (row % 2 == 0) ? 0 : brickW / 2;
            for (int col = -1; col < 3; col++) {
                int bx = x + col * brickW + offset;
                int by = y + row * brickH;
                float variation = 0.9f + rand.nextFloat() * 0.2f;
                pm.setColor(baseColor.r * variation, baseColor.g * variation, baseColor.b * variation, 1f);
                pm.fillRectangle(bx + 1, by + 1, brickW - 2, brickH - 2);
                pm.setColor(baseColor.r * 0.6f, baseColor.g * 0.6f, baseColor.b * 0.6f, 1f);
                pm.drawRectangle(bx, by, brickW, brickH);
            }
        }
    }

    private void generateMossyStoneTile(Pixmap pm, int x, int y, int size, Random rand) {
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        Color moss = new Color(0.2f, 0.35f, 0.15f, 1f);
        for (int i = 0; i < 20; i++) {
            int px = x + rand.nextInt(size);
            int py = y + rand.nextInt(size);
            pm.setColor(moss.r, moss.g, moss.b, 0.5f);
            pm.fillCircle(px, py, 2 + rand.nextInt(3));
        }
    }

    private void generateTorchTile(Pixmap pm, int x, int y, int size, Random rand) {
        generateStoneBrickTile(pm, x, y, size, rand, STONE_DARK, true);
        pm.setColor(0.3f, 0.2f, 0.1f, 1f);
        pm.fillRectangle(x + size/2 - 3, y + size/2, 6, size/3);
        pm.setColor(TORCH_ORANGE);
        pm.fillCircle(x + size/2, y + size/4, 8);
        pm.setColor(1f, 0.9f, 0.5f, 1f);
        pm.fillCircle(x + size/2, y + size/4, 4);
    }

    private void generateDoorTile(Pixmap pm, int x, int y, int size, Random rand) {
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(0.35f, 0.2f, 0.1f, 1f);
        pm.fillRectangle(x + 8, y + 4, size - 16, size - 8);
        pm.setColor(GOLD_MID);
        pm.fillCircle(x + size - 16, y + size/2, 4);
    }

    private void generateCombatIcon(Pixmap pm, int x, int y, int size, Random rand) {
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        pm.setColor(0.6f, 0.6f, 0.65f, 1f);
        pm.fillRectangle(x + 12, y + 8, 4, 40);
        pm.fillRectangle(x + size - 16, y + 8, 4, 40);
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillCircle(x + size/2, y + size/2, 12);
        pm.setColor(STONE_DARK);
        pm.fillCircle(x + size/2 - 4, y + size/2 - 2, 3);
        pm.fillCircle(x + size/2 + 4, y + size/2 - 2, 3);
    }

    private void generateBossIcon(Pixmap pm, int x, int y, int size, Random rand) {
        pm.setColor(0.25f, 0.08f, 0.08f, 1f);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(0.8f, 0.75f, 0.7f, 1f);
        pm.fillCircle(x + size/2, y + size/2 + 4, 16);
        pm.setColor(BLOOD_RED);
        pm.fillCircle(x + size/2 - 6, y + size/2, 4);
        pm.fillCircle(x + size/2 + 6, y + size/2, 4);
    }

    private void generateTreasureIcon(Pixmap pm, int x, int y, int size, Random rand) {
        generateStoneBrickTile(pm, x, y, size, rand, STONE_MID, false);
        pm.setColor(0.4f, 0.25f, 0.1f, 1f);
        pm.fillRectangle(x + 10, y + 20, size - 20, size - 30);
        pm.setColor(GOLD_MID);
        pm.fillRectangle(x + 10, y + 18, size - 20, 3);
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size/2, y + 8, 4);
    }

    private void generateShopIcon(Pixmap pm, int x, int y, int size, Random rand) {
        pm.setColor(PARCHMENT_MID);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(GOLD_MID);
        pm.fillRectangle(x + size/2 - 2, y + 16, 4, 32);
        pm.setColor(GOLD_LIGHT);
        pm.fillCircle(x + size/2, y + size - 14, 8);
    }

    private void generateRestIcon(Pixmap pm, int x, int y, int size, Random rand) {
        generateStoneBrickTile(pm, x, y, size, rand, STONE_DARK, false);
        pm.setColor(BLOOD_RED);
        pm.fillCircle(x + size/2, y + size/2 + 8, 10);
        pm.setColor(TORCH_ORANGE);
        pm.fillCircle(x + size/2, y + size/2 + 4, 8);
        pm.setColor(1f, 0.9f, 0.4f, 1f);
        pm.fillCircle(x + size/2, y + size/2, 5);
    }

    private void generateStairsIcon(Pixmap pm, int x, int y, int size, Random rand) {
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        int steps = 5;
        for (int i = 0; i < steps; i++) {
            float shade = 0.35f - i * 0.05f;
            pm.setColor(shade, shade * 0.9f, shade * 0.85f, 1f);
            pm.fillRectangle(x + 8 + i * 8, y + 8 + i * 10, size - 16 - i * 8, size - 8 - i * 10);
        }
        pm.setColor(GOLD_MID);
        pm.fillTriangle(x + size/2 - 10, y + size/2 - 8, x + size/2 + 10, y + size/2 - 8, x + size/2, y + size/2 + 10);
    }

    private void generateFogTile(Pixmap pm, int x, int y, int size, Random rand) {
        pm.setColor(0.05f, 0.05f, 0.08f, 1f);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(0.2f, 0.2f, 0.25f, 1f);
        pm.fillCircle(x + size/2, y + size/3, 12);
    }

    private void generateUIElements() {
        // Simplified - just create basic UI texture
        Pixmap pm = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        uiTexture = new Texture(pm);
        pm.dispose();
        uiElements.put("corner", new TextureRegion(uiTexture, 0, 0, 32, 32));
    }


    private void generateBatSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.2f, 0.15f, 0.2f, 1f);
        pm.fillCircle(cx, cy, 10);
        pm.setColor(0.25f, 0.2f, 0.25f, 1f);
        pm.fillTriangle(cx - 8, cy, cx - 24, cy - 12, cx - 20, cy + 8);
        pm.fillTriangle(cx + 8, cy, cx + 24, cy - 12, cx + 20, cy + 8);
        pm.setColor(1f, 0.3f, 0.2f, 1f);
        pm.fillCircle(cx - 4, cy - 2, 2);
        pm.fillCircle(cx + 4, cy - 2, 2);
        pm.setColor(0.2f, 0.15f, 0.2f, 1f);
        pm.fillTriangle(cx - 6, cy - 8, cx - 2, cy - 8, cx - 4, cy - 16);
        pm.fillTriangle(cx + 6, cy - 8, cx + 2, cy - 8, cx + 4, cy - 16);
    }

    private void generateTrollSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.3f, 0.4f, 0.3f, 1f);
        pm.fillRectangle(cx - 16, cy - 6, 32, 32);
        pm.setColor(0.35f, 0.45f, 0.35f, 1f);
        pm.fillCircle(cx, cy - 14, 14);
        pm.setColor(0.4f, 0.5f, 0.4f, 1f);
        pm.fillCircle(cx, cy - 10, 6);
        pm.setColor(0.9f, 0.4f, 0.2f, 1f);
        pm.fillCircle(cx - 6, cy - 18, 3);
        pm.fillCircle(cx + 6, cy - 18, 3);
        pm.setColor(0.9f, 0.85f, 0.7f, 1f);
        pm.fillRectangle(cx - 8, cy - 4, 3, 8);
        pm.fillRectangle(cx + 5, cy - 4, 3, 8);
    }

    private void generateElementalSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(1f, 0.4f, 0.1f, 0.9f);
        pm.fillCircle(cx, cy, 16);
        pm.setColor(1f, 0.6f, 0.2f, 0.8f);
        pm.fillCircle(cx, cy - 4, 12);
        pm.setColor(1f, 0.8f, 0.3f, 0.7f);
        pm.fillCircle(cx, cy - 8, 8);
        pm.setColor(1f, 0.5f, 0.1f, 0.8f);
        pm.fillTriangle(cx - 10, cy - 8, cx - 4, cy - 8, cx - 7, cy - 24);
        pm.fillTriangle(cx + 4, cy - 8, cx + 10, cy - 8, cx + 7, cy - 22);
        pm.fillTriangle(cx - 4, cy - 12, cx + 4, cy - 12, cx, cy - 28);
        pm.setColor(0.2f, 0.1f, 0.05f, 1f);
        pm.fillCircle(cx - 5, cy - 2, 3);
        pm.fillCircle(cx + 5, cy - 2, 3);
    }

    private void generateGoblinKingSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.5f, 0.15f, 0.15f, 1f);
        pm.fillRectangle(cx - 14, cy - 4, 28, 30);
        pm.setColor(0.4f, 0.6f, 0.35f, 1f);
        pm.fillCircle(cx, cy - 14, 13);
        pm.setColor(0.8f, 0.7f, 0.2f, 1f);
        pm.fillRectangle(cx - 10, cy - 26, 20, 6);
        for (int i = 0; i < 5; i++) {
            pm.fillRectangle(cx - 10 + i * 5, cy - 32, 3, 8);
        }
        pm.setColor(0.45f, 0.65f, 0.4f, 1f);
        pm.fillCircle(cx - 16, cy - 14, 7);
        pm.fillCircle(cx + 16, cy - 14, 7);
        pm.setColor(1f, 0.3f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 14, 3);
        pm.fillCircle(cx + 5, cy - 14, 3);
    }

    private void generateNecromancerSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.1f, 0.08f, 0.12f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 32);
        pm.setColor(0.08f, 0.06f, 0.1f, 1f);
        pm.fillCircle(cx, cy - 16, 14);
        pm.fillRectangle(cx - 14, cy - 22, 28, 12);
        pm.setColor(0.7f, 0.65f, 0.6f, 0.8f);
        pm.fillCircle(cx, cy - 14, 8);
        pm.setColor(0.2f, 1f, 0.3f, 1f);
        pm.fillCircle(cx - 3, cy - 15, 2);
        pm.fillCircle(cx + 3, cy - 15, 2);
        pm.setColor(0.3f, 0.2f, 0.15f, 1f);
        pm.fillRectangle(x + size - 12, cy - 20, 4, 44);
        pm.setColor(0.8f, 0.75f, 0.7f, 1f);
        pm.fillCircle(x + size - 10, cy - 24, 6);
        pm.setColor(0.2f, 0.8f, 0.3f, 0.2f);
        pm.fillCircle(cx, cy, 20);
    }
    private void generateCharacterSprites() {
        int spriteSize = 64;
        Pixmap pm = new Pixmap(spriteSize * 3, spriteSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(456);

        generateWarriorSprite(pm, 0, 0, spriteSize, rand);
        generateRogueSprite(pm, spriteSize, 0, spriteSize, rand);
        generateMageSprite(pm, spriteSize * 2, 0, spriteSize, rand);

        Texture spriteTex = new Texture(pm);
        pm.dispose();

        playerSprites.put("WARRIOR", new TextureRegion(spriteTex, 0, 0, spriteSize, spriteSize));
        playerSprites.put("ROGUE", new TextureRegion(spriteTex, spriteSize, 0, spriteSize, spriteSize));
        playerSprites.put("MAGE", new TextureRegion(spriteTex, spriteSize * 2, 0, spriteSize, spriteSize));
    }

    private void generateWarriorSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.4f, 0.4f, 0.45f, 1f);
        pm.fillRectangle(cx - 12, cy - 4, 24, 28);
        pm.setColor(0.8f, 0.65f, 0.5f, 1f);
        pm.fillCircle(cx, cy - 14, 10);
        pm.setColor(0.5f, 0.5f, 0.55f, 1f);
        pm.fillRectangle(cx - 11, cy - 26, 22, 14);
    }

    private void generateRogueSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.3f, 0.25f, 0.2f, 1f);
        pm.fillRectangle(cx - 10, cy - 4, 20, 26);
        pm.setColor(0.25f, 0.2f, 0.15f, 1f);
        pm.fillCircle(cx, cy - 12, 12);
    }

    private void generateMageSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.2f, 0.15f, 0.35f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 32);
        pm.setColor(0.75f, 0.6f, 0.5f, 1f);
        pm.fillCircle(cx, cy - 12, 8);
    }

    private void generateRatSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2 + 8;
        pm.setColor(0.35f, 0.25f, 0.2f, 1f);
        pm.fillCircle(cx, cy, 14);
        pm.fillCircle(cx - 8, cy - 4, 8);
        pm.setColor(0.8f, 0.2f, 0.2f, 1f);
        pm.fillCircle(cx - 12, cy - 6, 3);
    }

    private void generateSkeletonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillCircle(cx, cy - 14, 12);
        pm.setColor(0.1f, 0.05f, 0.05f, 1f);
        pm.fillCircle(cx - 4, cy - 14, 4);
        pm.fillCircle(cx + 4, cy - 14, 4);
    }

    private void generateGoblinSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.35f, 0.5f, 0.3f, 1f);
        pm.fillCircle(cx, cy - 12, 11);
        pm.setColor(0.4f, 0.55f, 0.35f, 1f);
        pm.fillCircle(cx - 14, cy - 12, 6);
        pm.fillCircle(cx + 14, cy - 12, 6);
    }

    private void generateOrcSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.4f, 0.5f, 0.35f, 1f);
        pm.fillCircle(cx, cy - 14, 13);
        pm.setColor(0.9f, 0.85f, 0.8f, 1f);
        pm.fillRectangle(cx - 8, cy - 6, 3, 8);
        pm.fillRectangle(cx + 5, cy - 6, 3, 8);
    }

    private void generateGhostSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.7f, 0.75f, 0.8f, 0.7f);
        pm.fillCircle(cx, cy - 8, 16);
        pm.setColor(0.1f, 0.1f, 0.15f, 1f);
        pm.fillCircle(cx - 5, cy - 10, 4);
        pm.fillCircle(cx + 5, cy - 10, 4);
    }

    private void generateDemonSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.5f, 0.15f, 0.1f, 1f);
        pm.fillRectangle(cx - 14, cy - 6, 28, 30);
        pm.setColor(1f, 0.8f, 0.2f, 1f);
        pm.fillCircle(cx - 5, cy - 14, 4);
        pm.fillCircle(cx + 5, cy - 14, 4);
    }

    private void generateLichSprite(Pixmap pm, int x, int y, int size, Random rand) {
        int cx = x + size/2;
        int cy = y + size/2;
        pm.setColor(0.15f, 0.1f, 0.2f, 1f);
        pm.fillRectangle(cx - 14, cy - 8, 28, 34);
        pm.setColor(0.85f, 0.8f, 0.75f, 1f);
        pm.fillCircle(cx, cy - 14, 10);
        pm.setColor(0.1f, 0.8f, 0.3f, 1f);
        pm.fillCircle(cx - 4, cy - 15, 3);
        pm.fillCircle(cx + 4, cy - 15, 3);
    }

    private void generatePortraits() {
        int portSize = 64;
        Pixmap pm = new Pixmap(portSize * 3, portSize, Pixmap.Format.RGBA8888);
        Random rand = new Random(789);
        generatePortrait(pm, 0, 0, portSize, new Color(0.3f, 0.4f, 0.8f, 1f), "W", rand);
        generatePortrait(pm, portSize, 0, portSize, new Color(0.3f, 0.6f, 0.3f, 1f), "R", rand);
        generatePortrait(pm, portSize * 2, 0, portSize, new Color(0.6f, 0.3f, 0.7f, 1f), "M", rand);
        portraitTexture = new Texture(pm);
        pm.dispose();
        portraits.put("WARRIOR", new TextureRegion(portraitTexture, 0, 0, portSize, portSize));
        portraits.put("ROGUE", new TextureRegion(portraitTexture, portSize, 0, portSize, portSize));
        portraits.put("MAGE", new TextureRegion(portraitTexture, portSize * 2, 0, portSize, portSize));
    }

    private void generatePortrait(Pixmap pm, int x, int y, int size, Color frameColor, String initial, Random rand) {
        pm.setColor(STONE_DARK);
        pm.fillRectangle(x, y, size, size);
        pm.setColor(STONE_MID);
        pm.fillRectangle(x + 4, y + 4, size - 8, size - 8);
        pm.setColor(frameColor);
        pm.drawRectangle(x + 6, y + 6, size - 12, size - 12);
    }

    private void generateFonts() {
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

    public TextureRegion getWhitePixel() { return whitePixel; }
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
        if (enemyTexture != null) enemyTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (normalFont != null) normalFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}


