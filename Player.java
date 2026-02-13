import java.awt.Rectangle;

public class Player {
    private int x;
    private int y;
    private int width;
    private int height;
    private int xVelocity;
    private int yVelocity;
    private double scaleX = 1.0;
    private double scaleY = 1.0;

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;
    }

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xVelocity = 0;
        this.yVelocity = 0;
    }

    public void update() {
        x += xVelocity;
        y += yVelocity;
    }

    public void setXVelocity(int xVelocity) {
        this.xVelocity = xVelocity;
    }

    public void setYVelocity(int yVelocity) {
        this.yVelocity = yVelocity;
    }

    public Rectangle getBounds() {
    return new Rectangle((int)x, (int)y, width, height);
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

    public int getXVelocity() {
        return xVelocity;
    }

    public int getYVelocity() {
        return yVelocity;
    }

    public void setScale(double sx, double sy) {
        this.scaleX = sx;
        this.scaleY = sy;
    }

    public double getScaleX(){
        return scaleX;
    }

    public double getScaleY(){
        return scaleY;
    }

    // squish logic for wall jumping
    public void updateAnimation() {
        scaleX += (1.0 - scaleX) * 0.1; 
        scaleY += (1.0 - scaleY) * 0.1;
    }
    
    
}