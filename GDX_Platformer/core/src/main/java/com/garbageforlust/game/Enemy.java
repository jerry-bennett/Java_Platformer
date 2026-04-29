package com.garbageforlust.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Rectangle bounds;
    public Vector2 velocity;
    public boolean movingRight = true;
    public float hurtTimer = 0;
    private Vector2 spawnPoint;

    public Enemy(float x, float y) {
        bounds = new Rectangle(x, y, 32, 32); // Adjust size as needed
        velocity = new Vector2(0, 0);
        spawnPoint = new Vector2(x, y);
    }

    public void tickHurtTimer(float dt) {
        if (hurtTimer > 0) hurtTimer -= dt;
    }

    public void respawn() {
        bounds.setPosition(spawnPoint.x, spawnPoint.y);
        velocity.set(0, 0);
    }
}