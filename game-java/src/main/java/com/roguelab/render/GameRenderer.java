package com.roguelab.render;

import com.roguelab.domain.*;
import com.roguelab.dungeon.Floor;
import com.roguelab.game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Main game renderer using Java2D.
 * Renders the dungeon map, combat, and UI elements.
 */
public class GameRenderer extends JPanel {
    
    private static final int ROOM_SIZE = 80;
    private static final int ROOM_GAP = 20;
    private static final int PADDING = 30;
    private static final int STATS_WIDTH = 250;
    private static final int LOG_HEIGHT = 150;
    
    private GameSession session;
    private final java.util.List<String> messageLog = new java.util.ArrayList<>();
    private static final int MAX_LOG_MESSAGES = 8;
    
    public GameRenderer() {
        setBackground(RenderColors.BACKGROUND);
        setDoubleBuffered(true);
        setFocusable(true);
    }
    
    public void setSession(GameSession session) {
        this.session = session;
        repaint();
    }
    
    public void addMessage(String message) {
        messageLog.add(0, message);
        if (messageLog.size() > MAX_LOG_MESSAGES) {
            messageLog.remove(messageLog.size() - 1);
        }
        repaint();
    }
    
    public void clearMessages() {
        messageLog.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        if (session == null) {
            drawWelcomeScreen(g2);
            return;
        }
        
        // Layout: [Map Area] [Stats Panel]
        //         [Message Log           ]
        
        int mapWidth = getWidth() - STATS_WIDTH - PADDING * 3;
        int mapHeight = getHeight() - LOG_HEIGHT - PADDING * 3;
        
        // Draw dungeon map
        drawDungeonMap(g2, PADDING, PADDING, mapWidth, mapHeight);
        
        // Draw stats panel
        drawStatsPanel(g2, getWidth() - STATS_WIDTH - PADDING, PADDING, STATS_WIDTH, mapHeight);
        
        // Draw message log
        drawMessageLog(g2, PADDING, getHeight() - LOG_HEIGHT - PADDING, getWidth() - PADDING * 2, LOG_HEIGHT);
        
        // Draw combat overlay if in combat
        if (session.getState() == GameState.IN_COMBAT) {
            drawCombatOverlay(g2);
        }
    }
    
