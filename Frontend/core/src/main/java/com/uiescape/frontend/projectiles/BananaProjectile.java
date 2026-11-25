
package com.uiescape.frontend.projectiles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class BananaProjectile implements Pool.Poolable {

    private enum State {
        FLYING,
        PEELING,
        FINISHED
    }

    private static final float SIZE = 10f;

    private float x;
    private float y;

    private float dirX;
    private float dirY;

    private float speed;
    private float maxDistance;
    private float distance;
    private float stateTime;

    private boolean enemyOwned;

    private TextureRegion bananaImage;
    private Animation<TextureRegion> peelAnimation;

    private State state = State.FINISHED;

    private final Rectangle bounds = new Rectangle();

    public void init(
        boolean enemyOwned,
        float x,
        float y,
        float dirX,
        float dirY,
        TextureRegion bananaImage,
        Animation<TextureRegion> peelAnimation
    ) {
        this.enemyOwned = enemyOwned;

        this.x = x;
        this.y = y;

        this.dirX = dirX;
        this.dirY = dirY;

        this.bananaImage = bananaImage;
        this.peelAnimation = peelAnimation;

        speed = enemyOwned ? 75f : 120f;
        maxDistance = enemyOwned ? 120f : 85f;

        distance = 0f;
        stateTime = 0f;

        state = State.FLYING;
    }

    public void update(float delta) {

        if (state == State.FINISHED) return;

        stateTime += delta;

        if (state == State.FLYING) {

            float movement = speed * delta;

            x += dirX * movement;
            y += dirY * movement;

            distance += movement;

            if (distance >= maxDistance) {
                startPeeling();
            }

        } else if (state == State.PEELING) {

            float finishTime =
                peelAnimation.getAnimationDuration() + 0.4f;

            if (stateTime >= finishTime) {
                state = State.FINISHED;
            }
        }
    }

    public void startPeeling() {

        if (state == State.FLYING) {
            state = State.PEELING;
            stateTime = 0f;
        }
    }

    public void render(SpriteBatch batch) {

        if (state == State.FINISHED) return;

        if (enemyOwned) {
            batch.setColor(1f, 0.55f, 0.35f, 1f);
        }

        if (state == State.FLYING) {

            batch.draw(
                bananaImage, x, y, SIZE, SIZE
            );

        } else {

            TextureRegion frame =
                peelAnimation.getKeyFrame(stateTime, false);

            batch.draw(frame, x, y, SIZE, SIZE);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public Rectangle getBounds() {
        return bounds.set(
            x + 2f, y + 2f,
            SIZE - 4f, SIZE - 4f
        );
    }

    public boolean isEnemyOwned() {
        return enemyOwned;
    }

    public boolean isFlying() {
        return state == State.FLYING;
    }

    public boolean isFinished() {
        return state == State.FINISHED;
    }

    @Override
    public void reset() {
        x = 0f;
        y = 0f;

        dirX = 0f;
        dirY = 0f;

        distance = 0f;
        stateTime = 0f;

        enemyOwned = false;

        bananaImage = null;
        peelAnimation = null;

        state = State.FINISHED;
    }
}
