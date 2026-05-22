package src.main.java.com.railshooter.entities.enemies;

import com.railshooter.entities.Bullet;
import com.railshooter.strategy.StraightMovement;

/**
 * Босс — появляется каждые 5 волн.
 * Большой, много HP, стреляет тройным залпом.
 */
public class BossEnemy extends Enemy {
    private float spreadTimer = 0f;

    public BossEnemy(float x, float y, int waveNumber) {
        super(x, y, 20 + waveNumber * 5, 200, 2.5f);
        setMovementStrategy(new StraightMovement(40f));
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        super.update(delta, playerX, playerY);
        // Дополнительный спред-залп каждые 5 секунд
        spreadTimer -= delta;
        if (spreadTimer <= 0) {
            spreadTimer = 5f;
            fireSpread(playerX, playerY);
        }
    }

    private void fireSpread(float px, float py) {
        float dx = px - x, dy = py - y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        float spd = 220f;
        float[] angles = { -20f, 0f, 20f };
        for (float deg : angles) {
            float rad = (float)(Math.atan2(dy, dx) + Math.toRadians(deg));
            getEnemyBullets().add(new Bullet(x, y,
                (float)Math.cos(rad) * spd, (float)Math.sin(rad) * spd));
        }
    }

    @Override public String getType() { return "Boss"; }
}
