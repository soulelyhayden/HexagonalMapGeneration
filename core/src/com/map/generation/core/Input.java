package com.map.generation.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import static com.badlogic.gdx.Input.Keys.*;


public class Input implements InputProcessor {

    boolean forward = false;
    boolean back = false;
    boolean right = false;
    boolean left = false;
    boolean down = false;
    boolean up = false;

    boolean turn = false;

    float xTurn, yTurn;


    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case (W):
            case (UP):
                forward = true;
                break;
            case (A):
            case (LEFT):
                left = true;
                break;
            case (S):
            case (DOWN):
                back = true;
                break;
            case (D):
            case (RIGHT):
                right = true;
                break;
            case (SPACE):
                up = true;
                break;
            case (SHIFT_LEFT):
            case (SHIFT_RIGHT):
                down = true;
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case (W):
            case (UP):
                forward = false;
                break;
            case (A):
            case (LEFT):
                left = false;
                break;
            case (S):
            case (DOWN):
                back = false;
                break;
            case (D):
            case (RIGHT):
                right = false;
                break;
            case (SPACE):
                up = false;
                break;
            case (SHIFT_LEFT):
            case (SHIFT_RIGHT):
                down = false;
                break;

        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        turn = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        turn = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {


        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
