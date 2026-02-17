package com.garbageforlust.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue; // ADDED THIS IMPORT
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private Rectangle player;
    private float xVelocity = 0, yVelocity = 0;
    private final float GRAVITY = 1.0f; 
    private final float JUMP_SPEED = -15f; 
    private final float MOVE_SPEED = 300f; 

    private int coyoteCounter = 0;
    private final int COYOTE_TIME_MAX = 10; 
    private boolean isWallGrabbing = false;
    private boolean onGround = false;
    private Array<Rectangle> platforms;
    private Array<Enemy> enemies;

    // Dashing variables:
    private float dashCooldown = 0;
    private float dashTimer = 0;
    private final float DASH_DURATION = 0.15f;
    private final float DASH_SPEED = 800f;
    private final float DASH_COOLDOWN_MAX = 0.5f;
    private boolean isDashing = false;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 800, 450); 

        player = new Rectangle(100, 100, 50, 50);
        
        // 1. Initialize the lists FIRST
        platforms = new Array<>();
        enemies = new Array<>(); // <--- THIS MUST BE HERE
        
        // 2. Load the level SECOND
        loadLDtkLevel("maps/level1(new).ldtk"); 
    }

    private void loadLDtkLevel(String path) {
        platforms.clear();
        com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal(path));
        
        // Get the first level in the LDtk file
        JsonValue level = root.get("levels").get(0); 
        JsonValue layerInstances = level.get("layerInstances");

        for (JsonValue layer : layerInstances) {
            String layerName = layer.getString("__identifier");

            // 1. Load Collisions (IntGrid)
            if (layerName.equals("IntGrid")) { // Double check this matches your LDtk layer name!
                int gridSize = layer.getInt("__gridSize");
                int cWid = layer.getInt("__cWid");
                int[] gridData = layer.get("intGridCsv").asIntArray();

                for (int i = 0; i < gridData.length; i++) {
                    if (gridData[i] > 0) { 
                        int x = (i % cWid) * gridSize;
                        int y = (i / cWid) * gridSize;
                        platforms.add(new Rectangle(x, y, gridSize, gridSize));
                    }
                }
            }
            
            // 2. Load Entities
            if (layerName.equals("Entities")) {
                for (JsonValue entity : layer.get("entityInstances")) {
                    String name = entity.getString("__identifier");
                    float[] coords = entity.get("px").asFloatArray();
                    
                    if (name.equals("Player_Spawn")) {
                        player.setPosition(coords[0], coords[1]);
                    }
                    if (name.equals("Enemy")) {
                        enemies.add(new Enemy(coords[0], coords[1]));
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // 1. DASH INPUT
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && dashCooldown <= 0) {
            isDashing = true;
            dashTimer = DASH_DURATION;
            dashCooldown = DASH_COOLDOWN_MAX;
            // Set dash velocity immediately
            if (xVelocity == 0) xVelocity = DASH_SPEED * dt; 
            else xVelocity = (xVelocity > 0 ? DASH_SPEED : -DASH_SPEED) * dt;
        }

        // 2. DASH STATE vs NORMAL STATE
        if (isDashing) {
            dashTimer -= dt;
            yVelocity = 0; // Freeze Y-axis during dash
            if (dashTimer <= 0) isDashing = false;
        } else {
            // --- NORMAL MOVEMENT (Only happens when NOT dashing) ---
            dashCooldown -= dt;

            // Horizontal Input
            if (Gdx.input.isKeyPressed(Input.Keys.A)) xVelocity = -MOVE_SPEED * dt;
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) xVelocity = MOVE_SPEED * dt;
            else xVelocity = 0;

            // Coyote Time Logic
            if (onGround) coyoteCounter = COYOTE_TIME_MAX;
            else if (coyoteCounter > 0) coyoteCounter--;

            // Jump Input
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && (onGround || coyoteCounter > 0)) {
                yVelocity = JUMP_SPEED;
                onGround = false;
                coyoteCounter = 0;
            }

            // Wall Grab & Gravity
            isWallGrabbing = false;
            if (!onGround && Math.abs(xVelocity) > 0) {
                for (Rectangle p : platforms) {
                    if (player.overlaps(p)) isWallGrabbing = true;
                }
            }

            if (isWallGrabbing) yVelocity = 50f * dt; 
            else yVelocity += GRAVITY;
        }

        // 3. APPLY PHYSICS (Always run this)
        player.x += xVelocity;
        checkCollisions(true);
        player.y += yVelocity;
        checkCollisions(false);

        // 4. CAMERA & DRAWING
        camera.position.set(player.x + player.width/2, player.y + player.height/2, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 1. Draw Platforms (Grey)
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle p : platforms) shapeRenderer.rect(p.x, p.y, p.width, p.height);

        // 2. Draw Player (Red)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        // 3. Draw Enemies (Yellow)
        shapeRenderer.setColor(Color.YELLOW);
        for (Enemy e : enemies) {
            shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
        }
        
        // Now it's safe to end!
        shapeRenderer.end();

        // 4. Update Logic (Can stay outside)
        updateEnemies(dt);
        
    }

    private void updateEnemies(float dt) {
        for (Enemy e : enemies) {
            e.tickHurtTimer(dt);

            // 1. Gravity (Positive pulls DOWN in Ortho True)
            e.velocity.y += GRAVITY;
            e.bounds.y += e.velocity.y;

            // 2. Vertical Collision
            boolean onPlatform = false; // We need this for the AI logic below
            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    if (e.velocity.y > 0) { // Falling DOWN
                        e.bounds.y = p.y - e.bounds.height;
                        e.velocity.y = 0;
                        onPlatform = true; // Found the floor!
                    } else if (e.velocity.y < 0) { // Hitting ceiling (UP)
                        e.bounds.y = p.y + p.height;
                        e.velocity.y = 0;
                    }
                }
            }

            // 3. Horizontal Movement
            if (e.hurtTimer <= 0) {
                // Use a consistent speed
                float speed = e.movingRight ? 100f * dt : -100f * dt;
                e.bounds.x += speed;
            }

            // 4. Wall Bouncing (Only flip if we hit a wall side)
            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    e.movingRight = !e.movingRight;
                    e.bounds.x += (e.movingRight ? 5 : -5); 
                    break;
                }
            }

            // 5. Jump AI (The Logic Fix)
            // Sensor: slightly in front of feet, 5 pixels below the enemy
            float sensorX = e.movingRight ? e.bounds.x + e.bounds.width : e.bounds.x - 5;
            Rectangle edgeSensor = new Rectangle(sensorX, e.bounds.y + e.bounds.height + 5, 5, 5);
            
            boolean groundAhead = false;
            for (Rectangle p : platforms) {
                if (edgeSensor.overlaps(p)) { groundAhead = true; break; }
            }

            // ONLY check for jumps if we just reached an edge
            if (!groundAhead && onPlatform) {
                // Look for a landing spot (100px ahead, 200px down)
                Rectangle jumpReach = new Rectangle(
                    e.movingRight ? e.bounds.x + 32 : e.bounds.x - 100, 
                    e.bounds.y + e.bounds.height, 100, 200
                );
                
                boolean canLand = false;
                for (Rectangle p : platforms) {
                    if (jumpReach.overlaps(p)) { canLand = true; break; }
                }

                if (canLand) {
                    e.velocity.y = -12f; // JUMP UP
                } else {
                    e.movingRight = !e.movingRight; // Turn around
                }
            }
        }
    }

    private void checkCollisions(boolean horizontal) {
        for (Rectangle p : platforms) {
            if (player.overlaps(p)) {
                if (horizontal) {
                    if (xVelocity > 0) player.x = p.x - player.width;
                    else if (xVelocity < 0) player.x = p.x + p.width;
                } else {
                    if (yVelocity > 0) {
                        player.y = p.y - player.height;
                        yVelocity = 0;
                        onGround = true;
                    } else if (yVelocity < 0) {
                        player.y = p.y + p.height;
                        yVelocity = 0;
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}