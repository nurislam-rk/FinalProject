package src.main.java.com.railshooter.entities;

import com.railshooter.strategy.ShootingStrategy;
import com.railshooter.strategy.SingleShotStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * Игрок — Builder, Strategy.
 * + cooldown, dodge (Shift), shield, speed-boost
 */
public class Player {
    public float x, y;
    public int health, maxHealth;
    public boolean shield = false;
    private float shieldTimer = 0f;

    // Кулдаун между выстрелами
    private float shootCooldown  = 0f;
    public  float shootInterval  = 0.28f;  // секунды

    // Ускорение
    public  float speedMultiplier = 1f;
    private float speedTimer      = 0f;

    // Уклонение (перекат)
    public  boolean dodging      = false;
    private float   dodgeTimer   = 0f;
    private float   dodgeCooldownTimer = 0f;
    public static final float DODGE_DURATION  = 0.35f;
    public static final float DODGE_COOLDOWN  = 1.2f;
    public static final float DODGE_SPEED     = 600f;
    private float dodgeDirX = 0f;

    private ShootingStrategy shootingStrategy;
    private final List<Bullet> bullets = new ArrayList<>();

    private Player() {}

    public void setShootingStrategy(ShootingStrategy s) { this.shootingStrategy = s; }

    public boolean canShoot() { return shootCooldown <= 0f; }

    public void shoot(float targetX, float targetY) {
        if (!canShoot() || shootingStrategy == null) return;
        bullets.addAll(shootingStrategy.shoot(x, y + 30, targetX, targetY));
        shootCooldown = shootInterval;
    }

    public void activateShield(float duration) {
        shield = true;
        shieldTimer = duration;
    }

    public void activateSpeed(float duration) {
        speedMultiplier = 1.8f;
        speedTimer = duration;
    }

    public boolean startDodge(float dirX) {
        if (dodgeCooldownTimer > 0 || dodging) return false;
        dodging = true;
        dodgeTimer = DODGE_DURATION;
        dodgeCooldownTimer = DODGE_COOLDOWN;
        dodgeDirX = dirX == 0 ? 1f : Math.signum(dirX);
        return true;
    }

    public float getDodgeDirX() { return dodgeDirX; }

    public void update(float delta) {
        if (shootCooldown > 0) shootCooldown -= delta;

        if (shield) { shieldTimer -= delta; if (shieldTimer <= 0) shield = false; }
        if (speedMultiplier > 1f) { speedTimer -= delta; if (speedTimer <= 0) speedMultiplier = 1f; }

        if (dodging) {
            dodgeTimer -= delta;
            if (dodgeTimer <= 0) dodging = false;
        }
        if (dodgeCooldownTimer > 0) dodgeCooldownTimer -= delta;

        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update(delta);
    }

    public List<Bullet> getBullets() { return bullets; }

    public void takeDamage(int dmg) {
        if (dodging || shield) return;   // неуязвимость при перекате и щите
        health = Math.max(0, health - dmg);
    }

    public boolean isDead() { return health <= 0; }

    public float getDodgeCooldownRatio() {
        return Math.max(0, 1f - dodgeCooldownTimer / DODGE_COOLDOWN);
    }

    // ===== PATTERN: Builder =====
    public static class Builder {
        private float x = 400, y = 70;
        private int health = 5;
        private ShootingStrategy strategy = new SingleShotStrategy();

        public Builder position(float x, float y) { this.x = x; this.y = y; return this; }
        public Builder health(int hp) { this.health = hp; return this; }
        public Builder shootingStrategy(ShootingStrategy s) { this.strategy = s; return this; }

        public Player build() {
            Player p = new Player();
            p.x = x; p.y = y;
            p.health = health; p.maxHealth = health;
            p.shootingStrategy = strategy;
            return p;
        }
    }
}
