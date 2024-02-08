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

    private final int JUMP_SPEED = 10;
    private final int MOVE_SPEED = 5;
    private final int GRAVITY = 1;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean levelLoaded = false;
    private LevelEndRectangle levelEndRectangle = new LevelEndRectangle(450, 0, 50, 500);
    private Player player = new Player(50, 50, 50, 50); // adjust the values as needed

    private List<Platform> platforms = new ArrayList<>();

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
                levelLoaded = true;
            }
            scanner.close();
        } catch (NullPointerException e) {
            System.err.println("Could not find file: " + levelFilePath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private enum CollisionSide {
        LEFT, RIGHT, TOP, BOTTOM, NONE
    }
    
    private boolean isCollidingWithPlatform(Platform platform) {
        Rectangle playerBounds = player.getBounds();
        Rectangle platformBounds = platform.getBounds();
    
        if (playerBounds.intersects(platformBounds)) {
            CollisionSide collisionSide = determineCollisionSide(playerBounds, platformBounds);
            handleCollision(collisionSide, platform);
            return true;
        }
        return false;
    }
    
    private CollisionSide determineCollisionSide(Rectangle playerBounds, Rectangle platformBounds) {
        float playerLeft = playerBounds.x;
        float playerRight = playerBounds.x + playerBounds.width;
        float playerTop = playerBounds.y;
        float playerBottom = playerBounds.y + playerBounds.height;

        float platformLeft = platformBounds.x;
        float platformRight = platformBounds.x + platformBounds.width;
        float platformTop = platformBounds.y;
        float platformBottom = platformBounds.y + platformBounds.height;

        boolean collidingFromLeft = playerRight > platformLeft && playerLeft < platformLeft && player.getXVelocity() > 0;
        boolean collidingFromRight = playerLeft < platformRight && playerRight > platformRight && player.getXVelocity() < 0;
        boolean collidingFromTop = playerBottom > platformTop && playerTop < platformTop && player.getYVelocity() > 0;
        boolean collidingFromBottom = playerTop < platformBottom && playerBottom > platformBottom && player.getYVelocity() < 0;

        // Prioritize vertical collisions due to gravity's constant effect
        if (collidingFromTop && !collidingFromBottom) {
            return CollisionSide.TOP;
        } else if (!collidingFromTop && collidingFromBottom) {
            return CollisionSide.BOTTOM;
        } else if (collidingFromLeft && !collidingFromRight) {
            return CollisionSide.LEFT;
        } else if (!collidingFromLeft && collidingFromRight) {
            return CollisionSide.RIGHT;
        }

        return CollisionSide.NONE; // If no clear side of collision is detected

    }
    
    private void handleCollision(CollisionSide collisionSide, Platform platform) {
        switch (collisionSide) {
            case LEFT:
                // Handle collision on the left side
                player.setXVelocity(0);
                break;
            case RIGHT:
                // Handle collision on the right side
                player.setXVelocity(0);
                break;
            case TOP:
                player.setYVelocity(0);  // Example of handling top side collision
                break;
            case BOTTOM:
                // Handle collision on the bottom side
                player.setY(platform.getY() + platform.getHeight());
                player.setYVelocity(0);
                onGround = true;
                break;
            default:
                // No collision handling needed
                break;
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
        // Update player position based on current velocity
        player.setX(player.getX() + player.getXVelocity());
        player.setY(player.getY() + player.getYVelocity());
    
        // Check for collisions with platforms
        boolean collided = false;
        for (Platform platform : platforms) {
            if (isCollidingWithPlatform(platform)) {
                collided = true;
                //put the player on top of the platform
                if(bottom = true){

                }else if (top = true){

                }
                if (isPlayerCollidingWithLevelEnd(levelEndRectangle, player)) {
                    // Load a new level if the player collides with a green platform
                    String newLevelFilePath = "/Levels/level2.csv";
                    loadNewLevel(newLevelFilePath);
                }
                break;
            }
        }
    
        // Check if player reached the bottom of the game window
        if (player.getY() + player.getHeight() >= getHeight()) {
            onGround = true;
        }
    
        // Apply gravity if not on a platform
        if (!collided) {
            if (onGround) {
                player.setYVelocity(0); // set the vertical velocity to zero if on the ground
            } else {
                player.setYVelocity(player.getYVelocity() + GRAVITY); // apply gravity if not on the ground
            }
            onGround = false;
        }
    
        // Check boundaries
        if (player.getX() < 0) { // player hits left boundary of the window
            player.setX(0);
            player.setXVelocity(0);
        } else if (player.getX() + player.getWidth() > getWidth()) { // player hits right boundary of the window
            player.setX(getWidth() - player.getWidth());
            player.setXVelocity(0);
        }
    
        repaint();
    }        
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        if (isPlayerCollidingWithLevelEnd(levelEndRectangle, player)) {
            // Load a new level if the player collides with the level end rectangle
            String newLevelFilePath = "/Levels/level2.csv";
            loadNewLevel(newLevelFilePath);
        }
    }

@Override
public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
        case KeyEvent.VK_W:
            // Only jump if on the ground and not already pressing jump
            // if(onGround == false){
            //     break;
            // }
            player.setYVelocity(-JUMP_SPEED);
            onGround = false;
            jumpPressed = true;
            System.out.println("Jump");
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

public void keyReleased(KeyEvent e) {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
        case KeyEvent.VK_W:
            jumpPressed = false;
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

@Override
public void keyTyped(KeyEvent e) {
    int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_W:
            System.out.println("Jumping");
            player.setYVelocity(-JUMP_SPEED);
            onGround = false;
                break;
            case KeyEvent.VK_A:
                player.setXVelocity(-MOVE_SPEED);
                break;
            case KeyEvent.VK_S:
                // do nothing
                break;
            case KeyEvent.VK_D:
                player.setXVelocity(MOVE_SPEED);
                break;
            case KeyEvent.VK_ESCAPE:
                // stop the game
                System.exit(0);
                break;
        }
}


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

    public void setVolume(float volume) {
    }

    public int getVolume() {
        return 0;
    }

    public void resetHighScore() {
    }

}