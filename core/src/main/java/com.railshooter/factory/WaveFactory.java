package src.main.java.com.railshooter.factory;

import com.railshooter.entities.enemies.Enemy;
import com.railshooter.wave.*;
import java.util.List;

/**
 * PATTERN: Abstract Factory — теперь делегирует Template Method (Wave).
 * Оставлен для обратной совместимости.
 */
public class WaveFactory {
    public enum Difficulty { EASY, MEDIUM, HARD }

    public List<Enemy> createWave(Difficulty diff, float screenW, float screenH) {
        Wave wave;
        switch (diff) {
            case EASY:   wave = new EasyWave();   break;
            case MEDIUM: wave = new MediumWave();  break;
            default:     wave = new HardWave();    break;
        }
        return wave.build(screenH, 1);
    }
}
