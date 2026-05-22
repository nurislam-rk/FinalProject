package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.enemies.Enemy;

/** PATTERN: Strategy — прямолинейное движение вниз */
public class StraightMovement implements EnemyMovementStrategy {
    private final float speed;
    public StraightMovement(float speed) { this.speed = speed; }

    @Override
    public void move(Enemy enemy, float delta, float playerX, float playerY) {
        enemy.y -= speed * delta;
        // Лёгкое отслеживание игрока по X
        float dx = playerX - enemy.x;
        enemy.x += dx * 0.4f * delta;
    }
}
