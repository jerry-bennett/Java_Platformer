package com.garbageforlust.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    // Ported Variables from your Game.java
    private Rectangle player;
    private float xVelocity = 0, yVelocity = 0;
    private final float GRAVITY = 1.0f; // Adjusted for 60fps
    private final float JUMP_SPEED = -15f; 
    private final float MOVE_SPEED = 300f; // Pixels per second

    private int coyoteCounter = 0;
    private final int COYOTE_TIME_MAX = 10; 
    private boolean isWallGrabbing = false;
    private float wallStickTimer = 0;
    private final float MAX_STICK_TIME = 0.25f; // 0.25 seconds
    
    private boolean onGround = false;
    private Array<Rectangle> platforms; // LibGDX specific List type

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 800, 450); // Y-down to match your old logic

        player = new Rectangle(100, 100, 50, 50);
        
        // Let's build a few platforms manually for now
        platforms = new Array<>();
        platforms.add(new Rectangle(0, 400, 800, 50));   // Floor
        platforms.add(new Rectangle(300, 300, 200, 20)); // Floating platform
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // --- Coyote Time Logic ---
        if (onGround) {
            coyoteCounter = COYOTE_TIME_MAX;
        } else if (coyoteCounter > 0) {
            coyoteCounter--;
        }

        // --- Wall Grab Logic (X-axis) ---
        isWallGrabbing = false;
        player.x += xVelocity;
        for (Rectangle p : platforms) {
            if (player.overlaps(p)) {
                if (xVelocity > 0) {
                    player.x = p.x - player.width;
                    if (!onGround && yVelocity > 0) isWallGrabbing = true;
                } else if (xVelocity < 0) {
                    player.x = p.x + p.width;
                    if (!onGround && yVelocity > 0) isWallGrabbing = true;
                }
            }
        }

        // --- Gravity & Wall Slide (Y-axis) ---
        if (isWallGrabbing) {
            yVelocity = 50f * dt; // Slow slide down the wall
        } else {
            yVelocity += GRAVITY;
        }

        // 1. INPUT (Ported from your KeyListener)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) xVelocity = -MOVE_SPEED * dt;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) xVelocity = MOVE_SPEED * dt;
        else xVelocity = 0;

        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && (onGround || coyoteCounter > 0)) {
            yVelocity = JUMP_SPEED;
            onGround = false;
            coyoteCounter = 0; // Use it up!
        }

        // 2. PHYSICS & COLLISION (The "Move" logic)
        yVelocity += GRAVITY; // Apply Gravity
        
        // Handle X Movement
        player.x += xVelocity;
        for (Rectangle p : platforms) {
            if (player.overlaps(p)) {
                if (xVelocity > 0) player.x = p.x - player.width;
                else if (xVelocity < 0) player.x = p.x + p.width;
            }
        }

        // Handle Y Movement
        player.y += yVelocity;
        onGround = false;
        for (Rectangle p : platforms) {
            if (player.overlaps(p)) {
                if (yVelocity > 0) { // Falling down
                    player.y = p.y - player.height;
                    yVelocity = 0;
                    onGround = true;
                } else if (yVelocity < 0) { // Hitting ceiling
                    player.y = p.y + p.height;
                    yVelocity = 0;
                }
            }
        }

        // 3. CAMERA (Smoother than Swing!)
        // Lerp makes the camera "lazy" and follow the player smoothly
        float lerp = 0.1f;
        camera.position.x += (player.x - camera.position.x) * lerp;
        camera.position.y += (player.y - camera.position.y) * lerp;
        camera.update();

        // 4. DRAWING
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw Platforms
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle p : platforms) {
            shapeRenderer.rect(p.x, p.y, p.width, p.height);
        }

        // Draw Player
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void loadLevel(String internalPath) {
        com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
        com.badlogic.gdx.utils.JsonValue root = reader.parse(Gdx.files.internal(internalPath));
        
        // This looks for a layer named "Collisions" in your LDtk/Tiled file
        com.badlogic.gdx.utils.JsonValue layers = root.get("layers");
        
        platforms.clear(); // Clear old platforms
        
        for (com.badlogic.gdx.utils.JsonValue layer : layers) {
            if (layer.getString("type").equals("objectgroup")) {
                for (com.badlogic.gdx.utils.JsonValue obj : layer.get("objects")) {
                    platforms.add(new Rectangle(
                        obj.getFloat("x"), 
                        obj.getFloat("y"), 
                        obj.getFloat("width"), 
                        obj.getFloat("height")
                    ));
                }
            }
        }
    }
}