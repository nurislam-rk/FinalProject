package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.Bullet;
import com.railshooter.entities.BulletType;
import java.util.Collections;
import java.util.List;

/** PATTERN: Strategy — взрывная пуля (медленнее, но AOE) */
public class ExplosiveBulletStrategy implements ShootingStrategy {
    private static final float SPEED = 380f;

    @Override
    public List<Bullet> shoot(float ox, float oy, float tx, float ty) {
        float dx = tx - ox, dy = ty - oy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1) { dy = 1; len = 1; }
        Bullet b = new Bullet(ox, oy, dx/len*SPEED, dy/len*SPEED, BulletType.EXPLOSIVE);
        return Collections.singletonList(b);
    }
}
