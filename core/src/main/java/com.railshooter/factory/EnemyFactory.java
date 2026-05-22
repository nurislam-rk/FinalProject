package src.main.java.com.railshooter.factory;

import com.railshooter.entities.enemies.Enemy;

/**
 * PATTERN: Factory Method — абстрактная фабрика врагов
 */
public abstract class EnemyFactory {
    public abstract Enemy create(float x, float y);
}
