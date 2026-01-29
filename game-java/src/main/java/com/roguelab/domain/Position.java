package com.roguelab.domain;

/**
 * Immutable 2D position in the game world.
 * Uses integer coordinates (tile-based movement).
 */
public record Position(int x, int y) {
    
    public static final Position ORIGIN = new Position(0, 0);
    
    public Position offset(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
    
    public Position north() {
        return offset(0, -1);
    }
    
    public Position south() {
        return offset(0, 1);
    }
    
    public Position east() {
        return offset(1, 0);
    }
    
    public Position west() {
        return offset(-1, 0);
    }
    
    /**
     * Manhattan distance to another position.
     */
    public int distanceTo(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
    
    /**
     * Check if this position is adjacent to another (including diagonals).
     */
    public boolean isAdjacentTo(Position other) {
        int dx = Math.abs(x - other.x);
        int dy = Math.abs(y - other.y);
        return dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0);
    }
}
