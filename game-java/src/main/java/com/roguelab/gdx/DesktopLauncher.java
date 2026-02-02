package com.roguelab.gdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Desktop launcher for RogueLab.
 * Configures the LWJGL3 window and starts the game.
 */
public class DesktopLauncher {
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // Window settings
        config.setTitle("RogueLab");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        
        // Window icon (if available)
        // config.setWindowIcon("icon.png");
        
        // Start the game
        new Lwjgl3Application(new RogueLabGame(), config);
    }
}
