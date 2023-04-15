import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Game extends JPanel implements KeyListener {
    private int x = 50;
    private int y = 50;
    private final int size = 50;
    private boolean onGround = false;

    private final int MAX_JUMP_HEIGHT = 100;
    private final int JUMP_SPEED = 10;
    private final int MOVE_SPEED = 5;
    private final int GRAVITY = 1;
    private final String levelFilePath;
    private LevelEndRectangle levelEndRectangle = new LevelEndRectangle(450, 0, 50, 500);
    private int currentLevel = 1;


    private Player player = new Player(50, 50, 50, 50); // adjust the values as needed

    private List<Platform> platforms = new ArrayList<>();

    public Game(String levelFilePath) {
        this.levelFilePath = levelFilePath;
        setFocusable(true);
        setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);
        setFocusable(true);
        addKeyListener(this);
        Timer timer = new Timer(10, e -> move());
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
            }
            scanner.close();
        } catch (NullPointerException e) {
            System.err.println("Could not find file: " + levelFilePath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean isCollidingWithPlatform(Platform platform) {
        Rectangle playerRect = player.getBounds();
        Rectangle platformRect = platform.getBounds();
    
        // Check for horizontal collision
        if (playerRect.intersects(platformRect)) {
            int playerBottom = playerRect.y + playerRect.height;
            int platformTop = platformRect.y;
            
            // Check for vertical collision
            if (playerBottom > platformTop && playerRect.y < platformTop) {
                return true;
            }
        }
    
        return false;
    }

    private static boolean isPlayerCollidingWithLevelEnd(LevelEndRectangle levelEnd, Player player){
        var playerBounds = new Rectangle(player.getBounds());
        var levelEndBounds = new Rectangle(levelEnd.getBounds());
        return playerBounds.intersects(levelEndBounds);
    }
    
    
    private int getPlatformTop(int platformY, int platformHeight) {
        return platformY - player.getHeight() - 1;
    }

    private void loadNewLevel(String newLevelFilePath) {
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
                int platformTop = getPlatformTop(platform.getY(), platform.getHeight());
                player.setY(platformTop - 1);  // move the player to the top of the platform
                player.setYVelocity(0);        // set the vertical velocity to zero
                onGround = true;
                if (platform.getColor() == Color.GREEN) {
                    // Load a new level if the player collides with a green platform
                    String newLevelFilePath = "/Levels/level2.csv";
                    removeKeyListener(this); // remove the current KeyListener
                    Game newGame = new Game(newLevelFilePath);
                    newGame.addKeyListener(this); // add the KeyListeners to the new instance of the Game class
                    newGame.requestFocus(); 
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    frame.setContentPane(newGame);
                    frame.pack();
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


    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_W:
                // do nothing
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                player.setXVelocity(0);
                break;
            case KeyEvent.VK_S:
                // do nothing
                break;
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        displayMainMenu();
    }

    private static void displayMainMenu() {
        JFrame frame = new JFrame("Main Menu");
        MainMenu mainMenu = new MainMenu(new Game("/Levels/level1.csv"));
        frame.add(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void startGame() {
        Game game = new Game("/Levels/level1.csv"); // or pass in a file path if you have a default level file
        JFrame frame = new JFrame("My Game");
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
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
