package src.main.java.com.railshooter.factory;

import com.railshooter.entities.enemies.Bat;
import com.railshooter.entities.enemies.Enemy;

/** PATTERN: Factory Method — фабрика летучих мышей */
public class BatFactory extends EnemyFactory {
    @Override
    public Enemy create(float x, float y) { return new Bat(x, y); }
}
