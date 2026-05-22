package src.main.java.com.railshooter.entities.enemies;

import com.railshooter.entities.Bullet;
import com.railshooter.strategy.EnemyMovementStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Factory Method — базовый класс врага.
 * Теперь враги могут стрелять в игрока.
 */
public abstract class Enemy {
    public float x, y;
    public int health, maxHealth;
    public int points;
    public boolean alive = true;
    protected EnemyMovementStrategy movementStrategy;

    // Стрельба врага
    private float shootTimer;
    private final float shootInterval;
    private final List<Bullet> enemyBullets = new ArrayList<>();

    public Enemy(float x, float y, int health, int points, float shootInterval) {
        this.x = x; this.y = y;
        this.health = health; this.maxHealth = health;
        this.points = points;
        this.shootInterval = shootInterval;
        this.shootTimer = shootInterval * 0.5f; // небольшая задержка первого выстрела
    }

    public void setMovementStrategy(EnemyMovementStrategy s) { this.movementStrategy = s; }

    public void update(float delta, float playerX, float playerY) {
        if (movementStrategy != null)
            movementStrategy.move(this, delta, playerX, playerY);

        if (shootInterval > 0) {
            shootTimer -= delta;
            if (shootTimer <= 0) {
                shootTimer = shootInterval;
                fireAt(playerX, playerY);
            }
        }
        enemyBullets.removeIf(b -> !b.active);
        for (Bullet b : enemyBullets) b.update(delta);
    }

    protected void fireAt(float px, float py) {
        float dx = px - x, dy = py - y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        float spd = 200f;
        enemyBullets.add(new Bullet(x, y, dx / len * spd, dy / len * spd));
    }

    public List<Bullet> getEnemyBullets() { return enemyBullets; }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health <= 0) alive = false;
    }

    public abstract String getType();
}
