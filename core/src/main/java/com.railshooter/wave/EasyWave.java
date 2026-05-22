package com.railshooter.wave;

import com.railshooter.entities.enemies.Enemy;
import com.railshooter.entities.enemies.Zombie;
import java.util.List;

public class EasyWave extends Wave {
    @Override
    protected void spawnNormal(List<Enemy> out, float screenH, int waveNumber) {
        int count = 2 + waveNumber;
        for (int i = 0; i < count; i++)
            out.add(new Zombie(randX(), spawnY(screenH, i, 60f)));
    }
}
