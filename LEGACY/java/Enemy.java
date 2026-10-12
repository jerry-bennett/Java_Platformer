import java.awt.Rectangle;

public class Enemy {
    private int x, y, width, height;
    private int xVelocity = 3;
    private int yVelocity = 0;
    private boolean movingRight = true;
    private int startX, startY;
    private int health = 3;
    private int maxHealth = 3;
    private int hurtTimer = 0;

    public Enemy(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.width = width;
        this.height = height;
    }

    public void respawn() {
        this.x = startX;
        this.y = startY;
        this.yVelocity = 0;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        this.hurtTimer = 20; // Stun them for 20 frames
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
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getHurtTimer() { return hurtTimer; }

    public void tickHurtTimer() { if(hurtTimer > 0) hurtTimer--; }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    
    public Rectangle getEdgeSensor() {
       int sensorX = movingRight ? x + width : x - 10;
        return new Rectangle(sensorX, y + height + 2, 10, 10);
    }
}