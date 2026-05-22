package src.main.java.com.railshooter.entities;

/** Препятствие в тоннеле (камень или балка) */
public class Obstacle {
    public enum ObstacleType { ROCK, BEAM }
    public float x, y;
    public float width, height;
    public ObstacleType type;
    public boolean active = true;

    public Obstacle(float x, float y, float width, float height, ObstacleType type) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
        this.type = type;
    }

    public void update(float delta, float speed) {
        y -= speed * delta;
        if (y + height < -20) active = false;
    }
}
