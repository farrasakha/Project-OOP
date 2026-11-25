package com.uiescape.frontend.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.uiescape.frontend.items.Coin;
import com.uiescape.frontend.items.Key;
import com.uiescape.frontend.items.Chest;

public class Level {

    private static final String[] MAP_PATHS = {
        "maplevelsatu/mapdua.tmx",
        "maplevelsatu/maptiga.tmx"
    };

    private static final boolean[] READY = {
        true, false
    };

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final float worldWidth;
    private final float worldHeight;

    private final int[] backgroundLayers;
    private final int[] foregroundLayers;

    // Collision
    private final Array<Polygon> collisionShapes =
        new Array<>();

    private final EarClippingTriangulator triangulator =
        new EarClippingTriangulator();

    private final Polygon obstacleTest = new Polygon(
        new float[] {
            0, 0, 1, 0, 1, 1, 0, 1
        }
    );

    // Level items
    private final Array<Coin> coins = new Array<>();
    private final Array<Key> keys = new Array<>();
    private final Array<Chest> chests = new Array<>();

    private Chest bananaChest;
    private Chest heartChest;

    // Item textures
    private Texture itemSheet;
    private Texture chestSheet;

    private TextureRegion coinImage;
    private TextureRegion keyImage;
    private TextureRegion heartImage;

    private TextureRegion closedChestImage;
    private TextureRegion openedChestImage;

    // Gameplay objects
    private final Rectangle playerSpawn;
    private final Rectangle monkeySpawn;
    private final Rectangle exchangeTable;
    private final Rectangle exit;

    private final int coinCost;
    private final float patrolMinY;
    private final float patrolMaxY;

    // =========================
    // LEVEL LOADING
    // =========================

    public static boolean isReady(int stageIndex) {

        return stageIndex >= 0
            && stageIndex < MAP_PATHS.length
            && READY[stageIndex]
            && Gdx.files.internal(
            MAP_PATHS[stageIndex]
        ).exists();
    }

    public Level() {
        this(0);
    }

    public Level(int stageIndex) {

        if (!isReady(stageIndex)) {
            throw new IllegalArgumentException(
                "Level is not ready: " + stageIndex
            );
        }

        map = new TmxMapLoader().load(
            MAP_PATHS[stageIndex]
        );

        renderer = new OrthogonalTiledMapRenderer(map);

        // Map dimensions
        int mapWidth =
            map.getProperties().get("width", Integer.class);

        int mapHeight =
            map.getProperties().get("height", Integer.class);

        int tileWidth =
            map.getProperties().get("tilewidth", Integer.class);

        int tileHeight =
            map.getProperties().get("tileheight", Integer.class);

        worldWidth = mapWidth * tileWidth;
        worldHeight = mapHeight * tileHeight;

        // Camera and viewport
        camera = new OrthographicCamera();

        viewport = new FitViewport(
            worldWidth, worldHeight, camera
        );

        resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Background and foreground layers
        IntArray background = new IntArray();
        IntArray foreground = new IntArray();

        for (int i = 0; i < map.getLayers().getCount(); i++) {

            MapLayer layer = map.getLayers().get(i);

            if (!(layer instanceof TiledMapTileLayer)) {
                continue;
            }

            if ("Foreground".equals(layer.getName())) {
                foreground.add(i);
            } else {
                background.add(i);
            }
        }

        backgroundLayers = background.toArray();
        foregroundLayers = foreground.toArray();

        // Load collision and items
        loadCollision();
        loadItemTextures();

        loadEntities("Coins", "coin");
        loadEntities("Keys", "key");
        loadEntities("Chests", "chest");

        // Read Gameplay object layer
        playerSpawn = readRectangle(
            "PlayerSpawn", 210, 12, 12, 20
        );

        monkeySpawn = readRectangle(
            "Monkey", 243, 138, 16, 16
        );

        exchangeTable = readRectangle(
            "ExchangeTable", 33, 170, 25, 22
        );

        exit = readRectangle(
            "Exit", 254, 220, 19, 16
        );

        // Read custom properties
        coinCost = Math.max(1, (int) readNumber(
            "ExchangeTable", "coinCost", 3
        ));

        patrolMinY = readNumber(
            "Monkey", "patrolMinY", 120
        );

        patrolMaxY = readNumber(
            "Monkey", "patrolMaxY", 170
        );
    }

