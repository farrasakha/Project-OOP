package com.uiescape.frontend.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Enemy {

    protected float x;
    protected float y;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(float delta);

    public void draw(ShapeRenderer renderer) {
        renderer.setColor(Color.RED);
        renderer.rect(x, y, 28, 28);
    }
}
