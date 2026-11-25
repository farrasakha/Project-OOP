package com.uiescape.frontend.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {

    private final Texture spriteSheet;

    private final Animation<TextureRegion> walkDown;
    private final Animation<TextureRegion> walkUp;
    private final Animation<TextureRegion> walkLeft;
    private final Animation<TextureRegion> walkRight;

    private Animation<TextureRegion> currentAnimation;
    private TextureRegion currentFrame;

    private Array<Polygon> collisions = new Array<>();

    private final Rectangle bounds = new Rectangle();

    private final Polygon footPolygon = new Polygon(
        new float[] {
            3, 1,
            9, 1,
            9, 6,
            3, 6
        }
    );

    private float x;
    private float y;
    private float stateTime;

    private final float speed = 80f;
    private final float width = 12f;
    private final float height = 20f;

    private float worldWidth = 320f;
    private float worldHeight = 240f;

    private float facingX = 0f;
    private float facingY = 1f;

    public Player(float x, float y) {

        this.x = x;
        this.y = y;

        spriteSheet = new Texture("player/SpriteSheet.png");

        spriteSheet.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        TextureRegion[][] frames =
            TextureRegion.split(spriteSheet, 23, 36);

        walkDown = createAnimation(frames[0]);
        walkRight = createAnimation(frames[2]);
        walkUp = createAnimation(frames[3]);
        walkLeft = createAnimation(frames[6]);

        currentAnimation = walkUp;
        currentFrame = walkUp.getKeyFrame(0);
    }

    private Animation<TextureRegion> createAnimation(
        TextureRegion[] frames
    ) {
        return new Animation<>(0.12f, frames);
    }

    public void setWorldSize(float width, float height) {
        worldWidth = width;
        worldHeight = height;
    }

    public void setCollisions(Array<Polygon> collisions) {
        this.collisions = collisions;
    }

    private boolean canMove(float nextX, float nextY) {

        if (nextX < 0 || nextY < 0
            || nextX + width > worldWidth
            || nextY + height > worldHeight) {
            return false;
        }

        footPolygon.setPosition(nextX, nextY);

        for (Polygon obstacle : collisions) {
            if (Intersector.overlapConvexPolygons(
                footPolygon, obstacle
            )) {
                return false;
            }
        }

        return true;
    }

    public void update(float delta) {

        float dx = 0f;
        float dy = 0f;
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {

            dy = 1f;
            facingX = 0f;
            facingY = 1f;
            currentAnimation = walkUp;
            moving = true;

        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {

            dy = -1f;
            facingX = 0f;
            facingY = -1f;
            currentAnimation = walkDown;
            moving = true;

        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {

            dx = -1f;
            facingX = -1f;
            facingY = 0f;
            currentAnimation = walkLeft;
            moving = true;

        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {

            dx = 1f;
            facingX = 1f;
            facingY = 0f;
            currentAnimation = walkRight;
            moving = true;
        }

        dx *= speed * delta;
        dy *= speed * delta;

        if (canMove(x + dx, y)) x += dx;
        if (canMove(x, y + dy)) y += dy;

        if (moving) {
            stateTime += delta;

            currentFrame = currentAnimation.getKeyFrame(
                stateTime, true
            );
        } else {
            stateTime = 0f;
            currentFrame = currentAnimation.getKeyFrame(0);
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentFrame, x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds.set(x + 3, y + 1, 6, 5);
    }

    public float getFacingX() {
        return facingX;
    }

    public float getFacingY() {
        return facingY;
    }

    public float getShootX() {
        return x + width / 2f - 5f + facingX * 10f;
    }

    public float getShootY() {
        return y + 4f + facingY * 10f;
    }

    public void dispose() {
        spriteSheet.dispose();
    }
}
