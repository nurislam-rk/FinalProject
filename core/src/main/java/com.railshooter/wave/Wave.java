package com.railshooter.wave;

import com.railshooter.entities.enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PATTERN: Template Method — шаблон создания волны.
 * Подклассы переопределяют шаги, но не общий алгоритм.
 */
public abstract class Wave {
    protected static final float TUNNEL_LEFT  = 160f;
    protected static final float TUNNEL_RIGHT = 480f;
    protected final Random rng = new Random();

    /** Шаблонный метод — алгоритм создания волны */
    public final List<Enemy> build(float screenH, int waveNumber) {
        List<Enemy> result = new ArrayList<>();
        prepare(waveNumber);
        spawnNormal(result, screenH, waveNumber);
        if (hasBoss(waveNumber)) spawnBoss(result, screenH, waveNumber);
        applyFormation(result);
        return result;
    }

    /** Хук — подготовка (можно переопределить) */
    protected void prepare(int waveNumber) {}

    /** Основной спавн обычных врагов */
    protected abstract void spawnNormal(List<Enemy> out, float screenH, int waveNumber);

    /** Нужен ли босс? */
    protected boolean hasBoss(int waveNumber) { return false; }

    /** Спавн босса */
    protected void spawnBoss(List<Enemy> out, float screenH, int waveNumber) {}

    /** Хук — расстановка в формацию */
    protected void applyFormation(List<Enemy> enemies) {}

    protected float randX() {
        return TUNNEL_LEFT + 20 + rng.nextFloat() * (TUNNEL_RIGHT - TUNNEL_LEFT - 40);
    }

    protected float spawnY(float screenH, int index, float spacing) {
        return screenH + 40 + index * spacing;
    }
}
