package com.uiescape.frontend.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.uiescape.frontend.items.Coin;
import com.uiescape.frontend.items.Key;
import com.uiescape.frontend.items.Chest;

public class InteractionMediator {

    // Penghubung dengan sistem inventory milik Diva
    public interface Inventory {

        void addCoin();

        void addKey();

        boolean useKey();

        boolean addLife();

        void unlockBananaWeapon();

        boolean hasMap();

        boolean exchangeMap(int cost);
    }

    private final Level level;
    private final Inventory inventory;

    private final Rectangle bananaDrop =
        new Rectangle();

    private final Rectangle heartDrop =
        new Rectangle();

    private boolean bananaDropVisible;
    private boolean heartDropVisible;

    private float animationTime;
    private float messageTimer;

    private String message = "";

    public InteractionMediator(
        Level level,
        Inventory inventory
    ) {

        this.level = level;
        this.inventory = inventory;
    }

    // =========================
    // UPDATE INTERACTION
    // =========================

    public void update(
        Rectangle playerBounds,
        boolean interactPressed,
        float delta
    ) {

        animationTime += delta;

        messageTimer = Math.max(
            0f, messageTimer - delta
        );

        collectCoins(playerBounds);
        collectKeys(playerBounds);

        if (interactPressed) {
            interact(playerBounds);
        }

        collectDrops(playerBounds);
    }

    // =========================
    // COLLECT COINS
    // =========================

    private void collectCoins(
        Rectangle playerBounds
    ) {

        for (Coin coin : level.getCoins()) {

            if (!coin.isCollected()
                && playerBounds.overlaps(
                coin.getBounds()
            )) {

                coin.collect();
                inventory.addCoin();
            }
        }
    }

    // =========================
    // COLLECT KEYS
    // =========================

    private void collectKeys(
        Rectangle playerBounds
    ) {

        for (Key key : level.getKeys()) {

            if (!key.isCollected()
                && playerBounds.overlaps(
                key.getBounds()
            )) {

                key.collect();
                inventory.addKey();
            }
        }
    }

    // =========================
    // INTERACTION
    // =========================

    private void interact(
        Rectangle playerBounds
    ) {

        // Exchange table
        if (playerBounds.overlaps(
            level.getExchangeTable()
        )) {

            exchangeMap();
            return;
        }

        // Chest interaction
        for (Chest chest : level.getChests()) {

            if (chest.isOpened()) {
                continue;
            }

            if (!playerBounds.overlaps(
                chest.getInteractionBounds()
            )) {
                continue;
            }

            if (chest != level.getBananaChest()
                && chest != level.getHeartChest()) {

                continue;
            }

            if (!inventory.useKey()) {

                showMessage("Butuh 1 key!");
                return;
            }

            chest.open();

            if (chest == level.getBananaChest()) {

                bananaDropVisible = true;

                bananaDrop.set(
                    chest.getX() + 3f,
                    Math.max(
                        3f, chest.getY() - 11f
                    ),
                    10f,
                    10f
                );

                showMessage(
                    "Ambil pisang di dekat peti!"
                );

            } else if (
                chest == level.getHeartChest()
            ) {

                heartDropVisible = true;

                heartDrop.set(
                    chest.getX() + 3f,
                    Math.max(
                        3f, chest.getY() - 11f
                    ),
                    10f,
                    10f
                );

                showMessage("Heart ditemukan!");
            }

            return;
        }
    }

    // =========================
    // EXCHANGE COINS FOR MAP
    // =========================

    private void exchangeMap() {

        if (inventory.hasMap()) {

            showMessage(
                "Kamu sudah memiliki peta!"
            );

            return;
        }

        if (inventory.exchangeMap(
            level.getCoinCost()
        )) {

            showMessage(
                "Peta berhasil didapat!"
            );

        } else {

            showMessage(
                "Butuh " + level.getCoinCost()
                    + " koin!"
            );
        }
    }

    // =========================
    // COLLECT CHEST DROPS
    // =========================

    private void collectDrops(
        Rectangle playerBounds
    ) {

        if (bananaDropVisible
            && playerBounds.overlaps(
            bananaDrop
        )) {

            bananaDropVisible = false;

            inventory.unlockBananaWeapon();

            showMessage(
                "Senjata pisang didapat!"
            );
        }

        if (heartDropVisible
            && playerBounds.overlaps(
            heartDrop
        )) {

            if (inventory.addLife()) {

                heartDropVisible = false;

                showMessage(
                    "Nyawa bertambah!"
                );

            } else {

                showMessage(
                    "Nyawa masih penuh!"
                );
            }
        }
    }

    // =========================
    // FINISH CONDITION
    // =========================

    public boolean checkFinish(
        Rectangle playerBounds,
        boolean monkeyStunned
    ) {

        if (!playerBounds.overlaps(
            level.getExit()
        )) {

            return false;
        }

        if (!inventory.hasMap()) {

            showMessage(
                "Butuh peta untuk keluar!"
            );

            return false;
        }

        if (!monkeyStunned) {

            showMessage(
                "Buat monyet stun terlebih dahulu!"
            );

            return false;
        }

        return true;
    }

    // =========================
    // RENDER CHEST DROPS
    // =========================

    public void renderDrops(
        SpriteBatch batch,
        TextureRegion bananaImage
    ) {

        float bounce =
            MathUtils.sin(
                animationTime * 4f
            ) * 2f;

        if (bananaDropVisible) {

            batch.draw(
                bananaImage,
                bananaDrop.x,
                bananaDrop.y + bounce,
                bananaDrop.width,
                bananaDrop.height
            );
        }

        if (heartDropVisible) {

            batch.draw(
                level.getHeartImage(),
                heartDrop.x,
                heartDrop.y + bounce,
                heartDrop.width,
                heartDrop.height
            );
        }
    }

    // =========================
    // INTERACTION MESSAGE
    // =========================

    private void showMessage(String text) {

        message = text;
        messageTimer = 2f;
    }

    public String getMessage(
        Rectangle playerBounds
    ) {

        if (messageTimer > 0f) {

            return message;
        }

        if (playerBounds.overlaps(
            level.getExchangeTable()
        ) && !inventory.hasMap()) {

            return "E: Tukar "
                + level.getCoinCost()
                + " koin dengan peta";
        }

        for (Chest chest : level.getChests()) {

            if (!chest.isOpened()
                && playerBounds.overlaps(
                chest.getInteractionBounds()
            )) {

                return "E: Buka peti";
            }
        }

        return "";
    }
}
