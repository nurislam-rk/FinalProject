package src.main.java.com.railshooter.strategy;

import com.railshooter.entities.enemies.Enemy;

/** PATTERN: Strategy — зигзаговое движение */
public class ZigZagMovement implements EnemyMovementStrategy {
    private final float speed, amplitude;
    private float phase = 0f;

    public ZigZagMovement(float speed, float amplitude) {
        this.speed = speed; this.amplitude = amplitude;
    }

    @Override
    public void move(Enemy enemy, float delta, float playerX, float playerY) {
        enemy.y -= speed * delta;
        phase += delta * 3.2f;
        enemy.x += (float)Math.sin(phase) * amplitude * delta;
        // Ограничение по тоннелю
        enemy.x = Math.max(150f, Math.min(490f, enemy.x));
    }
}
