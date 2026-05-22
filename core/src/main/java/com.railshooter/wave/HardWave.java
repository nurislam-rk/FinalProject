package com.railshooter.wave;

import com.railshooter.entities.enemies.*;
import java.util.List;

public class HardWave extends Wave {
    @Override
    protected void spawnNormal(List<Enemy> out, float screenH, int waveNumber) {
        for (int i = 0; i < 4; i++)
            out.add(new Zombie(randX(), spawnY(screenH, i, 45f)));
        for (int i = 0; i < 3; i++)
            out.add(new Spider(randX(), spawnY(screenH, i, 55f) + 200));
        for (int i = 0; i < 2; i++)
            out.add(new Bat(randX(), spawnY(screenH, i, 65f) + 420));
    }
}
