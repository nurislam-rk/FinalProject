package com.railshooter.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * PATTERN: Singleton — управляет всеми ресурсами игры.
 */
public class AssetManager {

    private static AssetManager instance;

    // Простые цветные текстуры для кубиков-врагов и пуль
    public Texture playerTexture;   // синий прямоугольник (тележка + персонаж)
    public Texture bulletTexture;   // жёлтый кружок
    public Texture zombieTexture;   // зелёный кубик
    public Texture spiderTexture;   // фиолетовый кубик
    public Texture batTexture;      // серый кубик
    public Texture cartTexture;     // серая тележка
    public Texture railTexture;     // коричневые рельсы
    public BitmapFont font;

    private AssetManager() {}

    public static AssetManager getInstance() {
        if (instance == null) instance = new AssetManager();
        return instance;
    }

    public void load() {
        playerTexture = makeTexture(32, 48, 0x4488FFFF); // синий
        bulletTexture = makeCircle(10,       0xFFFF00FF); // жёлтый круг
        zombieTexture = makeTexture(30, 30,  0x44FF44FF); // зелёный кубик
        spiderTexture = makeTexture(26, 26,  0xAA44FFFF); // фиолетовый кубик
        batTexture    = makeTexture(28, 22,  0x888888FF); // серый кубик
        cartTexture   = makeTexture(52, 28,  0x996633FF); // коричневый
        railTexture   = makeTexture(8,  8,   0x555544FF); // рельс
        font = new BitmapFont();
        font.getData().setScale(1.4f);
    }

    /** Создаёт закрашенный прямоугольник */
    private Texture makeTexture(int w, int h, int rgba8888) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(rgba8888ToColor(rgba8888));
        pm.fill();
        // Рисуем тёмную рамку
        pm.setColor(0, 0, 0, 0.5f);
        pm.drawRectangle(0, 0, w, h);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /** Создаёт круглую текстуру (для пуль) */
    private Texture makeCircle(int size, int rgba8888) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(rgba8888ToColor(rgba8888));
        pm.fillCircle(size / 2, size / 2, size / 2 - 1);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private com.badlogic.gdx.graphics.Color rgba8888ToColor(int rgba) {
        float r = ((rgba >> 24) & 0xFF) / 255f;
        float g = ((rgba >> 16) & 0xFF) / 255f;
        float b = ((rgba >>  8) & 0xFF) / 255f;
        float a = ( rgba        & 0xFF) / 255f;
        return new com.badlogic.gdx.graphics.Color(r, g, b, a);
    }

    public void dispose() {
        if (playerTexture != null) playerTexture.dispose();
        if (bulletTexture != null) bulletTexture.dispose();
        if (zombieTexture != null) zombieTexture.dispose();
        if (spiderTexture != null) spiderTexture.dispose();
        if (batTexture    != null) batTexture.dispose();
        if (cartTexture   != null) cartTexture.dispose();
        if (railTexture   != null) railTexture.dispose();
        if (font          != null) font.dispose();
    }
}
