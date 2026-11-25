
package com.uiescape.frontend.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Coin {

    private final float x;
    private final float y;

    private final float width = 10f;
    private final float height = 10f;

    private final TextureRegion coinImage;
    private boolean collected = false;

    // Waktu animasi
    private float animationTime = 0f;

    public Coin(float x, float y, TextureRegion coinImage) {
        this.x = x;
        this.y = y;
        this.coinImage = coinImage;
    }

    public void render(SpriteBatch batch) {
        if (collected) {
            return;
        }

        // Tambah waktu setiap frame
        animationTime += Gdx.graphics.getDeltaTime();

        // Efek melayang naik-turun
        float bounce = MathUtils.sin(animationTime * 4f) * 2f;

        batch.draw(
            coinImage,
            x,
            y + bounce,
            width,
            height
        );
    }

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
