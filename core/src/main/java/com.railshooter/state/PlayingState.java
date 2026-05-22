package src.main.java.com.railshooter.state;

/**
 * PATTERN: State — игра в процессе
 */
public class PlayingState implements GameState {
    @Override public void enter()  { System.out.println("State: PLAYING"); }
    @Override public void update(float delta) { /* основная логика в GameScreen */ }
    @Override public void exit()   { System.out.println("Exiting PLAYING state"); }
}
