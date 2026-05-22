package src.main.java.com.railshooter.observer;

/**
 * PATTERN: Observer — интерфейс слушателя игровых событий
 */
public interface GameEventListener {
    void onEnemyKilled(int points);
    void onPlayerDamaged(int damage);
    void onGameOver();
}
