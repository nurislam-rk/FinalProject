package src.main.java.com.railshooter.entities.enemies;
import com.railshooter.strategy.StraightMovement;

public class Bat extends Enemy {
    public Bat(float x, float y) {
        super(x, y, 1, 20, 0f); // HP=1, очки=20, не стреляет (летит быстро)
        setMovementStrategy(new StraightMovement(130f));
    }
    @Override public String getType() { return "Bat"; }
}
