import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


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
    private LevelEndRectangle levelEndRectangle = new LevelEndRectangle(750, 0, 50, 500);
    private Player player = new Player(50, 50, 50, 50); // adjust the values as needed

    private Image backgroundImage;
    private List<Platform> platforms = new ArrayList<>();

    private int camX = 0;
    private int camY = 0;

    boolean top = false;
    boolean bottom = false;

    public Game(String levelFilePath) {
        setFocusable(true);
        setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);
        setFocusable(true);
        addKeyListener(this);
        Timer timer = new Timer(5, e -> move());
        timer.start();

        //"floor" platform
        platforms.add(new Platform(0, 450, 10000, 50));

        // read platforms from the level file
        try {
            InputStream inputStream = getClass().getResourceAsStream(levelFilePath);
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                int x = Integer.parseInt(tokens[0]);
                int y = Integer.parseInt(tokens[1]);
                int width = Integer.parseInt(tokens[2]);
                int height = Integer.parseInt(tokens[3]);
                Platform platform = new Platform(x, y, width, height);
                platforms.add(platform);
            }
            scanner.close();
        } catch (NullPointerException e) {
            System.err.println("Could not find file: " + levelFilePath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean isPlayerCollidingWithLevelEnd(LevelEndRectangle levelEnd, Player player){
        var playerBounds = new Rectangle(player.getBounds());
        var levelEndBounds = new Rectangle(levelEnd.getBounds());
        return playerBounds.intersects(levelEndBounds);
    }

    private void loadNewLevel(String newLevelFilePath) {
        setFocusable(true);
        setPreferredSize(new Dimension(500, 500));
        removeKeyListener(this); // remove the previous key listener
        Timer timer = new Timer(5, e -> move());
        timer.start();
    
        try {
            InputStream inputStream = getClass().getResourceAsStream(newLevelFilePath);
            Scanner scanner = new Scanner(inputStream);
            List<Platform> newPlatforms = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                int x = Integer.parseInt(tokens[0]);
                int y = Integer.parseInt(tokens[1]);
                int width = Integer.parseInt(tokens[2]);
                int height = Integer.parseInt(tokens[3]);
                int platformRightEdge = x + width;
                if (platformRightEdge > levelWidth) levelWidth = platformRightEdge;
                Platform platform = new Platform(x, y, width, height);
                newPlatforms.add(platform);
            }
            scanner.close();
            Game newGame = new Game(newLevelFilePath);
            newGame.platforms = newPlatforms;
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(newGame);
            frame.pack();
            newGame.requestFocusInWindow(); // request focus for the new instance of the Game class
            newGame.addKeyListener(newGame); // add KeyListener after removing the old one
        } catch (NullPointerException e) {
            System.err.println("Could not find file: " + newLevelFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void move() {
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

    repaint();

    // Center the camera on the player
    camX = player.getX() - (getWidth() / 2);
    camY = player.getY() - (getHeight() / 2);

    // Keep camera from showing out-of-bounds (the "dead zone")
    if (camX < 0) camX = 0;
    if (camY < 0) camY = 0;

}
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //background image for level 
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        }
        g2d.translate(-camX, -camY); // Shift the world

        // draw platforms
        g.setColor(Color.BLUE);
        for (Platform platform : platforms) {
            g.fillRect(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        }

        // draw player
        g.setColor(Color.RED);
        g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        g.setColor(Color.GREEN);
        g.fillRect(levelEndRectangle.getX(), levelEndRectangle.getY(), levelEndRectangle.getWidth(), levelEndRectangle.getHeight());

        // logic for when the player reaches the level end
        if (isPlayerCollidingWithLevelEnd(levelEndRectangle, player)) {
            // Load a new level if the player collides with the level end rectangle
            String newLevelFilePath = "/Levels/level2.csv";
            loadNewLevel(newLevelFilePath);
        }

        g2d.translate(camX, camY); // Reset translation
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
            Game game = new Game("/Levels/level1.csv");
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