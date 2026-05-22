package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.Bullet;
import java.util.Collections;
import java.util.List;

/** PATTERN: Strategy — одиночный выстрел */
public class SingleShotStrategy implements ShootingStrategy {
    private static final float SPEED = 500f;

    @Override
    public List<Bullet> shoot(float ox, float oy, float tx, float ty) {
        float dx = tx - ox, dy = ty - oy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1f) { dy = 1f; len = 1f; }
        return Collections.singletonList(new Bullet(ox, oy, dx/len*SPEED, dy/len*SPEED));
    }
}
