package com.googlecode.jvmvm.ui.common;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import com.googlecode.jvmvm.loader.ProjectExecutionException;
import com.googlecode.jvmvm.ui.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class GameBase extends AbstractGame {

    private final int START = 0;
    private final int PUSH = START + 1;
    private final int PLAY_INIT = PUSH + 1;
    private final int PLAY = PLAY_INIT + 1;
    private final int STOP = PLAY + 1;

    private int state = START;
    private String code;
    HttpServer server;

    private String secret = "secret" + Math.random();
    private Obj player = null;
    private ArrayList<Obj> objs = new ArrayList<Obj>();
    private HashMap<String, Object> defMap = new HashMap<String, Object>();
    protected HashSet<String> inventory = new HashSet<String>();

    private Code lvlCode;

    private int pushCounter = 0;

    private String status;
    private boolean startOfStart;
    private boolean endOfStart;

    public GameBase(String code) {
        super("level_01", "CellBlockA.java");
        this.code = code;
    }

    public GameBase() {
        this(null);
    }


    @Override
    public boolean validateCode(String code) {
        return lvlCode.apply(code, false);
    }

    @Override
    public List<Integer> redLines() {
        return lvlCode.getReadonlyLines();
    }

    public void __auth(String command, String secret) {
        if (this.secret.equals(secret)) {
            if ("startOfStartLevel".equals(command)) {
                startOfStart = true;
            } else if ("endOfStartLevel".equals(command)) {
                endOfStart = true;
            }
        }
    }

    public Obj getPlayerObj() {
        return player;
    }

    abstract public HttpHandler getApiHandler();

    abstract public Class getBootstrapClass();

    abstract public Class getDefinitionClass();

    abstract public Class getLevelClass();

    abstract public Class getSourceClass();

    abstract public Class getMeClass();

    abstract public Object getMe();

    abstract public Class getPlayerClass();

    abstract public Object getPlayer();

    abstract public Class getMapClass();

    abstract public Object getMap();

    public boolean isPlayerAtLocation(int x, int y) {
        return (player.x == x) && (player.y == y);
    }

    class DefinitionExecutor {
        Object definition;

        public DefinitionExecutor(Object definition) {
            this.definition = definition;
        }

        char getSymbol() {
            try {
                return definition.getClass().getField("symbol").getChar(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return ' ';
        }

        boolean getImpassable() {
            try {
                return definition.getClass().getField("impassable").getBoolean(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return true;
        }

        String getType() {
            try {
                return (String) definition.getClass().getField("type").get(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }

        Color getColor() {
            try {
                return (Color) definition.getClass().getField("color").get(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return Color.LIGHT_GRAY;
        }

        public void onCollision(Object player) {
            levelVm.setupVM(getBootstrapClass().getName(), "onCollision", null, new Class[]{getDefinitionClass(), getPlayerClass()}, new Object[]{definition, player}).run(2000);
        }

        public void onPickUp(Object player) {
            levelVm.setupVM(getBootstrapClass().getName(), "onPickUp", null, new Class[]{getDefinitionClass(), getPlayerClass()}, new Object[]{definition, player}).run(2000);
        }

        public void behavior(Object me) {
            levelVm.setupVM(getBootstrapClass().getName(), "behavior", null, new Class[]{getDefinitionClass(), getMeClass()}, new Object[]{definition, me}).run(2000);
        }

        public void onDrop() {
            levelVm.setupVM(getBootstrapClass().getName(), "onDrop", null, new Class[]{getDefinitionClass()}, new Object[]{definition}).run(2000);
        }

    }

    @Override
    public void start() {
        startApiServer();
        configureLevel();
    }

    protected void configureVm(Project vm) throws IOException {
    }

    protected boolean configureLevel() {
        Exception error = null;
        try {
            String path = "src/main/java";
            String lvlSrc = getSourceClass().getCanonicalName().replace(".", "/") + ".java";
            String baseSrc = getLevelClass().getCanonicalName().replace(".", "/") + ".java";
            String bootstrapSrc = getBootstrapClass().getCanonicalName().replace(".", "/") + ".java";
            lvlCode = Code.parse(SrcUtil.loadSrc(path, lvlSrc));
            if (code != null) {
                lvlCode.apply(code, false);
            }

            if (code == null) {
                actions.add(new Action.LoadCode(lvlCode.toString()));
            }
            levelVm = new Project("level-vm")
                    .addFile(lvlSrc, lvlCode.toCompilationUnit(secret))
                    .addFile(baseSrc, SrcUtil.loadSrc(path, baseSrc))
                    .addFile(bootstrapSrc, SrcUtil.loadSrc(path, bootstrapSrc))
                    .addSystemClass(getMeClass().getName())
                    .addSystemClass(getDefinitionClass().getName())
                    .addSystemClass(getMapClass().getName())
                    .addSystemClass(getPlayerClass().getName())
                    .addSystemClasses(Vm.bootstrap);
            configureVm(levelVm);
            levelVm.compile()
                    .markObject("map", (java.io.Serializable) getMap());
        } catch (ProjectCompilerException e) {
            e.printStackTrace();
            error = e;
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            error = e;
        }
        if (error != null) {
            actions.add(new Action.MoveCaretToBottomRight());
            actions.add(new Action.Print("\n" + error.toString()));
            actions.add(new Action.ShowCode());
            state = STOP;
            return false;
        } else {
            return true;
        }
    }

    private void startApiServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(Editor.API_PORT), 0);
            server.createContext("/", getApiHandler());
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String baseSrc = "src/main/resources";
            byte[] response = SrcUtil.loadData(baseSrc, "docs/level_01/" + t.getRequestURI().getPath().replace("..", "").replaceFirst("^/", ""));
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Override
    public void step() {
        try {
            if (state == START) {
                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "definitions", null, new Class[]{java.util.Map.class}, new Object[]{defMap});
                levelVm.run(2000);

                startOfStart = false;
                endOfStart = false;
                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "execute", null, new Class[]{getMapClass()}, new Object[]{Project.Marker.byName("map")});
                levelVm.run(2000);
                if (!startOfStart || !endOfStart) {
                    actions.add(new Action.MoveCaretToBottomRight());
                    if (!startOfStart) {
                        actions.add(new Action.Print("\nstartLevel() has been tampered with!"));
                    } else if (!endOfStart) {
                        actions.add(new Action.Print("\nstartLevel() returned prematurely!"));
                    }
                    actions.add(new Action.ShowCode());
                    state = STOP;
                    return;
                }

                state = PUSH;
            } else if (state == PUSH) {
                pushLine();
                for (Obj obj : objs) {
                    if (obj.y == pushCounter) {
                        Object d = defMap.get(obj.type);
                        actions.add(new Action.MoveCaret(obj.x, 24));
                        Color color = new DefinitionExecutor(d).getColor();
                        char symbol = new DefinitionExecutor(d).getSymbol();
                        actions.add(new Action.Print(color, "" + symbol));
                    }
                }
                if (++pushCounter >= 25) {
                    state = PLAY_INIT;
                }
            } else if (state == PLAY || state == PLAY_INIT) {
                int toX = player.x, toY = player.y;
                if (key != null || state == PLAY_INIT) {
                    if(state == PLAY_INIT){
                        key = 0;
                    }
                    // move player
                    if (key == KeyEvent.VK_DOWN && player.y < 24) {
                        toY++;
                    } else if (key == KeyEvent.VK_UP && player.y > 0) {
                        toY--;
                    } else if (key == KeyEvent.VK_RIGHT && player.x < 49) {
                        toX++;
                    } else if (key == KeyEvent.VK_LEFT && player.x > 0) {
                        toX--;
                    }
                }
                Obj found = findObj(toX, toY);
                if (found != null && found != player) {
                    Object d = defMap.get(found.type);
                    if (new DefinitionExecutor(d).getImpassable()) {
                        toX = player.x;
                        toY = player.y;
                    }
                    if ("item".equals(new DefinitionExecutor(d).getType())) {
                        new DefinitionExecutor(d).onPickUp(getPlayer());
                        objs.remove(found);
                        inventory.add(found.type);
                    } else {
                        new DefinitionExecutor(d).onCollision(getPlayer());
                    }
                }
                player.x = toX;
                player.y = toY;

                if (key != null) {
                    // repaint on user action
                    actions.add(new Action.Clear());
                    for (Obj obj : objs) {
                        Object d = defMap.get(obj.type);
                        Color color = new DefinitionExecutor(d).getColor();
                        char symbol = new DefinitionExecutor(d).getSymbol();
                        actions.add(new Action.MoveCaret(obj.x, obj.y));
                        actions.add(new Action.Print(color, "" + symbol));
                    }
                    Object d = defMap.get("player");
                    Color color = new DefinitionExecutor(d).getColor();
                    char symbol = new DefinitionExecutor(d).getSymbol();
                    actions.add(new Action.MoveCaret(toX, toY));
                    actions.add(new Action.Print(color, "" + symbol));

                    // display statue if exists
                    if (status != null) {
                        displayStatus(status);
                        status = null;
                    }

                    // hide code if no computer
                    if (inventory.contains("computer")) {
                        actions.add(new Action.ShowCode());
                    } else {
                        actions.add(new Action.HideCode());
                    }

                    // format inventory
                    String inv = "";
                    for (String t : inventory) {
                        inv += new DefinitionExecutor(defMap.get(t)).getSymbol();
                    }
                    actions.add(new Action.Inventory(inv));
                }

                // next level
                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "getNext", null, new Class[]{}, new Object[]{});
                Object next = levelVm.run(2000);
                if (next != null) {
                    load((AbstractGame) Class.forName(next.toString()).newInstance());
                }

                state = PLAY;
            }
        } catch (ProjectExecutionException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            actions.add(new Action.MoveCaretToBottomRight());
            actions.add(new Action.Print("\n" + cause.toString()));
            actions.add(new Action.ShowCode());
            state = STOP;
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        server.stop(0);
    }


    public void placePlayer(int x, int y) {
        if (player != null) {
            throw new RuntimeException("Can't place player twice!");
        }
        player = new Obj(x, y, "player");
        objs.add(player);
    }

    private Obj findObj(int x, int y) {
        Obj found = null;
        for (Obj obj : objs) {
            if (obj.x == x && obj.y == y) {
                found = obj;
                break;
            }
        }
        return found;
    }

    public void placeObject(int x, int y, String type) {
        if (!defMap.containsKey(type)) {
            throw new RuntimeException("There is no type of object named " + type);
        }
        Obj found = findObj(x, y);
        if (found != null) {
            objs.remove(found);
        }
        objs.add(new Obj(x, y, type));
    }

    public int getWidth() {
        return 50;
    }

    public int getHeight() {
        return 25;
    }


    public void writeStatus(String text) {
        status = text;
    }

    public void displayStatus(String text) {
        List<String> strings = new ArrayList<String>();
        strings.add(text);

        if (text.length() > getWidth()) {
            // split into two lines
            int minCutoff = getWidth() - 10;
            int cutoff = minCutoff + text.substring(minCutoff).indexOf(" ");
            strings.clear();
            strings.add(text.substring(0, cutoff));
            strings.add(text.substring(cutoff + 1));
        }

        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            int x = (int) Math.floor((getWidth() - str.length()) / 2);
            int y = getHeight() + i - strings.size() - 1;
            drawText(x, y, str);
        }
    }

    public boolean hasItem(String type) {
        return inventory.contains(type);
    }
}