import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainMenu extends JPanel { 
    private JButton newGameButton;
    private JButton loadGameButton; // added button
    private Image backgroundImage = new ImageIcon(getClass().getResource("/background.jpg")).getImage();

    public MainMenu() {

        setLayout(new BorderLayout());

        // Create a title panel and add it to the main menu
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Platform Game");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 72));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Create a panel for the buttons and add it to the main menu
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        buttonPanel.setOpaque(false);

        newGameButton = createButton("New Game", 300, 100);
        newGameButton.addActionListener(e -> startNewGame());
        buttonPanel.add(newGameButton);

        JButton optionsButton = createButton("Options", 300, 100);
        optionsButton.addActionListener(e -> showOptions());
        buttonPanel.add(optionsButton);

        // Replace high scores button with load game button
        loadGameButton = createButton("Load Game", 300, 100);
        loadGameButton.addActionListener(e -> Game.loadGame()); // call loadGame() method
        buttonPanel.add(loadGameButton);

        JButton exitButton = createButton("Exit", 300, 100);
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    // 2. Add this method to draw the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void startNewGame() {
        // Create a new Game instance here instead
        Game game = new Game("/Levels/level1.json"); // Initialize the game when needed
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.remove(this);
        frame.add(game);
        game.requestFocus();
        frame.pack();
        frame.setVisible(true); // Make sure the frame is visible again if needed
    }

    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Montserrat", Font.BOLD, 36));
        button.setForeground(Color.WHITE);
        // Apply a background color
        button.setOpaque(true);
        button.setBackground(new Color(60, 63, 65)); // Dark background
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(20)); // Adjust the radius as needed
        button.setPreferredSize(new Dimension(width, height));
    
        // Mouse listener for hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.YELLOW); // Change text color on hover
                button.setBackground(new Color(85, 83, 85)); // Lighter background on hover
            }
    
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(60, 63, 65)); // Original background color
            }
        });
        
        return button;
    }
    

    private void showOptions() {
        // code for options dialog
    }

    public class RoundedBorder implements Border {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        }
    
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius / 2, this.radius / 2, this.radius / 2, this.radius / 2);
        }
    
        public boolean isBorderOpaque() {
            return false;
        }

    }
}