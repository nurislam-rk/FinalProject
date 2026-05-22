package src.main.java.com.railshooter.entities;

import com.railshooter.entities.enemies.Enemy;
import java.util.List;

/**
 * Сокомандник.
 * - Следует за игроком.
 * - Автоматически стреляет по ближайшему врагу (пули добавляются в общий список teammateBullets).
 * - Имеет хитбокс: принимает урон от врагов (см. EnemyPlayerHandler).
 * - Макс. количество в игре ограничено в TeammatePickupHandler.MAX_TEAMMATES = 1.
 *
 * PATTERN: нет отдельного, но используется Strategy-образное поведение (update/shootAt).
 */
public class Teammate {

    public static final int   MAX_HEALTH      = 3;
    private static final float SHOOT_INTERVAL = 1.2f;
    /** Радиус хитбокса (полуширина) */
    public  static final float HIT_RADIUS     = 14f;

    public float x, y;
    public boolean active = true;
    public int health = MAX_HEALTH;

    private float shootCooldown = 0f;

    /** Ссылка на общий список пуль (teammateBullets из GameScreen) */
    private final List<Bullet> bullets;

    public enum TeammateType { MINER, ENGINEER, SCOUT }
    public final TeammateType teammateType;

    public Teammate(float x, float y, List<Bullet> sharedBullets) {
        this.x       = x;
        this.y       = y;
        this.bullets = sharedBullets;
        int t = (int) (Math.random() * 3);
        this.teammateType = TeammateType.values()[t];
    }

    public void update(float delta, float playerX, float playerY, List<Enemy> enemies) {
        // Следует за игроком (смещение вправо на 35px)
        float followX = playerX + 35f;
        float dx = followX - x;
        float dy = playerY  - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 10) {
            float spd = 200f;
            x += dx / dist * spd * delta;
            y += dy / dist * spd * delta;
        }

        // Авто-прицел и стрельба по ближайшему врагу
        shootCooldown -= delta;
        if (shootCooldown <= 0 && !enemies.isEmpty()) {
            Enemy nearest  = null;
            float minDist  = Float.MAX_VALUE;
            for (Enemy e : enemies) {
                float d = (float) Math.sqrt((e.x - x) * (e.x - x) + (e.y - y) * (e.y - y));
                if (d < minDist) { minDist = d; nearest = e; }
            }
            if (nearest != null) {
                shootAt(nearest.x, nearest.y);
                shootCooldown = SHOOT_INTERVAL;
            }
        }

        // Чистим неактивные пули из общего списка
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update(delta);
    }

    private void shootAt(float tx, float ty) {
        float dx  = tx - x;
        float dy  = ty - y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        float spd = 480f;
        // Добавляем в общий список — BulletEnemyHandler его обработает
        bullets.add(new Bullet(x, y + 20, dx / len * spd, dy / len * spd));
    }

    /**
     * Урон союзнику. Вызывается из EnemyPlayerHandler.
     */
    public void takeDamage(int dmg) {
        health -= dmg;
        if (health <= 0) active = false;
    }
}
