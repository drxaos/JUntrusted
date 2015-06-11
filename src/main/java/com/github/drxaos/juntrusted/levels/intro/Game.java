package com.github.drxaos.juntrusted.levels.intro;

import com.github.drxaos.juntrusted.*;
import com.github.drxaos.jvmvm.loader.Project;

import java.io.IOException;
import java.util.HashMap;

public class Game extends AbstractGame {


    public Game() {
        printLevelName("Initialize.java");
    }

    public Game(Code code) {
        this();
    }

    @Override
    public void start() {
        try {
            actions.add(new Action.HideCode());
            String path = "src/main/java";
            String src = Initialize.class.getCanonicalName().replace(".", "/") + ".java";
            String src1 = Level.class.getCanonicalName().replace(".", "/") + ".java";
            levelVm = new Project("intro")
                    .addFile(src, SrcUtil.loadSrc(path, src))
                    .addFile(src1, SrcUtil.loadSrc(path, src1))
                    .addSystemClass(Map.class.getName())
                    .addSystemClasses(Vm.bootstrap)
                    .compile()
                    .markObject("map", new Map(this))
                    .setupVM(Level.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void step() {
        if (levelVm.isActive()) {
            for (int i = 0; i < 1; i++) {
                levelVm.step();
            }
        } else {
            if (key != null) {
                try {
                    load(new com.github.drxaos.juntrusted.levels.level_01.internal.Game());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public String getMusic() {
        return null;
    }

    @Override
    public boolean applyEdit(Code.Edit edit) {
        return true;
    }

    @Override
    public java.util.HashMap<Integer, Code.Line> redLines() {
        return new HashMap<Integer, Code.Line>();
    }

    @Override
    public String getLevelNumber() {
        return "00";
    }

    @Override
    public String getLevelName() {
        return "Initialize.java";
    }

    @Override
    public String getLevelFolder() {
        return "intro";
    }
}
