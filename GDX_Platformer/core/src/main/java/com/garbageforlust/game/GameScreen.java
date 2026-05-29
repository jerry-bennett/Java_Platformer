package com.garbageforlust.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameScreen implements Screen {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Array<Dust> dustParticles = new Array<>();

    private Rectangle player;
    private float xVelocity = 0, yVelocity = 0;
    private final float GRAVITY = 1.0f; 
    private final float JUMP_SPEED = -15f; 
    private final float MOVE_SPEED = 300f; 

    private int coyoteCounter = 0;
    private final int COYOTE_TIME_MAX = 10; 
    private boolean isWallGrabbing = false;
    private boolean leftGrab = false;
    private boolean rightGrab = false;
    private boolean onGround = false;
    private Array<Rectangle> platforms;
    private Array<Rectangle> walls;
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
    private Vector2 enemyRespawnPoint;
    private boolean isDead = false;
    private float deathTimer = 0;
    private final float DEATH_DELAY = 1.0f; // 1 second wait

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 800, 450); 

        batch = new SpriteBatch();
        font = new BitmapFont(true);
        interactables = new Array<>();

        deathZones = new Array<>(); 
        respawnPoint = new Vector2();
        enemyRespawnPoint = new Vector2();

        player = new Rectangle(0, 0, 50, 50);
        
        // Initialize the lists
        platforms = new Array<>();
        walls = new Array<>();
        enemies = new Array<>();
        
        // Load the level
        loadLDtkLevel("maps/level1(new).ldtk"); 
    }

    private void loadLDtkLevel(String path) {
        platforms.clear();
        walls.clear();
        com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal(path));
        
        JsonValue level = root.get("levels").get(0); 
        JsonValue layerInstances = level.get("layerInstances");

        for (JsonValue layer : layerInstances) {
            String layerName = layer.getString("__identifier");

            // Load Platforms
            if (layerName.equals("IntGrid")) {
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

            // Load Walls
            if (layerName.equals("Walls")) {
                int gridSize = layer.getInt("__gridSize");
                int cWid = layer.getInt("__cWid");
                int[] gridData = layer.get("intGridCsv").asIntArray();

                for (int i = 0; i < gridData.length; i++) {
                    if (gridData[i] > 0) { 
                        int x = (i % cWid) * gridSize;
                        int y = (i / cWid) * gridSize;
                        walls.add(new Rectangle(x, y, gridSize, gridSize));
                    }
                }
            }
            
            // Load Entities
            if (layerName.equals("Entities")) {
                for (JsonValue entity : layer.get("entityInstances")) {
                    String name = entity.getString("__identifier");
                    float[] coords = entity.get("px").asFloatArray();
                    
                    if (name.equals("Player_Spawn")) {
                        float sx = coords[0];
                        float sy = coords[1];
                        
                        player.setPosition(sx, sy);
                        respawnPoint.set(sx, sy); 
                        System.out.println("Spawn point captured at: " + sx + ", " + sy);
                    }
                    if (name.equals("Enemy_Spawn")) {
                        float ex = coords[0];
                        float ey = coords[1];
                        
                        enemies.add(new Enemy(ex, ey));
                        enemyRespawnPoint.set(ex, ey); 
                        System.out.println("Enemy spawn point captured at: " + ex + ", " + ey);
                    }
                    if (name.equals("Death")) {
                        float w = entity.getInt("width");
                        float h = entity.getInt("height");
                        deathZones.add(new Rectangle(coords[0], coords[1], w, h));
                    }

                    if (name.equals("NPC")) {
                        String msg = "Default Message"; 
                        JsonValue fieldInstances = entity.get("fieldInstances");

                        if (fieldInstances != null && fieldInstances.size > 0) {
                            for (JsonValue field : fieldInstances) {
                                String id = field.getString("__identifier");
                                if (id.equalsIgnoreCase("message")) {
                                    JsonValue val = field.get("__value");
                                    if (val != null && !val.isNull()) {
                                        msg = val.asString();
                                    } else {
                                        msg = "Empty Value in LDtk";
                                    }
                                }
                            }
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
    public void render(float delta) {
        float dt = Gdx.graphics.getDeltaTime();

        if (isDead) {
            deathTimer -= dt;
            if (deathTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                respawnPlayer();
            }
        }
        
        if (!isDead){
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
                if (xVelocity == 0) xVelocity = DASH_SPEED * dt; 
                else xVelocity = (xVelocity > 0 ? DASH_SPEED : -DASH_SPEED) * dt;
            }

            // DASH STATE vs NORMAL STATE
            if (isDashing) {
                dashTimer -= dt;
                yVelocity = 0; // Freeze Y-axis during dash
                if (dashTimer <= 0) isDashing = false;
            } else {
                dashCooldown -= dt;

                // Horizontal Input
                if (Gdx.input.isKeyPressed(Input.Keys.A)) xVelocity = -MOVE_SPEED * dt;
                else if (Gdx.input.isKeyPressed(Input.Keys.D)){
                    if(rightGrab){
                        while (xVelocity < 0){
                            xVelocity += 0.01;
                        }
                        System.out.println("Right Wall Jump");
                    } else{
                        xVelocity = MOVE_SPEED * dt;
                    }
                } 
                else xVelocity = 0;

                // Coyote Time Logic
                if (onGround){
                    coyoteCounter = COYOTE_TIME_MAX; 
                    rightGrab = false;
                }
                else if (coyoteCounter > 0) coyoteCounter--;

                // Jump Input
                if (Gdx.input.isKeyJustPressed(Input.Keys.W) && (onGround || coyoteCounter > 0)) {
                    yVelocity = JUMP_SPEED;
                    onGround = false;
                    coyoteCounter = 0;

                    createDust(
                        player.x + player.width / 2, 
                        player.y + player.height, 
                        5
                    );

                    // Stretch UP on jump
                    playerScaleX = 0.8f; 
                    playerScaleY = 1.3f;
                }

                // Draw player left and right hitbox for wall grab debug

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.FOREST);
                float boxWidth = (player.width * playerScaleX) / 10;
                float boxHeight = player.height * playerScaleY;
                float visX = player.x + (player.width - boxWidth) / 2f;
                float visY = (player.y + player.height) - boxHeight;

                // Left
                Rectangle leftHitbox = new Rectangle(visX - 25, visY, boxWidth, boxHeight);
                shapeRenderer.rect(leftHitbox.x, leftHitbox.y, leftHitbox.width, leftHitbox.height);

                // Right
                Rectangle rightHitbox = new Rectangle(visX + 25, visY, boxWidth, boxHeight);
                shapeRenderer.rect(rightHitbox.x, rightHitbox.y, rightHitbox.width, rightHitbox.height);
                shapeRenderer.end();

                // Wall Grab & Gravity
                isWallGrabbing = false;
                if (!onGround && Math.abs(xVelocity) > 0) {
                    for (Rectangle w : walls) {
                        if (player.overlaps(w)){
                            isWallGrabbing = true;
                            xVelocity = 0;
                            System.out.println("Wall Grabbing");
                            yVelocity = 10f * dt; 
                            xVelocity = 0;

                            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                                yVelocity = 10f * dt; 

                                if (rightHitbox.overlaps(w)){
                                    System.out.println("Right Hitbox");
                                    rightGrab = true;
                                    xVelocity = -10;
                                } else if (leftHitbox.overlaps(w)){
                                    System.out.println("Left Hitbox");
                                    leftGrab = true;
                                    xVelocity = 10;
                                }
                                onGround = false;
                                coyoteCounter = 0;

                                createDust(
                                    player.x + player.width / 2, 
                                    player.y + player.height, 
                                    5
                                );

                                // Stretch UP on jump
                                playerScaleX = 0.8f; 
                                playerScaleY = 1.3f;
                            }
                        } 
                    }
                }

                if (isWallGrabbing){
                    yVelocity = 10f * dt; 
                    xVelocity = 0;
                    if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                        // Jump off wall logic
                        yVelocity = JUMP_SPEED;
                        if(rightGrab){
                            xVelocity = -50;
                            System.out.println("Right Grab");
                        } 
                        if(leftGrab) xVelocity = 10;
                        onGround = false;
                        coyoteCounter = 0;

                        createDust(
                            player.x + player.width / 2, 
                            player.y + player.height, 
                            5
                        );

                        // Stretch UP on jump
                        playerScaleX = 0.8f; 
                        playerScaleY = 1.3f;
                    }
                } 
                else yVelocity += GRAVITY;
            }

            // APPLY PHYSICS
            player.x += xVelocity;
            checkCollisions(true);
            player.y += yVelocity;
            checkCollisions(false);

            // Enemy collision / knockback logic
            for (Enemy e : enemies) {
                if (player.overlaps(e.bounds)) {
                    int pushPower = 20;
                    if (e.bounds.x < player.x) {
                        xVelocity = pushPower; 
                        player.setX(player.getX() + 5); 
                    } else {
                        xVelocity = (-pushPower);
                        player.setX(player.getX() - 10);
                        e.movingRight = true;
                    }
                    yVelocity = (-2);
                }
            }
        }

        // CAMERA & DRAWING
        camera.position.set(player.x + player.width/2, player.y + player.height/2, 0);
        camera.update();

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw Platforms
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle p : platforms) shapeRenderer.rect(p.x, p.y, p.width, p.height);

        // Draw Walls
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle w : walls) shapeRenderer.rect(w.x, w.y, w.width, w.height);

        // Enemy AI gap sensors
        shapeRenderer.setColor(Color.CYAN);
        for (Enemy e : enemies) {
            float sensorX = e.movingRight ? e.bounds.x + e.bounds.width + 10 : e.bounds.x - 15;
            shapeRenderer.rect(sensorX, e.bounds.y + e.bounds.height + 5, 5, 5); 
        }
        
        // Draw Player
        if (!isDead) {
            shapeRenderer.setColor(Color.RED);
            float visWidth = player.width * playerScaleX;
            float visHeight = player.height * playerScaleY;
            float visX = player.x + (player.width - visWidth) / 2f;
            float visY = (player.y + player.height) - visHeight;
            shapeRenderer.rect(visX, visY, visWidth, visHeight);
        }

        // Draw Enemies
        shapeRenderer.setColor(Color.YELLOW);
        for (Enemy e : enemies) shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);

        // Draw Enemy Stomp Hitbox (Top half visual debugging)
        shapeRenderer.setColor(Color.BLACK); 
        for (Enemy e : enemies){
            shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, (e.bounds.height / 2));
        }

        // UPDATE PARTICLES
        for (int i = dustParticles.size - 1; i >= 0; i--) {
            Dust d = dustParticles.get(i);
            d.update(delta);
            if (d.isDead()) {
                dustParticles.removeIndex(i);
            }
        }

        shapeRenderer.end();

        // Alpha blended rendering for particles
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Dust d : dustParticles) {
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, d.alpha);
            shapeRenderer.rect(d.x, d.y, d.size, d.size);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Update Interactables
        bobCounter += dt;
        for (Interactable i : interactables) {
            i.update(dt, player);
            if (i.isPlayerNear && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                i.isDialogOpen = !i.isDialogOpen;
                if (i.isDialogOpen) i.visibleChars = 0; 
            }
        }

        // Kill application
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Gdx.app.exit();
                }
            });
        }

        // Draw HUD/Interaction prompts
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Interactable i : interactables) {
            if (i.isPlayerNear && !i.isDialogOpen) {
                font.draw(batch, "[E]", i.bounds.x + 5, i.bounds.y - 10);
            }
        }
        batch.end();

        if (anyDialogOpen()) { 
            drawDialogBoxes();
        }

        updateEnemies(dt);
    }

    // TODO: Add logic for enemies interacting with walls
    private void updateEnemies(float dt) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.tickHurtTimer(dt);
            boolean onPlatform = false;

            // HORIZONTAL MOVEMENT
            float moveAmount = (e.movingRight ? 150f : -150f) * dt;
            e.bounds.x += moveAmount;

            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    float footPos = e.bounds.y + e.bounds.height;
                    boolean isStepUp = p.y < footPos && p.y > footPos - 15;

                    if (isStepUp) {
                        e.velocity.y = -18f; 
                    } else {
                        e.movingRight = !e.movingRight;
                        if (e.movingRight) e.bounds.x = p.x + p.width + 2;
                        else e.bounds.x = p.x - e.bounds.width - 2;
                        break; 
                    }
                }
            }

            // Collide with player logic
            if (e.bounds.overlaps(player)) {
                Rectangle enemyTopHitbox = new Rectangle(
                    e.bounds.x, 
                    e.bounds.y, 
                    e.bounds.width, 
                    e.bounds.height / 2
                );

                if (player.overlaps(enemyTopHitbox)) {
                    e.velocity.y = -10;
                    System.out.println("Kill");
                    enemies.removeIndex(i);
                    yVelocity = JUMP_SPEED * 0.6f;
                    onGround = false;
                    continue;
                } else {
                    e.velocity.x -= 50;
                    e.velocity.y -= 10;
                }
            }

            // VERTICAL MOVEMENT
            e.velocity.y += GRAVITY;
            e.bounds.y += e.velocity.y;

            for (Rectangle p : platforms) {
                if (e.bounds.overlaps(p)) {
                    if (e.velocity.y > 0) { 
                        e.bounds.y = p.y - e.bounds.height;
                        e.velocity.y = 0;
                        onPlatform = true; 
                    } else if (e.velocity.y < 0) { 
                        e.bounds.y = p.y + p.height;
                        e.velocity.y = 0;
                    }
                }
            }

            // GAP & LEAP LOGIC
            if (onPlatform) {
                float sensorX = e.movingRight ? e.bounds.x + e.bounds.width + 10 : e.bounds.x - 15;
                Rectangle gapSensor = new Rectangle(sensorX, e.bounds.y + e.bounds.height + 5, 5, 5);

                boolean groundAhead = false;
                for (Rectangle p : platforms) {
                    if (gapSensor.overlaps(p)) {
                        groundAhead = true;
                        break;
                    }
                }

                if (!groundAhead) {
                    float leapX = e.movingRight ? e.bounds.x + e.bounds.width + 40 : e.bounds.x - 140;
                    Rectangle leapSensor = new Rectangle(leapX, e.bounds.y - 50, 100, 200);

                    boolean landingSpotFound = false;
                    for (Rectangle p : platforms) {
                        if (leapSensor.overlaps(p)) {
                            landingSpotFound = true;
                            break;
                        }
                    }

                    if (landingSpotFound) {
                        e.velocity.y = -16f; 
                    } else {
                        e.movingRight = !e.movingRight;
                        e.bounds.x += (e.movingRight ? 5 : -5); 
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
                    if (yVelocity > 0) { 
                        if (wasFalling && yVelocity > 5f) { 
                            playerScaleX = 1.4f; 
                            playerScaleY = 0.7f;
                            wasFalling = false; 
                        }
                        
                        player.y = p.y - player.height;
                        onGround = true;
                        yVelocity = 0;
                    } else if (yVelocity < 0) { 
                        player.y = p.y + p.height;
                        yVelocity = 0;
                    }
                }
            }
        }
    }

    private void drawDialogBoxes() {
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
        wasFalling = false; 
        System.out.println("Respawning at captured LDtk point: " + respawnPoint);
        
        enemies.clear();
        enemies.add(new Enemy(enemyRespawnPoint.x, enemyRespawnPoint.y));
        System.out.println("Respawning enemy at captured LDtk point: " + enemyRespawnPoint);
    }
    
    private void createDust(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float vx = MathUtils.random(-2f, 2f); 
            float vy = MathUtils.random(-2f, 0f); 
            dustParticles.add(new Dust(x, y, vx, vy));
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}