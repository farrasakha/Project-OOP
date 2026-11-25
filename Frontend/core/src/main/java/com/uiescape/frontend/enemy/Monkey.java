
package com.uiescape.frontend.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import com.uiescape.frontend.Player;
import com.uiescape.frontend.projectiles.ProjectileSystem;

public class Monkey extends Enemy {

    private static final int FRAME_SIZE = 32;

    private static final float SPEED = 22f;
    private static final float STUN_DURATION = 4f;
    private static final float SHOOT_INTERVAL = 2f;
    private static final float SHOOT_RANGE = 110f;

    private final float minY;
    private final float maxY;

    private int direction = 1;

    private float stunTime;
    private float shootTimer = SHOOT_INTERVAL;
    private float animationTime;

    private final Texture idleSheet;
    private final Texture runSheet;
    private final Texture hitSheet;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private final Animation<TextureRegion> hitAnimation;

    private final Rectangle bounds = new Rectangle();

    public Monkey(
        float x, float y,
        float minY, float maxY
    ) {
        super(x, y);

        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);

        this.y = MathUtils.clamp(
            y, this.minY, this.maxY
        );

        idleSheet = loadTexture("Idle.png");
        runSheet = loadTexture("Run.png");
        hitSheet = loadTexture("Hit.png");

        idleAnimation = createAnimation(idleSheet, 0.12f);
        runAnimation = createAnimation(runSheet, 0.10f);
        hitAnimation = createAnimation(hitSheet, 0.15f);
    }

    private Texture loadTexture(String filename) {
        Texture texture = new Texture(
            "enemy/monkey/" + filename
        );

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        return texture;
    }

    private Animation<TextureRegion> createAnimation(
        Texture texture, float duration
    ) {
        TextureRegion[][] frames = TextureRegion.split(
            texture, FRAME_SIZE, FRAME_SIZE
        );

        return new Animation<>(duration, frames[0]);
    }

    @Override
    public void update(float delta) {

        animationTime += delta;

        if (isStunned()) {
            stunTime = Math.max(0f, stunTime - delta);
            return;
        }

        y += SPEED * direction * delta;

        if (y >= maxY) {
            y = maxY;
            direction = -1;
        } else if (y <= minY) {
            y = minY;
            direction = 1;
        }

        shootTimer = Math.max(0f, shootTimer - delta);
    }

    public void tryShoot(
        Player player,
        ProjectileSystem projectiles
    ) {
        if (isStunned() || shootTimer > 0f) {
            return;
        }

        Rectangle target = player.getBounds();

        float centerX = x + 8f;
        float centerY = y + 6f;

        float dx = target.x + target.width / 2f - centerX;
        float dy = target.y + target.height / 2f - centerY;

        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared > SHOOT_RANGE * SHOOT_RANGE
            || distanceSquared == 0f) {
            return;
        }

        projectiles.shoot(
            true,
            centerX - 5f,
            centerY - 5f,
            dx,
            dy
        );

        shootTimer = SHOOT_INTERVAL;
    }

    public void stun() {
        stunTime = STUN_DURATION;
        shootTimer = SHOOT_INTERVAL;
        animationTime = 0f;
    }

    public boolean isStunned() {
        return stunTime > 0f;
    }

    public Rectangle getBounds() {
        return bounds.set(x + 3f, y + 1f, 10f, 15f);
    }

    public void render(SpriteBatch batch) {

        TextureRegion frame;

        if (isStunned()) {

            if (animationTime < hitAnimation.getAnimationDuration()) {
                frame = hitAnimation.getKeyFrame(
                    animationTime, false
                );
            } else {
                frame = idleAnimation.getKeyFrame(
                    animationTime, true
                );
            }

            batch.setColor(0.65f, 0.8f, 1f, 1f);

        } else {
            frame = runAnimation.getKeyFrame(
                animationTime, true
            );
        }

        batch.draw(frame, x - 4f, y - 2f, 24f, 24f);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        idleSheet.dispose();
        runSheet.dispose();
        hitSheet.dispose();
    }
}
