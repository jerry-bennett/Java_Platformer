import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Game extends JPanel implements KeyListener {
    private boolean onGround = false;
    private int coyoteCounter = 0;
    private final int COYOTE_TIME_MAX = 10;
    private boolean isWallGrabbing = false;
    private Platform currentWall = null;

    private final int JUMP_SPEED = 15;
    private final int MOVE_SPEED = 5;
    private final int GRAVITY = 1;
    private int levelWidth = 0;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private LevelEndRectangle levelEndRectangle = null;
    private Player player = new Player(50, 50, 50, 50); // adjust the values as needed

    private BufferedImage levelBackground; // Distant, blurred, parallaxed
    private BufferedImage levelForeground; // Close, sharp, moves 1:1 with player

    private List<Platform> platforms = new ArrayList<>();

    private int currentLevelNumber = 1;

    private int wallStickTimer = 0;
    private final int MAX_STICK_TIME = 15;

    private int camX = 0;
    private int camY = 0;

    private int jumpLockoutTimer = 0;
    private int lockoutDirection = 0;

    private List<Rectangle> deathZones = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<TrailPoint> playerTrails = new ArrayList<>();

    private int levelHeight = 0;

    private float cameraSmoothing = 0.05f; // Adjust this for more or less "weight"

    private int bgOffsetX = 0;
    private int bgOffsetY = 0;

    private int fgOffsetX = 0;
    private int fgOffsetY = 0;

    private int spawnX;
    private int spawnY;
    
    private boolean isDead = false;
    private int deathTimer = 0;
    private final int DEATH_DELAY_MAX = 40;

    private List<Dust> dustParticles = new ArrayList<>();

    private int shakeIntensity = 0;
    private final java.util.Random random = new java.util.Random();

    boolean top = false;
    boolean bottom = false;

    private int particleTimer = 0;

    public Game(String levelFilePath) {
        setFocusable(true);
        setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);

        Timer timer = new Timer(5, e -> move());
        timer.start();

        // read platforms from the level file
        loadPlatformsFromJson(levelFilePath);

        // PRE-LOAD SOUNDS
        SoundManager.loadSound("jump", "/sfx/jump.wav");
    }

    private BufferedImage blurImage(BufferedImage src) {
        float[] matrix = {
            1/16f, 2/16f, 1/16f,
            2/16f, 4/16f, 2/16f,
            1/16f, 2/16f, 1/16f
        };
        java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(new java.awt.image.Kernel(3, 3, matrix));
        return op.filter(src, null);
    }

    private void createDust(int x, int y, int count) {
        for (int i = 0; i < count; i++) {
            // Random velocity: x is left/right, y is slightly upward
            double vx = (Math.random() - 0.5) * 4; 
            double vy = (Math.random() * -2); 
            dustParticles.add(new Dust(x, y, vx, vy));
        }
    }

    private void triggerDeath() {
        if (!isDead) {
            isDead = true;
            deathTimer = DEATH_DELAY_MAX;
        }
    }
    private void loadPlatformsFromJson(String path) {
            platforms.clear(); // Clear old platforms
            levelWidth = 0;

            try (InputStream inputStream = getClass().getResourceAsStream(path)) {
                if (inputStream == null) {
                    System.err.println("Could not find file: " + path);
                    return;
                }

            JSONTokener tokener = new JSONTokener(inputStream);
            JSONObject root = new JSONObject(tokener);
            this.levelWidth = root.getInt("width") * root.getInt("tilewidth");
            this.levelHeight = root.getInt("height") * root.getInt("tileheight");
            System.out.println("Level Width: " + levelWidth);
            JSONArray layers = root.getJSONArray("layers");

            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                String layerType = layer.getString("type");

            // 1. HANDLE IMAGE LAYER
            if (layerType.equals("imagelayer")) {
                String layerName = layer.getString("name").toLowerCase(); // Get the Tiled layer name
                String imagePath = layer.getString("image");
                File file = new File(imagePath);
                String fileName = file.getName();

                try {
                    //read background image layer
                    String imageResourcePath = "/Levels/level" + currentLevelNumber + "/" + fileName;
                    BufferedImage img = ImageIO.read(getClass().getResourceAsStream(imageResourcePath));
                    if (layerName.equals("background")) {
                        levelBackground = blurImage(img);
                        bgOffsetY = layer.optInt("offsety", 0);
                        bgOffsetX = layer.optInt("offsetx", 0);
                    }else {
                        levelForeground = img;
                        fgOffsetX = layer.optInt("offsetx", 0);
                        fgOffsetY = layer.optInt("offsety", 0);
                        System.out.println("Background Y Offset: " + bgOffsetY);
                        System.out.println("Foreground Y Offset: " + fgOffsetY);
                        System.out.println("Loaded Foreground: " + fileName);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load background image: " + fileName);
                }
            }

                // 2. HANDLE OBJECT LAYER
                if (layer.getString("type").equals("objectgroup")) {
                    JSONArray objects = layer.getJSONArray("objects");
                    for (int j = 0; j < objects.length(); j++) {
                        JSONObject obj = objects.getJSONObject(j);
                        
                        int x = obj.getInt("x");
                        int y = obj.getInt("y");
                        int width = obj.getInt("width");
                        int height = obj.getInt("height");

                        //debugging:
                        String objName = obj.optString("name", "platform");

                        if (obj.optString("name").equalsIgnoreCase("death")) {
                            deathZones.add(new Rectangle(x, y, width, height));
                            continue; 
                        }

                        if (obj.optString("name").equalsIgnoreCase("wall")) {
                            // You could create a specific 'Wall' class, or just add a boolean to Platform
                            Platform wall = new Platform(x, y, width, height);
                            wall.setIsClimbable(true); // You'll need to add this field to your Platform class
                            platforms.add(wall);
                            wall.setLabel("WALL");
                            continue;
                        }

                        if (obj.optString("name").equalsIgnoreCase("enemy")) {
                            enemies.add(new Enemy(x, y, width, height));
                            continue;
                        }
                        
                        // CHECK FOR SPAWN POINT
                        if (obj.optString("name").equalsIgnoreCase("spawn")) {
                            spawnX = x;
                            spawnY = y;
                            player.setX(spawnX);
                            player.setY(spawnY);
                            System.out.println("Player spawned at: " + x + ", " + y);
                            continue; // Don't make a platform out of the spawn point
                        }
                        // CHECK FOR GOAL
                        if (obj.optString("name").equals("goal")) {
                            levelEndRectangle = new LevelEndRectangle(x, y, width, height);
                            continue; // Skip adding this to the platforms list
                        }

                        // Otherwise, it's a normal platform
                        platforms.add(new Platform(x, y, width, height));
                        if (x + width > levelWidth) levelWidth = x + width;
                        if (y + height > levelHeight) levelHeight = y + height;

                    }
                }
            }
    } catch (Exception e) {
        System.err.println("Error parsing JSON at: " + path);
        e.printStackTrace();
        }
    }

    private void advanceToNextLevel() {
        currentLevelNumber++;
        String nextLevelPath = "/Levels/level" + currentLevelNumber + ".json";
        
        // Check if the next level file actually exists
        if (getClass().getResource(nextLevelPath) != null) {
            loadNewLevel(nextLevelPath);
        } else {
            // If there's no level3.json, you've beaten the game!
            System.out.println("No more levels! You win!");
            showWinScreen(); 
        }
    }

    private void showWinScreen() {
        JOptionPane.showMessageDialog(this, "Congratulations! You've cleared all levels!");
        System.exit(0);
    }
    
    private static boolean isPlayerCollidingWithLevelEnd(LevelEndRectangle levelEnd, Player player) {
        if (levelEnd == null) return false;
        return player.getBounds().intersects(levelEnd.getBounds());
    }

    private void loadNewLevel(String newLevelFilePath) {
        // Wipe the old data
        platforms.clear();
        deathZones.clear();
        levelBackground = null;
        levelForeground = null;

        // Load the new JSON and images
        loadPlatformsFromJson(newLevelFilePath);

        // Reset player physics
        player.setYVelocity(0);
        player.setXVelocity(0);
        
        // Reset camera to spawn point to prevent "flash" of previous level's position
        camX = player.getX() - (getWidth() / 2);
        camY = player.getY() - (getHeight() / 2);

        System.out.println("Level Loaded: " + newLevelFilePath);
    }

    private void move() {
        updateEnemies();
        particleTimer++;
        if (jumpLockoutTimer > 0) jumpLockoutTimer--;

        if (shakeIntensity > 0) {
            shakeIntensity--;
        }
        
        // Reset jump direction timer
        if (jumpLockoutTimer > 0) {
            jumpLockoutTimer--;
            if (jumpLockoutTimer == 0) {
                lockoutDirection = 0;
            }
        }
        // dust logic
        dustParticles.removeIf(d -> {
            d.update();
            return d.alpha <= 0;
        });

        // trail logic
        playerTrails.removeIf(tp -> {
            tp.update();
            return tp.alpha <= 0 || tp.size <= 0;
        });

        if (isDead) {
            deathTimer--;
            if (deathTimer <= 0) {
                deathTimer--;
            }
            repaint(); // Still need to repaint to see the "death effect"
            return; // STOP all physics/movement while dead
        }else{
            player.updateAnimation();
            //trail logic
            for (int i = 0; i < 3; i++) {
                // Only spawn every 4th frame
                if (particleTimer % 4 == 0) {
                    playerTrails.add(new TrailPoint(
                        player.getX(), 
                        player.getY(), 
                        player.getWidth(), // startSize
                        player.getWidth(), // playerWidth
                        (float)player.getXVelocity() // The new "tilt" parameter
                    ));
                }
            }

            //shake logic
            for (Enemy e : enemies) {
                if (player.getBounds().intersects(e.getBounds())) {
                    // 1. Trigger Screen Shake
                    shakeIntensity = 10; 

                    // 2. Determine push direction
                    // If enemy is to the left of player, push player right, and vice versa
                    int pushPower = 10;
                    if (e.getX() < player.getX()) {
                        player.setXVelocity(pushPower); // Knockback
                        player.setX(player.getX() + 5); // Physical nudge to prevent sticking
                    } else {
                        player.setXVelocity(-pushPower);
                        player.setX(player.getX() - 5);
                    }
                    
                    // 3. Optional: Bounce the player up slightly
                    player.setYVelocity(-8);
                }
            }

            // movement
            // 1. Handle X Movement
            isWallGrabbing = false;
            currentWall = null;

            // Apply friction if no keys are pressed
            if (!leftPressed && !rightPressed && jumpLockoutTimer <= 0) {
                player.setXVelocity((int)(player.getXVelocity() * 0.8)); // 0.8 is the friction coefficient
                if (Math.abs(player.getXVelocity()) < 1) player.setXVelocity(0);
            }
            player.setX(player.getX() + player.getXVelocity());

            for (Platform platform : platforms) {
                if (player.getBounds().intersects(platform.getBounds())) {
                    if (player.getXVelocity() > 0) {
                        player.setX(platform.getX() - player.getWidth());
                        player.setXVelocity(0);
                    } else if (player.getXVelocity() < 0) {
                        player.setX(platform.getX() + platform.getWidth());
                        player.setXVelocity(0);
                    }
                    if (platform.isClimbable() && !onGround) {
                        isWallGrabbing = true;
                        wallStickTimer = MAX_STICK_TIME;
                        currentWall = platform;
                        // Only create dust if we just hit the wall
                        if (Math.abs(player.getXVelocity()) > 1) {
                            createDust(player.getX(), player.getY() + player.getHeight()/2, 5);
                        }
                    }
                }
            }
            if (wallStickTimer > 0) {
                wallStickTimer--;
                if (wallStickTimer == 0) isWallGrabbing = false;
            }
            // 2. Handle Y Movement & Gravity
            if (isWallGrabbing && player.getYVelocity() > 0) {
                player.setYVelocity(0); // Slow slide
            } else {
                player.setYVelocity(player.getYVelocity() + GRAVITY);
            }

            player.setY(player.getY() + player.getYVelocity());

            // 3. Collision Resolution
            for (Platform platform : platforms) {
                if (player.getBounds().intersects(platform.getBounds())) {
                    if (player.getYVelocity() > 0) { // Falling Down
                        if (player.getYVelocity() > 5) { // Only squash if falling with some speed
                            player.setScale(1.4, 0.7); // Wide and short
                            createDust(player.getX() + player.getWidth()/2, platform.getY(), 8);// Dust particles
                        }
                        player.setY(platform.getY() - player.getHeight());
                        player.setYVelocity(0);
                    } else if (player.getYVelocity() < 0) { // Hitting ceiling
                        player.setY(platform.getY() + platform.getHeight());
                        player.setYVelocity(0);
                    }
                }
            }

            // 4. Floor Boundary
            onGround = checkOnGround();
            if (onGround) {
                coyoteCounter = COYOTE_TIME_MAX; // Refill the "grace period"
            } else {
                if (coyoteCounter > 0) coyoteCounter--; // Use up the grace period
            }

            // 5. UPDATE onGround status at the very end and set camera
            camX = player.getX() - (getWidth() / 2);
            camY = player.getY() - (getHeight() / 2);
            onGround = checkOnGround();

            if (isPlayerCollidingWithLevelEnd(levelEndRectangle, player)) {
                System.out.println("Goal reached!");
                advanceToNextLevel();
            }

            // 6. CHECK FOR DEATH ZONES
            for (Rectangle zone : deathZones) {
                if (player.getBounds().intersects(zone)) {
                    System.out.println("Player fell into a death zone!");
                    triggerDeath();
                    break; 
                }
            }

            if (player.getY() > 2000) { // Safety net if they miss a death zone
                respawnPlayer();
            }

            // 1. Calculate the IDEAL target (centered on player)
            int targetX = player.getX() - (getWidth() / 2);
            int targetY = player.getY() - (getHeight() / 2);

            // 2. Apply Vertical and Horizontal Clamping
            if (targetX < 0) targetX = 0;
            if (targetX > levelWidth - getWidth()) targetX = levelWidth - getWidth();

            if (targetY < 0) targetY = 0;
            if (levelHeight > getHeight() && targetY > levelHeight - getHeight()) {
                targetY = levelHeight - getHeight();
            }

            // 3. THE SMOOTHING (LERP)
            camX += (targetX - camX) * cameraSmoothing;
            camY += (targetY - camY) * cameraSmoothing;

            // Keep camera from showing out-of-bounds (the "dead zone")
            if (camX < 0) camX = 0;
            //if (camY < 0) camY = 0;
        }
        repaint();

    }

    private void updateEnemies() {
        for (Enemy e : enemies) {
            // 1. Gravity
            e.setYVelocity(e.getYVelocity() + GRAVITY);
            e.setY(e.getY() + e.getYVelocity());

            // 2. Vertical Collision (Floor)
            boolean onPlatform = false;
            for (Platform p : platforms) {
                if (e.getBounds().intersects(p.getBounds())) {
                    if (e.getYVelocity() > 0) {
                        e.setY(p.getY() - e.getHeight());
                        e.setYVelocity(0);
                        onPlatform = true;
                    }
                }
            }

            // 3. Horizontal Movement
            int speed = e.isMovingRight() ? 3 : -3;
            e.setX(e.getX() + speed);

            // 4. Wall & Boundary Collision (Bounce)
            for (Platform p : platforms) {
                if (e.getBounds().intersects(p.getBounds())) {
                    e.setMovingRight(!e.isMovingRight());
                    // Nudge out of the wall to prevent sticking
                    e.setX(e.getX() + (e.isMovingRight() ? 5 : -5)); 
                }
            }

            // Level Bounds
            if (e.getX() < 0 || e.getX() + e.getWidth() > levelWidth) {
                e.setMovingRight(!e.isMovingRight());
            }

            // 5. Jump Logic (Smart Edge Detection)
            Rectangle edgeSensor = e.getEdgeSensor();
            boolean groundImmediatelyAhead = false;
            for (Platform p : platforms) {
                if (edgeSensor.intersects(p.getBounds())) {
                    groundImmediatelyAhead = true;
                    break;
                }
            }

            // 6. CHECK FOR ENEMY DEATH (Respawn)
            boolean hitDeathZone = false;
            for (Rectangle zone : deathZones) {
                if (e.getBounds().intersects(zone)) {
                    hitDeathZone = true;
                    break;
                }
            }

            if (hitDeathZone || e.getY() > 2000) {
                e.respawn();
            }

            if (!groundImmediatelyAhead && onPlatform) {
                // 1. Calculate how far we want to look (e.g., 200 pixels)
                int reachDistance = 100; 
                
                // 2. Define the sensor area based on direction
                int sensorX = e.isMovingRight() ? (e.getX() + e.getWidth()) : (e.getX() - reachDistance);
                
                // 3. Create the sensor (Width is reachDistance, Height scans down to levelHeight)
                Rectangle jumpReachSensor = new Rectangle(
                    sensorX, 
                    e.getY(), 
                    reachDistance, 
                    300 
                );

                boolean landingSpotFound = false;
                for (Platform p : platforms) {
                    if (jumpReachSensor.intersects(p.getBounds())) {
                        landingSpotFound = true;
                        break;
                    }
                }

                if (landingSpotFound) {
                    // Safe to jump!
                    e.setYVelocity(-16); 
                    int jumpPush = e.isMovingRight() ? 5 : -5; 
                    e.setX(e.getX() + jumpPush);
                } else {
                    // Nothing found within 200px. Turn back!
                    e.setMovingRight(!e.isMovingRight());
                    // Nudge to prevent getting stuck in the "no ground" loop
                    e.setX(e.getX() + (e.isMovingRight() ? 5 : -5));
                }
            }
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;


        int currentShakeX = 0;
        int currentShakeY = 0;
        if (shakeIntensity > 0) {
            currentShakeX = random.nextInt(shakeIntensity * 2 + 1) - shakeIntensity;
            currentShakeY = random.nextInt(shakeIntensity * 2 + 1) - shakeIntensity;
        }
        // --- 1. DRAW DISTANT BACKGROUND (Parallax) ---
        if (levelBackground != null) {
            java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
            
            // A. Calculate the parallax shift
            int pX = (int) (camX * 0.5); 
            int pY = (int) (camY * 0.5);

            // B. Move the camera for the background pass
            g2d.translate(-camX + pX + currentShakeX, -camY + pY + currentShakeY);
            
            g.drawImage(levelBackground, bgOffsetX, bgOffsetY, null);
    
            g2d.setTransform(oldTransform);
        }

        // --- 2. DRAW THE PHYSICAL WORLD (Full Speed) ---
        g2d.translate(-camX, -camY);

        //DEBUG PLATFORMS
        // g.setColor(Color.WHITE);
        // g.setFont(new Font("Arial", Font.BOLD, 12)); // Make it readable
        // g.setColor(Color.WHITE);
        // for (Platform p : platforms) {
        //     g.drawRect(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        //     // Draw the label
        //     g.drawString("TYPE: " + p.getLabel(), p.getX() + 5, p.getY() + 20);
        //     g.drawString("SIZE: " + p.getWidth() + "x" + p.getHeight(), p.getX() + 5, p.getY() + 35);
        // }
        Composite originalComp = g2d.getComposite();

        // --- PASS 1: Particles & Enemies ---
        g2d.setXORMode(Color.WHITE);
        for (TrailPoint tp : playerTrails) {
            g.setColor(tp.getColor());
            g.fillRect((int)tp.x, (int)tp.y, (int)tp.size, (int)tp.size);
        }
        
        g.setColor(Color.CYAN);
        for (Enemy e : enemies) {
            g.fillRect(e.getX(), e.getY(), e.getWidth(), e.getHeight());
        }
        g2d.setPaintMode(); // Reset to normal briefly

        // --- PASS 2: The Player (Isolated) ---
        if (!isDead) {
            g2d.setXORMode(Color.WHITE);
            g.setColor(Color.RED);

            int vWidth = (int)(player.getWidth() * player.getScaleX());
            int vHeight = (int)(player.getHeight() * player.getScaleY());

            // Offset the drawing so it scales from the center/bottom rather than the top-left
            int offsetX = (vWidth - player.getWidth()) / 2;
            int offsetY = (vHeight - player.getHeight()); // Anchors to feet

            g.fillRect(player.getX() - offsetX, player.getY() - offsetY, vWidth, vHeight);

            // D. Cleanup
            g2d.setPaintMode();
            g2d.setComposite(originalComp);
        }

        // --- 3. DRAW EVERYTHING ELSE (Goal, etc.) ---
        g.setColor(Color.GREEN);
        g.fillRect(levelEndRectangle.getX(), levelEndRectangle.getY(), levelEndRectangle.getWidth(), levelEndRectangle.getHeight());

        g.setColor(new Color(200, 200, 200)); // Light Gray
        for (Dust d : dustParticles) {
            g.fillRect((int)d.x, (int)d.y, d.size, d.size);
        }

        // 3. DRAW FOREGROUND
        if (levelForeground != null) {
            g.drawImage(levelForeground, fgOffsetX, fgOffsetY, null);
        }

        g2d.translate(camX, camY); // Reset for next frame
    }

    private void respawnPlayer() {
        player.setX(spawnX);
        player.setY(spawnY);
        player.setXVelocity(0);
        player.setYVelocity(0);
    }

// WASD controls, key pressed
@Override
public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (isDead && deathTimer <= 0) {
        isDead = false;
        respawnPlayer();
        return; // Don't process movement on the same frame as respawn
    }
    switch (keyCode) {
        case KeyEvent.VK_W:
            if (coyoteCounter > 0) { // Can jump if on ground OR just walked off
                    player.setYVelocity(-JUMP_SPEED);
                    createDust(player.getX() + player.getWidth()/2, player.getY() + player.getHeight(), 5);
                    player.setScale(0.8, 1.3);
                    onGround = false; 
                    coyoteCounter = 0; // Prevent double jumping in mid-air
                    SoundManager.playSound("jump");
            }
            else if (isWallGrabbing && currentWall != null) {
                // Wall Jump!
                player.setYVelocity(-JUMP_SPEED);
                jumpLockoutTimer = 15;
                // Check player position relative to the wall to determine jump direction
                // If player center is left of wall center, jump Left
                if (player.getX() + (player.getWidth()/2) < currentWall.getX()) {
                    player.setXVelocity(-MOVE_SPEED * 2);
                    lockoutDirection = 1; // Prevent moving back into wall immediately
                } else {
                    player.setXVelocity(MOVE_SPEED * 2);
                    lockoutDirection = -1;
                }
                
                isWallGrabbing = false;
                wallStickTimer = 0;
                SoundManager.playSound("jump");
            }
    break;
        case KeyEvent.VK_A:
            leftPressed = true;
            if (jumpLockoutTimer <= 0 || lockoutDirection != -1) player.setXVelocity(-MOVE_SPEED);
            break;
        case KeyEvent.VK_D:
            rightPressed = true;
            if (jumpLockoutTimer <= 0 || lockoutDirection != 1) player.setXVelocity(MOVE_SPEED);
            break;
        case KeyEvent.VK_ESCAPE:
            // stop the game
            System.exit(0);
            break;
    }
}

