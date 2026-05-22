package com.railshooter.collision;

import com.railshooter.entities.DropItem;

/** Бонусы vs игрок */
public class DropPickupHandler extends CollisionHandler {
    @Override
    protected void doHandle(CollisionContext ctx) {
        for (DropItem d : ctx.drops) {
            if (d.collected || d.y < -40) { d.collected = true; continue; }
            if (Math.abs(d.x - ctx.player.x) < 28 && Math.abs(d.drawY - ctx.player.y) < 28) {
                d.collected = true;
                switch (d.type) {
                    case HEALTH:
                        if (ctx.player.health < ctx.player.maxHealth)
                            ctx.player.health = Math.min(ctx.player.maxHealth, ctx.player.health + 1);
                        break;
                    case SHIELD: ctx.player.activateShield(6f); break;
                    case SPEED:  ctx.player.activateSpeed(5f);  break;
                }
            }
        }
    }
}
