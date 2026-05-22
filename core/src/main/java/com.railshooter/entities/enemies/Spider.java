package src.main.java.com.railshooter.entities.enemies;
import com.railshooter.strategy.ZigZagMovement;

public class Spider extends Enemy {
    public Spider(float x, float y) {
        super(x, y, 1, 15, 3.0f); // HP=1, очки=15, стреляет часто
        setMovementStrategy(new ZigZagMovement(90f, 120f));
    }
    @Override public String getType() { return "Spider"; }
}
