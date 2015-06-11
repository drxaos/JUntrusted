package com.github.drxaos.juntrusted.levels.level_01.internal;

import com.github.drxaos.juntrusted.levels.level_01.*;
import com.github.drxaos.juntrusted.levels.level_01.Definition;
import com.github.drxaos.juntrusted.levels.level_01.Level;
import com.github.drxaos.juntrusted.levels.level_01.Map;
import com.github.drxaos.juntrusted.levels.level_01.Player;

import java.awt.*;

class Bootstrap {

    private static Level level;
    private static Map map;
    private static String next;

    private static void execute(Map map) {
        Bootstrap.map = map;
        level = new CellBlockA();
        level.startLevel(map);
    }

    private static boolean onExit(Map map) {
        return level.onExit(map);
    }

    public static void onCollision(Definition d, Player player) {
        d.onCollision(player);
    }

    public static void onPickUp(Definition d, Player player) {
        d.onPickUp(player);
    }

    public static void onDrop(Definition d) {
        d.onDrop();
    }

    public static void behavior(Definition d, Me me) {
        d.behavior(me);
    }

    public static Boolean impassable(Definition d, Player player, String type, Me me) {
        return d.impassable;
    }

    public static void definitions(java.util.Map defMap) {

        defMap.put("player", new Definition() {

            {
                color = Color.GREEN;
                symbol = '@';
            }
        });
        defMap.put("computer", new Definition() {
            {
                type = "item";
                color = new Color(0x99, 0x99, 0x99);
                symbol = '⌘';
            }

            @Override
            public void onPickUp(Player player) {
                map.writeStatus("You have picked up the computer!");
                // show code
            }

            @Override
            public void onDrop() {
                // hide code
            }
        });
        defMap.put("block", new Definition() {
            {
                color = Color.LIGHT_GRAY;
                symbol = '#';
                impassable = true;
            }

            @Override
            public void onCollision(Player player) {
                //map.writeStatus("Can't go thru walls");
            }
        });
        defMap.put("exit", new Definition() {
            {
                color = new Color(0f, 1f, 1f);
                symbol = '⎕';

            }

            @Override
            public void onCollision(Player player) {
                if (onExit(map)) {
                    next = "com.github.drxaos.jvmvm.ui.levels.level_02.internal.Game";
                }
            }
        });

    }

    public static String getNext() {
        return next;
    }
}
