package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.Bullet;
import java.util.List;

/**
 * PATTERN: Strategy — интерфейс стратегии стрельбы
 */
public interface ShootingStrategy {
    List<Bullet> shoot(float originX, float originY, float targetX, float targetY);
}
