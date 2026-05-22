package com.railshooter.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.railshooter.utils.AssetManager;

/**
 * PATTERN: Decorator — добавляет полоску здоровья поверх базового HUD
 */
public class HealthBarDecorator implements HUDComponent {
    private final HUDComponent wrapped;
    private int health, maxHealth;
    private final BitmapFont font;

    public HealthBarDecorator(HUDComponent wrapped) {
        this.wrapped = wrapped;
        this.font = AssetManager.getInstance().font;
    }

    public void setHealth(int health, int maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;
    }

    @Override
    public void render(SpriteBatch batch) {
        wrapped.render(batch);
        font.draw(batch, "HP: " + health + "/" + maxHealth, 10, 450);
    }
}
