package src.main.java.com.railshooter.state;

/**
 * PATTERN: State — игра на паузе
 */
public class PausedState implements GameState {
    @Override public void enter()  { System.out.println("State: PAUSED"); }
    @Override public void update(float delta) { /* ничего не делаем на паузе */ }
    @Override public void exit()   { System.out.println("Exiting PAUSED state"); }
}
