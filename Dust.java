public class Dust {
    double x, y, vx, vy;
    int size;
    float alpha = 1.0f;

    public Dust(int x, int y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = (int)(Math.random() * 5) + 2; // Small 2-6px specks
    }

    public void update() {
        x += vx;
        y += vy;
        alpha -= 0.01f; 
    }
}