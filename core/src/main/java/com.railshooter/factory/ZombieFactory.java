package src.main.java.com.railshooter.factory;

import com.railshooter.entities.enemies.Enemy;
import com.railshooter.entities.enemies.Zombie;

/** PATTERN: Factory Method — фабрика зомби */
public class ZombieFactory extends EnemyFactory {
    @Override
    public Enemy create(float x, float y) { return new Zombie(x, y); }
}
