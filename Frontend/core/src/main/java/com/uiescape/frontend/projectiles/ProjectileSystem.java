
package com.uiescape.frontend.projectiles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import com.uiescape.frontend.player.Player;
import com.uiescape.frontend.player.PlayerStats;
import com.uiescape.frontend.enemy.Monkey;
import com.uiescape.frontend.level.Level;

public class ProjectileSystem {

    private final TextureRegion bananaImage;

    private final Animation<TextureRegion> peelAnimation;

    private final Array<BananaProjectile> active =
        new Array<>();

    private final Pool<BananaProjectile> pool =
        new Pool<BananaProjectile>(16, 128) {

            @Override
            protected BananaProjectile newObject() {
                return new BananaProjectile();
            }
        };

    public ProjectileSystem(
        TextureRegion bananaImage,
        Animation<TextureRegion> peelAnimation
    ) {
        this.bananaImage = bananaImage;
        this.peelAnimation = peelAnimation;
    }

    public void shoot(
        boolean enemyOwned,
        float x,
        float y,
        float dx,
        float dy
    ) {

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0f) return;

        BananaProjectile projectile = pool.obtain();

        projectile.init(
            enemyOwned,
            x,
            y,
            dx / length,
            dy / length,
            bananaImage,
            peelAnimation
        );

        active.add(projectile);
    }

    public void update(
        float delta,
        Player player,
        PlayerStats stats,
        Monkey monkey,
        Level level
    ) {

        for (int i = active.size - 1; i >= 0; i--) {

            BananaProjectile projectile = active.get(i);

            projectile.update(delta);

            if (projectile.isFlying()) {

                Rectangle bounds = projectile.getBounds();

                boolean outsideMap =
                    bounds.x < 0 ||
                        bounds.y < 0 ||
                        bounds.x + bounds.width > level.getWorldWidth() ||
                        bounds.y + bounds.height > level.getWorldHeight();

                if (outsideMap || level.hitsObstacle(bounds)) {

                    projectile.startPeeling();

                } else if (projectile.isEnemyOwned()) {

                    if (bounds.overlaps(player.getBounds())) {

                        stats.takeDamage(1);
                        projectile.startPeeling();
                    }

                } else {

                    if (bounds.overlaps(monkey.getBounds())) {

                        monkey.stun();
                        projectile.startPeeling();
                    }
                }
            }

            if (projectile.isFinished()) {
                active.removeIndex(i);
                pool.free(projectile);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (BananaProjectile projectile : active) {
            projectile.render(batch);
        }
    }

    public void clear() {

        for (BananaProjectile projectile : active) {
            pool.free(projectile);
        }

        active.clear();
        pool.clear();
    }
}
