package com.railshooter.collision;

import com.railshooter.entities.*;
import com.railshooter.entities.enemies.Enemy;
import java.util.List;

/** Пули игрока/сокомандников vs враги */
public class BulletEnemyHandler extends CollisionHandler {
    private static final float EXPLOSION_RADIUS = 55f;

    @Override
    protected void doHandle(CollisionContext ctx) {
        for (Bullet b : ctx.playerBullets) {
            if (!b.active) continue;
            if (b.x < 0 || b.x > ctx.screenW || b.y < -10 || b.y > ctx.screenH + 10) {
                b.active = false; continue;
            }
            for (Enemy e : ctx.enemies) {
                if (!e.alive) continue;
                float radius = e.getType().equals("Boss") ? 30f : 18f;
                if (hit(b, e, radius)) {
                    if (b.type == BulletType.EXPLOSIVE) {
                        handleExplosion(b, ctx);
                    } else {
                        e.takeDamage(b.damage);
                        ctx.hitEffects.add(new HitEffect(e.x, e.y));
                        if (!e.alive) ctx.events.notifyEnemyKilled(calcPoints(e.points, ctx));
                    }
                    if (b.type != BulletType.PIERCING) { b.active = false; break; }
                }
            }
        }

        // Пули сокомандников
        if (ctx.teammmateBullets == null) return;
        for (Bullet b : ctx.teammmateBullets) {
            if (!b.active) continue;
            if (b.x < 0 || b.x > ctx.screenW || b.y < -10 || b.y > ctx.screenH + 10) {
                b.active = false; continue;
            }
            for (Enemy e : ctx.enemies) {
                if (!e.alive) continue;
                if (hit(b, e, 18f)) {
                    e.takeDamage(b.damage);
                    ctx.hitEffects.add(new HitEffect(e.x, e.y));
                    if (!e.alive) ctx.events.notifyEnemyKilled(e.points);
                    b.active = false; break;
                }
            }
        }
    }

    private void handleExplosion(Bullet b, CollisionContext ctx) {
        ctx.hitEffects.add(new HitEffect(b.x, b.y, true));
        for (Enemy e : ctx.enemies) {
            if (!e.alive) continue;
            float d = dist(b.x, b.y, e.x, e.y);
            if (d < EXPLOSION_RADIUS) {
                e.takeDamage(b.damage);
                if (!e.alive) ctx.events.notifyEnemyKilled(calcPoints(e.points, ctx));
            }
        }
        b.active = false;
    }

    private int calcPoints(int base, CollisionContext ctx) {
        ctx.comboCount++;
        if (ctx.comboCount >= 3) { ctx.comboActive = true; return base * 2; }
        return base;
    }

    private boolean hit(Bullet b, Enemy e, float radius) {
        return Math.abs(b.x - e.x) < radius && Math.abs(b.y - e.y) < radius;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
}
