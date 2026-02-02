package com.roguelab.gdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.roguelab.gdx.RogueLabGame;
import com.roguelab.gdx.screen.GameScreen.GameState;
import com.roguelab.gdx.screen.GameScreen.RoomType;

/**
 * Renders the dungeon exploration view - map of rooms, player position, etc.
 */
public class DungeonRenderer {
    
    private final RogueLabGame game;
    private final GameState state;
    private final GlyphLayout layout;
    
    private static final int ROOM_SIZE = 80;
    private static final int ROOM_SPACING = 100;
    
    public DungeonRenderer(RogueLabGame game, GameState state) {
        this.game = game;
        this.state = state;
        this.layout = new GlyphLayout();
    }
    
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float delta) {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        
        // Calculate map offset to center on current room
        float mapWidth = state.roomCount * ROOM_SPACING;
        float mapStartX = centerX - mapWidth / 2f + ROOM_SPACING / 2f;
        float mapY = centerY + 50;
        
        // Draw connections first (below rooms)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
        
        for (int i = 0; i < state.roomCount - 1; i++) {
            float x1 = mapStartX + i * ROOM_SPACING;
            float x2 = mapStartX + (i + 1) * ROOM_SPACING;
            shapeRenderer.rectLine(x1, mapY, x2, mapY, 4);
        }
        shapeRenderer.end();
        
        // Draw rooms
        batch.begin();
        
        for (int i = 0; i < state.roomCount; i++) {
            float roomX = mapStartX + i * ROOM_SPACING - ROOM_SIZE / 2f;
            float roomY = mapY - ROOM_SIZE / 2f;
            
            boolean visited = state.visitedRooms[i];
            boolean current = (i == state.currentRoom);
            boolean cleared = state.roomsCleared[i];
            
            // Room background
            TextureRegion tile = getRoomTile(state.rooms[i], visited);
            
            // Draw room tile
            if (visited || current) {
                batch.setColor(current ? Color.WHITE : new Color(0.7f, 0.7f, 0.7f, 1f));
            } else {
                batch.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
            }
            batch.draw(tile, roomX, roomY, ROOM_SIZE, ROOM_SIZE);
            
            // Draw cleared marker
            if (cleared) {
                batch.setColor(new Color(0.2f, 0.8f, 0.2f, 0.5f));
                batch.draw(game.getAssets().getWhitePixel(), roomX, roomY, ROOM_SIZE, ROOM_SIZE);
            }
            
            // Draw current room indicator
            if (current) {
                // Player icon
                TextureRegion playerSprite = game.getAssets().getPlayerSprite(state.playerClass);
                batch.setColor(Color.WHITE);
                batch.draw(playerSprite, roomX + ROOM_SIZE / 2f - 16, roomY + ROOM_SIZE / 2f - 16, 32, 32);
            }
            
            batch.setColor(Color.WHITE);
        }
        
        // Draw floor indicator
        BitmapFont normalFont = game.getAssets().getNormalFont();
        normalFont.setColor(Color.WHITE);
        String floorText = "FLOOR " + state.floor;
        layout.setText(normalFont, floorText);
        normalFont.draw(batch, floorText, centerX - layout.width / 2, mapY + ROOM_SIZE / 2f + 60);
        
        // Draw room info for current room
        drawRoomInfo(batch, centerY - 120);
        
        // Draw controls hint
        BitmapFont smallFont = game.getAssets().getSmallFont();
        smallFont.setColor(Color.GRAY);
        String controls = "A/D or LEFT/RIGHT to move | SPACE to interact | ESC for menu";
        layout.setText(smallFont, controls);
        smallFont.draw(batch, controls, centerX - layout.width / 2, 40);
        
        batch.end();
    }
    
    private void drawRoomInfo(SpriteBatch batch, float y) {
        float centerX = Gdx.graphics.getWidth() / 2f;
        
        BitmapFont normalFont = game.getAssets().getNormalFont();
        BitmapFont smallFont = game.getAssets().getSmallFont();
        
        RoomType type = state.rooms[state.currentRoom];
        boolean cleared = state.roomsCleared[state.currentRoom];
        
        // Room type name
        normalFont.setColor(getRoomColor(type));
        String roomName = getRoomName(type);
        if (cleared && type != RoomType.EMPTY) {
            roomName += " (Cleared)";
        }
        layout.setText(normalFont, roomName);
        normalFont.draw(batch, roomName, centerX - layout.width / 2, y);
        
        // Room description
        smallFont.setColor(Color.LIGHT_GRAY);
        String desc = getRoomDescription(type, cleared);
        layout.setText(smallFont, desc);
        smallFont.draw(batch, desc, centerX - layout.width / 2, y - 35);
    }
    
    private TextureRegion getRoomTile(RoomType type, boolean visited) {
        if (!visited) {
            return game.getAssets().getTile("fog");
        }
        
        switch (type) {
            case COMBAT: return game.getAssets().getTile("combat");
            case BOSS: return game.getAssets().getTile("boss");
            case TREASURE: return game.getAssets().getTile("chest");
            case SHOP: return game.getAssets().getTile("shop");
            case REST: return game.getAssets().getTile("rest");
            case STAIRS: return game.getAssets().getTile("stairs_down");
            default: return game.getAssets().getTile("floor");
        }
    }
    
    private Color getRoomColor(RoomType type) {
        switch (type) {
            case COMBAT: return new Color(0.9f, 0.3f, 0.3f, 1f);
            case BOSS: return new Color(1f, 0.5f, 0.1f, 1f);
            case TREASURE: return new Color(1f, 0.85f, 0.2f, 1f);
            case SHOP: return new Color(0.3f, 0.8f, 0.9f, 1f);
            case REST: return new Color(0.3f, 0.9f, 0.4f, 1f);
            case STAIRS: return new Color(0.7f, 0.7f, 0.9f, 1f);
            default: return Color.LIGHT_GRAY;
        }
    }
    
    private String getRoomName(RoomType type) {
        switch (type) {
            case COMBAT: return "Monster Den";
            case BOSS: return "Boss Chamber";
            case TREASURE: return "Treasure Room";
            case SHOP: return "Merchant";
            case REST: return "Rest Site";
            case STAIRS: return "Stairs Down";
            default: return "Empty Room";
        }
    }
    
    private String getRoomDescription(RoomType type, boolean cleared) {
        if (cleared) {
            return "Nothing left here.";
        }
        
        switch (type) {
            case COMBAT: return "Enemies lurk in the shadows. Prepare for battle!";
            case BOSS: return "A powerful foe awaits. Defeat it to proceed.";
            case TREASURE: return "Press SPACE to open the chest.";
            case SHOP: return "Press SPACE to buy a weapon upgrade (50 gold).";
            case REST: return "Press SPACE to rest and recover 30% HP.";
            case STAIRS: return state.bossDefeated ? "Press SPACE to descend." : "Defeat the boss first!";
            default: return "An empty chamber.";
        }
    }
}
