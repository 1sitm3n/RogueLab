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
import com.roguelab.domain.PlayerClass;
import com.roguelab.gdx.RogueLabGame;

/**
 * Main menu screen with class selection.
 * Uses the real PlayerClass enum from the domain model.
 */
public class MenuScreen implements Screen {

    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private int selectedIndex = 0;
    private final PlayerClass[] classes = PlayerClass.values();
    private final Color[] classColors = {
        new Color(0.2f, 0.4f, 0.8f, 1f),  // WARRIOR - blue
        new Color(0.3f, 0.6f, 0.3f, 1f),  // ROGUE - green
        new Color(0.6f, 0.2f, 0.6f, 1f)   // MAGE - purple
    };

    private float titlePulse = 0;
    private final GlyphLayout layout;

    public MenuScreen(RogueLabGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.layout = new GlyphLayout();
    }

    @Override
    public void show() {
        Gdx.app.log("MenuScreen", "Showing menu");
    }

    @Override
    public void render(float delta) {
        // Update
        handleInput();
        titlePulse += delta * 2;

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        // Draw background decorations
        drawBackground();

        // Draw UI
        batch.begin();

        BitmapFont titleFont = game.getAssets().getTitleFont();
        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();

        // Title with pulse effect
        float titleScale = 1f + (float) Math.sin(titlePulse) * 0.05f;
        titleFont.getData().setScale(3f * titleScale);
        titleFont.setColor(new Color(0.9f, 0.8f, 0.3f, 1f));
        layout.setText(titleFont, "ROGUELAB");
        titleFont.draw(batch, "ROGUELAB", centerX - layout.width / 2, centerY + 200);
        titleFont.getData().setScale(3f);

        // Subtitle
        normalFont.setColor(Color.LIGHT_GRAY);
        layout.setText(normalFont, "A Data-Driven Roguelike");
        normalFont.draw(batch, "A Data-Driven Roguelike", centerX - layout.width / 2, centerY + 140);

        // Class selection header
        normalFont.setColor(Color.WHITE);
        layout.setText(normalFont, "SELECT YOUR CLASS");
        normalFont.draw(batch, "SELECT YOUR CLASS", centerX - layout.width / 2, centerY + 60);

        // Class options
        for (int i = 0; i < classes.length; i++) {
            float y = centerY - 20 - i * 70;
            boolean selected = (i == selectedIndex);
            PlayerClass pc = classes[i];

            // Selection indicator
            if (selected) {
                normalFont.setColor(classColors[i]);
                normalFont.draw(batch, "> ", centerX - 150, y);
                normalFont.draw(batch, " <", centerX + 120, y);
            }

            // Class name
            normalFont.setColor(selected ? classColors[i] : Color.GRAY);
            layout.setText(normalFont, pc.getDisplayName());
            normalFont.draw(batch, pc.getDisplayName(), centerX - layout.width / 2, y);

            // Description (only for selected)
            if (selected) {
                smallFont.setColor(Color.LIGHT_GRAY);
                layout.setText(smallFont, pc.getDescription());
                smallFont.draw(batch, pc.getDescription(), centerX - layout.width / 2, y - 25);

                // Stats
                smallFont.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
                String stats = String.format("HP: %d  ATK: %d  DEF: %d",
                    pc.getStartingHealth(), pc.getStartingAttack(), pc.getStartingDefense());
                layout.setText(smallFont, stats);
                smallFont.draw(batch, stats, centerX - layout.width / 2, y - 45);
            }
        }

        // Instructions
        smallFont.setColor(Color.GRAY);
        String instructions = "UP/DOWN or W/S to select  |  ENTER or SPACE to start  |  ESC to quit";
        layout.setText(smallFont, instructions);
        smallFont.draw(batch, instructions, centerX - layout.width / 2, 60);

        // Version
        smallFont.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        smallFont.draw(batch, "v0.5.0 - LibGDX + Domain Integration", 10, 30);

        batch.end();
    }

    private void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Grid pattern
        for (int x = 0; x < Gdx.graphics.getWidth(); x += 64) {
            for (int y = 0; y < Gdx.graphics.getHeight(); y += 64) {
                float alpha = 0.02f + (float) Math.sin((x + y) * 0.01f + titlePulse * 0.5f) * 0.01f;
                shapeRenderer.setColor(0.3f, 0.3f, 0.4f, alpha);
                shapeRenderer.rect(x + 2, y + 2, 60, 60);
            }
        }

        shapeRenderer.end();
    }

    private void handleInput() {
        // Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + classes.length) % classes.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % classes.length;
        }

        // Selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.startGame(classes[selectedIndex]);
        }

        // Quit
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
