
package com.uiescape.frontend.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.uiescape.frontend.player.PlayerStats;

public class GameManager {

    public enum Action {
        NONE,
        START,
        RESTART,
        NEXT,
        MENU
    }

    public enum State {

        MAIN_MENU {
            @Override
            Action handle(GameManager manager) {

                if (pressed(Input.Keys.ENTER)) {
                    return Action.START;
                }

                return Action.NONE;
            }
        },

        PLAYING {
            @Override
            Action handle(GameManager manager) {

                if (pausePressed()) {
                    manager.state = PAUSED;
                }

                return Action.NONE;
            }
        },

        PAUSED {
            @Override
            Action handle(GameManager manager) {

                if (pausePressed()) {
                    manager.state = PLAYING;
                }

                return Action.NONE;
            }
        },

        LEVEL_COMPLETE {
            @Override
            Action handle(GameManager manager) {

                if (pressed(Input.Keys.ENTER)) {
                    return Action.NEXT;
                }

                return Action.NONE;
            }
        },

        GAME_OVER {
            @Override
            Action handle(GameManager manager) {

                if (pressed(Input.Keys.ESCAPE)) {
                    return Action.MENU;
                }

                if (pressed(Input.Keys.ENTER)) {
                    return Action.RESTART;
                }

                return Action.NONE;
            }
        },

        VICTORY {
            @Override
            Action handle(GameManager manager) {

                if (pressed(Input.Keys.ENTER)
                    || pressed(Input.Keys.ESCAPE)) {

                    return Action.MENU;
                }

                return Action.NONE;
            }
        };

        abstract Action handle(GameManager manager);
    }

    private State state = State.MAIN_MENU;

    public Action handleInput() {
        return state.handle(this);
    }

    private static boolean pressed(int key) {
        return Gdx.input.isKeyJustPressed(key);
    }

    private static boolean pausePressed() {

        return pressed(Input.Keys.P)
            || pressed(Input.Keys.ESCAPE);
    }

    public void startPlaying() {
        state = State.PLAYING;
    }

    public void completeLevel() {
        state = State.LEVEL_COMPLETE;
    }

    public void showMenu() {
        state = State.MAIN_MENU;
    }

    public void showVictory() {
        state = State.VICTORY;
    }

    public void onStatsChanged(PlayerStats stats) {

        if (state == State.PLAYING
            && stats.getLives() <= 0) {

            state = State.GAME_OVER;
        }
    }

    public State getState() {
        return state;
    }

    public boolean isPlaying() {
        return state == State.PLAYING;
    }
}
