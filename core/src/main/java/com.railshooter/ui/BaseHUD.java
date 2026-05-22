package com.railshooter.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.railshooter.utils.AssetManager;

/**
 * PATTERN: Decorator — базовый HUD (счёт)
 */
public class BaseHUD implements HUDComponent {
    private int score;
    private final BitmapFont font;

    public BaseHUD() {
        this.font = AssetManager.getInstance().font;
    }

    public void setScore(int score) { this.score = score; }

    @Override
    public void render(SpriteBatch batch) {
        font.draw(batch, "Score: " + score, 10, 470);
    }
}
