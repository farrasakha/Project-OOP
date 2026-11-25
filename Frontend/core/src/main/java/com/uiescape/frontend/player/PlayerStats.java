package com.uiescape.frontend.player;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {

    public interface Listener {
        void onStatsChanged(PlayerStats stats);
    }

    public static final int MAX_LIVES = 3;
    public static final float INVULNERABILITY = 1.5f;

    private int lives;
    private int coins;
    private int keys;

    private boolean bananaWeapon;
    private boolean hasMap;

    private float invulnerabilityTime;

    private final List<Listener> listeners =
        new ArrayList<>();

    public PlayerStats(int startingLives) {
        lives = Math.max(
            1, Math.min(MAX_LIVES, startingLives)
        );
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
        listener.onStatsChanged(this);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onStatsChanged(this);
        }
    }

    public void update(float delta) {
        invulnerabilityTime = Math.max(
            0f, invulnerabilityTime - delta
        );
    }

    public void addCoin() {
        coins++;
        notifyListeners();
    }

    public void addKey() {
        keys++;
        notifyListeners();
    }

    public boolean useKey() {
        if (keys <= 0) return false;

        keys--;
        notifyListeners();
        return true;
    }

    public boolean addLife() {
        if (lives >= MAX_LIVES) return false;

        lives++;
        notifyListeners();
        return true;
    }

    public boolean takeDamage(int amount) {
        if (amount <= 0 || lives <= 0
            || invulnerabilityTime > 0f) {
            return false;
        }

        lives = Math.max(0, lives - amount);
        invulnerabilityTime = INVULNERABILITY;

        notifyListeners();
        return true;
    }

    public void unlockBananaWeapon() {
        bananaWeapon = true;
        notifyListeners();
    }

    public boolean exchangeMap(int cost) {
        if (hasMap || cost <= 0 || coins < cost) {
            return false;
        }

        coins -= cost;
        hasMap = true;

        notifyListeners();
        return true;
    }

    public int getLives() {
        return lives;
    }

    public int getCoins() {
        return coins;
    }

    public int getKeys() {
        return keys;
    }

    public boolean hasKey() {
        return keys > 0;
    }

    public boolean hasBananaWeapon() {
        return bananaWeapon;
    }

    public boolean hasMap() {
        return hasMap;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTime > 0f;
    }
}
