package src.main.java.com.railshooter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.railshooter.Main;
import com.railshooter.biome.Biome;
import com.railshooter.collision.*;
import com.railshooter.command.ShootCommand;
import com.railshooter.entities.*;
import com.railshooter.entities.enemies.BossEnemy;
import com.railshooter.entities.enemies.Enemy;
import com.railshooter.facade.GameFacade;
import com.railshooter.observer.*;
import com.railshooter.state.*;
import com.railshooter.strategy.*;
import com.railshooter.ui.*;
import com.railshooter.utils.AssetManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный игровой экран.
 *
 * Паттерны:
 *   Observer         — GameEventManager / GameEventListener
 *   State            — GameStateManager (Playing / Paused / GameOver)
 *   Command          — ShootCommand
 *   Factory          — ZombieFactory / SpiderFactory / BatFactory
 *   Strategy         — ShootingStrategy (Single / Spread / Piercing / Explosive)
 *   Decorator        — HUD: BaseHUD → HealthBarDecorator → WaveInfoDecorator
 *   Builder          — Player.Builder
 *   Template Method  — Wave (EasyWave / MediumWave / HardWave / BossWave)
 *   Facade           — GameFacade (враги, дропы, пикапы, препятствия)
 *   Chain of Resp.   — CollisionHandler (Bullet→Enemy→Drop→TeammatePickup)
 *
 * FIX v4:
 *   1. FitViewport (640×480) — корректный масштаб при любом разрешении / fullscreen (F11).
 *   2. Максимум 1 союзник (ограничение перенесено в TeammatePickupHandler.MAX_TEAMMATES = 1).
 *   3. Союзник стреляет — уже работало; теперь пули точно попадают в список teammateBullets.
 *   4. Хитбокс союзника — EnemyPlayerHandler теперь бьёт и по союзникам.
 */
public class GameScreen implements Screen, GameEventListener {

    private final Main game;
    private final SpriteBatch   batch;
    private final ShapeRenderer shapes;

    // ── FitViewport (PATTERN: нет, но ключевой фикс fullscreen) ──────────
    private final OrthographicCamera camera;
    private final Viewport           viewport;

    /** Логические размеры мира (не меняются при ресайзе) */
    private static final float SW = 640f;
    private static final float SH = 480f;

    private static final float TUNNEL_LEFT  = 130f;
    private static final float TUNNEL_RIGHT = 510f;
    private static final float TUNNEL_W     = TUNNEL_RIGHT - TUNNEL_LEFT;
    private static final float RAIL_OFFSET  = 60f;
    private static final float LEFT_RAIL    = TUNNEL_LEFT  + RAIL_OFFSET;
    private static final float RIGHT_RAIL   = TUNNEL_RIGHT - RAIL_OFFSET;

    // --- Скролл (3 слоя параллакса) ---
    private float scrollNear = 0f;
    private float scrollMid  = 0f;
    private float scrollFar  = 0f;
    private float cartSpeed  = 180f;

    // --- Сущности ---
    private Player player;
    /** Список союзников — макс. 1 (ограничено в TeammatePickupHandler) */
    private final List<Teammate>  teammates       = new ArrayList<>();
    /** Общий список пуль всех союзников */
    private final List<Bullet>    teammateBullets = new ArrayList<>();
    private final List<HitEffect> hitEffects      = new ArrayList<>();

    // --- Паттерны ---
    private final GameEventManager eventManager = new GameEventManager();
    private final GameStateManager stateManager = new GameStateManager();
    private final GameFacade       facade       = new GameFacade();

    // --- Chain of Responsibility (коллизии) ---
    private final CollisionHandler collisionChain;

    // --- HUD (Decorator chain) ---
    private BaseHUD            baseHUD;
    private HealthBarDecorator healthHUD;
    private WaveInfoDecorator  waveHUD;

    // --- Игровые данные ---
    private int   score        = 0;
    private int   wave         = 1;
    private float waveTimer    = 0f;
    private float waveInterval = 8f;

    // --- Комбо ---
    private int     comboCount  = 0;
    private boolean comboActive = false;
    private float   comboTimer  = 0f;

    // --- Биом ---
    private Biome biome           = Biome.MINE;
    private Biome prevBiome       = Biome.MINE;
    private float biomeTransition = 1f;

    private float envTimer = 0f;

    // --- Рекорд ---
    private int bestScore;

    // --- Анимированный счёт ---
    private float displayScore = 0f;

    private int totalKills = 0;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public GameScreen(Main game) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.shapes = new ShapeRenderer();

        // FitViewport сохраняет пропорции 640×480 при любом окне/fullscreen
        camera   = new OrthographicCamera();
        viewport = new FitViewport(SW, SH, camera);
        viewport.apply(true);

        bestScore = Gdx.app.getPreferences("RailShooter").getInteger("best", 0);

