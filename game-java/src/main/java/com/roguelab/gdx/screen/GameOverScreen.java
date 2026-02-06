package com.roguelab.gdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.roguelab.gdx.Assets;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.audio.SoundManager;
import com.roguelab.gdx.audio.SoundManager.SoundEffect;

/**
 * Game over screen showing victory or defeat.
 */
public class GameOverScreen implements Screen {

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final SoundManager sound;
    private final GlyphLayout layout;

    private final boolean victory;
    private final int goldEarned;
    private final int floorsReached;

    private float animTimer = 0;
    private boolean soundPlayed = false;

    // Colors
    private static final Color STONE_DARK = Assets.STONE_DARK;
    private static final Color STONE_MID = Assets.STONE_MID;
    private static final Color GOLD = Assets.GOLD_MID;
    private static final Color GOLD_LIGHT = Assets.GOLD_LIGHT;
    private static final Color BLOOD = Assets.BLOOD_RED;
    private static final Color PARCHMENT = Assets.PARCHMENT_MID;

    public GameOverScreen(RogueLabGame game, boolean victory, int goldEarned, int floorsReached) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.sound = game.getSoundManager();
        this.layout = new GlyphLayout();
        this.victory = victory;
        this.goldEarned = goldEarned;
        this.floorsReached = floorsReached;
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        animTimer += delta;

        // Play sound once after slight delay
        if (!soundPlayed && animTimer > 0.3f) {
            if (victory) {
                sound.play(SoundEffect.VICTORY);
            } else {
                sound.play(SoundEffect.DEFEAT);
            }
            soundPlayed = true;
        }

        handleInput();

        // Background
        Color bgColor = victory ? new Color(0.08f, 0.1f, 0.06f, 1f) : new Color(0.1f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        // Panel
        float panelW = 500;
        float panelH = 350;
        float panelX = centerX - panelW / 2f;
        float panelY = centerY - panelH / 2f;

        drawPanel(panelX, panelY, panelW, panelH);

        batch.begin();

        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont font = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Title with animation
        float titlePulse = 0.8f + MathUtils.sin(animTimer * 3) * 0.2f;
        if (victory) {
            titleFont.setColor(GOLD_LIGHT.r * titlePulse, GOLD_LIGHT.g * titlePulse, GOLD_LIGHT.b * 0.5f, 1f);
        } else {
            titleFont.setColor(BLOOD.r * titlePulse, BLOOD.g * 0.3f, BLOOD.b * 0.3f, 1f);
        }

        titleFont.getData().setScale(3f);
        String title = victory ? "VICTORY!" : "DEFEAT";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, centerX - layout.width / 2f, panelY + panelH - 50);

        // Subtitle
        font.setColor(PARCHMENT);
        String subtitle = victory ? "You have conquered the dungeon!" : "You have fallen in battle...";
        layout.setText(font, subtitle);
        font.draw(batch, subtitle, centerX - layout.width / 2f, panelY + panelH - 110);

        // Stats
        float statsY = centerY + 20;
        
        font.setColor(PARCHMENT);
        font.draw(batch, "Floors Reached:", panelX + 60, statsY);
        font.setColor(victory ? GOLD_LIGHT : Color.WHITE);
        font.draw(batch, String.valueOf(floorsReached), panelX + panelW - 100, statsY);

        statsY -= 40;
        font.setColor(PARCHMENT);
        font.draw(batch, "Gold Collected:", panelX + 60, statsY);
        font.setColor(GOLD);
        font.draw(batch, String.valueOf(goldEarned), panelX + panelW - 100, statsY);

        // Prompt
        float promptPulse = 0.5f + MathUtils.sin(animTimer * 4) * 0.5f;
        smallFont.setColor(PARCHMENT.r, PARCHMENT.g, PARCHMENT.b, promptPulse);
        String prompt = "Press SPACE or ENTER to continue";
        layout.setText(smallFont, prompt);
        smallFont.draw(batch, prompt, centerX - layout.width / 2f, panelY + 50);

        batch.end();
    }

    private void drawPanel(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Outer border
        shapeRenderer.setColor(STONE_DARK.r * 0.3f, STONE_DARK.g * 0.3f, STONE_DARK.b * 0.3f, 1f);
        shapeRenderer.rect(x - 8, y - 8, w + 16, h + 16);

        // Frame
        shapeRenderer.setColor(STONE_MID);
        shapeRenderer.rect(x - 4, y - 4, w + 8, h + 8);

        // Inner panel
        shapeRenderer.setColor(STONE_DARK.r * 0.8f, STONE_DARK.g * 0.8f, STONE_DARK.b * 0.8f, 0.95f);
        shapeRenderer.rect(x, y, w, h);

        shapeRenderer.end();

        // Decorative border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Color borderColor = victory ? GOLD : BLOOD;
        shapeRenderer.setColor(borderColor.r * 0.6f, borderColor.g * 0.6f, borderColor.b * 0.4f, 1f);
        shapeRenderer.rect(x + 10, y + 10, w - 20, h - 20);
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || 
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            sound.play(SoundEffect.MENU_CONFIRM);
            game.returnToMenu();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
