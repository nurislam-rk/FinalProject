package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.Bullet;
import java.util.ArrayList;
import java.util.List;

/** PATTERN: Strategy — веерный выстрел (3 пули) */
public class SpreadShotStrategy implements ShootingStrategy {
    private static final float SPEED = 480f;

    @Override
    public List<Bullet> shoot(float ox, float oy, float tx, float ty) {
        float dx = tx - ox, dy = ty - oy;
        float baseAngle = (float)Math.atan2(dy, dx);
        float[] offsets = { -0.22f, 0f, 0.22f };
        List<Bullet> bullets = new ArrayList<>();
        for (float off : offsets) {
            float a = baseAngle + off;
            bullets.add(new Bullet(ox, oy, (float)Math.cos(a)*SPEED, (float)Math.sin(a)*SPEED));
        }
        return bullets;
    }
}
