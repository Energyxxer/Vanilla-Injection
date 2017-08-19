package com.energyxxer.inject_demo.common;

import com.energyxxer.inject.utils.MinecraftUtils;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Test graphical interface for the setup of injection modules.
 */
public class DisplayWindow extends JFrame {

    private static final Color BACKGROUND = new Color(60, 63, 65);
    private static final Color FOREGROUND = new Color(187, 187, 187);
    private static final Color ERROR = new Color(187, 63, 63);

    private String moduleName;
    private SetupListener listener;

    private File log;
    private File world;

    public DisplayWindow(String name, String defaultWorld, SetupListener l) throws HeadlessException {
        super(name);
        this.moduleName = name;
        this.listener = l;
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel setupPanel = new JPanel();
        setupPanel.setBackground(BACKGROUND);
        setupPanel.setLayout(new BoxLayout(setupPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Setup for injection module '" + name + "'");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(FOREGROUND);
        titleLabel.setMaximumSize(new Dimension(400, 50));
        titleLabel.setPreferredSize(new Dimension(400, 50));
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        setupPanel.add(titleLabel);

        JLabel setupLogLabel = new JLabel(" Path to log file: ");
        setupLogLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupLogLabel.setForeground(FOREGROUND);
        setupLogLabel.setMaximumSize(new Dimension(400, 40));
        setupLogLabel.setPreferredSize(new Dimension(400, 20));
        setupPanel.add(setupLogLabel);

        JTextField logInput = new JTextField();
        logInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        logInput.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        logInput.setBackground(BACKGROUND);
        logInput.setForeground(FOREGROUND);
        logInput.setCaretColor(FOREGROUND);
        logInput.setMaximumSize(new Dimension(400, 25));
        logInput.setBorder(new CompoundBorder(new EmptyBorder(0,0,10,0),new CompoundBorder(new LineBorder(new Color(128,128,128)),new LineBorder(BACKGROUND,5))));
        setupPanel.add(logInput);

        logInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.update();
            }

            private void update() {
                File f = new File(logInput.getText());
                logInput.setForeground((f.exists() && !f.isDirectory()) ? FOREGROUND : ERROR);
            }
        });
        logInput.setText(MinecraftUtils.getDefaultMinecraftDir() + File.separator + "logs" + File.separator + "latest.log");

        JLabel setupWorldLabel = new JLabel(" Path to world log: ");
        setupWorldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupWorldLabel.setForeground(FOREGROUND);
        setupWorldLabel.setMaximumSize(new Dimension(400, 40));
        setupWorldLabel.setPreferredSize(new Dimension(400, 20));
        setupPanel.add(setupWorldLabel);

        JTextField worldInput = new JTextField();
        worldInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        worldInput.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        worldInput.setBackground(BACKGROUND);
        worldInput.setForeground(FOREGROUND);
        worldInput.setCaretColor(FOREGROUND);
        worldInput.setMaximumSize(new Dimension(400, 25));
        worldInput.setBorder(new CompoundBorder(new EmptyBorder(0,0,10,0),new CompoundBorder(new LineBorder(new Color(128,128,128)),new LineBorder(BACKGROUND,5))));
        setupPanel.add(worldInput);

        worldInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.update();
            }

            private void update() {
                File f = new File(worldInput.getText());
                worldInput.setForeground((f.exists() && f.isDirectory()) ? FOREGROUND : ERROR);
            }
        });
        worldInput.setText(MinecraftUtils.getDefaultMinecraftDir() + File.separator + "saves" + File.separator + defaultWorld);

        JButton confirm = new JButton("OK") {

            private final Color borderColor = new Color(150,150,150);
            private final Color rolloverColor = new Color(80, 81, 83);
            private final Color pressedColor = new Color(50, 52, 53);
            private final int borderThickness = 1;

            {
                this.setBorderPainted(false);
                this.setFocusPainted(false);
                this.setOpaque(false);
                this.setContentAreaFilled(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(borderColor);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                if(this.getModel().isPressed()) {
                    g.setColor(pressedColor);
                } else if(this.getModel().isRollover()) {
                    g.setColor(rolloverColor);
                } else {
                    g.setColor(this.getBackground());
                }
                g.fillRect(borderThickness, borderThickness, this.getWidth()-2* borderThickness, this.getHeight()-2* borderThickness);
                super.paintComponent(g);
            }
        };
        confirm.setBackground(new Color(70, 72, 75));
        confirm.setForeground(FOREGROUND);

        Runnable confirmAction = () -> {
            this.log = new File(logInput.getText());
            this.world = new File(worldInput.getText());

            this.dispatchSetupEvent();
            this.updateContentPane();
        };
        logInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmAction.run();
                }
            }
        });

        worldInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmAction.run();
                }
            }
        });

        confirm.addActionListener(e -> confirmAction.run());

        setupPanel.add(confirm);

        JPanel endPadding = new JPanel();
        endPadding.setOpaque(false);
        setupPanel.add(endPadding);

        setupPanel.setPreferredSize(new Dimension(500, 200));

        this.setContentPane(setupPanel);
        this.pack();
        this.setVisible(true);
    }

    private void updateContentPane() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BACKGROUND);

        JLabel label = new JLabel("Currently running module '" + moduleName + "' on world '" + world.getName() + "'");
        label.setForeground(FOREGROUND);
        label.setBorder(new EmptyBorder(25, 25, 25, 25));
        infoPanel.add(label);

        this.setContentPane(infoPanel);
        this.revalidate();
        this.repaint();
        this.setVisible(false);
        this.pack();
        this.setVisible(true);
    }

    private void dispatchSetupEvent() {
        listener.onSetup(this.log, this.world);
    }
}
