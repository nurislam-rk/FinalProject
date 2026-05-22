package src.main.java.com.railshooter.entities;

/** Пикап для подбора сокомандника */
public class TeammatePickup {
    public float x, y;
    public boolean collected = false;
    private float bobTimer = 0f;
    public float drawY;

    public TeammatePickup(float x, float y) {
        this.x = x; this.y = y; this.drawY = y;
    }

    public void update(float delta) {
        y -= 50f * delta;
        bobTimer += delta * 2.5f;
        drawY = y + (float)Math.sin(bobTimer) * 6f;
    }
}
