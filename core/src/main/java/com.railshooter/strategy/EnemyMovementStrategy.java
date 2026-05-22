package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.enemies.Enemy;

/**
 * PATTERN: Strategy — интерфейс движения врага
 */
public interface EnemyMovementStrategy {
    void move(Enemy enemy, float delta, float playerX, float playerY);
}