        // Строим цепочку коллизий
        BulletEnemyHandler    bulletHandler   = new BulletEnemyHandler();
        EnemyPlayerHandler    enemyHandler    = new EnemyPlayerHandler();
        DropPickupHandler     dropHandler     = new DropPickupHandler();
        TeammatePickupHandler teammateHandler = new TeammatePickupHandler();
        bulletHandler.setNext(enemyHandler).setNext(dropHandler).setNext(teammateHandler);
        collisionChain = bulletHandler;

        initGame();
    }

    private void initGame() {
        player = new Player.Builder()
            .position((TUNNEL_LEFT + TUNNEL_RIGHT) / 2f, 70f)
            .health(5)
            .shootingStrategy(new SingleShotStrategy())
            .build();

        eventManager.subscribe(this);
        stateManager.setState(new PlayingState());

        baseHUD   = new BaseHUD();
        healthHUD = new HealthBarDecorator(baseHUD);
        waveHUD   = new WaveInfoDecorator(healthHUD);

        facade.spawnWave(wave, SH);
    }

    // =========================================================
    //  RENDER
    // =========================================================
    @Override
    public void render(float delta) {
        stateManager.update(delta);

        // Сброс экрана — обязательно перед viewport
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Применяем viewport (letterbox/pillarbox при несоответствии сторон)
        viewport.apply();

        if (stateManager.getCurrentState() instanceof PausedState) {
            drawAll();
            renderPauseOverlay();
            if (Gdx.input.isKeyJustPressed(Input.Keys.P))
                stateManager.setState(new PlayingState());
            return;
        }
        if (stateManager.getCurrentState() instanceof GameOverState) {
            renderGameOver();
            return;
        }

        handleInput(delta);
        updateGame(delta);
        drawAll();
    }

    /** Вызывается LibGDX при изменении размера окна / переключении fullscreen */
    @Override
    public void resize(int w, int h) {
        // FitViewport сам пересчитывает letterbox/pillarbox
        viewport.update(w, h, true);
    }

    private void drawAll() {
        drawBackground();
        drawTunnel();
        drawObstacles();
        drawEntities();
        drawHUD();
    }

    // =========================================================
    //  INPUT
    // =========================================================
    private void handleInput(float delta) {
        // F11 — полный экран
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.toggleFullscreen();
        }

        float speed = 220f * player.speedMultiplier;

        float moveDir = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.x = Math.max(TUNNEL_LEFT + 20, player.x - speed * delta);
            moveDir = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.x = Math.min(TUNNEL_RIGHT - 20, player.x + speed * delta);
            moveDir = 1;
        }

        // Уклонение (PATTERN: Command)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            player.startDodge(moveDir);
        }

        // Выстрел — координаты мыши переводим в мировые (PATTERN: Command)
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            float mx = screenToWorldX(Gdx.input.getX());
            float my = screenToWorldY(Gdx.input.getY());
            if (player.canShoot()) {
                new ShootCommand(player, mx, my).execute();
            }
        }

        // Переключение стратегий стрельбы (PATTERN: Strategy)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            player.setShootingStrategy(new SingleShotStrategy());
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            player.setShootingStrategy(new SpreadShotStrategy());
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
            player.setShootingStrategy(new PiercingBulletStrategy());
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4))
            player.setShootingStrategy(new ExplosiveBulletStrategy());

        if (Gdx.input.isKeyJustPressed(Input.Keys.P))
            stateManager.setState(new PausedState());
    }

    // =========================================================
    //  UPDATE
    // =========================================================
    private void updateGame(float delta) {
        envTimer += delta;

        // Перекат
        if (player.dodging) {
            float dd = Player.DODGE_SPEED * delta * player.getDodgeDirX();
            player.x = MathUtils.clamp(player.x + dd, TUNNEL_LEFT + 20, TUNNEL_RIGHT - 20);
        }

        // Параллакс
        scrollNear = (scrollNear + cartSpeed         * delta) % 80f;
        scrollMid  = (scrollMid  + cartSpeed * 0.55f * delta) % 80f;
        scrollFar  = (scrollFar  + cartSpeed * 0.25f * delta) % 80f;

        // Биом
        Biome newBiome = Biome.fromWave(wave);
        if (newBiome != biome) { prevBiome = biome; biome = newBiome; biomeTransition = 0f; }
        if (biomeTransition < 1f) biomeTransition = Math.min(1f, biomeTransition + delta * 1.2f);

        player.update(delta);

        // Союзники
        for (Teammate tm : teammates)
            tm.update(delta, player.x, player.y, facade.getEnemies());
        teammates.removeIf(t -> !t.active);

        // Волны
        waveTimer += delta;
        if (waveTimer >= waveInterval && facade.enemiesEmpty()) {
            waveTimer = 0;
            wave++;
            cartSpeed = Math.min(300f, 180f + wave * 5f);
            facade.spawnWave(wave, SH);
        }

        facade.maybeSpawnObstacle(SH, biome);
        for (Enemy e : facade.getEnemies()) e.update(delta, player.x, player.y);
        facade.update(delta, cartSpeed);

        // Chain of Responsibility — коллизии
        CollisionContext ctx = new CollisionContext(
            player, player.getBullets(),
            facade.getEnemies(), facade.getDrops(),
            facade.getPickups(), facade.getObstacles(),
            teammates, teammateBullets,
            eventManager, hitEffects, SW, SH, comboCount, comboActive
        );
        collisionChain.handle(ctx);
        comboCount  = ctx.comboCount;
        comboActive = ctx.comboActive;

        // Дропы при смерти врагов
        for (Enemy e : new ArrayList<>(facade.getEnemies())) {
            if (!e.alive) {
                boolean boss = e instanceof BossEnemy;
                facade.onEnemyDied(e.x, e.y, boss);
                totalKills++;
            }
        }

        hitEffects.removeIf(h -> { h.life -= delta; return h.life <= 0; });

        if (comboActive) {
            comboTimer += delta;
            if (comboTimer > 4f) { comboActive = false; comboTimer = 0; }
        }

        displayScore += (score - displayScore) * Math.min(1f, delta * 8f);

        // Препятствия vs игрок
        for (Obstacle o : facade.getObstacles()) {
            if (!o.active) continue;
            if (Math.abs(player.x - (o.x + o.width / 2))  < o.width  / 2 + 10 &&
                Math.abs(player.y - (o.y + o.height / 2)) < o.height / 2 + 20) {
                eventManager.notifyPlayerDamaged(1);
                o.active = false;
            }
        }

        if (player.isDead()) { saveBest(); eventManager.notifyGameOver(); }

        baseHUD.setScore(score);
        healthHUD.setHealth(player.health, player.maxHealth);
        waveHUD.setWave(wave);
    }

    // =========================================================
    //  HELPERS — перевод экранных координат в мировые
    // =========================================================

    /** Экранный X → мировой X (учитывает letterbox/pillarbox от FitViewport) */
    private float screenToWorldX(int screenX) {
        return viewport.unproject(new com.badlogic.gdx.math.Vector2(screenX, Gdx.input.getY())).x;
    }

    /** Экранный Y → мировой Y (LibGDX: 0 сверху → переводим) */
    private float screenToWorldY(int screenY) {
        return viewport.unproject(new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), screenY)).y;
    }

    // =========================================================
    //  DRAW — BACKGROUND
    // =========================================================
    private void drawBackground() {
        float[] wc = lerpBiomeColor(prevBiome.wallColor(),   biome.wallColor());
        float[] fc = lerpBiomeColor(prevBiome.floorColor(),  biome.floorColor());
        float[] ac = lerpBiomeColor(prevBiome.accentColor(), biome.accentColor());

        Gdx.gl.glClearColor(wc[0] * 0.4f, wc[1] * 0.4f, wc[2] * 0.4f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(wc[0] * 0.6f, wc[1] * 0.6f, wc[2] * 0.6f, 1f);
        shapes.rect(0, 0, TUNNEL_LEFT, SH);
        shapes.rect(TUNNEL_RIGHT, 0, SW - TUNNEL_RIGHT, SH);

        shapes.setColor(fc[0], fc[1], fc[2], 1f);
        shapes.rect(TUNNEL_LEFT, 0, TUNNEL_W, SH);

        shapes.setColor(wc[0] * 0.5f, wc[1] * 0.5f, wc[2] * 0.5f, 1f);
        float colSpacing = 120f;
        for (int i = -1; i < 6; i++) {
            float colY = (i * colSpacing + scrollFar * 2.5f) % (colSpacing * 6);
            if (colY < -10 || colY > SH + 10) continue;
            shapes.rect(TUNNEL_LEFT - 18, colY - 8, 18, 16);
            shapes.rect(TUNNEL_RIGHT,     colY - 8, 18, 16);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(1f);
        shapes.setColor(wc[0] * 0.5f, wc[1] * 0.5f, wc[2] * 0.5f, 1f);
        for (int row = 0; row < 14; row++) {
            float ry = (row * 38 + scrollMid * 1.4f) % SH;
            shapes.line(0, ry, TUNNEL_LEFT, ry);
        }
        for (int row = 0; row < 14; row++) {
            float ry = (row * 38 + scrollMid * 1.4f + 19) % SH;
            shapes.line(TUNNEL_RIGHT, ry, SW, ry);
        }
        for (int col = 1; col <= 2; col++) {
            shapes.line(col * (TUNNEL_LEFT / 3f), 0, col * (TUNNEL_LEFT / 3f), SH);
            float cx = TUNNEL_RIGHT + col * ((SW - TUNNEL_RIGHT) / 3f);
            shapes.line(cx, 0, cx, SH);
        }
        shapes.end();

        drawBiomeDecor(ac, fc);
    }

    private void drawBiomeDecor(float[] ac, float[] fc) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float flicker = 0.85f + 0.15f * MathUtils.sin(envTimer * 4f);

        switch (biome) {
            case MINE:
                shapes.setColor(ac[0] * flicker, ac[1] * flicker, ac[2] * flicker * 0.3f, 1f);
                drawTorches(ac, flicker, 160f, 0.8f);
                break;
            case UNDERGROUND_RIVER:
                shapes.setColor(ac[0] * 0.5f, ac[1] * 0.7f, ac[2], 1f);
                float dropSpacing = 45f;
                for (int i = 0; i < 12; i++) {
                    float dy = (i * dropSpacing + scrollMid * 1.2f) % (dropSpacing * 12);
                    if (dy < 0 || dy > SH) continue;
                    shapes.ellipse(TUNNEL_LEFT - 8, dy, 6, 10);
                    shapes.ellipse(TUNNEL_RIGHT + 3, dy + 20, 6, 10);
                }
                shapes.setColor(fc[0] + 0.1f, fc[1] + 0.15f, fc[2] + 0.25f, 1f);
                shapes.rect(TUNNEL_LEFT + 20, 0, TUNNEL_W - 40, 20);
                break;
            case CRYSTAL_CAVE:
                float crystalSpacing = 90f;
                for (int i = 0; i < 8; i++) {
                    float cy = (i * crystalSpacing + scrollMid * 0.9f) % (crystalSpacing * 8);
                    if (cy < 0 || cy > SH) continue;
                    float glow = 0.7f + 0.3f * MathUtils.sin(envTimer * 2f + i);
                    shapes.setColor(ac[0] * glow, ac[1] * 0.3f * glow, ac[2] * glow, 1f);
                    shapes.triangle(TUNNEL_LEFT - 5, cy, TUNNEL_LEFT - 18, cy - 10, TUNNEL_LEFT - 18, cy + 10);
                    shapes.triangle(TUNNEL_LEFT - 5, cy + 5, TUNNEL_LEFT - 14, cy - 5, TUNNEL_LEFT - 14, cy + 20);
                    shapes.triangle(TUNNEL_RIGHT + 5, cy, TUNNEL_RIGHT + 18, cy - 10, TUNNEL_RIGHT + 18, cy + 10);
                }
                break;
            case LAVA_CAVERN:
                float lavaFlicker = 0.6f + 0.4f * MathUtils.sin(envTimer * 6f);
                shapes.setColor(ac[0] * lavaFlicker, ac[1] * 0.25f * lavaFlicker, 0f, 1f);
                shapes.rect(TUNNEL_LEFT + 10, 0, TUNNEL_W - 20, 18);
                for (int i = 0; i < 6; i++) {
                    float cy = (i * 80 + scrollMid * 1.5f) % (80 * 6);
                    shapes.setColor(ac[0] * lavaFlicker, ac[1] * 0.2f, 0f, 0.8f);
                    shapes.rectLine(TUNNEL_LEFT - 12, cy, TUNNEL_LEFT - 5, cy + 30, 3);
                    shapes.rectLine(TUNNEL_RIGHT + 12, cy + 10, TUNNEL_RIGHT + 5, cy + 40, 3);
                }
                break;
        }
        shapes.end();
    }

    private void drawTorches(float[] ac, float flicker, float spacing, float speedMult) {
        for (int i = 0; i < 5; i++) {
            float ty = (i * spacing + scrollMid * speedMult * 80f) % (spacing * 5);
            if (ty < 0 || ty > SH) continue;
            shapes.setColor(ac[0] * flicker, ac[1] * flicker * 0.55f, ac[2] * flicker * 0.1f, 1f);
            shapes.triangle(TUNNEL_LEFT - 5, ty, TUNNEL_LEFT - 15, ty, TUNNEL_LEFT - 10, ty + 22);
            shapes.triangle(TUNNEL_RIGHT + 5, ty, TUNNEL_RIGHT + 15, ty, TUNNEL_RIGHT + 10, ty + 22);
        }
    }

    // =========================================================
    //  DRAW — ТОННЕЛЬ
    // =========================================================
    private void drawTunnel() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.35f, 0.22f, 0.10f, 1f);
        for (int i = -1; i < 9; i++) {
            float sy = (i * 80f + scrollNear) % (80f * 9);
            if (sy < -20 || sy > SH + 20) continue;
            shapes.rect(TUNNEL_LEFT + 10, sy - 5, TUNNEL_W - 20, 12);
        }

        shapes.setColor(0.65f, 0.60f, 0.55f, 1f);
        shapes.rect(LEFT_RAIL - 5, 0, 10, SH);
        shapes.rect(RIGHT_RAIL - 5, 0, 10, SH);
        shapes.setColor(0.85f, 0.82f, 0.80f, 1f);
        shapes.rect(LEFT_RAIL - 2, 0, 3, SH);
        shapes.rect(RIGHT_RAIL - 2, 0, 3, SH);

        shapes.setColor(0.28f, 0.22f, 0.16f, 1f);
        shapes.rect(TUNNEL_LEFT - 12, 0, 12, SH);
        shapes.rect(TUNNEL_RIGHT, 0, 12, SH);

        drawCart();

        shapes.end();
    }

    private void drawCart() {
        float cx   = player.x;
        float cy   = 28f;
        float alpha = player.dodging ? 0.55f : 1f;

        shapes.setColor(0f, 0f, 0f, 0.35f * alpha);
        shapes.ellipse(cx - 30, cy - 10, 60, 14);

        shapes.setColor(0.55f * alpha, 0.40f * alpha, 0.20f * alpha, 1f);
        shapes.rect(cx - 30, cy, 60, 26);
        shapes.setColor(0.40f * alpha, 0.28f * alpha, 0.12f * alpha, 1f);
        shapes.rect(cx - 30, cy + 22, 60, 5);
        shapes.rect(cx - 30, cy, 5, 26);
        shapes.rect(cx + 25, cy, 5, 26);
        shapes.rect(cx - 12, cy, 4, 22);

        shapes.setColor(0.18f * alpha, 0.18f * alpha, 0.18f * alpha, 1f);
        shapes.ellipse(cx - 26, cy - 10, 18, 12);
        shapes.ellipse(cx + 8, cy - 10, 18, 12);
        shapes.setColor(0.45f * alpha, 0.35f * alpha, 0.20f * alpha, 1f);
        shapes.rectLine(cx - 17, cy - 4, cx - 17, cy + 2, 2);
        shapes.rectLine(cx + 17, cy - 4, cx + 17, cy + 2, 2);

        if (!player.dodging) {
            shapes.setColor(0.25f, 0.50f, 1.00f, 1f);
            shapes.rect(cx - 10, cy + 22, 20, 26);
            shapes.setColor(0.95f, 0.80f, 0.65f, 1f);
            shapes.ellipse(cx - 9, cy + 44, 18, 18);
            shapes.setColor(0.8f, 0.6f, 0.1f, 1f);
            shapes.ellipse(cx - 10, cy + 50, 20, 12);

            float mx    = screenToWorldX(Gdx.input.getX());
            float my    = screenToWorldY(Gdx.input.getY());
            float angle  = (float) Math.atan2(my - (cy + 48), mx - cx);
            float gunLen = 22f;
            shapes.setColor(0.55f, 0.55f, 0.55f, 1f);
            shapes.rectLine(cx, cy + 48,
                cx + (float) Math.cos(angle) * gunLen,
                cy + 48 + (float) Math.sin(angle) * gunLen, 4f);
        } else {
            shapes.setColor(0.25f, 0.50f, 1.00f, 0.6f);
            shapes.rect(cx - 14, cy + 22, 28, 18);
        }

        if (player.shield) {
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            float pulse = 0.7f + 0.3f * MathUtils.sin(envTimer * 8f);
            shapes.setColor(0.3f, 0.6f, 1f, pulse);
            shapes.ellipse(cx - 38, cy - 8, 76, 80);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        }

        // Рисуем союзника (максимум 1)
        for (Teammate tm : teammates) drawTeammate(tm);
    }

    private void drawTeammate(Teammate tm) {
        float tx = tm.x, ty = tm.y + 28;
        switch (tm.teammateType) {
            case MINER:    shapes.setColor(0.6f, 0.35f, 0.15f, 1f); break;
            case ENGINEER: shapes.setColor(0.2f, 0.6f,  0.3f,  1f); break;
            case SCOUT:    shapes.setColor(0.7f, 0.2f,  0.6f,  1f); break;
        }
        shapes.rect(tx - 8, ty, 16, 22);
        shapes.setColor(0.95f, 0.80f, 0.65f, 1f);
        shapes.ellipse(tx - 7, ty + 20, 14, 14);

        // HP бар союзника
        shapes.setColor(0.7f, 0.1f, 0.1f, 1f);
        shapes.rect(tx - 10, ty + 36, 20, 3);
        shapes.setColor(0.1f, 0.8f, 0.1f, 1f);
        shapes.rect(tx - 10, ty + 36, 20f * tm.health / Teammate.MAX_HEALTH, 3);
    }

    // =========================================================
    //  DRAW — ПРЕПЯТСТВИЯ
    // =========================================================
    private void drawObstacles() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Obstacle o : facade.getObstacles()) {
            if (!o.active) continue;
            if (o.type == Obstacle.ObstacleType.ROCK) {
                shapes.setColor(0.38f, 0.32f, 0.26f, 1f);
                shapes.ellipse(o.x, o.y, o.width, o.height);
                shapes.setColor(0.50f, 0.44f, 0.36f, 1f);
                shapes.ellipse(o.x + 4, o.y + 4, o.width - 10, o.height - 8);
            } else {
                shapes.setColor(0.42f, 0.28f, 0.14f, 1f);
                shapes.rect(o.x, o.y, o.width, o.height);
                shapes.setColor(0.55f, 0.38f, 0.20f, 1f);
                shapes.rect(o.x + 3, o.y + 3, o.width - 6, 4);
                shapes.setColor(0.65f, 0.60f, 0.55f, 1f);
                shapes.ellipse(o.x + 4, o.y + 2, 8, 8);
                shapes.ellipse(o.x + o.width - 12, o.y + 2, 8, 8);
            }
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.2f, 0.15f, 0.1f, 0.8f);
        for (Obstacle o : facade.getObstacles()) {
            if (!o.active) continue;
            if (o.type == Obstacle.ObstacleType.ROCK)
                shapes.ellipse(o.x, o.y, o.width, o.height);
            else
                shapes.rect(o.x, o.y, o.width, o.height);
        }
        shapes.end();
    }

    // =========================================================
    //  DRAW — СУЩНОСТИ
    // =========================================================
    private void drawEntities() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Враги
        for (Enemy e : facade.getEnemies()) {
            float ex = e.x, ey = e.y;
            if (e instanceof BossEnemy) { drawBoss(e); continue; }
            switch (e.getType()) {
                case "Zombie":
                    shapes.setColor(0.10f, 0.75f, 0.15f, 1f);
                    shapes.rect(ex - 15, ey - 15, 30, 30);
                    shapes.setColor(0.05f, 0.40f, 0.08f, 1f);
                    shapes.rectLine(ex-15,ey-15,ex+15,ey-15,2); shapes.rectLine(ex-15,ey+15,ex+15,ey+15,2);
                    shapes.rectLine(ex-15,ey-15,ex-15,ey+15,2); shapes.rectLine(ex+15,ey-15,ex+15,ey+15,2);
                    shapes.setColor(1f,0f,0f,1f);
                    shapes.rect(ex-8,ey+4,5,5); shapes.rect(ex+3,ey+4,5,5);
                    break;
                case "Spider":
                    shapes.setColor(0.60f,0.15f,0.85f,1f);
                    shapes.rect(ex-13,ey-13,26,26);
                    shapes.setColor(0.35f,0.05f,0.50f,1f);
                    shapes.rectLine(ex-13,ey-13,ex+13,ey-13,2); shapes.rectLine(ex-13,ey+13,ex+13,ey+13,2);
                    shapes.rectLine(ex-13,ey-13,ex-13,ey+13,2); shapes.rectLine(ex+13,ey-13,ex+13,ey+13,2);
                    shapes.rectLine(ex-13,ey+5,ex-24,ey+12,2); shapes.rectLine(ex-13,ey-5,ex-24,ey-12,2);
                    shapes.rectLine(ex+13,ey+5,ex+24,ey+12,2); shapes.rectLine(ex+13,ey-5,ex+24,ey-12,2);
                    break;
                case "Bat":
                    shapes.setColor(0.55f,0.55f,0.60f,1f);
                    shapes.rect(ex-13,ey-10,26,20);
                    shapes.setColor(0.30f,0.30f,0.35f,1f);
                    shapes.triangle(ex-13,ey,ex-30,ey+10,ex-30,ey-10);
                    shapes.triangle(ex+13,ey,ex+30,ey+10,ex+30,ey-10);
                    break;
            }
            float hpRatio = (float) e.health / e.maxHealth;
            shapes.setColor(0.7f,0.1f,0.1f,1f); shapes.rect(e.x-15,e.y+18,30,4);
            shapes.setColor(0.1f,0.9f,0.1f,1f); shapes.rect(e.x-15,e.y+18,30*hpRatio,4);
        }

        // Пули врагов
        for (Enemy e : facade.getEnemies()) {
            for (Bullet b : e.getEnemyBullets()) {
                if (!b.active) continue;
                shapes.setColor(1f, 0.2f, 0.1f, 1f);
                shapes.circle(b.x, b.y, 4f, 8);
            }
        }

        // Пули игрока
        for (Bullet b : player.getBullets()) {
            if (!b.active) continue;
            switch (b.type) {
                case NORMAL:    shapes.setColor(1f, 0.95f, 0.2f, 1f); shapes.circle(b.x,b.y,5f,10);  break;
                case PIERCING:  shapes.setColor(0.3f,0.9f,1f,1f);     shapes.rect(b.x-3,b.y-7,6,14); break;
                case EXPLOSIVE: shapes.setColor(1f,0.5f,0.1f,1f);     shapes.circle(b.x,b.y,8f,12);  break;
            }
            float angle = (float) Math.atan2(b.velocityY, b.velocityX);
            shapes.setColor(1f,0.55f,0.1f,0.55f);
            shapes.rectLine(b.x,b.y,b.x-(float)Math.cos(angle)*14,b.y-(float)Math.sin(angle)*14,3f);
        }

        // Пули союзника — зелёные
        for (Bullet b : teammateBullets) {
            if (!b.active) continue;
            shapes.setColor(0.2f, 1f, 0.4f, 1f);
            shapes.circle(b.x, b.y, 4f, 8);
        }

        // Дропы
        for (DropItem d : facade.getDrops()) {
            if (d.collected) continue;
            switch (d.type) {
                case HEALTH:
                    shapes.setColor(0.9f,0.1f,0.2f,1f);
                    shapes.rect(d.x-6,d.drawY-2,12,4); shapes.rect(d.x-2,d.drawY-6,4,12);
                    break;
                case SHIELD:
                    shapes.setColor(0.2f,0.5f,1f,1f);
                    shapes.ellipse(d.x-10,d.drawY-10,20,20);
                    break;
                case SPEED:
                    shapes.setColor(1f,0.85f,0.1f,1f);
                    shapes.triangle(d.x,d.drawY+12,d.x-10,d.drawY-10,d.x+10,d.drawY-10);
                    break;
            }
        }

        // Пикапы союзников
        for (TeammatePickup tp : facade.getPickups()) {
            if (tp.collected) continue;
            float pulse = 0.8f + 0.2f * MathUtils.sin(envTimer * 5f);
            shapes.setColor(0.3f, pulse, 0.5f, 1f);
            shapes.ellipse(tp.x - 14, tp.drawY - 14, 28, 28);
            shapes.setColor(0.95f, 0.80f, 0.65f, 1f);
            shapes.ellipse(tp.x - 6, tp.drawY - 2, 12, 12);
            shapes.setColor(0.3f, 0.7f, 0.35f, 1f);
            shapes.rect(tp.x - 6, tp.drawY - 12, 12, 14);
        }

        // Hit-эффекты
        for (HitEffect h : hitEffects) {
            float alpha = h.life / HitEffect.MAX_LIFE;
            if (h.explosive) {
                shapes.setColor(1f,0.5f,0.1f,alpha*0.7f);
                shapes.circle(h.x,h.y,55f*(1f-alpha+0.2f),16);
                shapes.setColor(1f,0.85f,0.3f,alpha);
                shapes.circle(h.x,h.y,28f*(1f-alpha+0.3f),12);
            } else {
                shapes.setColor(1f,0.8f,0f,alpha);
                shapes.circle(h.x,h.y,14f*(1f-alpha+0.3f),10);
            }
        }
        shapes.end();

        // Прицел
        float mx = screenToWorldX(Gdx.input.getX());
        float my = screenToWorldY(Gdx.input.getY());
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f,1f,1f,0.75f);
        shapes.circle(mx,my,8f,16);
        shapes.line(mx-14,my,mx+14,my);
        shapes.line(mx,my-14,mx,my+14);
        shapes.end();
    }

    private void drawBoss(Enemy e) {
        float ex = e.x, ey = e.y;
        float pulse = 0.75f + 0.25f * MathUtils.sin(envTimer * 3f);
        shapes.setColor(0.85f*pulse, 0.1f, 0.1f*pulse, 1f);
        shapes.rect(ex-30,ey-30,60,60);
        shapes.setColor(0.55f,0.05f,0.05f,1f);
        shapes.rectLine(ex-30,ey-30,ex+30,ey-30,3); shapes.rectLine(ex-30,ey+30,ex+30,ey+30,3);
        shapes.rectLine(ex-30,ey-30,ex-30,ey+30,3); shapes.rectLine(ex+30,ey-30,ex+30,ey+30,3);
        shapes.setColor(1f,1f,0f,pulse);
        shapes.ellipse(ex-14,ey+8,12,12); shapes.ellipse(ex+2,ey+8,12,12);
        float hpRatio = (float)e.health/e.maxHealth;
        shapes.setColor(0.7f,0.05f,0.05f,1f); shapes.rect(ex-30,ey+34,60,6);
        shapes.setColor(1f,0.1f,0.1f,1f);    shapes.rect(ex-30,ey+34,60*hpRatio,6);
    }

    // =========================================================
    //  DRAW — HUD
    // =========================================================
    private void drawHUD() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f,0f,0f,0.6f); shapes.rect(0, SH-52, SW, 52);
        shapes.setColor(0f,0f,0f,0.4f); shapes.rect(0, 0, SW, 22);
        shapes.end();

        AssetManager am = AssetManager.getInstance();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        am.font.getData().setScale(1.0f);
        am.font.setColor(Color.WHITE);
        am.font.draw(batch, "Score: " + (int) displayScore, 10, SH - 10);
        am.font.draw(batch, "Wave:  " + wave, 10, SH - 28);

        am.font.getData().setScale(0.85f);
        am.font.setColor(1f, 0.85f, 0.35f, biomeTransition);
        am.font.draw(batch, biome.displayName(), SW / 2 - 60, SH - 10);

        am.font.getData().setScale(1.0f);
        am.font.draw(batch, "HP:", SW - 160, SH - 10);
        for (int i = 0; i < player.maxHealth; i++) {
            am.font.setColor(i < player.health ? Color.RED : Color.DARK_GRAY);
            am.font.draw(batch, "♥", SW - 115 + i * 22, SH - 10);
        }

        if (player.shield) {
            am.font.setColor(0.3f, 0.6f, 1f, 1f);
            am.font.draw(batch, "⬡ SHIELD", 10, SH - 46);
        }
        if (player.speedMultiplier > 1f) {
            am.font.setColor(1f, 0.85f, 0.1f, 1f);
            am.font.draw(batch, "⚡ SPEED", 100, SH - 46);
        }

        float dodgeRatio = player.getDodgeCooldownRatio();
        am.font.setColor(dodgeRatio >= 1f ? Color.CYAN : Color.GRAY);
        am.font.getData().setScale(0.85f);
        am.font.draw(batch, "SHIFT:" + (dodgeRatio >= 1f ? "READY" : "..."), SW - 165, SH - 30);

        if (comboCount > 0) {
            float alpha = comboActive ? (0.7f + 0.3f * MathUtils.sin(envTimer * 8f)) : 0.9f;
            am.font.getData().setScale(comboActive ? 1.4f : 1.1f);
            am.font.setColor(1f, comboActive ? 0.85f : 0.65f, 0f, alpha);
            am.font.draw(batch, comboActive ? "COMBO x2!" : "COMBO: " + comboCount, SW / 2 - 50, SH - 30);
        }

        am.font.getData().setScale(0.85f);
        am.font.setColor(0.7f, 0.7f, 0.7f, 1f);
        am.font.draw(batch, "[1]Single [2]Spread [3]Pierce [4]Bomb  [A/D]Move [SHIFT]Dodge [P]Pause [F11]FS", 5, 16);

        if (!teammates.isEmpty()) {
            Teammate tm = teammates.get(0);
            am.font.setColor(0.3f, 1f, 0.5f, 1f);
            am.font.getData().setScale(0.85f);
            am.font.draw(batch, "Союзник HP: " + tm.health + "/" + Teammate.MAX_HEALTH, SW - 180, SH - 46);
        }

        am.font.getData().setScale(1.0f);
        batch.end();
    }

    // =========================================================
    //  OVERLAYS
    // =========================================================
    private void renderPauseOverlay() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f,0f,0f,0.65f); shapes.rect(0,0,SW,SH);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        AssetManager am = AssetManager.getInstance();
        am.font.getData().setScale(1.6f);
        am.font.setColor(Color.YELLOW);
        am.font.draw(batch, "⏸  PAUSED", 240, 280);
        am.font.getData().setScale(1.0f);
        am.font.setColor(Color.WHITE);
        am.font.draw(batch, "Press  P  to resume", 210, 248);
        am.font.getData().setScale(1.0f);
        batch.end();
    }

    private void renderGameOver() {
        Gdx.gl.glClearColor(0.04f,0f,0f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.35f,0f,0f,0.95f);
        shapes.rect(100,100,440,280);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        AssetManager am = AssetManager.getInstance();
        am.font.getData().setScale(1.8f);
        am.font.setColor(Color.RED);
        am.font.draw(batch,"GAME OVER",210,365);

        am.font.getData().setScale(1.05f);
        am.font.setColor(Color.WHITE);
        am.font.draw(batch,"Score: " + score,240,320);
        am.font.draw(batch,"Best:  " + bestScore,240,296);
        am.font.draw(batch,"Waves: " + wave,240,272);
        am.font.draw(batch,"Kills: " + totalKills,240,248);

        if (score >= bestScore) {
            am.font.setColor(1f,0.9f,0.1f,1f);
            am.font.draw(batch,"★ NEW RECORD!",225,222);
        }

        am.font.setColor(Color.LIGHT_GRAY);
        am.font.getData().setScale(1.0f);
        am.font.draw(batch,"[R] Restart    [ESC] Menu",185,175);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new GameScreen(game)); dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game)); dispose();
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private float[] lerpBiomeColor(float[] from, float[] to) {
        float t = biomeTransition;
        return new float[]{
            from[0] + (to[0]-from[0])*t,
            from[1] + (to[1]-from[1])*t,
            from[2] + (to[2]-from[2])*t
        };
    }

    private void saveBest() {
        if (score > bestScore) {
            bestScore = score;
            Gdx.app.getPreferences("RailShooter").putInteger("best", bestScore).flush();
        }
    }

    // =========================================================
    //  OBSERVER callbacks
    // =========================================================
    @Override public void onEnemyKilled(int points) {
        score += comboActive ? points * 2 : points;
    }
    @Override public void onPlayerDamaged(int dmg) { player.takeDamage(dmg); }
    @Override public void onGameOver() { stateManager.setState(new GameOverState()); }

    // =========================================================
    //  LIFECYCLE
    // =========================================================
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() { batch.dispose(); shapes.dispose(); }
}
