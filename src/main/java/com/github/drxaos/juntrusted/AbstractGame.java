package com.github.drxaos.juntrusted;

import com.github.drxaos.jvmvm.loader.Project;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractGame {
    protected List<Action> actions = new ArrayList<Action>();
    protected Project levelVm;
    protected Integer key;
    protected AbstractGame loadLevel;
    protected Code lvlCode;

    public AbstractGame() {
    }

    public void printLevelName(String level) {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.GREEN, "\n> " + level));
    }

    public List<Action> getActions() {
        List<Action> res = new ArrayList<Action>();
        while (actions.size() > 0) {
            Action action = actions.remove(0);
            if (action instanceof Action.Nop) {
                break;
            } else {
                res.add(action);
            }
        }
        return res;
    }

    public void drawText(int x, int y, String str) {
        if (y >= 0 && y < 25 && x < 50) {
            while (x < 0 && str.length() > 0) {
                str = str.substring(1);
                x++;
            }
            if (str.length() + x > 50) {
                str.substring(0, 50 - x);
            }
        }
        actions.add(new Action.MoveCaret(x, y));
        actions.add(new Action.Print(Color.LIGHT_GRAY, str));
    }

    public void pushLine() {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.LIGHT_GRAY, "\n"));
    }

    public void displayChapter(String title) {
        actions.add(new Action.DisplayChapter(title));
    }


    public void setKey(Integer key) {
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    public AbstractGame getNextLevel() {
        return loadLevel;
    }

    public void load(AbstractGame game) {
        loadLevel = game;
    }

    public abstract void start();

    public abstract void step();

    public abstract void stop();

    public abstract String getMusic();

    public abstract boolean applyEdit(Code.Edit edits);

    public abstract HashMap<Integer, Code.Line> redLines();

    public abstract String getLevelNumber();

    public abstract String getLevelName();

    public abstract String getLevelFolder();

    public Code getCurrentCode() {
        return lvlCode;
    }

    public void setCode(Code code) {
        lvlCode = code;
        actions.add(new Action.LoadCode(lvlCode.toString()));
    }

    public String getCode() {
        return lvlCode.toString();
    }
}
