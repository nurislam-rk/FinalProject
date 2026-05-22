package com.railshooter.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.railshooter.utils.AssetManager;

/**
 * PATTERN: Decorator — добавляет инфо о волне
 */
public class WaveInfoDecorator implements HUDComponent {
    private final HUDComponent wrapped;
    private int wave;
    private final BitmapFont font;

    public WaveInfoDecorator(HUDComponent wrapped) {
        this.wrapped = wrapped;
        this.font = AssetManager.getInstance().font;
    }

    public void setWave(int wave) { this.wave = wave; }

    @Override
    public void render(SpriteBatch batch) {
        wrapped.render(batch);
        font.draw(batch, "Wave: " + wave, 10, 430);
    }
}
