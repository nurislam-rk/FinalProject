package src.main.java.com.railshooter.factory;

import com.railshooter.entities.enemies.Enemy;
import com.railshooter.entities.enemies.Spider;

/** PATTERN: Factory Method — фабрика пауков */
public class SpiderFactory extends EnemyFactory {
    @Override
    public Enemy create(float x, float y) { return new Spider(x, y); }
}
