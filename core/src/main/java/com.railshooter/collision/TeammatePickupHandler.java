package com.railshooter.collision;

import com.railshooter.entities.Bullet;
import com.railshooter.entities.Teammate;
import com.railshooter.entities.TeammatePickup;

import java.util.ArrayList;

/**
 * Chain of Responsibility — пикапы союзников vs игрок.
 *
 * FIX v4: MAX_TEAMMATES = 1. Подбор пикапа работает только когда союзников < 1.
 * Новый союзник получает ту же ссылку на список teammateBullets из контекста,
 * чтобы его пули корректно попадали в BulletEnemyHandler.
 */
public class TeammatePickupHandler extends CollisionHandler {

    /** Максимальное количество одновременных союзников */
    public static final int MAX_TEAMMATES = 1;

    @Override
    protected void doHandle(CollisionContext ctx) {
        for (TeammatePickup tp : ctx.pickups) {
            if (tp.collected || tp.y < -40) { tp.collected = true; continue; }

            if (Math.abs(tp.x - ctx.player.x) < 30 && Math.abs(tp.drawY - ctx.player.y) < 30) {
                tp.collected = true;
                if (ctx.teammates.size() < MAX_TEAMMATES) {
                    // Передаём общий список пуль — BulletEnemyHandler их обработает
                    ctx.teammates.add(
                        new Teammate(ctx.player.x + 40, ctx.player.y, ctx.teammmateBullets)
                    );
                }
            }
        }
    }
}
