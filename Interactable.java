import java.awt.Rectangle;

public class Interactable {
    public int x, y, width, height;
    public String message;
    public boolean isPlayerNear = false;
    public boolean isDialogOpen = false;
    public int visibleChars = 0;
    public int typeTimer = 0;
    public final int TYPE_SPEED = 2;

    public Interactable(int x, int y, int width, int height, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = message;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}