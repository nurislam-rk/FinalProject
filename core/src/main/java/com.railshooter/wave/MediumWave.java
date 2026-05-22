package com.railshooter.wave;

import com.railshooter.entities.enemies.*;
import java.util.List;

public class MediumWave extends Wave {
    @Override
    protected void spawnNormal(List<Enemy> out, float screenH, int waveNumber) {
        for (int i = 0; i < 3; i++)
            out.add(new Zombie(randX(), spawnY(screenH, i, 55f)));
        for (int i = 0; i < 2; i++)
            out.add(new Spider(randX(), spawnY(screenH, i, 65f) + 200));
    }

    @Override
    protected void applyFormation(List<Enemy> enemies) {
        // V-образная формация
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            e.x = TUNNEL_LEFT + 20 + (float)(TUNNEL_RIGHT - TUNNEL_LEFT - 40) / enemies.size() * i + 20;
        }
    }
}
