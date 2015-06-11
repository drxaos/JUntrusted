package com.github.drxaos.juntrusted.levels.level_08.internal;


import com.github.drxaos.juntrusted.Action;
import com.github.drxaos.juntrusted.Code;
import com.github.drxaos.juntrusted.SrcUtil;
import com.github.drxaos.juntrusted.common.GameBase;
import com.github.drxaos.juntrusted.levels.intro.Initialize;
import com.github.drxaos.juntrusted.levels.level_08.*;
import com.github.drxaos.juntrusted.levels.level_08.Object;
import com.github.drxaos.jvmvm.loader.Project;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Game extends GameBase {
    public Game(Code code) {
        super(code);
    }

    public Game() {
        this(null);
    }

    @Override
    protected void configureVm(Project vm) throws IOException {
    }

    @Override
    public HttpHandler getApiHandler() {
        return new ApiHandler();
    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String baseSrc = "src/main/resources";
            byte[] response = SrcUtil.loadData(baseSrc, "docs/level_08/" + t.getRequestURI().getPath().replace("..", "").replaceFirst("^/", ""));
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Override
    protected boolean configureLevel() {
        actions.add(new Action.ShowCode());
        inventory.add("computer");
        inventory.add("phone");
        return super.configureLevel();
    }

    @Override
    protected Class getPhoneCallbackClass() {
        return Function.class;
    }

    @Override
    public java.lang.Object createObject(String id) {
        return new Object(this, id);
    }

    @Override
    public Class getBootstrapClass() {
        return Bootstrap.class;
    }

    @Override
    public Class getDefinitionClass() {
        return Definition.class;
    }

    @Override
    public Class getLevelClass() {
        return Level.class;
    }

    @Override
    public Class getSourceClass() {
        return IntoTheWoods.class;
    }

    @Override
    public Class getObjectClass() {
        return Object.class;
    }

    @Override
    public Class getPlayerClass() {
        return Player.class;
    }

    Player player;

    @Override
    public java.lang.Object getPlayer() {
        if (player == null) {
            try {
                Constructor<Player> c = Player.class.getDeclaredConstructor(new Class[]{Game.class});
                c.setAccessible(true);
                player = c.newInstance(this);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return player;
    }

    @Override
    public Class getMapClass() {
        return Map.class;
    }

    Map map;

    @Override
    public java.lang.Object getMap() {
        if (map == null) {
            try {
                Constructor<Map> c = Map.class.getDeclaredConstructor(new Class[]{Game.class, Player.class});
                c.setAccessible(true);
                map = c.newInstance(this, getPlayer());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public String getMusic() {
        return "Broke_For_Free_-_01_-_Night_Owl.mp3";
    }

    @Override
    public String getLevelNumber() {
        return "08";
    }

    @Override
    public String getLevelName() {
        return "IntoTheWoods.java";
    }

    @Override
    public String getLevelFolder() {
        return "level_08";
    }
}
