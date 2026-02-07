package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.roguelab.domain.PlayerClass;
import com.roguelab.gdx.Assets;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.audio.SoundManager;
import com.roguelab.gdx.audio.SoundManager.SoundEffect;

/**
 * Main menu with proper viewport scaling.
 */
public class MenuScreen implements Screen {

    private static final float VIRTUAL_WIDTH = 1280;
    private static final float VIRTUAL_HEIGHT = 720;

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final SoundManager sound;

    private final OrthographicCamera camera;
    private final Viewport viewport;

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
        this.sound = game.getSoundManager();
        this.layout = new GlyphLayout();

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply(true);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        handleInput();
        animTimer += delta;

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        float centerX = VIRTUAL_WIDTH / 2f;
        float centerY = VIRTUAL_HEIGHT / 2f;

        drawStoneBackground();
        
        float panelW = 600;
        float panelH = 480;
        float panelX = centerX - panelW / 2f;
        float panelY = centerY - panelH / 2f;
        
        drawOrnatePanel(panelX, panelY, panelW, panelH);

        batch.begin();

        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Title
        float flicker = 0.85f + MathUtils.sin(animTimer * 2) * 0.15f;
        titleFont.setColor(GOLD_LIGHT.r * flicker, GOLD_LIGHT.g * flicker, GOLD_LIGHT.b * 0.5f, 1f);
        titleFont.getData().setScale(3.5f);
        layout.setText(titleFont, "ROGUELAB");
        titleFont.draw(batch, "ROGUELAB", centerX - layout.width / 2f, panelY + panelH - 35);
        titleFont.getData().setScale(3f);

        // Subtitle
        font.setColor(STONE_LIGHT);
        layout.setText(font, "A Dungeon of Data");
        font.draw(batch, "A Dungeon of Data", centerX - layout.width / 2f, panelY + panelH - 90);

        batch.end();
        drawGoldDivider(panelX + 50, panelY + panelH - 115, panelW - 100);
        batch.begin();

        // Header
        font.setColor(PARCHMENT_LIGHT);
        layout.setText(font, "CHOOSE YOUR CLASS");
        font.draw(batch, "CHOOSE YOUR CLASS", centerX - layout.width / 2f, panelY + panelH - 145);

        // Class selection
        float classY = panelY + panelH - 200;
        for (int i = 0; i < classes.length; i++) {
            PlayerClass pc = classes[i];
            boolean selected = (i == selectedIndex);
            
            if (selected) {
                batch.end();
                float pulse = 0.4f + MathUtils.sin(animTimer * 4) * 0.2f;
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
                shapeRenderer.rect(panelX + 40, classY - 32, panelW - 80, 65);
                shapeRenderer.end();
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(GOLD_LIGHT);
                shapeRenderer.rect(panelX + 40, classY - 32, panelW - 80, 65);
                shapeRenderer.end();
                batch.begin();
            }

            TextureRegion portrait = game.getAssets().getPortrait(pc.name());
            float portraitX = panelX + 60;
            batch.draw(portrait, portraitX, classY - 28, 52, 52);

            font.setColor(selected ? GOLD_LIGHT : PARCHMENT);
            font.draw(batch, pc.getDisplayName(), portraitX + 65, classY + 12);

            smallFont.setColor(selected ? PARCHMENT_LIGHT : STONE_LIGHT);
            String stats = String.format("HP: %d   ATK: %d   DEF: %d",
                pc.getStartingHealth(), pc.getStartingAttack(), pc.getStartingDefense());
            smallFont.draw(batch, stats, portraitX + 65, classY - 8);

            if (selected) {
                smallFont.setColor(STONE_LIGHT);
                smallFont.draw(batch, pc.getDescription(), portraitX + 65, classY - 26);
            }

            classY -= 85;
        }

        batch.end();
        drawGoldDivider(panelX + 50, panelY + 75, panelW - 100);
        batch.begin();

        // Instructions
        float instructY = panelY + 52;
        smallFont.setColor(STONE_LIGHT);
        
        String nav = "[ W/S or UP/DOWN ] Select Class";
        layout.setText(smallFont, nav);
        smallFont.draw(batch, nav, centerX - layout.width / 2f, instructY);
        
        float enterPulse = 0.6f + MathUtils.sin(animTimer * 3) * 0.4f;
        smallFont.setColor(GOLD.r * enterPulse + 0.3f, GOLD.g * enterPulse + 0.2f, GOLD.b * 0.3f, 1f);
        String enter = "[ ENTER or SPACE ] Begin Quest";
        layout.setText(smallFont, enter);
        smallFont.draw(batch, enter, centerX - layout.width / 2f, instructY - 20);
        
        smallFont.setColor(STONE_MID);
        String esc = "[ ESC ] Quit";
        layout.setText(smallFont, esc);
        smallFont.draw(batch, esc, centerX - layout.width / 2f, instructY - 40);

