package com.github.drxaos.juntrusted.levels.level_01;

import com.github.drxaos.juntrusted.levels.level_01.internal.Game;

import java.io.Serializable;

final public class Map implements Serializable {
    private Player player;
    private Game game;

    private Map(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    /**
     * Displays the given chapter name.
     */
    public void displayChapter(String title) {
        game.displayChapter(title);
    }

    /**
     * Places the player at the given coordinates.
     */
    public void placePlayer(int x, int y) {
        game.placePlayer(x, y);
    }

    /**
     * Places an object of the given type at the given coordinates.
     */
    public void placeObject(int x, int y, String type) {
        game.placeObject(x, y, type);
    }

    /**
     * Returns the width of the map, in cells.
     */
    public int getWidth() {
        return 50;
    }

    /**
     * Returns the height of the map, in cells.
     */
    public int getHeight() {
        return 25;
    }

    /**
     * Returns the Player object.
     */
    public Player getPlayer() {
        return player;
    }


    public void writeStatus(String text) {
        game.writeStatus(text);
    }

    void __auth(String command, String secret) {
        game.__auth(command, secret);
    }
}
