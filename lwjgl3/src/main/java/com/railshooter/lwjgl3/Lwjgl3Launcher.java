package com.railshooter.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.railshooter.Main;

/**
 * Desktop launcher.
 * PATTERN: Singleton (через Main.getInstance())
 *
 * Поддержка полного экрана:
 *  - Окно стартует в windowed 640×480.
 *  - Нажатие F11 в игре переключает fullscreen/windowed через Main.toggleFullscreen().
 *  - GameScreen.resize() масштабирует viewport корректно в любом разрешении.
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Minecart Rail Shooter");
        cfg.useVsync(true);
        cfg.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        cfg.setWindowedMode(640, 480);
        cfg.setResizable(true);   // <-- разрешаем ресайз окна
        cfg.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return cfg;
    }
}
