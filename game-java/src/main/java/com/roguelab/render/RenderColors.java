package com.roguelab.render;

import java.awt.Color;

/**
 * Color scheme for the game renderer.
 */
public final class RenderColors {
    
    private RenderColors() {}
    
    // Background
    public static final Color BACKGROUND = new Color(20, 20, 30);
    public static final Color PANEL_BG = new Color(30, 30, 45);
    public static final Color BORDER = new Color(60, 60, 80);
    
    // Room types
    public static final Color ROOM_COMBAT = new Color(120, 40, 40);
    public static final Color ROOM_SHOP = new Color(40, 100, 40);
    public static final Color ROOM_REST = new Color(40, 80, 120);
    public static final Color ROOM_TREASURE = new Color(180, 150, 40);
    public static final Color ROOM_BOSS = new Color(150, 30, 30);
    public static final Color ROOM_EVENT = new Color(100, 60, 120);
    public static final Color ROOM_EMPTY = new Color(50, 50, 60);
    
    // Room states
    public static final Color ROOM_CURRENT = new Color(255, 255, 100);
    public static final Color ROOM_CLEARED = new Color(80, 180, 80);
    public static final Color ROOM_UNVISITED = new Color(100, 100, 120);
    
    // Player
    public static final Color PLAYER = new Color(80, 200, 255);
    public static final Color PLAYER_HEALTH = new Color(80, 200, 80);
    public static final Color PLAYER_HEALTH_LOW = new Color(200, 80, 80);
    public static final Color PLAYER_HEALTH_BG = new Color(60, 30, 30);
    
    // Enemies
    public static final Color ENEMY = new Color(220, 80, 80);
    public static final Color ENEMY_HEALTH = new Color(200, 60, 60);
    public static final Color ENEMY_HEALTH_BG = new Color(60, 30, 30);
    
    // Items
    public static final Color ITEM_COMMON = new Color(180, 180, 180);
    public static final Color ITEM_UNCOMMON = new Color(80, 200, 80);
    public static final Color ITEM_RARE = new Color(80, 140, 255);
    public static final Color ITEM_EPIC = new Color(180, 80, 220);
    public static final Color ITEM_LEGENDARY = new Color(255, 180, 40);
    
    // Text
    public static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    public static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    public static final Color TEXT_MUTED = new Color(120, 120, 140);
    public static final Color TEXT_GOLD = new Color(255, 215, 0);
    public static final Color TEXT_DAMAGE = new Color(255, 100, 100);
    public static final Color TEXT_HEAL = new Color(100, 255, 100);
    public static final Color TEXT_CRIT = new Color(255, 200, 50);
    
    // UI elements
    public static final Color BUTTON = new Color(60, 60, 80);
    public static final Color BUTTON_HOVER = new Color(80, 80, 100);
    public static final Color BUTTON_ACTIVE = new Color(100, 100, 140);
}
