package com.garbageforlust.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue; // ADDED THIS IMPORT
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.garbageforlust.game.Interactable;

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

    // Interactable Variables
    private SpriteBatch batch;
    private BitmapFont font;
    private Array<Interactable> interactables;
    public float bobCounter = 0;

    // Squish variables
    private float playerScaleX = 1f;
    private float playerScaleY = 1f;
    private final float LERP_SPEED = 10f;
    private boolean wasFalling = false;

    // Death variables
    private Array<Rectangle> deathZones;
    private Vector2 respawnPoint; // To remember where the level started
    private boolean isDead = false;
    private float deathTimer = 0;
    private final float DEATH_DELAY = 1.0f; // 1 second wait

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 800, 450); 

        batch = new SpriteBatch();
        font = new BitmapFont(true);
        interactables = new Array<>();

        deathZones = new Array<>(); 
        respawnPoint = new Vector2();

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

            // Load Collisions (IntGrid)
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
            
            // Load Entities
            if (layerName.equals("Entities")) {
                for (JsonValue entity : layer.get("entityInstances")) {
                    String name = entity.getString("__identifier");
                    float[] coords = entity.get("px").asFloatArray();
                    
                    if (name.equals("Player_Spawn")) {
                        // 1. Get the actual pixel coordinates from LDtk
                        float spawnX = coords[0];
                        float spawnY = coords[1];

                        // 2. Move the player rectangle there immediately
                        player.setPosition(spawnX, spawnY);

                        // 3. Update the respawnPoint vector so it's ready for the death logic
                        respawnPoint.set(spawnX, spawnY); 
                        
                        System.out.println("Spawn Point Set to: " + spawnX + ", " + spawnY);
                    }
                    if (name.equals("Enemy")) {
                        enemies.add(new Enemy(coords[0], coords[1]));
                    }
                    if (name.equals("Death")) {
                        float w = entity.getInt("width");
                        float h = entity.getInt("height");
                        deathZones.add(new Rectangle(coords[0], coords[1], w, h));
                    }

                    if (name.equals("NPC")) {
                        String msg = "Default Message"; 
                        
                        // Get the array of fields
                        JsonValue fieldInstances = entity.get("fieldInstances");

                        if (fieldInstances != null && fieldInstances.size > 0) {
                            for (JsonValue field : fieldInstances) {
                                String id = field.getString("__identifier");
                                
                                // DEBUG: This will print every field name found to the console
                                System.out.println("Found field: " + id);

                                if (id.equalsIgnoreCase("message")) {
                                    // Let's see the raw JSON of the value node
                                    JsonValue val = field.get("__value");
                                    
                                    if (val != null && !val.isNull()) {
                                        msg = val.asString();
                                        System.out.println("Success! Message is: " + msg);
                                    } else {
                                        // If we are here, LDtk has 'null' written in the JSON file
                                        msg = "Empty Value in LDtk";
                                        System.out.println("Field found, but 'value' is explicitly null in the JSON file.");
                                    }
                                }
                            }
                        } else {
                            System.out.println("Warning: NPC entity found, but it has NO field instances!");
                        }

                        float x = entity.get("px").asFloatArray()[0];
                        float y = entity.get("px").asFloatArray()[1];
                        interactables.add(new Interactable(x, y, 32, 32, msg)); 
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        if (isDead) {
            deathTimer -= dt;
            if (deathTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                respawnPlayer();
            }
            return; // Stop the rest of the movement/physics logic
        }

        // Check Death Zones
        for (Rectangle zone : deathZones) {
            if (player.overlaps(zone)) {
                triggerDeath();
            }
        }

        // Smoothly return to 1.0 scale (squish)
        playerScaleX += (1f - playerScaleX) * LERP_SPEED * dt;
        playerScaleY += (1f - playerScaleY) * LERP_SPEED * dt;

        // Track falling state for the landing squish
        if (!onGround) {
            wasFalling = true;
        }

        // DASH INPUT
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && dashCooldown <= 0) {
            isDashing = true;
            dashTimer = DASH_DURATION;
            dashCooldown = DASH_COOLDOWN_MAX;
            // Set dash velocity immediately
            if (xVelocity == 0) xVelocity = DASH_SPEED * dt; 
            else xVelocity = (xVelocity > 0 ? DASH_SPEED : -DASH_SPEED) * dt;
        }

        // DASH STATE vs NORMAL STATE
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

                // Stretch UP on jump
                playerScaleX = 0.8f; 
                playerScaleY = 1.3f;
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

        // APPLY PHYSICS
        player.x += xVelocity;
        checkCollisions(true);
        player.y += yVelocity;
        checkCollisions(false);

        // CAMERA & DRAWING
        camera.position.set(player.x + player.width/2, player.y + player.height/2, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw Platforms, Player, and Enemies
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle p : platforms) shapeRenderer.rect(p.x, p.y, p.width, p.height);
        
        // Player
        shapeRenderer.setColor(Color.RED);

        // Calculate visual dimensions
        float visWidth = player.width * playerScaleX;
        float visHeight = player.height * playerScaleY;

        // Offset the X and Y so the squish happens from the bottom-center
        float visX = player.x + (player.width - visWidth) / 2f;
        float visY = (player.y + player.height) - visHeight;

        // Draw the player
        shapeRenderer.rect(visX, visY, visWidth, visHeight);

        // Enemies
        shapeRenderer.setColor(Color.YELLOW);
        for (Enemy e : enemies) shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);

        // CLOSE IT HERE! We are done with basic world shapes.
        shapeRenderer.end();

        //Draw Typewriter
        bobCounter += dt;
        for (Interactable i : interactables) {
            i.update(dt, player);
            if (i.isPlayerNear && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                i.isDialogOpen = !i.isDialogOpen;
                if (i.isDialogOpen) i.visibleChars = 0; 
            }
        }

        // Draw the [E] Prompt
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Interactable i : interactables) {
            if (i.isPlayerNear && !i.isDialogOpen) {
                font.draw(batch, "[E]", i.bounds.x + 5, i.bounds.y - 10);
            }
        }
        batch.end();

        // 3. DRAW DIALOG BOXES
        if (anyDialogOpen()) { 
            drawDialogBoxes();
        }

        // 4. Update Logic (Can stay outside)
        updateEnemies(dt);
        
    }

    private void updateEnemies(float dt) {
        for (Enemy e : enemies) {
            e.tickHurtTimer(dt);

            // 1. Gravity (Always pull down)
            e.velocity.y += GRAVITY;
            e.bounds.y += e.velocity.y;

            // 2. Vertical Collision
            boolean onPlatform = false; 
            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    if (e.velocity.y > 0) { // Falling
                        e.bounds.y = p.y - e.bounds.height;
                        e.velocity.y = 0;
                        onPlatform = true; 
                    } else if (e.velocity.y < 0) { // Ceiling
                        e.bounds.y = p.y + p.height;
                        e.velocity.y = 0;
                    }
                }
            }

            // 3. Horizontal Movement
            float moveAmount = (e.movingRight ? 120f : -120f) * dt;
            e.bounds.x += moveAmount;

            // 4. Smart Wall Bouncing & Jumping UP
            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    // Look for ledges slightly above the feet but below head-height
                    // We use a broader range (up to 150px) to ensure it catches the platform
                    boolean isLedgeAbove = p.y < (e.bounds.y + e.bounds.height) && p.y > (e.bounds.y - 150);

                    if (isLedgeAbove && onPlatform) {
                        e.velocity.y = -18f; // Stronger jump to clear the tile height
                        // Push them toward the ledge so they don't hit the wall again
                        e.bounds.x += (e.movingRight ? 5 : -5); 
                        break; 
                    } 
                    
                    // ONLY turn around if we are on the ground and not jumping
                    // This stops the enemy from flipping direction mid-air
                    else if (onPlatform && (e.bounds.y + e.bounds.height > p.y + 2)) {
                        e.movingRight = !e.movingRight;
                        e.bounds.x += (e.movingRight ? 12 : -12); // Stronger nudge away from the wall
                        break;
                    }
                }
            }

            // 5. Jump/Gap AI (Refined for Cross-Level Movement)
            if (onPlatform) {
                // Look specifically for a gap at the feet
                float sensorX = e.movingRight ? e.bounds.x + e.bounds.width + 5 : e.bounds.x - 10;
                Rectangle gapSensor = new Rectangle(sensorX, e.bounds.y + e.bounds.height + 5, 5, 5);
                // float leapX = e.movingRight ? e.bounds.x + 100 : e.bounds.x - 200; 
                // Rectangle leapSensor = new Rectangle(leapX, e.bounds.y - 100, 150, 300);

                boolean groundAhead = false;
                for (Rectangle p : platforms) {
                    if (gapSensor.overlaps(p)) { groundAhead = true; break; }
                }

                if (!groundAhead) {
                    // We found a gap! Can we leap it?
                    // Search further ahead and slightly below for a landing spot
                    float leapX = e.movingRight ? e.bounds.x + 80 : e.bounds.x - 180;
                    Rectangle leapSensor = new Rectangle(leapX, e.bounds.y, 100, 250);

                    boolean landingSpotFound = false;
                    for (Rectangle p : platforms) {
                        if (leapSensor.overlaps(p)) { landingSpotFound = true; break; }
                    }

                    if (landingSpotFound) {
                        e.velocity.y = -15f; // Jump across!
                    } else {
                        // NO LANDING SPOT: Turn around and head to the other side of the level
                        e.movingRight = !e.movingRight;
                        e.bounds.x += (e.movingRight ? 15 : -15); 
                    }
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
                    if (yVelocity > 0) { // Falling Down
                        // TRIGGER ONLY ONCE ON LANDING
                        if (wasFalling && yVelocity > 5f) { 
                            playerScaleX = 1.4f; 
                            playerScaleY = 0.7f;
                            wasFalling = false; // Prevent re-squishing every frame
                        }
                        
                        player.y = p.y - player.height;
                        onGround = true;
                        yVelocity = 0;
                    } else if (yVelocity < 0) { // Hitting ceiling
                        player.y = p.y + p.height;
                        yVelocity = 0;
                    }
                }
            }
        }
    }

    private void drawDialogBoxes() {
        // Enable transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Interactable i : interactables) {
            if (i.isDialogOpen) {
                float boxW = 250;
                float boxH = 80;
                float bobOffset = (float) Math.sin(bobCounter * 5) * 3;
                float boxX = i.bounds.x - (boxW / 2) + (i.bounds.width / 2);
                float boxY = i.bounds.y - boxH - 20 + bobOffset;

                shapeRenderer.setColor(0, 0, 0, 0.7f); 
                shapeRenderer.rect(boxX, boxY, boxW, boxH);
                shapeRenderer.triangle(i.bounds.x + 10, boxY + boxH, i.bounds.x + 22, boxY + boxH, i.bounds.x + 16, boxY + boxH + 10);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        // Now draw the text on top
        batch.begin();
        for (Interactable i : interactables) {
            if (i.isDialogOpen) {
                float boxW = 250;
                float boxH = 80;
                float bobOffset = (float) Math.sin(bobCounter * 5) * 3;
                float boxX = i.bounds.x - (boxW / 2) + (i.bounds.width / 2);
                float boxY = i.bounds.y - boxH - 20 + bobOffset;

                String visibleText = i.message.substring(0, Math.min(i.visibleChars, i.message.length()));
                font.draw(batch, visibleText, boxX + 10, boxY + 10, boxW - 20, 1, true);
            }
        }
        batch.end();
    }

    private boolean anyDialogOpen() {
        for (Interactable i : interactables) {
            if (i.isDialogOpen) return true;
        }
        return false;
    }

    private void triggerDeath() {
        if (!isDead) {
            isDead = true;
            deathTimer = DEATH_DELAY;
            System.out.println("WASTED");
        }
    }

    private void respawnPlayer() {
        isDead = false;
        player.setPosition(respawnPoint.x, respawnPoint.y);
        xVelocity = 0;
        yVelocity = 0;
        System.out.println("Respawned at: " + respawnPoint);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}