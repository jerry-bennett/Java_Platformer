import java.awt.Color;

public class TrailPoint {
    public float alpha; 
    public float size;
    public float x, y, vx, vy;

    public TrailPoint(int x, int y, int startSize, int playerWidth, float playerVX) {
        // Center the spawn on the player with some random spread
        this.x = x + (float)(Math.random() * playerWidth); 
        this.y = y;

        // TILT LOGIC: 
        float tilt = -playerVX * 0.2f; 
        
        this.vx = tilt + (float)(Math.random() * 2 - 1); // Drift + Randomness
        this.vy = (float)(Math.random() * -2 - 1);     
        this.alpha = 1f;
        this.size = (float)(Math.random() * (startSize / 2) + 5);  
    }

    public void update() {
        this.x += vx + (Math.random() * 2 - 1);
        this.y += vy;
        this.alpha -= 0.015f; 
        this.size -= 0.3f;
    }

    public Color getColor() {
        int r = 255;
        int g = (int) (255 * alpha); // High green + high red = Yellow
        int b = 0;
        
        return new Color(r, g, b);
    }
}