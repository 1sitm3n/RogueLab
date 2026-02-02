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
import com.roguelab.gdx.RogueLabGame;

/**
 * Main menu screen with class selection.
 */
public class MenuScreen implements Screen {
    
    private final RogueLabGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    
    private int selectedClass = 0;
    private final String[] classes = {"WARRIOR", "ROGUE", "MAGE"};
    private final String[] classDescriptions = {
        "High HP, balanced stats. Good for beginners.",
        "Fast and deadly. High attack, low defense.",
        "Powerful magic. High damage, fragile."
    };
    private final Color[] classColors = {
        new Color(0.2f, 0.4f, 0.8f, 1f),
        new Color(0.3f, 0.6f, 0.3f, 1f),
        new Color(0.6f, 0.2f, 0.6f, 1f)
    };
    
    private float titlePulse = 0;
    private GlyphLayout layout;
    
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
        float titleScale = 1f + (float)Math.sin(titlePulse) * 0.05f;
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
            boolean selected = (i == selectedClass);
            
            // Selection indicator
            if (selected) {
                normalFont.setColor(classColors[i]);
                normalFont.draw(batch, "> ", centerX - 150, y);
                normalFont.draw(batch, " <", centerX + 120, y);
            }
            
            // Class name
            normalFont.setColor(selected ? classColors[i] : Color.GRAY);
            layout.setText(normalFont, classes[i]);
            normalFont.draw(batch, classes[i], centerX - layout.width / 2, y);
            
            // Description (only for selected)
            if (selected) {
                smallFont.setColor(Color.LIGHT_GRAY);
                layout.setText(smallFont, classDescriptions[i]);
                smallFont.draw(batch, classDescriptions[i], centerX - layout.width / 2, y - 30);
            }
        }
        
        // Instructions
        smallFont.setColor(Color.GRAY);
        String instructions = "UP/DOWN or W/S to select  |  ENTER or SPACE to start  |  ESC to quit";
        layout.setText(smallFont, instructions);
        smallFont.draw(batch, instructions, centerX - layout.width / 2, 60);
        
        // Version
        smallFont.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        smallFont.draw(batch, "v0.5.0 - LibGDX Edition", 10, 30);
        
        batch.end();
    }
    
    private void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw some decorative elements
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        
        // Grid pattern
        for (int x = 0; x < Gdx.graphics.getWidth(); x += 64) {
            for (int y = 0; y < Gdx.graphics.getHeight(); y += 64) {
                float alpha = 0.02f + (float)Math.sin((x + y) * 0.01f + titlePulse * 0.5f) * 0.01f;
                shapeRenderer.setColor(0.3f, 0.3f, 0.4f, alpha);
                shapeRenderer.rect(x + 2, y + 2, 60, 60);
            }
        }
        
        shapeRenderer.end();
    }
    
    private void handleInput() {
        // Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedClass = (selectedClass - 1 + classes.length) % classes.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedClass = (selectedClass + 1) % classes.length;
        }
        
        // Selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.startGame(classes[selectedClass]);
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
