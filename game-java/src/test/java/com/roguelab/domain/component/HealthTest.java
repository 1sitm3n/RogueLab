package com.roguelab.domain.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Health Component")
class HealthTest {
    
    @Test
    @DisplayName("should initialize with full health")
    void initializesWithFullHealth() {
        Health health = new Health(100);
        
        assertThat(health.getCurrent()).isEqualTo(100);
        assertThat(health.getMaximum()).isEqualTo(100);
        assertThat(health.isFullHealth()).isTrue();
        assertThat(health.isAlive()).isTrue();
        assertThat(health.isDead()).isFalse();
    }
    
    @Test
    @DisplayName("should reject non-positive maximum health")
    void rejectsInvalidMaxHealth() {
        assertThatThrownBy(() -> new Health(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Health(-10))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("should take damage correctly")
    void takesDamageCorrectly() {
        Health health = new Health(100);
        
        int dealt = health.takeDamage(30);
        
        assertThat(dealt).isEqualTo(30);
        assertThat(health.getCurrent()).isEqualTo(70);
        assertThat(health.isAlive()).isTrue();
    }
    
    @Test
    @DisplayName("should not go below zero health")
    void healthCannotGoNegative() {
        Health health = new Health(50);
        
        int dealt = health.takeDamage(100);
        
        assertThat(dealt).isEqualTo(50); // Only 50 actual damage dealt
        assertThat(health.getCurrent()).isEqualTo(0);
        assertThat(health.isDead()).isTrue();
    }
    
    @Test
    @DisplayName("should heal correctly")
    void healsCorrectly() {
        Health health = new Health(100);
        health.takeDamage(40);
        
        int healed = health.heal(25);
        
        assertThat(healed).isEqualTo(25);
        assertThat(health.getCurrent()).isEqualTo(85);
    }
    
    @Test
    @DisplayName("should not overheal past maximum")
    void cannotOverheal() {
        Health health = new Health(100);
        health.takeDamage(20);
        
        int healed = health.heal(50);
        
        assertThat(healed).isEqualTo(20); // Only healed to max
        assertThat(health.getCurrent()).isEqualTo(100);
        assertThat(health.isFullHealth()).isTrue();
    }
    
    @Test
    @DisplayName("should calculate percentage correctly")
    void calculatesPercentageCorrectly() {
        Health health = new Health(100);
        health.takeDamage(25);
        
        assertThat(health.getPercent()).isCloseTo(0.75, within(0.001));
    }
    
    @Test
    @DisplayName("should increase maximum health")
    void increasesMaximumHealth() {
        Health health = new Health(100);
        health.takeDamage(20);
        
        health.increaseMaximum(50);
        
        assertThat(health.getMaximum()).isEqualTo(150);
        assertThat(health.getCurrent()).isEqualTo(130); // 80 + 50
    }
    
    @Test
    @DisplayName("should reject negative damage")
    void rejectsNegativeDamage() {
        Health health = new Health(100);
        
        assertThatThrownBy(() -> health.takeDamage(-10))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("should reject negative heal")
    void rejectsNegativeHeal() {
        Health health = new Health(100);
        
        assertThatThrownBy(() -> health.heal(-10))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
