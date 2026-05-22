package src.main.java.com.railshooter.entities;

/**
 * PATTERN: Prototype — пуля клонирует себя.
 * Расширена типами: NORMAL, PIERCING, EXPLOSIVE
 */
public class Bullet implements Cloneable {
    public float x, y;
    public float velocityX, velocityY;
    public boolean active = true;
    public int damage = 1;
    public BulletType type = BulletType.NORMAL;

    public Bullet(float x, float y, float velocityX, float velocityY) {
        this.x = x; this.y = y;
        this.velocityX = velocityX; this.velocityY = velocityY;
    }

    public Bullet(float x, float y, float velocityX, float velocityY, BulletType type) {
        this(x, y, velocityX, velocityY);
        this.type = type;
        if (type == BulletType.EXPLOSIVE) this.damage = 2;
    }

    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
    }

    @Override
    public Bullet clone() {
        try { return (Bullet) super.clone(); }
        catch (CloneNotSupportedException e) {
            return new Bullet(x, y, velocityX, velocityY, type);
        }
    }
}
