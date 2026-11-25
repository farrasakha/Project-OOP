package com.uiescape.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import com.uiescape.frontend.player.Player;
import com.uiescape.frontend.player.PlayerStats;

import com.uiescape.frontend.level.Level;
import com.uiescape.frontend.level.InteractionMediator;

import com.uiescape.frontend.enemy.Monkey;
import com.uiescape.frontend.projectiles.ProjectileSystem;

import com.uiescape.frontend.state.GameManager;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private GameHUD hud;
    private GameManager gameManager;
    private Texture fruitSheet;
    private TextureRegion bananaImage;
    private Animation<TextureRegion> peelAnimation;

    private Level level;
    private Player player;
    private PlayerStats stats;

    private Monkey monkey;
    private ProjectileSystem projectiles;
    private InteractionMediator mediator;

    private int stageIndex;

    private float shootCooldown;
    private float animationTime;

    @Override
    public void create() {

        batch = new SpriteBatch();

        hud = new GameHUD();
        gameManager = new GameManager();

        fruitSheet = new Texture("items/Fruit+.png");
        fruitSheet.setFilter(Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest);
        bananaImage = new TextureRegion(fruitSheet, 112, 0, 16, 16);
        peelAnimation = new Animation<>(0.15f,
            new TextureRegion(fruitSheet, 112, 64, 16, 16),
            new TextureRegion(fruitSheet, 112, 80, 16, 16));

        gameManager.showMenu();
    }

    private void startStage(
        int newStageIndex,
        int startingLives
    ) {

        disposeStage();

        if (!Level.isReady(newStageIndex)) {

            Gdx.app.error(
                "Main",
                "Stage tidak tersedia: " + newStageIndex
            );

            gameManager.showVictory();
            return;
        }

        stageIndex = newStageIndex;

        // MAP DAN LEVEL

        level = new Level(stageIndex);

        // PLAYER

        Rectangle spawn = level.getPlayerSpawn();

        player = new Player(spawn.x, spawn.y);

        player.setWorldSize(
            level.getWorldWidth(),
            level.getWorldHeight()
        );

        player.setCollisions(
            level.getCollisions()
        );

        // PLAYER DATA

        stats = new PlayerStats(startingLives);

        Rectangle monkeySpawn = level.getMonkeySpawn();
        monkey = new Monkey(monkeySpawn.x, monkeySpawn.y,
            level.getPatrolMinY(), level.getPatrolMaxY());

        projectiles = new ProjectileSystem(bananaImage, peelAnimation);
        mediator = new InteractionMediator(level, new InventoryAdapter(stats));

        // OBSERVER

        hud.setStats(stats);

        gameManager.startPlaying();

        stats.addListener(
            gameManager::onStatsChanged
        );

        shootCooldown = 0f;
        animationTime = 0f;
    }

    private void handleGameFlow() {

        GameManager.Action action =
            gameManager.handleInput();

        switch (action) {

            case START:

                startStage(
                    0,
                    PlayerStats.MAX_LIVES
                );

                break;

            case RESTART:

                startStage(
                    stageIndex,
                    PlayerStats.MAX_LIVES
                );

                break;

            case NEXT:

                int nextIndex = stageIndex + 1;

                if (Level.isReady(nextIndex)) {

                    int remainingLives =
                        stats.getLives();

                    startStage(
                        nextIndex,
                        remainingLives
                    );

                } else {

                    disposeStage();

                    gameManager.showVictory();
                }

                break;

            case MENU:

                disposeStage();

                gameManager.showMenu();

                break;

            default:
                break;
        }
    }

    private void updatePlaying(float delta) {

        if (level == null) {
            return;
        }

        animationTime += delta;

        // UPDATE PLAYER

        stats.update(delta);
        player.update(delta);

        // ITEM DAN INTERAKSI

        mediator.update(
            player.getBounds(),
            Gdx.input.isKeyJustPressed(Input.Keys.E),
            delta
        );

        // PLAYER SHOOT

        shootCooldown -= delta;

        if (stats.hasBananaWeapon()
            && shootCooldown <= 0f
            && Gdx.input.isKeyJustPressed(
            Input.Keys.SPACE
        )) {

            projectiles.shoot(
                false,
                player.getShootX(),
                player.getShootY(),
                player.getFacingX(),
                player.getFacingY()
            );

            shootCooldown = 0.25f;
        }

        // MONKEY MOVEMENT

        monkey.update(delta);

        // PROJECTILE COLLISION DAN DAMAGE

        projectiles.update(
            delta,
            player,
            stats,
            monkey,
            level
        );

        // GAME OVER CHECK

        if (!gameManager.isPlaying()) {
            return;
        }

        // MONKEY SHOOT

        monkey.tryShoot(
            player,
            projectiles
        );

        // CEK FINISH

        if (mediator.checkFinish(
            player.getBounds(),
            monkey.isStunned()
        )) {

            gameManager.completeLevel();
        }
    }

    private void renderStage() {

        if (level == null) {
            return;
        }

        // MAP BACKGROUND

        level.renderBackground();

        batch.setProjectionMatrix(
            level.getCamera().combined
        );

        batch.begin();

        // ITEMS

        level.renderItems(batch);

        mediator.renderDrops(batch, bananaImage);

        // MONKEY

        monkey.render(batch);

        // PLAYER INVULNERABILITY BLINK

        boolean visible =
            !stats.isInvulnerable()
                || ((int) (animationTime * 12f)) % 2 == 0;

        if (visible) {

            player.render(batch);
        }

        // PROJECTILES

        projectiles.render(batch);

        batch.end();

        // MAP FOREGROUND

        level.renderForeground();

        // HUD

        hud.renderPlaying(
            stageIndex + 1,
            mediator.getMessage(
                player.getBounds()
            )
        );
    }

    @Override
    public void render() {

        float delta = Math.min(
            Gdx.graphics.getDeltaTime(),
            0.05f
        );

        Gdx.gl.glClearColor(
            0.15f,
            0.15f,
            0.2f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        // GAME STATE INPUT

        handleGameFlow();

        // UPDATE ONLY WHEN PLAYING

        if (gameManager.isPlaying()) {

            updatePlaying(delta);
        }

        // RENDER GAME

        renderStage();

        // RENDER MENU AND OVERLAY

        hud.renderOverlay(
            gameManager.getState(),
            stageIndex + 1
        );
    }

    @Override
    public void resize(
        int width,
        int height
    ) {

        if (level != null) {

            level.resize(width, height);
        }

        if (hud != null) {

            hud.resize(width, height);
        }
    }

    private void disposeStage() {

        if (hud != null) {

            hud.setStats(null);
        }

        if (projectiles != null) {

            projectiles.clear();
            projectiles = null;
        }

        if (monkey != null) {

            monkey.dispose();
            monkey = null;
        }

        if (player != null) {

            player.dispose();
            player = null;
        }

        if (level != null) {

            level.dispose();
            level = null;
        }

        mediator = null;
        stats = null;
    }

    @Override
    public void dispose() {

        disposeStage();

        if (hud != null) {

            hud.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (fruitSheet != null) {
            fruitSheet.dispose();
        }
    }
    // Adapter: connects Kyla's Inventory interface to Diva's PlayerStats.
    private static final class InventoryAdapter implements InteractionMediator.Inventory {
        private final PlayerStats stats;

        InventoryAdapter(PlayerStats stats) {
            this.stats = stats;
        }

        @Override public void addCoin() { stats.addCoin(); }
        @Override public void addKey() { stats.addKey(); }
        @Override public boolean useKey() { return stats.useKey(); }
        @Override public boolean addLife() { return stats.addLife(); }
        @Override public void unlockBananaWeapon() { stats.unlockBananaWeapon(); }
        @Override public boolean hasMap() { return stats.hasMap(); }
        @Override public boolean exchangeMap(int cost) { return stats.exchangeMap(cost); }
    }
}
