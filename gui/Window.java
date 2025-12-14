package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import parser.Parser;
import scanner.Lexer;
import scanner.Token;
import utils.Utils;

public class Window extends JFrame {

    private Editor editor;
    private JTextArea tokensLbl;
    private JTextArea treeLbl;
    private Lexer lexer;
    private Parser parser;
    private File file;
    
    public Window() {
        super("Compiler Construction - SPL++");
        this.lexer = new Lexer();
        this.parser = new Parser();
        

        JMenuBar mb = new JMenuBar();
        mb.add(initFileMenu());

        mb.add(Box.createHorizontalGlue());
        mb.add(initRunButton());
        setJMenuBar(mb);

        this.editor = new Editor();
        this.add(editor);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1080, 920);

        var tokensFrame = new JFrame("Tokens");
        this.tokensLbl = new JTextArea();
        tokensFrame.add(tokensLbl);
        tokensFrame.setLocation(getX() + getWidth(), getY());
        tokensFrame.setSize(320, getHeight()/2);
        tokensFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        tokensFrame.setVisible(true);

        var treeFrame = new JFrame("Tree");
        this.treeLbl = new JTextArea();
        treeFrame.add(treeLbl);
        treeFrame.setLocation(getX() + getWidth(), tokensFrame.getY() + tokensFrame.getHeight());
        treeFrame.setSize(320, getHeight()/2);
        treeFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        treeFrame.setVisible(true);
    }

    private JButton initRunButton() {
        var runBtn = new JButton("▶");
        runBtn.setToolTipText("Run");
        runBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lexer.reset(editor.getText());
                ArrayList<Token> tokens = new ArrayList<>();
                try {
                    tokens = lexer.tokenize();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                tokensLbl.setText(Utils.collectionToString(tokens, '\n'));

                parser.reset(tokens);
                var p = parser.parse();
                p.execute();

                treeLbl.setText(p.toString());
            }
        });
        return runBtn;
    }

    private JMenu initFileMenu() {
        var frame = this;
        JMenu fm = new JMenu("File");

        JMenuItem mi = new JMenuItem("Open");
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setFileFilter(new FileNameExtensionFilter("SPL files", "spl"));
                int result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    frame.editor.setText(Utils.loadSPL(file.getAbsolutePath()));
                }
            }
        });
        fm.add(mi);

        fm.addSeparator();

        mi = new JMenuItem("Save");
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file == null) return;
                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write(frame.editor.getText());
                    fw.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        fm.add(mi);

        mi = new JMenuItem("Save As");
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                fc.setFileFilter(new FileNameExtensionFilter("SPL files", "spl"));
                int result = fc.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    try {
                        FileWriter fw = new FileWriter(file+".spl");
                        fw.write(frame.editor.getText());
                        fw.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        fm.add(mi);

        fm.addSeparator();

        mi = new JMenuItem("Exit");
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }});
        fm.add(mi);
        return fm;
    }
}
