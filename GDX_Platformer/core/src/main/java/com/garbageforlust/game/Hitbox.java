package com.garbageforlust.game;

import com.badlogic.gdx.math.Rectangle;


public class Hitbox {
    public Rectangle bounds;


    public Hitbox(float x, float y) {
        bounds = new Rectangle(x, y, 32, 32); // Adjust size as needed
    }

}