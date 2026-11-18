
package com.uiescape.frontend.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player {

    private float x, y;
    private float speed = 180f;
    private int health = 3;
    private static final float SIZE = 28f;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            y += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            y -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * delta;
        }

        // Player tidak keluar dari batas layar
        x = Math.max(0, Math.min(x,
            Gdx.graphics.getWidth() - SIZE));
        y = Math.max(0, Math.min(y,
            Gdx.graphics.getHeight() - SIZE));
    }

    public void draw(ShapeRenderer renderer) {
        renderer.setColor(Color.YELLOW);
        renderer.rect(x, y, SIZE, SIZE);
    }

    public int getHealth() {
        return health;
    }
}
