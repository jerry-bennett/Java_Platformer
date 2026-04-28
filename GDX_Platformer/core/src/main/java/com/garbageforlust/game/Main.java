package com.garbageforlust.game;

import com.badlogic.gdx.Game;

public class Main extends Game {
    @Override
    public void create() {
        // This is the starting point. We tell the game to show the menu first.
        this.setScreen(new MainMenu(this));
    }
}