    private void drawWelcomeScreen(Graphics2D g2) {
        g2.setColor(RenderColors.TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        String title = "ROGUELAB";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 30);
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.setColor(RenderColors.TEXT_SECONDARY);
        String subtitle = "Press SPACE to start";
        int subWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (getWidth() - subWidth) / 2, getHeight() / 2 + 20);
    }
    
    private void drawDungeonMap(Graphics2D g2, int x, int y, int width, int height) {
        // Panel background
        drawPanel(g2, x, y, width, height, "FLOOR " + session.getCurrentFloorNumber());
        
        Floor floor = session.getCurrentFloor();
        if (floor == null) return;
        
        List<Room> rooms = floor.getRooms();
        int totalRoomsWidth = rooms.size() * ROOM_SIZE + (rooms.size() - 1) * ROOM_GAP;
        int startX = x + (width - totalRoomsWidth) / 2;
        int roomY = y + (height - ROOM_SIZE) / 2;
        
        // Draw connections between rooms
        g2.setColor(RenderColors.BORDER);
        g2.setStroke(new BasicStroke(3));
        for (int i = 0; i < rooms.size() - 1; i++) {
            int rx1 = startX + i * (ROOM_SIZE + ROOM_GAP) + ROOM_SIZE;
            int rx2 = startX + (i + 1) * (ROOM_SIZE + ROOM_GAP);
            g2.drawLine(rx1, roomY + ROOM_SIZE / 2, rx2, roomY + ROOM_SIZE / 2);
        }
        
        // Draw rooms
        int currentIndex = floor.getCurrentRoomIndex();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int rx = startX + i * (ROOM_SIZE + ROOM_GAP);
            boolean isCurrent = (i == currentIndex);
            boolean isCleared = room.isCleared();
            boolean isVisited = (i <= currentIndex);
            
            drawRoom(g2, rx, roomY, room, isCurrent, isCleared, isVisited);
        }
    }
    
    private void drawRoom(Graphics2D g2, int x, int y, Room room, boolean isCurrent, boolean isCleared, boolean isVisited) {
        // Room background based on type
        Color roomColor = switch (room.getType()) {
            case COMBAT -> RenderColors.ROOM_COMBAT;
            case SHOP -> RenderColors.ROOM_SHOP;
            case REST -> RenderColors.ROOM_REST;
            case TREASURE -> RenderColors.ROOM_TREASURE;
            case BOSS -> RenderColors.ROOM_BOSS;
            case EVENT -> RenderColors.ROOM_EVENT;
            default -> RenderColors.ROOM_EMPTY;
        };
        
        // Dim unvisited rooms
        if (!isVisited) {
            roomColor = darker(roomColor, 0.4f);
        } else if (isCleared) {
            roomColor = blend(roomColor, RenderColors.ROOM_CLEARED, 0.5f);
        }
        
        // Draw room shape
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, ROOM_SIZE, ROOM_SIZE, 12, 12);
        g2.setColor(roomColor);
        g2.fill(rect);
        
        // Current room highlight
        if (isCurrent) {
            g2.setColor(RenderColors.ROOM_CURRENT);
            g2.setStroke(new BasicStroke(3));
            g2.draw(rect);
        }
        
        // Room type icon/text
        g2.setColor(RenderColors.TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        String label = switch (room.getType()) {
            case COMBAT -> "FIGHT";
            case SHOP -> "SHOP";
            case REST -> "REST";
            case TREASURE -> "LOOT";
            case BOSS -> "BOSS";
            case EVENT -> "EVENT";
            default -> "?";
        };
        
        FontMetrics fm = g2.getFontMetrics();
        int labelX = x + (ROOM_SIZE - fm.stringWidth(label)) / 2;
        int labelY = y + ROOM_SIZE / 2 + fm.getAscent() / 2;
        g2.drawString(label, labelX, labelY);
        
        // Enemy count indicator
        if (room.getType() == RoomType.COMBAT || room.getType() == RoomType.BOSS) {
            int enemyCount = room.getEnemies().size();
            if (enemyCount > 0 && !isCleared) {
                g2.setColor(RenderColors.ENEMY);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                String count = "x" + enemyCount;
                g2.drawString(count, x + ROOM_SIZE - fm.stringWidth(count) - 5, y + 15);
            }
        }
        
        // Cleared checkmark
        if (isCleared) {
            g2.setColor(RenderColors.ROOM_CLEARED);
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.drawString("✓", x + 5, y + 18);
        }
    }
    
    private void drawStatsPanel(Graphics2D g2, int x, int y, int width, int height) {
        drawPanel(g2, x, y, width, height, "STATS");
        
        Player player = session.getPlayer();
        int textX = x + 15;
        int textY = y + 50;
        int lineHeight = 28;
        
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        // Player name and class
        g2.setColor(RenderColors.PLAYER);
        g2.drawString(player.getName(), textX, textY);
        g2.setColor(RenderColors.TEXT_SECONDARY);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString("Level " + player.getLevel() + " " + player.getPlayerClass(), textX, textY + 16);
        textY += lineHeight + 15;
        
        // Health bar
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.setColor(RenderColors.TEXT_PRIMARY);
        g2.drawString("HP", textX, textY);
        
        int barX = textX + 30;
        int barWidth = width - 60;
        int barHeight = 16;
        
        float healthPct = (float) player.getHealth().getCurrent() / player.getHealth().getMaximum();
        Color healthColor = healthPct > 0.3f ? RenderColors.PLAYER_HEALTH : RenderColors.PLAYER_HEALTH_LOW;
        
        drawProgressBar(g2, barX, textY - 12, barWidth, barHeight, healthPct, healthColor, RenderColors.PLAYER_HEALTH_BG);
        
        g2.setColor(RenderColors.TEXT_PRIMARY);
        String healthText = player.getHealth().getCurrent() + "/" + player.getHealth().getMaximum();
        g2.drawString(healthText, barX + barWidth + 5, textY);
        textY += lineHeight + 5;
        
        // Stats
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(RenderColors.TEXT_SECONDARY);
        g2.drawString("ATK: " + player.getCombat().getTotalAttack(), textX, textY);
        g2.drawString("DEF: " + player.getCombat().getTotalDefense(), textX + 80, textY);
        textY += lineHeight;
        
        // Gold
        g2.setColor(RenderColors.TEXT_GOLD);
        g2.drawString("Gold: " + player.getInventory().getGold(), textX, textY);
        textY += lineHeight;
        
        // XP
        g2.setColor(RenderColors.TEXT_SECONDARY);
        g2.drawString("XP: " + player.getExperience(), textX, textY);
        textY += lineHeight + 10;
        
        // Game state
        g2.setColor(RenderColors.TEXT_MUTED);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("State: " + session.getState(), textX, textY);
        textY += lineHeight;
        
        // Run statistics
        RunStatistics stats = session.getStatistics();
        g2.drawString("Kills: " + stats.getEnemiesKilled(), textX, textY);
        textY += 18;
        g2.drawString("Rooms: " + stats.getRoomsCleared() + "/" + stats.getRoomsVisited(), textX, textY);
        textY += lineHeight + 10;
        
        // Controls hint
        g2.setColor(RenderColors.TEXT_MUTED);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("[SPACE] Advance", textX, height - 60);
        g2.drawString("[R] Rest  [I] Items", textX, height - 45);
        g2.drawString("[Q] Quit", textX, height - 30);
    }
    
    private void drawMessageLog(Graphics2D g2, int x, int y, int width, int height) {
        drawPanel(g2, x, y, width, height, "LOG");
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int textY = y + 40;
        int lineHeight = 16;
        
        for (int i = 0; i < messageLog.size() && i < MAX_LOG_MESSAGES; i++) {
            String msg = messageLog.get(i);
            float alpha = 1.0f - (i * 0.1f);
            g2.setColor(new Color(
                RenderColors.TEXT_SECONDARY.getRed(),
                RenderColors.TEXT_SECONDARY.getGreen(),
                RenderColors.TEXT_SECONDARY.getBlue(),
                (int)(alpha * 255)
            ));
            g2.drawString("> " + msg, x + 15, textY + i * lineHeight);
        }
    }
    
    private void drawCombatOverlay(Graphics2D g2) {
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        int panelWidth = 500;
        int panelHeight = 350;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;
        
        drawPanel(g2, panelX, panelY, panelWidth, panelHeight, "COMBAT");
        
        Player player = session.getPlayer();
        Room room = session.getCurrentRoom();
        List<Enemy> enemies = room.getEnemies().stream()
            .filter(e -> !e.isDead())
            .toList();
        
        int contentX = panelX + 20;
        int contentY = panelY + 50;
        
        // Player side
        g2.setColor(RenderColors.PLAYER);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString(player.getName(), contentX, contentY);
        
        float playerHealthPct = (float) player.getHealth().getCurrent() / player.getHealth().getMaximum();
        drawProgressBar(g2, contentX, contentY + 10, 180, 20, playerHealthPct, 
            RenderColors.PLAYER_HEALTH, RenderColors.PLAYER_HEALTH_BG);
        
        g2.setColor(RenderColors.TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString(player.getHealth().getCurrent() + "/" + player.getHealth().getMaximum() + " HP",
            contentX, contentY + 50);
        g2.drawString("ATK: " + player.getCombat().getTotalAttack() + 
            "  DEF: " + player.getCombat().getTotalDefense(), contentX, contentY + 70);
        
        // VS
        g2.setColor(RenderColors.TEXT_MUTED);
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2.drawString("VS", panelX + panelWidth / 2 - 15, panelY + panelHeight / 2 - 20);
        
        // Enemies side
        int enemyX = panelX + panelWidth - 200;
        int enemyY = contentY;
        
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        for (int i = 0; i < Math.min(enemies.size(), 4); i++) {
            Enemy enemy = enemies.get(i);
            int ey = enemyY + i * 60;
            
            g2.setColor(RenderColors.ENEMY);
            g2.drawString(enemy.getType().name(), enemyX, ey);
            
            float enemyHealthPct = (float) enemy.getHealth().getCurrent() / enemy.getHealth().getMaximum();
            drawProgressBar(g2, enemyX, ey + 10, 150, 16, enemyHealthPct,
                RenderColors.ENEMY_HEALTH, RenderColors.ENEMY_HEALTH_BG);
            
            g2.setColor(RenderColors.TEXT_SECONDARY);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.drawString(enemy.getHealth().getCurrent() + "/" + enemy.getHealth().getMaximum(),
                enemyX, ey + 40);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        }
        
        // Combat instruction
        g2.setColor(RenderColors.TEXT_MUTED);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString("Press SPACE to fight", panelX + panelWidth / 2 - 70, panelY + panelHeight - 30);
    }
    
    private void drawPanel(Graphics2D g2, int x, int y, int width, int height, String title) {
        // Background
        g2.setColor(RenderColors.PANEL_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, width, height, 10, 10));
        
        // Border
        g2.setColor(RenderColors.BORDER);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new RoundRectangle2D.Float(x, y, width, height, 10, 10));
        
        // Title
        if (title != null) {
            g2.setColor(RenderColors.TEXT_PRIMARY);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString(title, x + 15, y + 22);
            
            // Title underline
            g2.setColor(RenderColors.BORDER);
            g2.drawLine(x + 10, y + 30, x + width - 10, y + 30);
        }
    }
    
    private void drawProgressBar(Graphics2D g2, int x, int y, int width, int height, 
                                  float percentage, Color fillColor, Color bgColor) {
        // Background
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(x, y, width, height, 6, 6));
        
        // Fill
        int fillWidth = (int)(width * Math.max(0, Math.min(1, percentage)));
        if (fillWidth > 0) {
            g2.setColor(fillColor);
            g2.fill(new RoundRectangle2D.Float(x, y, fillWidth, height, 6, 6));
        }
    }
    
    private Color darker(Color c, float factor) {
        return new Color(
            (int)(c.getRed() * factor),
            (int)(c.getGreen() * factor),
            (int)(c.getBlue() * factor)
        );
    }
    
    private Color blend(Color c1, Color c2, float ratio) {
        float inv = 1 - ratio;
        return new Color(
            (int)(c1.getRed() * inv + c2.getRed() * ratio),
            (int)(c1.getGreen() * inv + c2.getGreen() * ratio),
            (int)(c1.getBlue() * inv + c2.getBlue() * ratio)
        );
    }
}
