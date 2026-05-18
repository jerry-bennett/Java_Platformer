package com.garbageforlust.game;

import com.badlogic.gdx.math.MathUtils;

public class Dust {
    float x, y, vx, vy;
    float size;
    float alpha = 1.0f;

    public Dust(float x, float y, float vx, float vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        // MathUtils is libGDX's optimized version of Math
        this.size = MathUtils.random(2f, 6f); 
    }

    public void update(float dt) {
        // Multiply by 60 so the speed matches your old frame-rate based game
        x += vx * 60 * dt;
        y += vy * 60 * dt;
        alpha -= 2.0f * dt; // Fades out in about half a second
    }

    public boolean isDead() {
        return alpha <= 0;
    }
}