package com.uiescape.frontend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.uiescape.frontend.state.GameManager;
import com.uiescape.frontend.player.PlayerStats;

public class GameHUD {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private final ShapeRenderer shapes;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final Texture itemSheet;
    private final Texture fruitSheet;
    private final Texture mapItemSheet;

    private final TextureRegion heartIcon;
    private final TextureRegion coinIcon;
    private final TextureRegion keyIcon;
    private final TextureRegion bananaIcon;
    private final TextureRegion mapIcon;

    private PlayerStats observedStats;

    private int lives;
    private String coinText = "0";
    private String keyText = "0";

    private boolean bananaWeapon;
    private boolean hasMap;

    private final PlayerStats.Listener listener = this::sync;

    public GameHUD() {

        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();

        shapes = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new FitViewport(320, 240, camera);

        font.getData().setScale(0.8f);

        // Load asset HUD

        itemSheet = loadTexture("ui/dungeons_demo.png");

        heartIcon = new TextureRegion(
            itemSheet, 210, 81, 12, 13
        );

        coinIcon = new TextureRegion(
            itemSheet, 193, 81, 12, 13
        );

        keyIcon = new TextureRegion(
            itemSheet, 193, 65, 13, 17
        );

        // Load banana icon

        fruitSheet = loadTexture("items/Fruit+.png");

        bananaIcon = new TextureRegion(
            fruitSheet, 112, 0, 16, 16
        );

        // Load map icon

        mapItemSheet = loadTexture(
            "items/treasure_items.png"
        );

        mapIcon = new TextureRegion(
            mapItemSheet, 160, 192, 32, 32
        );

        resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
    }

    private Texture loadTexture(String path) {

        Texture texture = new Texture(path);

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        return texture;
    }

    // Register HUD as an observer

    public void setStats(PlayerStats stats) {

        if (observedStats != null) {
            observedStats.removeListener(listener);
        }

        observedStats = stats;

        if (observedStats != null) {

            observedStats.addListener(listener);

        } else {

            lives = 0;
            coinText = "0";
            keyText = "0";
            bananaWeapon = false;
            hasMap = false;
        }
    }

    // Observer Pattern

    private void sync(PlayerStats stats) {

        lives = stats.getLives();

        coinText = String.valueOf(
            stats.getCoins()
        );

        keyText = String.valueOf(
            stats.getKeys()
        );

        bananaWeapon = stats.hasBananaWeapon();

        hasMap = stats.hasMap();
    }

    private void prepareViewport() {

        viewport.apply();

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        shapes.setProjectionMatrix(camera.combined);
    }

    // Gameplay HUD

    public void renderPlaying(
        int stageNumber,
        String message
    ) {

        prepareViewport();

        batch.begin();

        // HEART

        for (int i = 0; i < lives; i++) {

            batch.draw(
                heartIcon,
                10 + i * 17,
                213,
                14,
                15
            );
        }

        // STAGE NUMBER

        centerText(
            "Stage " + stageNumber,
            227
        );

        // COIN

        batch.draw(
            coinIcon,
            283,
            213,
            14,
            15
        );

        font.draw(
            batch,
            coinText,
            303,
            226
        );

        // KEY

        batch.draw(
            keyIcon,
            283,
            192,
            14,
            17
        );

        font.draw(
            batch,
            keyText,
            303,
            205
        );

        // BANANA WEAPON

        if (bananaWeapon) {

            batch.draw(
                bananaIcon,
                268,
                171,
                16,
                16
            );

            font.draw(
                batch,
                "SPC",
                286,
                184
            );
        }

        // MAP ITEM

        if (hasMap) {

            batch.draw(
                mapIcon,
                270,
                143,
                22,
                22
            );

            font.draw(
                batch,
                "MAP",
                292,
                157
            );
        }

        batch.end();

        // INTERACTION MESSAGE

        if (message != null && !message.isEmpty()) {

            drawPanel(
                5,
                3,
                310,
                20,
                0.75f
            );

            batch.begin();

            centerText(message, 18);

            batch.end();
        }
    }

    // Draw transparent background panel

    private void drawPanel(
        float x,
        float y,
        float width,
        float height,
        float alpha
    ) {

        Gdx.gl.glEnable(GL20.GL_BLEND);

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0f,
            0f,
            0f,
            alpha
        );

        shapes.rect(
            x,
            y,
            width,
            height
        );

        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // Draw centered text

    private void centerText(
        String text,
        float y
    ) {

        layout.setText(font, text);

        font.draw(
            batch,
            layout,
            (320f - layout.width) / 2f,
            y
        );
    }

    // Game state interface

    public void renderOverlay(
        GameManager.State state,
        int stageNumber
    ) {

        if (state == GameManager.State.PLAYING) {
            return;
        }

        prepareViewport();

        drawPanel(
            0,
            0,
            320,
            240,
            0.8f
        );

        batch.begin();

        font.getData().setScale(1.2f);

        switch (state) {

            // MAIN MENU

            case MAIN_MENU:

                centerText(
                    "UI ESCAPE",
                    176
                );

                font.getData().setScale(0.8f);

                centerText(
                    "Press ENTER to start",
                    135
                );

                centerText("WASD: Move", 97);

                centerText("E: Interact", 79);

                centerText(
                    "SPACE: Throw banana",
                    61
                );

                centerText(
                    "P / ESC: Pause",
                    43
                );

                break;

            // PAUSE MENU

            case PAUSED:

                centerText(
                    "PAUSED",
                    150
                );

                font.getData().setScale(0.8f);

                centerText(
                    "Press P or ESC to resume",
                    108
                );

                break;

            // LEVEL COMPLETE

            case LEVEL_COMPLETE:

                centerText(
                    "STAGE " + stageNumber + " SELESAI",
                    150
                );

                font.getData().setScale(0.8f);

                centerText(
                    "Press ENTER to continue",
                    108
                );

                break;

            // GAME OVER

            case GAME_OVER:

                centerText(
                    "GAME OVER",
                    150
                );

                font.getData().setScale(0.8f);

                centerText(
                    "ENTER: Retry stage",
                    110
                );

                centerText(
                    "ESC: Main menu",
                    88
                );

                break;

            // VICTORY

            case VICTORY:

                centerText(
                    "VICTORY!",
                    166
                );

                font.getData().setScale(0.8f);

                centerText(
                    "Semua stage tersedia selesai",
                    130
                );

                centerText(
                    "Stage berikutnya segera hadir",
                    108
                );

                centerText(
                    "ENTER: Main menu",
                    73
                );

                break;

            default:

                break;
        }

        font.getData().setScale(0.8f);

        batch.end();
    }

    // Window resize

    public void resize(
        int width,
        int height
    ) {

        viewport.update(
            width,
            height,
            true
        );
    }

    // Dispose resources

    public void dispose() {

        setStats(null);

        itemSheet.dispose();

        fruitSheet.dispose();

        mapItemSheet.dispose();

        font.dispose();

        batch.dispose();

        shapes.dispose();
    }
}
