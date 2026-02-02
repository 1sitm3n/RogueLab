package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.roguelab.domain.PlayerClass;
import com.roguelab.gdx.Assets;
import com.roguelab.gdx.RogueLabGame;

/**
 * Daggerfall-style main menu with gothic stone aesthetic.
 */
public class MenuScreen implements Screen {

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private int selectedIndex = 0;
    private final PlayerClass[] classes = PlayerClass.values();
    
    private float animTimer = 0;
    private final GlyphLayout layout;

    // Daggerfall colors
    private static final Color STONE_DARK = Assets.STONE_DARK;
    private static final Color STONE_MID = Assets.STONE_MID;
    private static final Color STONE_LIGHT = Assets.STONE_LIGHT;
    private static final Color PARCHMENT = Assets.PARCHMENT_MID;
    private static final Color PARCHMENT_LIGHT = Assets.PARCHMENT_LIGHT;
    private static final Color GOLD = Assets.GOLD_MID;
    private static final Color GOLD_LIGHT = Assets.GOLD_LIGHT;
    private static final Color BLOOD = Assets.BLOOD_RED;

    public MenuScreen(RogueLabGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.layout = new GlyphLayout();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        handleInput();
        animTimer += delta;

        // Dark dungeon background
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        // Stone background texture pattern
        drawStoneBackground(screenWidth, screenHeight);
        
        // Main panel
        float panelW = 600;
        float panelH = 500;
        float panelX = centerX - panelW / 2f;
        float panelY = centerY - panelH / 2f;
        
        drawOrnatePanel(panelX, panelY, panelW, panelH);

        batch.begin();

        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Title with flicker effect
        float flicker = 0.85f + MathUtils.sin(animTimer * 2) * 0.15f;
        titleFont.setColor(GOLD_LIGHT.r * flicker, GOLD_LIGHT.g * flicker, GOLD_LIGHT.b * 0.5f, 1f);
        titleFont.getData().setScale(3.5f);
        layout.setText(titleFont, "ROGUELAB");
        titleFont.draw(batch, "ROGUELAB", centerX - layout.width / 2f, panelY + panelH - 40);
        titleFont.getData().setScale(3f);

        // Subtitle
        font.setColor(STONE_LIGHT);
        layout.setText(font, "A Dungeon of Data");
        font.draw(batch, "A Dungeon of Data", centerX - layout.width / 2f, panelY + panelH - 100);

        // Divider line
        batch.end();
        drawGoldDivider(panelX + 50, panelY + panelH - 130, panelW - 100);
        batch.begin();

        // "Choose Your Class" header
        font.setColor(PARCHMENT_LIGHT);
        layout.setText(font, "CHOOSE YOUR CLASS");
        font.draw(batch, "CHOOSE YOUR CLASS", centerX - layout.width / 2f, panelY + panelH - 160);

        // Class selection
        float classY = panelY + panelH - 220;
        for (int i = 0; i < classes.length; i++) {
            PlayerClass pc = classes[i];
            boolean selected = (i == selectedIndex);
            
            // Selection highlight
            if (selected) {
                batch.end();
                float pulse = 0.4f + MathUtils.sin(animTimer * 4) * 0.2f;
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
                shapeRenderer.rect(panelX + 40, classY - 35, panelW - 80, 70);
                shapeRenderer.end();
                
                // Border
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(GOLD_LIGHT);
                shapeRenderer.rect(panelX + 40, classY - 35, panelW - 80, 70);
                shapeRenderer.end();
                batch.begin();
            }

            // Portrait
            TextureRegion portrait = game.getAssets().getPortrait(pc.name());
            float portraitX = panelX + 60;
            batch.draw(portrait, portraitX, classY - 30, 56, 56);

            // Class name
            font.setColor(selected ? GOLD_LIGHT : PARCHMENT);
            font.draw(batch, pc.getDisplayName(), portraitX + 70, classY + 15);

            // Stats
            smallFont.setColor(selected ? PARCHMENT_LIGHT : STONE_LIGHT);
            String stats = String.format("HP: %d   ATK: %d   DEF: %d",
                pc.getStartingHealth(), pc.getStartingAttack(), pc.getStartingDefense());
            smallFont.draw(batch, stats, portraitX + 70, classY - 10);

            // Description (only for selected)
            if (selected) {
                smallFont.setColor(STONE_LIGHT);
                smallFont.draw(batch, pc.getDescription(), portraitX + 70, classY - 28);
            }

            classY -= 90;
        }

        // Divider
        batch.end();
        drawGoldDivider(panelX + 50, panelY + 80, panelW - 100);
        batch.begin();

        // Instructions
        float instructY = panelY + 55;
        smallFont.setColor(STONE_LIGHT);
        
        // Arrow keys hint
        String nav = "[ W/S or UP/DOWN ] Select Class";
        layout.setText(smallFont, nav);
        smallFont.draw(batch, nav, centerX - layout.width / 2f, instructY);
        
        // Enter hint with pulse
        float enterPulse = 0.6f + MathUtils.sin(animTimer * 3) * 0.4f;
        smallFont.setColor(GOLD.r * enterPulse + 0.3f, GOLD.g * enterPulse + 0.2f, GOLD.b * 0.3f, 1f);
        String enter = "[ ENTER or SPACE ] Begin Quest";
        layout.setText(smallFont, enter);
        smallFont.draw(batch, enter, centerX - layout.width / 2f, instructY - 22);
        
        smallFont.setColor(STONE_MID);
        String esc = "[ ESC ] Quit";
        layout.setText(smallFont, esc);
        smallFont.draw(batch, esc, centerX - layout.width / 2f, instructY - 44);

        // Version
        smallFont.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        smallFont.draw(batch, "v0.5.1 - Classic Dungeon UI", 20, 30);

        batch.end();
    }

