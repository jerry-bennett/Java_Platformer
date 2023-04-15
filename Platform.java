import java.awt.Color;

public class Platform {
    private int x;
    private int y;
    private int width;
    private int height;
    private Color color;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public boolean collidesWith(Player player) {
        int playerX = player.getX();
        int playerY = player.getY();
        int playerWidth = player.getWidth();
        int playerHeight = player.getHeight();

        if (playerY + playerHeight <= y) {
            // player is completely above platform
            return false;
        }

        if (playerY >= y + height) {
            // player is completely below platform
            return false;
        }

        if (playerX + playerWidth <= x) {
            // player is completely to the left of platform
            return false;
        }

        if (playerX >= x + width) {
            // player is completely to the right of platform
            return false;
        }

        // player collides with platform
        return true;
    }
}
