package com.github.drxaos.juntrusted.levels.level_03;

public abstract class Level {
    /**
     * This method is called when the level loads.
     */
    public abstract void startLevel(Map map);

    /**
     * The player can exit the level only if this method returns true.
     */
    public boolean onExit(Map map) {
        return true;
    }

    /**
     * The level can be loaded only if this method returns true.
     */
    public abstract boolean validateLevel(Map map);
}
