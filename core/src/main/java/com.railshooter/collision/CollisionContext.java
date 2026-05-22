package com.railshooter.collision;

import com.railshooter.entities.*;
import com.railshooter.entities.enemies.Enemy;
import com.railshooter.observer.GameEventManager;
import java.util.List;

/** Контекст — все данные для обработки коллизий в одном кадре */
public class CollisionContext {
    public final List<Bullet>        playerBullets;
    public final List<Bullet>        teammmateBullets; // может быть null
    public final List<Enemy>         enemies;
    public final List<DropItem>      drops;
    public final List<TeammatePickup> pickups;
    public final List<Obstacle>      obstacles;
    public final Player              player;
    public final List<Teammate>      teammates;
    public final GameEventManager    events;
    public final List<HitEffect>     hitEffects;
    public final float               screenW, screenH;
    public       int                 comboCount;
    public       boolean             comboActive;

    public CollisionContext(Player player, List<Bullet> playerBullets,
                            List<Enemy> enemies, List<DropItem> drops,
                            List<TeammatePickup> pickups, List<Obstacle> obstacles,
                            List<Teammate> teammates, List<Bullet> teammateBullets,
                            GameEventManager events, List<HitEffect> hitEffects,
                            float sw, float sh, int combo, boolean comboActive) {
        this.player = player;
        this.playerBullets = playerBullets;
        this.teammates = teammates;
        this.teammmateBullets = teammateBullets;
        this.enemies = enemies;
        this.drops = drops;
        this.pickups = pickups;
        this.obstacles = obstacles;
        this.events = events;
        this.hitEffects = hitEffects;
        this.screenW = sw; this.screenH = sh;
        this.comboCount = combo;
        this.comboActive = comboActive;
    }
}
