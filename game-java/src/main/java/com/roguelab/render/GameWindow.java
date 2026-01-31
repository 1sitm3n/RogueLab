package com.roguelab.render;

import com.roguelab.combat.*;
import com.roguelab.domain.*;
import com.roguelab.dungeon.*;
import com.roguelab.game.*;
import com.roguelab.telemetry.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Main game window with keyboard input handling.
 */
public class GameWindow extends JFrame {
    
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 650;
    
    private final GameRenderer renderer;
    private GameSession session;
    private TelemetryWriter telemetry;
    private boolean gameStarted = false;
    
    public GameWindow() {
        super("RogueLab");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        renderer = new GameRenderer();
        add(renderer);
        
        setupKeyBindings();
        
        // Start with welcome screen
        renderer.addMessage("Welcome to RogueLab!");
        renderer.addMessage("Press SPACE to start a new run.");
    }
    
    private void setupKeyBindings() {
        InputMap inputMap = renderer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = renderer.getActionMap();
        
        // Space - Advance / Start / Fight
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "advance");
        actionMap.put("advance", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAdvance();
            }
        });
        
        // R - Rest
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rest");
        actionMap.put("rest", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRest();
            }
        });
        
        // Q - Quit
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "quit");
        actionMap.put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuit();
            }
        });
        
        // N - New run
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "newrun");
        actionMap.put("newrun", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewRun();
            }
        });
        
        // Escape - Cancel / Menu
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEscape();
            }
        });
    }
    
    private void handleAdvance() {
        if (!gameStarted) {
            startNewRun();
            return;
        }
        
        if (session == null || !session.isActive()) {
            renderer.addMessage("Press N to start a new run.");
            return;
        }
        
        GameState state = session.getState();
        
        switch (state) {
            case IN_COMBAT -> executeCombat();
            case IN_SHOP -> {
                session.leaveShop();
                renderer.addMessage("Left the shop.");
                advanceToNextRoom();
            }
            case AT_REST -> {
                renderer.addMessage("Press R to rest, or SPACE again to continue.");
                session.leaveRest();
                advanceToNextRoom();
            }
            case EXPLORING -> advanceToNextRoom();
            default -> renderer.addMessage("Cannot advance in state: " + state);
        }
        
        renderer.repaint();
    }
    
    private void advanceToNextRoom() {
        Floor floor = session.getCurrentFloor();
        
        // Collect items from current room
        Room room = session.getCurrentRoom();
        for (Item item : new ArrayList<>(room.getItems())) {
            session.pickUpItem(item);
            renderer.addMessage("Picked up: " + item.getName());
        }
        
        if (floor.hasNextRoom()) {
            session.advanceRoom();
            Room newRoom = session.getCurrentRoom();
            renderer.addMessage("Entered " + newRoom.getType() + " room.");
            
            if (session.getState() == GameState.IN_COMBAT) {
                renderer.addMessage("Enemies ahead! Press SPACE to fight.");
            }
        } else if (session.getDungeon().canDescend()) {
            session.descendFloor();
            renderer.addMessage("=== FLOOR " + session.getCurrentFloorNumber() + " ===");
            
            if (session.getState() == GameState.IN_COMBAT) {
                renderer.addMessage("Enemies ahead! Press SPACE to fight.");
            }
        } else {
            session.endRun(GameSessionListener.RunEndReason.VICTORY);
            renderer.addMessage("*** VICTORY! ***");
            renderer.addMessage("Press N for a new run.");
            closeTelemetry();
        }
    }
    
    private void executeCombat() {
        if (session.getState() != GameState.IN_COMBAT) return;
        
        CombatResult result = session.executeCombat();
        
        if (result.isVictory()) {
            renderer.addMessage("Victory! +" + result.goldEarned() + " gold, +" + result.experienceGained() + " XP");
        } else {
            renderer.addMessage("*** DEFEAT! ***");
            renderer.addMessage("You were slain. Press N for a new run.");
            closeTelemetry();
        }
    }
    
    private void handleRest() {
        if (session == null || !session.isActive()) return;
        
        if (session.getState() == GameState.AT_REST) {
            session.rest();
            int healed = (int)(session.getPlayer().getHealth().getMaximum() * 0.3);
            renderer.addMessage("Rested and healed " + healed + " HP.");
            session.leaveRest();
        } else {
            renderer.addMessage("You can only rest at rest sites.");
        }
        renderer.repaint();
    }
    
    private void handleQuit() {
        if (session != null && session.isActive()) {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Abandon current run?", 
                "Quit",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                session.endRun(GameSessionListener.RunEndReason.ABANDONED);
                closeTelemetry();
                renderer.addMessage("Run abandoned. Press N for a new run.");
            }
        } else {
            System.exit(0);
        }
    }
    
    private void handleEscape() {
        if (session != null && session.isActive() && session.getState() == GameState.IN_COMBAT) {
            // Can't escape combat
            renderer.addMessage("Cannot escape from combat!");
        }
    }
    
    private void startNewRun() {
        // Close previous telemetry
        closeTelemetry();
        
        long seed = System.currentTimeMillis();
        String runId = "run_" + seed;
        
        // Setup telemetry
        try {
            Path runsDir = Paths.get("runs");
            Files.createDirectories(runsDir);
            Path telemetryFile = runsDir.resolve(runId + ".jsonl");
            telemetry = new TelemetryWriter(telemetryFile, runId, false);
        } catch (IOException e) {
            renderer.addMessage("Warning: Could not create telemetry file.");
        }
        
        // Create session
        session = new GameSession(
            "Hero",
            PlayerClass.WARRIOR,
            seed,
            Difficulty.NORMAL,
            DungeonConfig.standard()
        );
        
        // Setup listeners
        GameSessionListener sessionListener = new RenderingSessionListener();
        if (telemetry != null) {
            GameSessionListener telemetryListener = new SimpleTelemetrySessionListener(telemetry);
            session.setListener(new CompositeListener(sessionListener, telemetryListener));
            session.setCombatListener(new SimpleTelemetryCombatListener(telemetry));
        } else {
            session.setListener(sessionListener);
        }
        
        // Start
        session.start();
        gameStarted = true;
        renderer.setSession(session);
        renderer.clearMessages();
        renderer.addMessage("=== RUN STARTED ===");
        renderer.addMessage("Floor 1 - " + session.getCurrentFloor().getRoomCount() + " rooms");
        
        if (session.getState() == GameState.IN_COMBAT) {
            renderer.addMessage("Enemies ahead! Press SPACE to fight.");
        } else {
            renderer.addMessage("Press SPACE to advance.");
        }
    }
    
    private void closeTelemetry() {
        if (telemetry != null) {
            try {
                telemetry.close();
            } catch (IOException e) {
                // Ignore
            }
            telemetry = null;
        }
    }
    
    /**
     * Session listener that updates the renderer.
     */
    private class RenderingSessionListener implements GameSessionListener {
        @Override
        public void onRunStarted(GameSession session) {
            renderer.repaint();
        }
        
        @Override
        public void onFloorEntered(GameSession session, Floor floor) {
            renderer.repaint();
        }
        
        @Override
        public void onRoomEntered(GameSession session, Room room) {
            renderer.repaint();
        }
        
        @Override
        public void onRoomCleared(GameSession session, Room room) {
            renderer.repaint();
        }
        
        @Override
        public void onCombatCompleted(GameSession session, CombatResult result) {
            renderer.repaint();
        }
        
        @Override
        public void onItemPicked(GameSession session, Item item) {
            renderer.repaint();
        }
        
        @Override
        public void onItemUsed(GameSession session, Item item) {
            renderer.repaint();
        }
        
        @Override
        public void onShopPurchase(GameSession session, Item item, int cost) {
            renderer.addMessage("Bought " + item.getName() + " for " + cost + " gold.");
            renderer.repaint();
        }
        
        @Override
        public void onPlayerRested(GameSession session, int healAmount) {
            renderer.repaint();
        }
        
        @Override
        public void onPlayerLevelUp(GameSession session, int newLevel) {
            renderer.addMessage("*** LEVEL UP! Now level " + newLevel + " ***");
            renderer.repaint();
        }
        
        @Override
        public void onRunEnded(GameSession session, RunEndReason reason) {
            renderer.repaint();
        }
    }
    
    /**
     * Composite listener that forwards to multiple listeners.
     */
    private static class CompositeListener implements GameSessionListener {
        private final GameSessionListener[] listeners;
        
        CompositeListener(GameSessionListener... listeners) {
            this.listeners = listeners;
        }
        
        @Override public void onRunStarted(GameSession s) { 
            for (var l : listeners) l.onRunStarted(s); 
        }
        @Override public void onFloorEntered(GameSession s, Floor f) { 
            for (var l : listeners) l.onFloorEntered(s, f); 
        }
        @Override public void onRoomEntered(GameSession s, Room r) { 
            for (var l : listeners) l.onRoomEntered(s, r); 
        }
        @Override public void onRoomCleared(GameSession s, Room r) { 
            for (var l : listeners) l.onRoomCleared(s, r); 
        }
        @Override public void onCombatCompleted(GameSession s, CombatResult r) { 
            for (var l : listeners) l.onCombatCompleted(s, r); 
        }
        @Override public void onItemPicked(GameSession s, Item i) { 
            for (var l : listeners) l.onItemPicked(s, i); 
        }
        @Override public void onItemUsed(GameSession s, Item i) { 
            for (var l : listeners) l.onItemUsed(s, i); 
        }
        @Override public void onShopPurchase(GameSession s, Item i, int c) { 
            for (var l : listeners) l.onShopPurchase(s, i, c); 
        }
        @Override public void onPlayerRested(GameSession s, int h) { 
            for (var l : listeners) l.onPlayerRested(s, h); 
        }
        @Override public void onPlayerLevelUp(GameSession s, int l) { 
            for (var x : listeners) x.onPlayerLevelUp(s, l); 
        }
        @Override public void onRunEnded(GameSession s, RunEndReason r) { 
            for (var l : listeners) l.onRunEnded(s, r); 
        }
    }
    
    /**
     * Launch the game window.
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default
            }
            
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
