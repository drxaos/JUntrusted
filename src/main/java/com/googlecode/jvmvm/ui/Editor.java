package com.googlecode.jvmvm.ui;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchDialogSearchContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CustomLineHighlightManager;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;

public class Editor extends JFrame implements ActionListener {

    public static final int API_PORT = 7241;
    private RSyntaxTextArea textArea;
    private RTextScrollPane textScroll;
    private JConsole playArea;
    private FindDialog findDialog;
    private ReplaceDialog replaceDialog;
    private JMenu menuSerach;
    private JPanel bottomPanel;
    private MenuWindow menuWindow;
    private NotepadWindow notepadWindow;
    private JLabel inventory;

    private Integer keyCode;
    private boolean resetRequest;
    private String resetCode;

    public Editor() {

        initSearchDialogs();

        //setJMenuBar(createMenuBar());
        JPanel cp = new JPanel(new BorderLayout());
        setContentPane(cp);

        playArea = new JConsole(50, 25);

        playArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // called multiple times during long key press
                keyCode = e.getKeyCode();
            }
        });
        cp.add(playArea, BorderLayout.WEST);

        textArea = new RSyntaxTextArea();
        try {
            Field lhmField = RTextArea.class.getDeclaredField("lineHighlightManager");
            lhmField.setAccessible(true);
            lhmField.set(textArea, new CustomLineHighlightManager(textArea));
        } catch (Exception e) {
            e.printStackTrace();
        }

        textArea.setFont(JConsole.DEFAULT_FONT);
        Font codeFont = new Font(JConsole.DEFAULT_FONT.getName(), Font.PLAIN, 12);
        Font btnFont = new Font(JConsole.DEFAULT_FONT.getName(), Font.PLAIN, 10);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        textArea.setTabsEmulated(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCodeFoldingEnabled(false);
        textArea.setHighlightCurrentLine(false);

        InputStream in = getClass().getResourceAsStream("/dark.xml");
        try {
            Theme theme = Theme.load(in);
            theme.apply(textArea);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        textScroll = new RTextScrollPane(textArea);
        textScroll.setPreferredSize(playArea.getPreferredSize());
        textScroll.setVisible(false);
        cp.add(textScroll, BorderLayout.CENTER);

        GridBagLayout layout = new GridBagLayout();
        bottomPanel = new JPanel(layout);
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setPreferredSize(new Dimension(100, 35));
        bottomPanel.setVisible(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        {
            inventory = new JLabel("Inventory: #$%");
            inventory.setForeground(Color.WHITE);
            inventory.setPreferredSize(new Dimension((int) playArea.getPreferredSize().getWidth(), 35));
            inventory.setFont(JConsole.DEFAULT_FONT);
            //layout.setConstraints(inventory, new GridBagConstraints(0,0,));
            bottomPanel.add(inventory);
        }
        {
            JButton apiBtn = new JButton("[^1]API");
            apiBtn.setForeground(Color.WHITE);
            apiBtn.setBackground(Color.BLACK);
            apiBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            apiBtn.setBorder(compound);
            apiBtn.setFont(btnFont);
            apiBtn.getActionMap().put("API", apiAction);
            apiBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) apiAction.getValue(javax.swing.Action.ACCELERATOR_KEY), "API");
            apiBtn.addActionListener(apiAction);
            bottomPanel.add(apiBtn);
        }
        {
            JButton toggleBtn = new JButton("[^2]Toggle Focus");
            toggleBtn.setForeground(Color.WHITE);
            toggleBtn.setBackground(Color.BLACK);
            toggleBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            toggleBtn.setBorder(compound);
            toggleBtn.setFont(btnFont);
            toggleBtn.getActionMap().put("Toggle", toggleAction);
            toggleBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) toggleAction.getValue(javax.swing.Action.ACCELERATOR_KEY), "Toggle");
            toggleBtn.addActionListener(toggleAction);
            bottomPanel.add(toggleBtn);
        }
        {
            JButton notepadBtn = new JButton("[^3]Notepad");
            notepadBtn.setForeground(Color.WHITE);
            notepadBtn.setBackground(Color.BLACK);
            notepadBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            notepadBtn.setBorder(compound);
            notepadBtn.setFont(btnFont);
            notepadBtn.getActionMap().put("Notepad", notepadAction);
            notepadBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) notepadAction.getValue(javax.swing.Action.ACCELERATOR_KEY), "Notepad");
            notepadBtn.addActionListener(notepadAction);
            bottomPanel.add(notepadBtn);
        }
        {
            JButton resetBtn = new JButton("[^4]Reset");
            resetBtn.setForeground(Color.WHITE);
            resetBtn.setBackground(Color.BLACK);
            resetBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            resetBtn.setBorder(compound);
            resetBtn.setFont(btnFont);
            resetBtn.getActionMap().put("Reset", resetAction);
            resetBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) resetAction.getValue(javax.swing.Action.ACCELERATOR_KEY), "Reset");
            resetBtn.addActionListener(resetAction);
            bottomPanel.add(resetBtn);
        }
        {
            JButton executeBtn = new JButton("[^5]Execute");
            executeBtn.setForeground(Color.WHITE);
            executeBtn.setBackground(Color.BLACK);
            executeBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            executeBtn.setBorder(compound);
            executeBtn.setFont(btnFont);
            executeBtn.getActionMap().put("Execute", executeAction);
            executeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) executeAction.getValue(javax.swing.Action.ACCELERATOR_KEY), "Execute");
            executeBtn.addActionListener(executeAction);
            bottomPanel.add(executeBtn);
        }
        {
            JButton phoneBtn = new JButton("[Q]Phone");
            phoneBtn.setForeground(Color.WHITE);
            phoneBtn.setBackground(Color.BLACK);
            phoneBtn.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            phoneBtn.setBorder(compound);
            phoneBtn.setFont(btnFont);
            phoneBtn.addActionListener(phoneAction);
            bottomPanel.add(phoneBtn);
        }
        {
            JLabel span = new JLabel("        ");
            bottomPanel.add(span);
        }
        {
            JButton inventory = new JButton("[^0]Menu");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }

        cp.add(bottomPanel, BorderLayout.SOUTH);

        setTitle("J-Untrusted");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        textArea.setFont(codeFont);

        setResizable(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });

        menuWindow = new MenuWindow(this);
        menuWindow.setVisible(false);
        notepadWindow = new NotepadWindow(this);
        notepadWindow.setVisible(false);
    }

    AbstractAction apiAction = new AbstractAction("API") {
        {
            putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 1"));
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI("http://localhost:" + API_PORT + "/index.html"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    AbstractAction toggleAction = new AbstractAction("Toggle") {
        {
            putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 2"));
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            JConsole console = Editor.this.getConsole();
            RSyntaxTextArea codeEditor = Editor.this.getCodeEditor();
            if (console.hasFocus()) {
                codeEditor.grabFocus();
            } else {
                console.grabFocus();
            }
        }
    };
    AbstractAction notepadAction = new AbstractAction("Notepad") {
        {
            putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 3"));
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            notepadWindow.setVisible(true);
        }
    };
    AbstractAction resetAction = new AbstractAction("Reset") {
        {
            putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 4"));
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            String message = "Reset this level?";
            int answer = JOptionPane.showConfirmDialog(Editor.this, message, "Reset",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                resetCode = null;
                resetRequest = true;
            }
        }
    };
    AbstractAction executeAction = new AbstractAction("Execute") {
        {
            putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 5"));
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            resetCode = getCodeEditor().getText();
            resetRequest = true;
        }
    };
    AbstractAction phoneAction = new AbstractAction("Phone") {
        @Override
        public void actionPerformed(ActionEvent ev) {
            keyCode = KeyEvent.VK_Q;
        }
    };

    public void setInventory(String inv) {
        if (inv == null || inv.isEmpty()) {
            inventory.setText(" ");
        } else {
            inventory.setText("Inventory: " + inv);
        }
    }

    public Integer getKeyCode() {
        return keyCode;
    }

    public void resetKeyCode() {
        keyCode = null;
    }

    public boolean hasResetRequest() {
        return resetRequest;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void resetResetRequest() {
        resetCode = null;
        resetRequest = false;
    }

    public void setText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    public void setMap(char[][] map) {

        playArea.setBackground(Color.BLACK);
        playArea.setForeground(Color.GRAY);
        playArea.setCursorPos(49, 24);
        playArea.write('\n');

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                char c = map[y][x];
                if (c == 0) {
                    playArea.setBackground(Color.BLACK);
                    playArea.setForeground(Color.GRAY);
                    playArea.write(' ');
                } else {
                    playArea.setBackground(Color.BLACK);
                    playArea.setForeground(Color.GRAY);
                    playArea.write(c);
                }
            }
        }
    }


    public void execute(java.util.List<Action> actions) {
        if (actions != null) {
            for (Action action : actions) {
                action.execute(this);
            }
        }
        playArea.repaint();
    }


    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        {
            JMenu menu = new JMenu("API");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Toggle Focus");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Notepad");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Reset");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Execute");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Menu");
            mb.add(menu);
        }
        {
            menuSerach = new JMenu("Search");
            menuSerach.add(new JMenuItem(new ShowFindDialogAction()));
            menuSerach.add(new JMenuItem(new ShowReplaceDialogAction()));
            menuSerach.add(new JMenuItem(new GoToLineAction()));
            mb.add(menuSerach);
        }
        return mb;
    }


    /**
     * Creates our Find and Replace dialogs.
     */
    public void initSearchDialogs() {

        findDialog = new FindDialog(this, this);
        replaceDialog = new ReplaceDialog(this, this);

        // This ties the properties of the two dialogs together (match
        // case, regex, etc.).
        replaceDialog.setSearchContext(findDialog.getSearchContext());

    }


    /**
     * Listens for events from our search dialogs and actually does the dirty
     * work.
     */
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();
        SearchDialogSearchContext context = findDialog.getSearchContext();

        if (FindDialog.ACTION_FIND.equals(command)) {
            if (!SearchEngine.find(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        } else if (ReplaceDialog.ACTION_REPLACE.equals(command)) {
            if (!SearchEngine.replace(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        } else if (ReplaceDialog.ACTION_REPLACE_ALL.equals(command)) {
            int count = SearchEngine.replaceAll(textArea, context).getCount();
            JOptionPane.showMessageDialog(null, count
                    + " occurrences replaced.");
        }

    }

    public JConsole getConsole() {
        return playArea;
    }

    public RSyntaxTextArea getCodeEditor() {
        return textArea;
    }

    public void showCode() {
        textScroll.setVisible(true);
        bottomPanel.setVisible(true);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void hideCode() {
        textScroll.setVisible(false);
        bottomPanel.setVisible(false);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void loadCode(String code) {
        setText(code);
    }

    private class GoToLineAction extends AbstractAction {

        public GoToLineAction() {
            super("Go To Line...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            GoToDialog dialog = new GoToDialog(Editor.this);
            dialog.setMaxLineNumberAllowed(textArea.getLineCount());
            dialog.setVisible(true);
            int line = dialog.getLineNumber();
            if (line > 0) {
                try {
                    textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    ble.printStackTrace();
                }
            }
        }

    }


    private class ShowFindDialogAction extends AbstractAction {

        public ShowFindDialogAction() {
            super("Find...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            findDialog.setVisible(true);
        }

    }


    private class ShowReplaceDialogAction extends AbstractAction {

        public ShowReplaceDialogAction() {
            super("Replace...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            replaceDialog.setVisible(true);
        }

    }

    private Player player;
    private String currentMusic;

    public void playMusic(String music) {
        if (music != null && music.equals(currentMusic)) {
            return;
        }
        if (player != null) {
            player.requestStop();
        }
        if (music != null) {

            try {
                player = new Player(
                        new BufferedInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream("music/" + music))
                );
                player.start();
                currentMusic = music;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
