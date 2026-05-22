package com.railshooter.wave;

import com.railshooter.entities.enemies.*;
import java.util.List;

public class BossWave extends Wave {
    @Override
    protected void spawnNormal(List<Enemy> out, float screenH, int waveNumber) {
        // Несколько слабых врагов-охранников
        for (int i = 0; i < 3; i++)
            out.add(new Zombie(randX(), spawnY(screenH, i, 55f) + 120));
    }

    @Override protected boolean hasBoss(int waveNumber) { return true; }

    @Override
    protected void spawnBoss(List<Enemy> out, float screenH, int waveNumber) {
        float cx = (TUNNEL_LEFT + TUNNEL_RIGHT) / 2f;
        out.add(0, new BossEnemy(cx, screenH + 80, waveNumber));
    }
}
