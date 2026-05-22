package src.main.java.com.railshooter.state;

/**
 * PATTERN: State — интерфейс состояния игры
 */
public interface GameState {
    void enter();
    void update(float delta);
    void exit();
}
