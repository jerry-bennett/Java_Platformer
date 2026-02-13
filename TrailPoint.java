public class TrailPoint {
        float alpha; // 1.0 is fresh, 0.0 is gone
        float size;
        float x, y, vx, vy;
        public TrailPoint(int x, int y, int startSize) {
            this.x = x + (float)(Math.random() * 20 - 10); // Random offset so it's not a stiff line
            this.y = y;
            this.vx = (float)(Math.random() * 2 - 1);      // Random horizontal drift
            this.vy = (float)(Math.random() * -2 - 1);     // Upward speed
            this.alpha = 1.0f;
            this.size = (float)(Math.random() * 15 + 10);  // Varied particle sizes
        }
        public void update() {
            this.x += vx;
            this.y += vy;
            this.alpha -= 0.04f; 
            this.size -= 0.5f;
        }
    }