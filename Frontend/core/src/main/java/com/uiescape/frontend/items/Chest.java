
package com.uiescape.frontend.items;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Chest {

    private final float x;
    private final float y;

    private final float width = 16f;
    private final float height = 16f;

    private final TextureRegion closedImage;
    private final TextureRegion openedImage;

    private boolean opened = false;

    public Chest(
        float x,
        float y,
        TextureRegion closedImage,
        TextureRegion openedImage
    ) {
        this.x = x;
        this.y = y;
        this.closedImage = closedImage;
        this.openedImage = openedImage;
    }

    public void render(SpriteBatch batch) {
        TextureRegion image = opened
            ? openedImage
            : closedImage;

        batch.draw(image, x, y, width, height);
    }

    public Rectangle getInteractionBounds() {
        return new Rectangle(
            x - 8,
            y - 8,
            width + 16,
            height + 16
        );
    }

    public Rectangle getCollisionBounds() {
        return new Rectangle(
            x + 2,
            y + 1,
            width - 4,
            7
        );
    }

    public boolean isOpened() {
        return opened;
    }

    public void open() {
        opened = true;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
