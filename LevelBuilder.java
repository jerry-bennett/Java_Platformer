import java.awt.Point;
import java.util.ArrayList;
import java.awt.Rectangle;

public class LevelBuilder {
    public static final int[][] platform_data = {
        {0, 400, 400, 50},
        {600, 400, 400, 50},
        {0, 200, 200, 50},
        {400, 200, 200, 50},
        {800, 200, 200, 50}
    };
    private ArrayList<Point> platforms;

    public LevelBuilder() {
        platforms = new ArrayList<Point>();
    }

    public void buildPlatforms(Game Game) {
        for (int i = 0; i < platform_data.length; i++) {
            int x = platform_data[i][0];
            int y = platform_data[i][1];
            int width = platform_data[i][2];
            int height = platform_data[i][3];
            Game.addPlatform(new Platform(x, y, width, height));
        }
    }

    public void addPlatform(Point p) {
        platforms.add(p);
    }

    public void removePlatform(Point p) {
        platforms.remove(p);
    }

    public ArrayList<Point> getPlatforms() {
        return platforms;
    }
}