        // Version and sound status
        smallFont.setColor(new Color(0.3f, 0.25f, 0.2f, 1f));
        smallFont.draw(batch, "v0.6.0 - Viewport Scaling", 20, 25);
        
        smallFont.setColor(sound.isEnabled() ? GOLD : STONE_MID);
        String soundStatus = "[M] Sound: " + (sound.isEnabled() ? "ON" : "OFF");
        layout.setText(smallFont, soundStatus);
        smallFont.draw(batch, soundStatus, VIRTUAL_WIDTH - layout.width - 20, 25);

        batch.end();
    }

    private void drawStoneBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(STONE_DARK.r * 0.5f, STONE_DARK.g * 0.5f, STONE_DARK.b * 0.5f, 1f);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        
        int brickW = 64;
        int brickH = 32;
        for (int y = 0; y < VIRTUAL_HEIGHT; y += brickH) {
            int offset = ((y / brickH) % 2 == 0) ? 0 : brickW / 2;
            for (int x = -brickW; x < VIRTUAL_WIDTH + brickW; x += brickW) {
                float variation = 0.4f + (float) Math.sin(x * 0.1 + y * 0.1) * 0.1f;
                shapeRenderer.setColor(
                    STONE_DARK.r * variation,
                    STONE_DARK.g * variation,
                    STONE_DARK.b * variation, 1f
                );
                shapeRenderer.rect(x + offset + 1, y + 1, brickW - 2, brickH - 2);
            }
        }
        
        // Vignette
        for (int i = 0; i < 10; i++) {
            float alpha = 0.05f * (10 - i);
            shapeRenderer.setColor(0, 0, 0, alpha);
            shapeRenderer.rect(i * 20, i * 20, VIRTUAL_WIDTH - i * 40, VIRTUAL_HEIGHT - i * 40);
        }
        
        shapeRenderer.end();
    }

    private void drawOrnatePanel(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(STONE_DARK.r * 0.3f, STONE_DARK.g * 0.3f, STONE_DARK.b * 0.3f, 1f);
        shapeRenderer.rect(x - 8, y - 8, w + 16, h + 16);
        
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x - 4, y - 4, w + 8, h + 8);
        
        shapeRenderer.setColor(STONE_DARK.r * 0.8f, STONE_DARK.g * 0.8f, STONE_DARK.b * 0.8f, 0.95f);
        shapeRenderer.rect(x, y, w, h);
        
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        shapeRenderer.setColor(GOLD.r * 0.5f, GOLD.g * 0.5f, GOLD.b * 0.3f, 1f);
        shapeRenderer.rect(x + 10, y + 10, w - 20, h - 20);
        
        shapeRenderer.setColor(GOLD);
        float cornerSize = 20;
        shapeRenderer.line(x + 10, y + h - 10, x + 10, y + h - 10 - cornerSize);
        shapeRenderer.line(x + 10, y + h - 10, x + 10 + cornerSize, y + h - 10);
        shapeRenderer.line(x + w - 10, y + h - 10, x + w - 10, y + h - 10 - cornerSize);
        shapeRenderer.line(x + w - 10, y + h - 10, x + w - 10 - cornerSize, y + h - 10);
        shapeRenderer.line(x + 10, y + 10, x + 10, y + 10 + cornerSize);
        shapeRenderer.line(x + 10, y + 10, x + 10 + cornerSize, y + 10);
        shapeRenderer.line(x + w - 10, y + 10, x + w - 10, y + 10 + cornerSize);
        shapeRenderer.line(x + w - 10, y + 10, x + w - 10 - cornerSize, y + 10);
        
        shapeRenderer.end();
    }

    private void drawGoldDivider(float x, float y, float w) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(GOLD.r * 0.5f, GOLD.g * 0.5f, GOLD.b * 0.3f, 1f);
        shapeRenderer.rect(x, y, w, 2);
        
        float cx = x + w / 2f;
        shapeRenderer.setColor(GOLD);
        shapeRenderer.triangle(cx - 6, y + 1, cx + 6, y + 1, cx, y + 8);
        shapeRenderer.triangle(cx - 6, y + 1, cx + 6, y + 1, cx, y - 6);
        
        shapeRenderer.rect(x, y - 2, 4, 6);
        shapeRenderer.rect(x + w - 4, y - 2, 4, 6);
        
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + classes.length) % classes.length;
            sound.play(SoundEffect.MENU_SELECT);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % classes.length;
            sound.play(SoundEffect.MENU_SELECT);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            sound.play(SoundEffect.MENU_CONFIRM);
            game.startGame(classes[selectedIndex]);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            sound.toggle();
            if (sound.isEnabled()) {
                sound.play(SoundEffect.MENU_SELECT);
            }
        }
    }

    @Override 
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
