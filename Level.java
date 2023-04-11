import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Level {
    private ArrayList<Platform> platforms;

    public Level() {
        this.platforms = new ArrayList<>();
    }

    public ArrayList<Platform> getPlatforms() {
        return this.platforms;
    }

    public void addPlatform(Platform platform) {
        this.platforms.add(platform);
    }

    public void loadPlatformsFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                System.out.println(Arrays.toString(data)); // Add this line
                int x = Integer.parseInt(data[0]);
                int y = Integer.parseInt(data[1]);
                int width = Integer.parseInt(data[2]);
                int height = Integer.parseInt(data[3]);
                Platform platform = new Platform(x, y, width, height);
                this.addPlatform(platform);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    }
    
