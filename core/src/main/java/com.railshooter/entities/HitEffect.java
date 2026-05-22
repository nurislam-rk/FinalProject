package src.main.java.com.railshooter.entities;

public class HitEffect {
    public static final float MAX_LIFE = 0.28f;
    public float x, y, life;
    public boolean explosive;
    public HitEffect(float x, float y) { this.x = x; this.y = y; this.life = MAX_LIFE; }
    public HitEffect(float x, float y, boolean explosive) {
        this(x, y); this.explosive = explosive;
    }
}
