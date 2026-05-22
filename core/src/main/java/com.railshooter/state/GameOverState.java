package src.main.java.com.railshooter.state;

/**
 * PATTERN: State — игра окончена
 */
public class GameOverState implements GameState {
    @Override public void enter()  { System.out.println("State: GAME OVER"); }
    @Override public void update(float delta) {}
    @Override public void exit()   {}
}
