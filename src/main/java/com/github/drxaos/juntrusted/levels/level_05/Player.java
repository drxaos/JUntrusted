package com.github.drxaos.juntrusted.levels.level_05;


import com.github.drxaos.juntrusted.levels.level_05.internal.Game;

final public class Player {
    private Game game;

    private Player(Game game) {
        this.game = game;
    }

    /**
     * Returns true if and only if the player has the given item.
     */
    public boolean hasItem(String type) {
        return game.hasItem(type);
    }

    /**
     * Returns true if and only if the player is at the given location.
     */
    public boolean atLocation(int x, int y) {
        return game.isPlayerAtLocation(x, y);
    }

    /**
     * Kills the player and displays the given text as the cause of death.
     */
    public void killedBy(String cause) {
        throw new RuntimeException("You have been killed by " + cause);
    }
}