    // =========================
    // GAMEPLAY OBJECTS
    // =========================

    private MapObject getGameplayObject(String name) {

        MapLayer layer = map.getLayers().get("Gameplay");

        if (layer == null) return null;

        return layer.getObjects().get(name);
    }

    private Rectangle readRectangle(
        String name,
        float x,
        float y,
        float width,
        float height
    ) {

        MapObject object = getGameplayObject(name);

        if (object instanceof RectangleMapObject) {

            Rectangle rect =
                ((RectangleMapObject) object).getRectangle();

            if (rect.width > 0f && rect.height > 0f) {
                return new Rectangle(rect);
            }
        }

        Gdx.app.log(
            "Level", "Using fallback position for " + name
        );

        return new Rectangle(x, y, width, height);
    }

    private float readNumber(
        String name,
        String property,
        float fallback
    ) {

        MapObject object = getGameplayObject(name);

        if (object == null) return fallback;

        Object value = object.getProperties().get(property);

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        return fallback;
    }

    // =========================
    // COLLISION SYSTEM
    // =========================

    private void loadCollision() {

        MapLayer layer = map.getLayers().get("Collision");

        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {

            if (object instanceof PolygonMapObject) {

                Polygon polygon =
                    ((PolygonMapObject) object).getPolygon();

                addPolygonCollision(
                    polygon.getTransformedVertices()
                );

            } else if (object instanceof RectangleMapObject) {

                Rectangle rect =
                    ((RectangleMapObject) object).getRectangle();

                addRectangleCollision(rect);
            }
        }
    }

    private void addRectangleCollision(Rectangle rect) {

        if (rect.width <= 0f || rect.height <= 0f) {
            return;
        }

        collisionShapes.add(new Polygon(
            new float[] {
                rect.x, rect.y,
                rect.x + rect.width, rect.y,
                rect.x + rect.width, rect.y + rect.height,
                rect.x, rect.y + rect.height
            }
        ));
    }

    private void addPolygonCollision(float[] vertices) {

        if (vertices.length < 6) return;

        ShortArray indices =
            triangulator.computeTriangles(vertices);

        for (int i = 0; i < indices.size; i += 3) {

            int a = indices.get(i) * 2;
            int b = indices.get(i + 1) * 2;
            int c = indices.get(i + 2) * 2;

            float ax = vertices[a];
            float ay = vertices[a + 1];

            float bx = vertices[b];
            float by = vertices[b + 1];

            float cx = vertices[c];
            float cy = vertices[c + 1];

            float area =
                (bx - ax) * (cy - ay)
                    - (by - ay) * (cx - ax);

            if (Math.abs(area) < 0.001f) continue;

            collisionShapes.add(new Polygon(
                new float[] {
                    ax, ay, bx, by, cx, cy
                }
            ));
        }
    }

    public Array<Polygon> getCollisions() {
        return collisionShapes;
    }

    public boolean hitsObstacle(Rectangle bounds) {

        obstacleTest.setPosition(
            bounds.x, bounds.y
        );

        obstacleTest.setScale(
            bounds.width, bounds.height
        );

        for (Polygon obstacle : collisionShapes) {

            if (Intersector.overlapConvexPolygons(
                obstacleTest, obstacle
            )) {
                return true;
            }
        }

        return false;
    }

    // =========================
    // ITEM TEXTURES
    // =========================

    private Texture loadTexture(String path) {

        Texture texture = new Texture(path);

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        return texture;
    }

