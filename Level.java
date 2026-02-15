import java.util.ArrayList;

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
    
    }
    
