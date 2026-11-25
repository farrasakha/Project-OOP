
package com.uiescape.frontend.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Key {

    private final float x;
    private final float y;

    private final float width = 10f;
    private final float height = 10f;

    private final TextureRegion keyImage;
    private boolean collected = false;

    private float animationTime = 0f;

    public Key(float x, float y, TextureRegion keyImage) {
        this.x = x;
        this.y = y;
        this.keyImage = keyImage;
    }

    public void render(SpriteBatch batch) {
        if (collected) {
            return;
        }

        // Animasi melayang naik-turun
        animationTime += Gdx.graphics.getDeltaTime();

        float bounce = MathUtils.sin(animationTime * 4f) * 2f;

        batch.draw(
            keyImage,
            x,
            y + bounce,
            width,
            height
        );
    }

    // Area untuk mendeteksi player mengambil key
    public Rectangle getBounds() {
        return new Rectangle(
            x,
            y - 2f,
            width,
            height + 4f
        );
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