    private void loadItemTextures() {

        itemSheet = loadTexture(
            "ui/dungeons_demo.png"
        );

        coinImage = new TextureRegion(
            itemSheet, 193, 81, 12, 13
        );

        keyImage = new TextureRegion(
            itemSheet, 193, 65, 13, 17
        );

        heartImage = new TextureRegion(
            itemSheet, 210, 81, 12, 13
        );

        chestSheet = loadTexture(
            "ui/dungeons_demo_animated.png"
        );

        closedChestImage = new TextureRegion(
            chestSheet, 4, 88, 24, 24
        );

        openedChestImage = new TextureRegion(
            chestSheet, 100, 88, 24, 24
        );
    }

    // =========================
    // ENTITY LOADING
    // =========================

    private void loadEntities(
        String layerName, String type
    ) {

        MapLayer layer = map.getLayers().get(layerName);

        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {

            if (object instanceof RectangleMapObject) {

                Rectangle rect =
                    ((RectangleMapObject) object).getRectangle();

                createEntity(
                    type, object.getName(), rect
                );
            }
        }
    }

    private void createEntity(
        String type, String name, Rectangle rect
    ) {

        switch (type) {

            case "coin":

                coins.add(new Coin(
                    rect.x, rect.y, coinImage
                ));

                break;

            case "key":

                keys.add(new Key(
                    rect.x, rect.y, keyImage
                ));

                break;

            case "chest":

                Chest chest = new Chest(
                    rect.x,
                    rect.y,
                    closedChestImage,
                    openedChestImage
                );

                chests.add(chest);

                if ("Chest1".equals(name)) {
                    bananaChest = chest;

                } else if ("Chest2".equals(name)) {
                    heartChest = chest;
                }

                addRectangleCollision(
                    chest.getCollisionBounds()
                );

                break;

            default:

                Gdx.app.error(
                    "Level", "Unknown entity: " + type
                );
        }
    }

    // =========================
    // RENDERING
    // =========================

    public void renderBackground() {

        viewport.apply();
        camera.update();

        renderer.setView(camera);
        renderer.render(backgroundLayers);
    }

    public void renderItems(SpriteBatch batch) {

        for (Coin coin : coins) {
            coin.render(batch);
        }

        for (Key key : keys) {
            key.render(batch);
        }

        for (Chest chest : chests) {
            chest.render(batch);
        }
    }

    public void renderForeground() {

        if (foregroundLayers.length > 0) {
            renderer.render(foregroundLayers);
        }
    }

    public void resize(int width, int height) {

        viewport.update(width, height, true);
    }

    // =========================
    // GETTERS
    // =========================

    public OrthographicCamera getCamera() {
        return camera;
    }

    public TiledMap getMap() {
        return map;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public Array<Coin> getCoins() {
        return coins;
    }

    public Array<Key> getKeys() {
        return keys;
    }

    public Array<Chest> getChests() {
        return chests;
    }

    public Chest getBananaChest() {
        return bananaChest;
    }

    public Chest getHeartChest() {
        return heartChest;
    }

    public TextureRegion getHeartImage() {
        return heartImage;
    }

    public Rectangle getPlayerSpawn() {
        return new Rectangle(playerSpawn);
    }

    public Rectangle getMonkeySpawn() {
        return new Rectangle(monkeySpawn);
    }

    public Rectangle getExchangeTable() {
        return new Rectangle(exchangeTable);
    }

    public Rectangle getExit() {
        return new Rectangle(exit);
    }

    public int getCoinCost() {
        return coinCost;
    }

    public float getPatrolMinY() {
        return patrolMinY;
    }

    public float getPatrolMaxY() {
        return patrolMaxY;
    }

    // =========================
    // RESOURCE CLEANUP
    // =========================

    public void dispose() {

        renderer.dispose();
        map.dispose();

        itemSheet.dispose();
        chestSheet.dispose();
    }
}
