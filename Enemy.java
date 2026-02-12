import java.awt.Rectangle;

public class Enemy {
    private int x, y, width, height;
    private int xVelocity = 2;
    private int yVelocity = 0;
    private boolean movingRight = true;

    public Enemy(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- GETTERS AND SETTERS ---
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getXVelocity() { return xVelocity; }
    public void setXVelocity(int xVelocity) { this.xVelocity = xVelocity; }
    public int getYVelocity() { return yVelocity; }
    public void setYVelocity(int yVelocity) { this.yVelocity = yVelocity; }
    public boolean isMovingRight() { return movingRight; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    
    public Rectangle getEdgeSensor() {
       int sensorX = movingRight ? x + width + 20 : x - 30;
        return new Rectangle(sensorX, y + height + 5, 20, 10);
    }
}