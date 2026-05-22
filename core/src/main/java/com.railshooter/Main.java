package src.main.java.com.railshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.railshooter.screens.MenuScreen;
import com.railshooter.utils.AssetManager;

/**
 * Main application class.
 * PATTERN: Singleton
 *
 * toggleFullscreen() использует только com.badlogic.gdx.Gdx — без прямого
 * импорта lwjgl3-бэкенда, поэтому компилируется в модуле core.
 */
public class Main extends Game {

    private static Main instance;
    private boolean fullscreen = false;

    public static Main getInstance() { return instance; }

    @Override
    public void create() {
        instance = this;
        AssetManager.getInstance().load();
        setScreen(new MenuScreen(this));
    }

    /**
     * Переключает полноэкранный / оконный режим.
     * Вызывается из GameScreen по клавише F11.
     * Gdx.graphics.setFullscreenMode / setWindowedMode доступны во всех бэкендах.
     */
    public void toggleFullscreen() {
        if (fullscreen) {
            Gdx.graphics.setWindowedMode(640, 480);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
        fullscreen = !fullscreen;
    }

    public boolean isFullscreen() { return fullscreen; }

    @Override
    public void dispose() {
        super.dispose();
        AssetManager.getInstance().dispose();
    }
}
