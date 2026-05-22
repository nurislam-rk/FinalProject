package src.main.java.com.railshooter.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Observer — издатель (Subject)
 * Рассылает игровые события всем подписчикам.
 */
public class GameEventManager {

    private final List<GameEventListener> listeners = new ArrayList<>();

    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyEnemyKilled(int points) {
        for (GameEventListener l : listeners) l.onEnemyKilled(points);
    }

    public void notifyPlayerDamaged(int damage) {
        for (GameEventListener l : listeners) l.onPlayerDamaged(damage);
    }

    public void notifyGameOver() {
        for (GameEventListener l : listeners) l.onGameOver();
    }
}
