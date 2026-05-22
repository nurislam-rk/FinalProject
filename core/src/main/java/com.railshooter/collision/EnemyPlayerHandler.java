package com.railshooter.collision;

import com.railshooter.entities.Bullet;
import com.railshooter.entities.Teammate;
import com.railshooter.entities.enemies.Enemy;

/**
 * Chain of Responsibility — обрабатывает:
 *   1. Физический контакт врагов с игроком.
 *   2. Пули врагов → игрок.
 *   3. Физический контакт врагов с союзником (FIX v4: хитбокс союзника).
 *   4. Пули врагов → союзник (FIX v4).
 */
public class EnemyPlayerHandler extends CollisionHandler {

    @Override
    protected void doHandle(CollisionContext ctx) {
        for (Enemy e : ctx.enemies) {

            // --- Враг vs игрок (контакт) ---
            if (Math.abs(e.x - ctx.player.x) < 26 && Math.abs(e.y - ctx.player.y) < 30) {
                e.alive = false;
                ctx.events.notifyPlayerDamaged(1);
                ctx.comboCount  = 0;
                ctx.comboActive = false;
            }

            if (e.y < -30) { e.alive = false; continue; }

            // --- Пули врагов vs игрок ---
            for (Bullet b : e.getEnemyBullets()) {
                if (!b.active) continue;
                if (outOfBounds(b, ctx)) { b.active = false; continue; }
                if (Math.abs(b.x - ctx.player.x) < 16 && Math.abs(b.y - ctx.player.y) < 16) {
                    b.active = false;
                    ctx.events.notifyPlayerDamaged(1);
                    ctx.comboCount  = 0;
                    ctx.comboActive = false;
                }
            }

            // --- FIX v4: Враг vs союзник (хитбокс) ---
            for (Teammate tm : ctx.teammates) {
                if (!tm.active) continue;
                float tmCenterY = tm.y + 28 + 11; // центр фигурки
                if (Math.abs(e.x - tm.x) < Teammate.HIT_RADIUS + 16
                    && Math.abs(e.y - tmCenterY) < Teammate.HIT_RADIUS + 16) {
                    e.alive = false;
                    tm.takeDamage(1);
                    ctx.comboCount  = 0;
                    ctx.comboActive = false;
                }
            }

            // --- FIX v4: Пули врагов vs союзник ---
            for (Bullet b : e.getEnemyBullets()) {
                if (!b.active) continue;
                for (Teammate tm : ctx.teammates) {
                    if (!tm.active) continue;
                    float tmCenterY = tm.y + 28 + 11;
                    if (Math.abs(b.x - tm.x) < Teammate.HIT_RADIUS
                        && Math.abs(b.y - tmCenterY) < Teammate.HIT_RADIUS) {
                        b.active = false;
                        tm.takeDamage(1);
                    }
                }
            }
        }
    }

    private boolean outOfBounds(Bullet b, CollisionContext ctx) {
        return b.y < -10 || b.y > ctx.screenH + 10 || b.x < 0 || b.x > ctx.screenW;
    }
}