// WASD controls, key released
public void keyReleased(KeyEvent e) {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
        case KeyEvent.VK_W:
            if (player.getYVelocity() < 0) {
                player.setYVelocity(player.getYVelocity() / 2); // Variable jump height
            }
            break;
        case KeyEvent.VK_A:
            leftPressed = false;
            break;
        case KeyEvent.VK_D:
            rightPressed = false;
            break;
    }
}

// logic for seeing if the player character is on the "ground".
private boolean checkOnGround() {
    // Check window floor first
    if (player.getY() + player.getHeight() >= getHeight() - 1) {
        return true;
    }

    // Check all platforms
    Rectangle footer = new Rectangle(player.getX(), player.getY() + player.getHeight(), player.getWidth(), 1);
    for (Platform platform : platforms) {
        if (footer.intersects(platform.getBounds())) {
            return true;
        }
    }
    return false;
}

//not sure why this is necessary but the game gets upset if i don't have this function
@Override
public void keyTyped(KeyEvent e) {
}

    // main loop
    public static void main(String[] args) {
        displayMainMenu();
    }

    private static void displayMainMenu() {
        JFrame frame = new JFrame("Main Menu");
        MainMenu mainMenu = new MainMenu();
        frame.add(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
    static void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            Game game = new Game("/Levels/level1.json");
            JFrame frame = new JFrame("My Game");
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    public int getVolume() {
        return 0;
    }

}