package com.energyxxer.inject_demo.common;

import com.energyxxer.inject.utils.MinecraftUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Test graphical interface for the setup of injection modules.
 */
public class DisplayWindow extends JFrame {

    private static final Color BACKGROUND = new Color(60, 63, 65);
    private static final Color FOREGROUND = new Color(187, 187, 187);

    private String moduleName;
    private SetupListener listener;

    private String directory;
    private String world;

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

        JLabel setupDirLabel = new JLabel(" Minecraft directory: ");
        setupDirLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupDirLabel.setForeground(FOREGROUND);
        setupDirLabel.setMaximumSize(new Dimension(400, 40));
        setupDirLabel.setPreferredSize(new Dimension(400, 20));
        setupPanel.add(setupDirLabel);

        JTextField directoryInput = new JTextField(MinecraftUtils.getDefaultMinecraftDir());
        directoryInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        directoryInput.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        directoryInput.setBackground(BACKGROUND);
        directoryInput.setForeground(FOREGROUND);
        directoryInput.setCaretColor(FOREGROUND);
        directoryInput.setMaximumSize(new Dimension(400, 25));
        directoryInput.setBorder(new CompoundBorder(new EmptyBorder(0,0,10,0),new CompoundBorder(new LineBorder(new Color(128,128,128)),new LineBorder(BACKGROUND,5))));
        setupPanel.add(directoryInput);

        JLabel setupSaveLabel = new JLabel(" World name: ");
        setupSaveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupSaveLabel.setForeground(FOREGROUND);
        setupSaveLabel.setMaximumSize(new Dimension(400, 40));
        setupSaveLabel.setPreferredSize(new Dimension(400, 20));
        setupPanel.add(setupSaveLabel);

        JTextField worldInput = new JTextField(defaultWorld);
        worldInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        worldInput.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        worldInput.setBackground(BACKGROUND);
        worldInput.setForeground(FOREGROUND);
        worldInput.setCaretColor(FOREGROUND);
        worldInput.setMaximumSize(new Dimension(400, 25));
        worldInput.setBorder(new CompoundBorder(new EmptyBorder(0,0,10,0),new CompoundBorder(new LineBorder(new Color(128,128,128)),new LineBorder(BACKGROUND,5))));
        setupPanel.add(worldInput);

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

        confirm.addActionListener(e -> {
            this.directory = directoryInput.getText();
            this.world = worldInput.getText();
            this.dispatchSetupEvent();
            this.updateContentPane();
        });

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

        JLabel label = new JLabel("Currently running module '" + moduleName + "' on world '" + world + "'");
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
        listener.onSetup(this.directory, this.world);
    }
}
