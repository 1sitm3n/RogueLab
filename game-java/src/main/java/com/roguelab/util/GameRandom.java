package com.roguelab.util;

import java.util.List;
import java.util.Random;

/**
 * Deterministic random number generator for reproducible game runs.
 * 
 * CRITICAL: All randomness in the game MUST go through this class.
 * Using java.util.Random directly or Math.random() will break reproducibility.
 * 
 * Given the same seed, this will produce the same sequence of values,
 * allowing runs to be replayed exactly for debugging and testing.
 */
public final class GameRandom {
    
    private final long seed;
    private final Random random;
    
    public GameRandom(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    /**
     * Create a GameRandom with the current system time as seed.
     * Use this only for actual gameplay, never for tests.
     */
    public static GameRandom fromCurrentTime() {
        return new GameRandom(System.currentTimeMillis());
    }
    
    public long getSeed() {
        return seed;
    }
    
    /**
     * Returns a random integer in [0, bound).
     */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
    
    /**
     * Returns a random integer in [min, max] (inclusive).
     */
    public int nextIntInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return min + random.nextInt(max - min + 1);
    }
    
    /**
     * Returns true with the given probability (0.0 to 1.0).
     */
    public boolean chance(double probability) {
        if (probability <= 0.0) return false;
        if (probability >= 1.0) return true;
        return random.nextDouble() < probability;
    }
    
    /**
     * Returns a random double in [0.0, 1.0).
     */
    public double nextDouble() {
        return random.nextDouble();
    }
    
    /**
     * Select a random element from a list.
     * @throws IllegalArgumentException if list is empty
     */
    public <T> T pick(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick from empty list");
        }
        return items.get(nextInt(items.size()));
    }
    
    /**
     * Shuffle a list in place using Fisher-Yates algorithm.
     */
    public <T> void shuffle(List<T> items) {
        for (int i = items.size() - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            T temp = items.get(i);
            items.set(i, items.get(j));
            items.set(j, temp);
        }
    }
}
