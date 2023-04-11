import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JPanel {
    private JButton newGameButton;
    private Game game;

    public MainMenu(Game game) {
        this.game = game;

        setPreferredSize(new Dimension(500, 500));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("My Platformer Game");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel);
        add(Box.createVerticalStrut(50));
        newGameButton = new JButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 24));
        newGameButton.addActionListener(e -> startNewGame());
        add(newGameButton);
    }

    private void startNewGame() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.remove(this);
        frame.add(game);
        game.requestFocus();
        frame.pack();
    }
}
