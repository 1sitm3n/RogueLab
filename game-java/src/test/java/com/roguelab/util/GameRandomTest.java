package com.roguelab.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameRandom (Deterministic RNG)")
class GameRandomTest {
    
    @Test
    @DisplayName("should produce same sequence for same seed")
    void sameSequenceForSameSeed() {
        GameRandom rng1 = new GameRandom(12345);
        GameRandom rng2 = new GameRandom(12345);
        
        for (int i = 0; i < 100; i++) {
            assertThat(rng1.nextInt(1000))
                .isEqualTo(rng2.nextInt(1000));
        }
    }
    
    @Test
    @DisplayName("should produce different sequence for different seed")
    void differentSequenceForDifferentSeed() {
        GameRandom rng1 = new GameRandom(12345);
        GameRandom rng2 = new GameRandom(54321);
        
        // Generate several values and check they're not all the same
        boolean anyDifferent = false;
        for (int i = 0; i < 100; i++) {
            if (rng1.nextInt(1000) != rng2.nextInt(1000)) {
                anyDifferent = true;
                break;
            }
        }
        assertThat(anyDifferent).isTrue();
    }
    
    @Test
    @DisplayName("should return seed")
    void returnsSeed() {
        GameRandom rng = new GameRandom(42);
        assertThat(rng.getSeed()).isEqualTo(42);
    }
    
    @Test
    @DisplayName("should generate int in range")
    void generatesIntInRange() {
        GameRandom rng = new GameRandom(12345);
        
        for (int i = 0; i < 1000; i++) {
            int value = rng.nextIntInRange(10, 20);
            assertThat(value).isBetween(10, 20);
        }
    }
    
    @Test
    @DisplayName("should reject invalid range")
    void rejectsInvalidRange() {
        GameRandom rng = new GameRandom(12345);
        
        assertThatThrownBy(() -> rng.nextIntInRange(20, 10))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("chance(0) should always return false")
    void chanceZeroAlwaysFalse() {
        GameRandom rng = new GameRandom(12345);
        
        for (int i = 0; i < 100; i++) {
            assertThat(rng.chance(0.0)).isFalse();
        }
    }
    
    @Test
    @DisplayName("chance(1) should always return true")
    void chanceOneAlwaysTrue() {
        GameRandom rng = new GameRandom(12345);
        
        for (int i = 0; i < 100; i++) {
            assertThat(rng.chance(1.0)).isTrue();
        }
    }
    
    @Test
    @DisplayName("chance(0.5) should return roughly equal true/false")
    void chanceFiftyPercent() {
        GameRandom rng = new GameRandom(12345);
        int trueCount = 0;
        int trials = 10000;
        
        for (int i = 0; i < trials; i++) {
            if (rng.chance(0.5)) {
                trueCount++;
            }
        }
        
        // Should be roughly 50% (within 5% tolerance)
        double ratio = (double) trueCount / trials;
        assertThat(ratio).isBetween(0.45, 0.55);
    }
    
    @Test
    @DisplayName("should pick from list")
    void picksFromList() {
        GameRandom rng = new GameRandom(12345);
        List<String> items = List.of("apple", "banana", "cherry");
        
        String picked = rng.pick(items);
        
        assertThat(items).contains(picked);
    }
    
    @Test
    @DisplayName("should reject picking from empty list")
    void rejectsEmptyList() {
        GameRandom rng = new GameRandom(12345);
        
        assertThatThrownBy(() -> rng.pick(List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("should shuffle list deterministically")
    void shufflesDeterministically() {
        List<Integer> list1 = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> list2 = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        
        GameRandom rng1 = new GameRandom(12345);
        GameRandom rng2 = new GameRandom(12345);
        
        rng1.shuffle(list1);
        rng2.shuffle(list2);
        
        assertThat(list1).isEqualTo(list2);
        // Should be actually shuffled (not in original order)
        assertThat(list1).isNotEqualTo(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}
