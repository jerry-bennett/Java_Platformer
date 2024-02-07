import java.awt.Rectangle;

public class Player {
    private int x;
    private float y;
    private int width;
    private int height;
    private int xVelocity;
    private float yVelocity;
    private Rectangle hitbox;

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(float newY) {
        this.y = newY;
    }

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.hitbox = new Rectangle(x, y, width, height);  // Initialize the hitbox
    }

    public void update() {
        x += xVelocity;
        y += yVelocity;

        // Update the hitbox position based on player's position
        hitbox.setLocation(x, (int) y);
    }

    public void setXVelocity(int xVelocity) {
        this.xVelocity = xVelocity;
    }

    public void setYVelocity(float yVelocity) {
        this.yVelocity = yVelocity;
    }

    public Rectangle getBounds() {
        return hitbox;
    }

    public int getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXVelocity() {
        return xVelocity;
    }

    public float getYVelocity() {
        return yVelocity;
    }

    public void setYVelocity(double d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setYVelocity'");
    }
}