    private void drawStoneBackground(float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Base dark stone
        shapeRenderer.setColor(STONE_DARK.r * 0.5f, STONE_DARK.g * 0.5f, STONE_DARK.b * 0.5f, 1f);
        shapeRenderer.rect(0, 0, w, h);
        
        // Stone brick pattern
        int brickW = 64;
        int brickH = 32;
        for (int y = 0; y < h; y += brickH) {
            int offset = ((y / brickH) % 2 == 0) ? 0 : brickW / 2;
            for (int x = -brickW; x < w + brickW; x += brickW) {
                float variation = 0.4f + (float) Math.sin(x * 0.1 + y * 0.1) * 0.1f;
                shapeRenderer.setColor(
                    STONE_DARK.r * variation,
                    STONE_DARK.g * variation,
                    STONE_DARK.b * variation, 1f
                );
                shapeRenderer.rect(x + offset + 1, y + 1, brickW - 2, brickH - 2);
            }
        }
        
        // Vignette effect
        for (int i = 0; i < 10; i++) {
            float alpha = 0.05f * (10 - i);
            shapeRenderer.setColor(0, 0, 0, alpha);
            shapeRenderer.rect(i * 20, i * 20, w - i * 40, h - i * 40);
        }
        
        shapeRenderer.end();
    }

    private void drawOrnatePanel(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer dark border
        shapeRenderer.setColor(STONE_DARK.r * 0.3f, STONE_DARK.g * 0.3f, STONE_DARK.b * 0.3f, 1f);
        shapeRenderer.rect(x - 8, y - 8, w + 16, h + 16);
        
        // Stone frame
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x - 4, y - 4, w + 8, h + 8);
        
        // Inner panel (parchment-ish)
        shapeRenderer.setColor(STONE_DARK.r * 0.8f, STONE_DARK.g * 0.8f, STONE_DARK.b * 0.8f, 0.95f);
        shapeRenderer.rect(x, y, w, h);
        
        shapeRenderer.end();
        
        // Decorative border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Gold inner border
        shapeRenderer.setColor(GOLD.r * 0.5f, GOLD.g * 0.5f, GOLD.b * 0.3f, 1f);
        shapeRenderer.rect(x + 10, y + 10, w - 20, h - 20);
        
        // Corner accents
        shapeRenderer.setColor(GOLD);
        float cornerSize = 20;
        // Top-left
        shapeRenderer.line(x + 10, y + h - 10, x + 10, y + h - 10 - cornerSize);
        shapeRenderer.line(x + 10, y + h - 10, x + 10 + cornerSize, y + h - 10);
        // Top-right
        shapeRenderer.line(x + w - 10, y + h - 10, x + w - 10, y + h - 10 - cornerSize);
        shapeRenderer.line(x + w - 10, y + h - 10, x + w - 10 - cornerSize, y + h - 10);
        // Bottom-left
        shapeRenderer.line(x + 10, y + 10, x + 10, y + 10 + cornerSize);
        shapeRenderer.line(x + 10, y + 10, x + 10 + cornerSize, y + 10);
        // Bottom-right
        shapeRenderer.line(x + w - 10, y + 10, x + w - 10, y + 10 + cornerSize);
        shapeRenderer.line(x + w - 10, y + 10, x + w - 10 - cornerSize, y + 10);
        
        shapeRenderer.end();
    }

    private void drawGoldDivider(float x, float y, float w) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Main line
        shapeRenderer.setColor(GOLD.r * 0.5f, GOLD.g * 0.5f, GOLD.b * 0.3f, 1f);
        shapeRenderer.rect(x, y, w, 2);
        
        // Center diamond
        float cx = x + w / 2f;
        shapeRenderer.setColor(GOLD);
        shapeRenderer.triangle(cx - 6, y + 1, cx + 6, y + 1, cx, y + 8);
        shapeRenderer.triangle(cx - 6, y + 1, cx + 6, y + 1, cx, y - 6);
        
        // End caps
        shapeRenderer.rect(x, y - 2, 4, 6);
        shapeRenderer.rect(x + w - 4, y - 2, 4, 6);
        
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + classes.length) % classes.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % classes.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.startGame(classes[selectedIndex]);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
