package src.main.java.com.railshooter.entities;

/** Бонус, выпадающий из врага */
public class DropItem {
    public enum Type { HEALTH, SHIELD, SPEED }

    public float x, y;
    public Type type;
    public boolean collected = false;
    private float bobTimer = 0f;
    public float drawY;

    public DropItem(float x, float y, Type type) {
        this.x = x; this.y = y; this.drawY = y;
        this.type = type;
    }

    public void update(float delta) {
        // Медленное падение вниз + плавное покачивание
        y -= 55f * delta;
        bobTimer += delta * 3f;
        drawY = y + (float)Math.sin(bobTimer) * 5f;
    }
}
