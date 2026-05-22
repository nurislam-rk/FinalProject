package src.main.java.com.railshooter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.railshooter.Main;
import com.railshooter.utils.AssetManager;

public class MenuScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private float time = 0f;
    private int bestScore;

    public MenuScreen(Main game) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        bestScore = Gdx.app.getPreferences("RailShooter").getInteger("best", 0);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.04f, 0.03f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float scroll = (time * 120f) % 80f;

        // Тоннель
        shapes.setColor(0.18f, 0.14f, 0.10f, 1f);
        shapes.rect(0, 0, 130, 480);
        shapes.rect(510, 0, 130, 480);
        shapes.setColor(0.22f, 0.18f, 0.13f, 1f);
        shapes.rect(130, 0, 380, 480);
        // Рельсы
        shapes.setColor(0.65f, 0.60f, 0.55f, 1f);
        shapes.rect(222, 0, 10, 480);
        shapes.rect(408, 0, 10, 480);
        // Шпалы
        shapes.setColor(0.35f, 0.22f, 0.10f, 1f);
        for (int i = -1; i < 8; i++) {
            float sy = (i * 80 + scroll);
            if (sy > -10 && sy < 490) shapes.rect(140, sy, 360, 12);
        }

        // Факелы
        float flicker = 0.85f + 0.15f * (float)Math.sin(time * 5f);
        shapes.setColor(1f*flicker, 0.6f*flicker, 0.1f*flicker, 1f);
        for (int i = 0; i < 4; i++) {
            float ty = (i * 130 + scroll * 0.7f) % (130 * 4);
            if (ty > 0 && ty < 480) {
                shapes.triangle(125,ty, 115,ty, 120,ty+20);
                shapes.triangle(515,ty, 525,ty, 520,ty+20);
            }
        }

        // Тёмный оверлей
        shapes.setColor(0f, 0f, 0f, 0.58f);
        shapes.rect(0, 0, 640, 480);
        shapes.end();

        batch.begin();
        AssetManager am = AssetManager.getInstance();

        am.font.getData().setScale(2.4f);
        am.font.setColor(1f, 0.7f, 0.05f, 1f);
        am.font.draw(batch, "MINECART", 195, 395);
        am.font.getData().setScale(1.6f);
        am.font.setColor(0.9f, 0.6f, 0.1f, 1f);
        am.font.draw(batch, "RAIL SHOOTER", 170, 360);

        am.font.getData().setScale(1.0f);
        am.font.setColor(0.6f, 0.6f, 0.6f, 1f);
        am.font.draw(batch, "Survive the dark tunnels!", 198, 328);

        float pulse = 0.8f + 0.2f * (float)Math.sin(time * 3.5f);
        am.font.getData().setScale(1.5f);
        am.font.setColor(pulse, pulse, 0.1f, 1f);
        am.font.draw(batch, "[ SPACE / ENTER ]  Play", 148, 290);

        if (bestScore > 0) {
            am.font.getData().setScale(0.95f);
            am.font.setColor(1f, 0.85f, 0.2f, 1f);
            am.font.draw(batch, "★ Best: " + bestScore, 270, 258);
        }

        // Управление
        am.font.getData().setScale(0.88f);
        am.font.setColor(0.55f, 0.55f, 0.55f, 1f);
        am.font.draw(batch, "A/D  — Двигаться          ЛКМ  — Стрелять", 150, 225);
        am.font.draw(batch, "1 — Одиночный   2 — Веер   3 — Пробивающий   4 — Взрывной", 90, 205);
        am.font.draw(batch, "SHIFT — Перекат           P  — Пауза", 168, 185);
        am.font.draw(batch, "Биомы: Шахта → Река → Кристаллы → Лава", 152, 162);
        am.font.draw(batch, "Подбирай союзников ★  Ищи боссов каждые 5 волн", 126, 142);

        am.font.getData().setScale(1.1f);
        am.font.setColor(0.5f, 0.5f, 0.5f, 1f);
        am.font.draw(batch, "[ ESC ]  Quit", 255, 110);
        am.font.getData().setScale(1.0f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game)); dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shapes.dispose(); }
}
