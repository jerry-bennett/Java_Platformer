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

    private final int JUMP_SPEED = 15;
    private final int MOVE_SPEED = 5;
    private final int GRAVITY = 1;
    private int levelWidth = 0;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private LevelEndRectangle levelEndRectangle = new LevelEndRectangle(1000, 0, 50, 500);
    private Player player = new Player(50, 50, 50, 50); // adjust the values as needed

    private BufferedImage levelBackground; // Distant, blurred, parallaxed
    private BufferedImage levelForeground; // Close, sharp, moves 1:1 with player

    private List<Platform> platforms = new ArrayList<>();

    private int currentLevelNumber = 1;

    private int camX = 0;
    private int camY = 0;

    private List<Rectangle> deathZones = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    private int levelHeight = 0;

    private float cameraSmoothing = 0.05f; // Adjust this for more or less "weight"

    private int bgOffsetX = 0;
    private int bgOffsetY = 0;

    private int spawnX;
    private int spawnY;

    boolean top = false;
    boolean bottom = false;

    public Game(String levelFilePath) {
        setFocusable(true);
        setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);

        Timer timer = new Timer(5, e -> move());
        timer.start();

        // read platforms from the level file
        loadPlatformsFromJson(levelFilePath);
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

                // Read the offsets from the JSON (Tiled uses these keys)
                int offX = layer.optInt("offsetx", 0);
                int offY = layer.optInt("offsety", 0);

                try {
                    //read background image layer
                    String imageResourcePath = "/Levels/level" + currentLevelNumber + "/" + fileName;
                    BufferedImage img = ImageIO.read(getClass().getResourceAsStream(imageResourcePath));
                    if (layerName.contains("background")) {
                        // Apply a blur to the background only
                        levelBackground = blurImage(img);
                        bgOffsetY = offY;
                        bgOffsetX = offX;
                    } else {
                        // Foreground stays sharp
                        levelForeground = img;
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

                        if (obj.optString("name").equalsIgnoreCase("death")) {
                            deathZones.add(new Rectangle(x, y, width, height));
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
        // 1. Handle X Movement
        player.setX(player.getX() + player.getXVelocity());
        for (Platform platform : platforms) {
            if (player.getBounds().intersects(platform.getBounds())) {
                if (player.getXVelocity() > 0) player.setX(platform.getX() - player.getWidth());
                else if (player.getXVelocity() < 0) player.setX(platform.getX() + platform.getWidth());
            }
        }

        // 2. Handle Y Movement & Gravity
        player.setYVelocity(player.getYVelocity() + GRAVITY);
        player.setY(player.getY() + player.getYVelocity());

        // 3. Collision Resolution
        for (Platform platform : platforms) {
            if (player.getBounds().intersects(platform.getBounds())) {
                if (player.getYVelocity() > 0) { // Falling Down
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
                respawnPlayer();
                break; 
            }
        }

        if (player.getY() > 2000) { // Safety net if they miss a death zone
            respawnPlayer();
        }

        // 1. Calculate the IDEAL target (centered on player)
        int targetX = player.getX() - (getWidth() / 2);
        int targetY = player.getY() - (getHeight() / 2);

        // 2. Apply Vertical and Horizontal Clamping (from our last step)
        if (targetX < 0) targetX = 0;
        if (targetX > levelWidth - getWidth()) targetX = levelWidth - getWidth();

        if (targetY < 0) targetY = 0;
        if (levelHeight > getHeight() && targetY > levelHeight - getHeight()) {
            targetY = levelHeight - getHeight();
        }

        // 3. THE SMOOTHING (LERP)
        // We move the camera a small fraction of the distance toward the target
        camX += (targetX - camX) * cameraSmoothing;
        camY += (targetY - camY) * cameraSmoothing;

        // Keep camera from showing out-of-bounds (the "dead zone")
        if (camX < 0) camX = 0;
        //if (camY < 0) camY = 0;
        
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

        // --- 1. DRAW DISTANT BACKGROUND (Parallax) ---
        if (levelBackground != null) {
            java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
            
            // This slides slower to create depth
            int parallaxX = (int) (camX * 0.5); 
            int parallaxY = (int) (camY * 0.5); 
            g2d.translate(-parallaxX, -parallaxY);
            
            g.drawImage(levelBackground, bgOffsetX, bgOffsetY, null);
            
            g2d.setTransform(oldTransform);
        }

        // --- 2. DRAW THE PHYSICAL WORLD (Full Speed) ---
        g2d.translate(-camX, -camY);

        // 3. DRAW FOREGROUND
        if (levelForeground != null) {
            g.drawImage(levelForeground, 0, 0, null);
        }

        // Draw player
        g.setColor(Color.RED);
        g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        // Draw enemies
        g.setColor(Color.ORANGE);
        for (Enemy e : enemies) {
            g.fillRect(e.getX(), e.getY(), e.getWidth(), e.getHeight());
        }

        // Draw level end
        g.setColor(Color.GREEN);
        g.fillRect(levelEndRectangle.getX(), levelEndRectangle.getY(), levelEndRectangle.getWidth(), levelEndRectangle.getHeight());

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
    switch (keyCode) {
        case KeyEvent.VK_W:
    if (coyoteCounter > 0) { // Can jump if on ground OR just walked off
            player.setYVelocity(-JUMP_SPEED);
            onGround = false; 
            coyoteCounter = 0; // Prevent double jumping in mid-air
        }
        break;
        case KeyEvent.VK_A:
            leftPressed = true;
            player.setXVelocity(-MOVE_SPEED);
            break;
        case KeyEvent.VK_D:
            rightPressed = true;
            player.setXVelocity(MOVE_SPEED);
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
            player.setYVelocity(player.getYVelocity() / 2);
        }
        break;
        case KeyEvent.VK_A:
            leftPressed = false;
            if (!rightPressed) { // Stop moving left if right is not pressed
                player.setXVelocity(0);
            } else {
                player.setXVelocity(MOVE_SPEED); // Move right if right is still pressed
            }
            break;
        case KeyEvent.VK_D:
            rightPressed = false;
            if (!leftPressed) { // Stop moving right if left is not pressed
                player.setXVelocity(0);
            } else {
                player.setXVelocity(-MOVE_SPEED); // Move left if left is still pressed
            }
            break;
    }
}

// logic for seeing if the player character is on the "ground". currently the game window border is the ground.
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