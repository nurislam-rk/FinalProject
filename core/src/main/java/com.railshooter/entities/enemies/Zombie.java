package src.main.java.com.railshooter.entities.enemies;
import com.railshooter.strategy.StraightMovement;

public class Zombie extends Enemy {
    public Zombie(float x, float y) {
        super(x, y, 2, 10, 4.5f); // HP=2, очки=10, стреляет каждые 4.5с
        setMovementStrategy(new StraightMovement(75f));
    }
    @Override public String getType() { return "Zombie"; }
}
