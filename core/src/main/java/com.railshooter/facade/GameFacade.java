package com.railshooter.facade;

import com.railshooter.biome.Biome;
import com.railshooter.entities.*;
import com.railshooter.entities.enemies.Enemy;
import com.railshooter.wave.*;
import java.util.*;

/**
 * PATTERN: Facade — упрощает взаимодействие подсистем (враги, дропы, пикапы, препятствия).
 */
public class GameFacade {
    private final List<Enemy>          enemies     = new ArrayList<>();
    private final List<DropItem>       drops       = new ArrayList<>();
    private final List<TeammatePickup> pickups     = new ArrayList<>();
    private final List<Obstacle>       obstacles   = new ArrayList<>();
    private final Random               rng         = new Random();

    private static final float TUNNEL_LEFT  = 130f;
    private static final float TUNNEL_RIGHT = 510f;

    public void spawnWave(int waveNumber, float screenH) {
        Wave wave;
        if (waveNumber % 5 == 0) wave = new BossWave();
        else if (waveNumber <= 2)  wave = new EasyWave();
        else if (waveNumber <= 5)  wave = new MediumWave();
        else                       wave = new HardWave();
        enemies.addAll(wave.build(screenH, waveNumber));
    }

    /** Вызывать когда враг убит — шанс выпасть бонуса или пикапа сокомандника */
    public void onEnemyDied(float ex, float ey, boolean isBoss) {
        float roll = rng.nextFloat();
        if (isBoss || roll < 0.25f) {
            DropItem.Type[] types = DropItem.Type.values();
            drops.add(new DropItem(ex, ey, types[rng.nextInt(types.length)]));
        }
        if (roll < 0.07f) {  // 7% шанс сокомандника
            pickups.add(new TeammatePickup(ex, ey));
        }
    }

    /** Периодически спавним препятствия */
    public void maybeSpawnObstacle(float screenH, Biome biome) {
        if (rng.nextFloat() < 0.008f) {   // ~0.8% per frame at 60fps → ~0.5/sec
            float ox = TUNNEL_LEFT + 30 + rng.nextFloat() * (TUNNEL_RIGHT - TUNNEL_LEFT - 60);
            boolean beam = rng.nextBoolean();
            obstacles.add(new Obstacle(ox, screenH + 20,
                beam ? 70 : 32, beam ? 14 : 28,
                beam ? Obstacle.ObstacleType.BEAM : Obstacle.ObstacleType.ROCK));
        }
    }

    public void update(float delta, float cartSpeed) {
        drops.removeIf(d -> d.collected || d.y < -50);
        for (DropItem d : drops) d.update(delta);

        pickups.removeIf(p -> p.collected || p.y < -50);
        for (TeammatePickup p : pickups) p.update(delta);

        obstacles.removeIf(o -> !o.active);
        for (Obstacle o : obstacles) o.update(delta, cartSpeed);

        enemies.removeIf(e -> !e.alive);
    }

    public List<Enemy>          getEnemies()   { return enemies;   }
    public List<DropItem>       getDrops()     { return drops;     }
    public List<TeammatePickup> getPickups()   { return pickups;   }
    public List<Obstacle>       getObstacles() { return obstacles; }
    public boolean              enemiesEmpty() { return enemies.isEmpty(); }

    public void reset() {
        enemies.clear(); drops.clear(); pickups.clear(); obstacles.clear();
    }
}
