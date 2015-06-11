package com.github.drxaos.juntrusted.levels.intro;

import com.github.drxaos.juntrusted.AbstractGame;

import java.io.Serializable;

public class Map implements Serializable {
    private AbstractGame game;

    public Map(AbstractGame game) {
        this.game = game;
    }

    public void displayChapter(String title) {
        game.displayChapter(title);
    }

    public void draw(int x, String text) {
        game.pushLine();
        game.drawText(x, 24, text);
    }
}
