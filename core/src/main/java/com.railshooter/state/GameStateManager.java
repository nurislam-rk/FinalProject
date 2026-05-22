package src.main.java.com.railshooter.state;

/**
 * PATTERN: State — менеджер состояний игры
 */
public class GameStateManager {
    private GameState currentState;

    public void setState(GameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        currentState.enter();
    }

    public void update(float delta) {
        if (currentState != null) currentState.update(delta);
    }

    public GameState getCurrentState() { return currentState; }
}
