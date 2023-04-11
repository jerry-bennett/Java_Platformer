import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JPanel implements KeyListener {
    private int x = 50;
    private int y = 50;
    private final int size = 50;
    private int dx = 0;
    private int dy = 0;
    private final int gravity = 1;
    private boolean onGround = false;
    private final int platformX = 200;
    private final int platformY = 200;
    private final int platform2X = 200;
    private final int platform2Y = 300;
    private final int platformWidth = 200;
    private final int platformHeight = 20;

    private boolean jumping = false;
    private int jumpCounter = 0;
    private Image offScreenImage;

    public Game() {
        setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);
        setFocusable(true);
        Timer timer = new Timer(10, e -> move());
        timer.start();
    }

    private void move() {
        x += dx;
        y += dy;
        if (!onGround) {
            dy += gravity;
        }
        if (y + size >= getHeight()) {
            y = getHeight() - size;
            dy = 0;
            onGround = true;
        } else if (dy >= 0 && y + size >= platformY && y + dy < platformY + platformHeight
                && x + size > platformX && x < platformX + platformWidth) {
            y = platformY - size;
            dy = 0;
            onGround = true;
        } else if (dy >= 0 && y + size >= platform2Y && y + dy < platform2Y + platformHeight
                && x + size > platform2X && x < platform2X + platformWidth) {
            y = platform2Y - size;
            dy = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        if (x + size > platformX && x < platformX + platformWidth && y + size >= platformY && y < platformY + platformHeight && dy < 0) {
            dy = -dy;
            y = platformY + platformHeight;
        } else if (x + size > platform2X && x < platform2X + platformWidth && y + size >= platform2Y && y < platform2Y + platformHeight && dy < 0) {
            dy = -dy;
            y = platform2Y + platformHeight;
        }
        if (x < 0) { // player hits left boundary of the window
            x = 0;
            dx = 0;
        } else if (x + size > getWidth()) { // player hits right boundary of the window
            x = getWidth() - size;
            dx = 0;
        }
        repaint();
    }
    
    
    
    

    private void jump() {
        if (onGround) {
            jumping = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (offScreenImage == null) {
            offScreenImage = createImage(getWidth(), getHeight());
        }
        Graphics offScreenGraphics = offScreenImage.getGraphics();
        offScreenGraphics.setColor(Color.BLACK);
        offScreenGraphics.fillRect(0, 0, getWidth(), getHeight()); // clear the image
        offScreenGraphics.setColor(Color.BLUE);
        offScreenGraphics.fillRect(platformX, platformY, platformWidth, platformHeight);
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.fillRect(x, y, size, size);
        offScreenGraphics.setColor(Color.BLUE);
        offScreenGraphics.fillRect(platform2X, platform2Y, platformWidth, platformHeight);

        g.drawImage(offScreenImage, 0, 0, null);
    }




    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_W:
                if (onGround) {
                    dy = -20;
                    onGround = false;
                    jumping = true; // set jumping to true when player jumps
                }
                break;
            case KeyEvent.VK_A:
                dx = -5;
                break;
            case KeyEvent.VK_S:
                // do nothing
                break;
            case KeyEvent.VK_D:
                dx = 5;
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
                dx = 0;
                break;
            case KeyEvent.VK_S:
                // do nothing
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("White Square");
        Game Game = new Game();
        MainMenu mainMenu = new MainMenu(Game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainMenu);
        frame.setResizable(false); // set the resizable property to false
        frame.pack();
        frame.setVisible(true);
    }
    
}
