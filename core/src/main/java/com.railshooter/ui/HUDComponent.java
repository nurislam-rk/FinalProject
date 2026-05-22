package com.railshooter.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * PATTERN: Decorator — интерфейс HUD компонента
 */
public interface HUDComponent {
    void render(SpriteBatch batch);
}
