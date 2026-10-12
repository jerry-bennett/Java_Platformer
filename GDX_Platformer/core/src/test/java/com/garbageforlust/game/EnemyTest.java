package com.garbageforlust.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for the Enemy class.
 * Tests initialization, hurt timer behavior, and respawn functionality.
 */
public class EnemyTest {

    private Enemy enemy;

    @BeforeEach
    public void setUp() {
        enemy = new Enemy(0f, 0f);
    }

    @Test
    public void testConstructorInitializesBoundsAndVelocity() {
        assertEquals(32, enemy.bounds.width);
        assertEquals(32, enemy.bounds.height);
        assertEquals(0f, enemy.velocity.x);
        assertEquals(0f, enemy.velocity.y);
    }

    @Test
    public void testTickHurtTimerDecreases() {
        enemy.hurtTimer = 5.0f;
        float dt = 2.0f;
        enemy.tickHurtTimer(dt);

        assertEquals(3.0f, enemy.hurtTimer);
    }

    @Test
    public void testTickHurtTimerCoversUp() {
        enemy.hurtTimer = 1.0f;
        float dt = 2.0f;
        enemy.tickHurtTimer(dt);

        assertEquals(0.0f, enemy.hurtTimer);
    }

    @Test
    public void testTickHurtTimerAtZeroRemainsZero() {
        assertEquals(0.0f, enemy.hurtTimer);
        float dt = 1.0f;
        enemy.tickHurtTimer(dt);
        assertEquals(0.0f, enemy.hurtTimer);
    }

    @Test
    public void testRespawnResetsPositionAndVelocity() {
        enemy = new Enemy(50f, 75f);
        enemy.bounds.setPosition(100f, 200f);
        enemy.velocity.set(300f, -400f);

        assertEquals(100f, enemy.bounds.x);
        assertEquals(200f, enemy.bounds.y);
        assertEquals(300f, enemy.velocity.x);
        assertEquals(-400f, enemy.velocity.y);

        enemy.respawn();

        assertEquals(50f, enemy.bounds.x);
        assertEquals(75f, enemy.bounds.y);
        assertEquals(0f, enemy.velocity.x);
        assertEquals(0f, enemy.velocity.y);
    }

    @Test
    public void testBoundsIsPubliclyAccessible() {
        assertNotNull(enemy.bounds);
        assertTrue(enemy.bounds.width > 0);
        assertTrue(enemy.bounds.height > 0);
    }
}
