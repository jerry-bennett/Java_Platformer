import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

public class MainMenu extends JPanel { 
    private JButton newGameButton;
    private Game game;

    public MainMenu(Game game) {
        this.game = game;

        setLayout(new BorderLayout());

        ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/background.jpg"));
        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setOpaque(false);
        backgroundPanel.add(new JLabel(backgroundImage), BorderLayout.CENTER);
        add(backgroundPanel);

        // Create a title panel and add it to the main menu
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("My Platformer Game");
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

        JButton highScoresButton = createButton("High Scores", 300, 100);
        highScoresButton.addActionListener(e -> showHighScores());
        buttonPanel.add(highScoresButton);

        JButton exitButton = createButton("Exit", 300, 100);
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private Object showHighScores() {
        return null;
    }

    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Montserrat", Font.BOLD, 36));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(10)); // Set the radius of the rounded corners to 10 pixels
        button.setPreferredSize(new Dimension(width, height));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.RED);
            }
    
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }
        });
        return button;
    }
    

    private void startNewGame() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.remove(this);
        frame.add(game);
        game.requestFocus();
        frame.pack();
    }

    private void showOptions() {
        JFrame frame = new JFrame("Options");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        frame.setResizable(false);
    
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 10, 10, 10);
    
        // Create a label for the volume slider
        JLabel volumeLabel = new JLabel("Volume:");
        panel.add(volumeLabel, c);
    
        // Create a slider for the volume
        c.gridx = 1;
        JSlider volumeSlider = new JSlider(0, 100, (int) (game.getVolume() * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100f;
            game.setVolume(volume);
        });
        panel.add(volumeSlider, c);
    
        // Add some padding between the volume slider and the OK button
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        panel.add(Box.createVerticalStrut(20), c);
    
        // Create a button to close the options dialog
        c.gridy = 2;
        c.gridwidth = 1;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> frame.dispose());
        panel.add(okButton, c);
    
        // Create a button to reset the high score
        c.gridx = 1;
        JButton resetButton = new JButton("Reset High Score");
        resetButton.addActionListener(e -> {
            game.resetHighScore();
            JOptionPane.showMessageDialog(frame, "High score reset to 0.", "High Score Reset", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(resetButton, c);
    
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
    
    /**
     * A class that implements the ActionListener interface to handle button clicks.
     */
    // private class ButtonHandler implements ActionListener {
    //     public void actionPerformed(ActionEvent e) {
    //         if (e.getSource() == startButton) {
    //             startNewGame();
    //         } else if (e.getSource() == optionsButton) {
    //             showOptions();
    //         } else if (e.getSource() == exitButton) {
    //             System.exit(0);
    //         }
    //     }
    // }
}    