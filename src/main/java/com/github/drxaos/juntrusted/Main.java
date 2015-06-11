package com.github.drxaos.juntrusted;

import com.github.drxaos.juntrusted.levels.intro.Game;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Main implements ActionListener {

    AbstractGame game;
    Editor editor;
    HashMap saveState = new HashMap();

    public Main() {
        editor = new Editor();
        editor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    // On exit - save game
                    saveState.put("notepad", editor.getNotepadText());
                    saveState.put("code" + game.getLevelNumber(), game.getCurrentCode());
                    saveState.put("timestamp", System.currentTimeMillis());
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./savegame.dat"));
                    out.writeObject(saveState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        editor.setVisible(true);
        ((AbstractDocument) editor.getCodeEditor().getDocument()).setDocumentFilter(new PartlyReadOnly());
        editor.getCodeEditor().getDocument().addDocumentListener(new PartlyReadOnly());

        try {
            // On start - load saved game
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("./savegame.dat"));
            saveState = (HashMap) in.readObject();
            if (saveState.containsKey("maxLevel")) {
                String lvl = (String) saveState.get("maxLevel");
                Code code = (Code) saveState.get("code" + lvl);
                game = (AbstractGame) Class.forName("" + saveState.get("class" + lvl)).getConstructor(Code.class).newInstance(code);
                game.start();
                editor.playMusic(game.getMusic());
                editor.setNotepadText((String) saveState.get("notepad"));
                game.setCode(code);
            }
            editor.displaySaveGames(saveState);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Validating document filter
     */
    class PartlyReadOnly extends DocumentFilter implements DocumentListener {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (game.applyEdit(new Code.Remove(length, offset))) {
                super.remove(fb, offset, length);

                if (!fb.getDocument().getText(0, fb.getDocument().getLength()).equals(game.getCurrentCode().toString())) {
                    editor.scheduleSetText(game.getCode(), false);
                }

            }
            editor.colorizeCodeEditor(game);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) {
                string = "";
            }
            if (game.applyEdit(new Code.Insert(string, offset))) {
                super.insertString(fb, offset, string, attr);

                if (!fb.getDocument().getText(0, fb.getDocument().getLength()).equals(game.getCurrentCode().toString())) {
                    editor.scheduleSetText(game.getCode(), false);
                }

            }
            editor.colorizeCodeEditor(game);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (string == null) {
                string = "";
            }
            if (game.applyEdit(new Code.Replace(string, offset, length))) {
                super.replace(fb, offset, length, string, attrs);

                if (!fb.getDocument().getText(0, fb.getDocument().getLength()).equals(game.getCurrentCode().toString())) {
                    editor.scheduleSetText(game.getCode(), false);
                }

            }
            editor.colorizeCodeEditor(game);
        }

        public void insertUpdate(DocumentEvent e) {
            if (e.getClass().getName().endsWith("UndoRedoDocumentEvent")) {
                try {
                    String text = e.getDocument().getText(0, e.getDocument().getLength());
                    String insert = text.substring(e.getOffset(), e.getOffset() + e.getLength());
                    game.applyEdit(new Code.Insert(insert, e.getOffset()));
                    if (!e.getDocument().getText(0, e.getDocument().getLength()).equals(game.getCurrentCode().toString())) {
                        editor.scheduleSetText(game.getCode(), false);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (e.getClass().getName().endsWith("UndoRedoDocumentEvent")) {
                try {
                    game.applyEdit(new Code.Remove(e.getLength(), e.getOffset()));
                    if (!e.getDocument().getText(0, e.getDocument().getLength()).equals(game.getCurrentCode().toString())) {
                        editor.scheduleSetText(game.getCode(), false);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void changedUpdate(DocumentEvent e) {
            if (e.getClass().getName().endsWith("UndoRedoDocumentEvent")) {
                System.out.println(e);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            // if no game - start intro
            if (game == null) {
                try {
                    game = new Game();
                    game.start();
                    editor.playMusic(game.getMusic());
                    saveState.put("class" + game.getLevelNumber(), game.getClass().getName());
                    saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                    saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                    if (!saveState.containsKey("maxLevel")) {
                        saveState.put("maxLevel", game.getLevelNumber());
                    }
                    editor.displaySaveGames(saveState);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            // controls
            game.setKey(editor.getKeyCode());
            editor.resetKeyCode();

            // reset
            if (editor.hasResetRequest()) {
                try {
                    game.stop();
                    game = game.getClass().getConstructor(Code.class).newInstance(
                            editor.getResetToCurrentCode() ? game.getCurrentCode() : null);
                    game.start();
                    editor.playMusic(game.getMusic());
                    editor.displaySaveGames(saveState);
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                editor.resetResetRequest();
            }

            // process step
            game.step();
            editor.execute(game.getActions());

            // next level game event
            if (game.getNextLevel() != null) {
                saveState.put("code" + game.getLevelNumber(), game.getCurrentCode());

                game.stop();
                game = game.getNextLevel();
                game.start();
                editor.playMusic(game.getMusic());

                saveState.put("class" + game.getLevelNumber(), game.getClass().getName());
                saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                if (saveState.get("maxLevel").toString().compareTo(game.getLevelNumber().toString()) < 0) {
                    saveState.put("maxLevel", game.getLevelNumber());
                }

                editor.displaySaveGames(saveState);
            }

            // load level menu event
            if (editor.getLoadLevelRequest() != null) {
                try {
                    saveState.put("code" + game.getLevelNumber(), game.getCurrentCode());
                    game.stop();
                    game = null;
                    String lvl = editor.getLoadLevelRequest();
                    editor.resetLoadLevelRequest();
                    Code code = (Code) saveState.get("code" + lvl);
                    game = (AbstractGame) Class.forName("" + saveState.get("class" + lvl)).getConstructor(Code.class).newInstance(code);
                    game.start();
                    editor.playMusic(game.getMusic());
                    game.setCode(code);

                    saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                    saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                    editor.displaySaveGames(saveState);
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    new Timer(15, new Main()).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
