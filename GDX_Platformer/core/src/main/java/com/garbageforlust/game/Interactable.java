package com.garbageforlust.game;

import com.badlogic.gdx.math.Rectangle;

public class Interactable {
    public Rectangle bounds;
    public String message;
    public boolean isPlayerNear = false;
    public boolean isDialogOpen = false;
    
    public int visibleChars = 0;
    public float typeTimer = 0;
    public final float TYPE_SPEED = 0.05f; // Seconds per character
    public float promptDelayTimer = 0;
    public final float PROMPT_DELAY_MAX = 1.0f;

    public Interactable(float x, float y, float width, float height, String message) {
        this.bounds = new Rectangle(x, y, width, height);
        this.message = message;
    }

    public void update(float dt, Rectangle playerBounds) {
        // Check if player is close (within 50 pixels)
        isPlayerNear = bounds.overlaps(new Rectangle(playerBounds.x - 50, playerBounds.y - 50, 
                                       playerBounds.width + 100, playerBounds.height + 100));

        if (!isPlayerNear) {
            isDialogOpen = false;
            visibleChars = 0;
        }

        if (isDialogOpen) {
            // Typewriter logic
            if (visibleChars < message.length()) {
                typeTimer += dt;
                if (typeTimer >= TYPE_SPEED) {
                    visibleChars++;
                    typeTimer = 0;
                }
            } else {
                promptDelayTimer += dt;
            }
        } else {
            promptDelayTimer = 0;
        }
    }
}