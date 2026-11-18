
package com.uiescape.frontend.enemy;

public class PatrolEnemy extends Enemy {

    private float speed = 100f;
    private int direction = 1;

    private float minX;
    private float maxX;

    public PatrolEnemy(float x, float y,
                       float minX, float maxX) {
        super(x, y);
        this.minX = minX;
        this.maxX = maxX;
    }

    @Override
    public void update(float delta) {
        x += speed * direction * delta;

        if (x >= maxX) {
            x = maxX;
            direction = -1;
        } else if (x <= minX) {
            x = minX;
            direction = 1;
        }
    }
}
