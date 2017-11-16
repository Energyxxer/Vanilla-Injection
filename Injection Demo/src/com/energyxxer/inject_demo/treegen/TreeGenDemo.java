package com.energyxxer.inject_demo.treegen;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import com.energyxxer.inject.v2.InjectionConnection;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;

import de.adrodoc55.minecraft.coordinate.Vec3I;

/**
 * Created by Energyxxer on 4/12/2017.
 */
public class TreeGenDemo implements SetupListener {

    private static InjectionConnection connection;

    private TreeGenDemo() {
        new DisplayWindow("Tree Generator", Commons.WORLD_NAME, this);
    }

    public static void main(String[] args) {
        new TreeGenDemo();
    }

    @Override
    public void onSetup(File log, File world) {
        try {
          connection = new InjectionConnection(log.toPath(), world.toPath(), "treegen");
        } catch (IOException | InterruptedException ex) {
          throw new UndeclaredThrowableException(ex);
        }
        connection.getLogObserver().setLogCheckFrequency(500, MILLISECONDS);
        connection.setFlushFrequency(500, MILLISECONDS);

        connection.setImpulseSize(new Vec3I(14, 128, 14));
        connection.setRepeatSize(new Vec3I());

        connection.getLogObserver().addSuccessListener("$genTree", false, l -> {
            //master.injectImpulseCommand("tellraw @a {\"text\":\"Generating a tree...\"}");
            Tree gen = new Tree(connection);
            gen.generate();
            connection.injectImpulseCommand("kill @e[type=armor_stand,name=$genTree]");
        });

        connection.getLogObserver().addChatListener(l -> {
            String text = l.getMessage();
            String[] args = text.split(" ");
            if(args.length < 1) return;
            if(args[0].equalsIgnoreCase(".setMinTreeHeight")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMinTreeHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setMinTreeHeight(Integer.parseInt(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Minimum tree height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMaxTreeHeight")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMaxTreeHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setMaxTreeHeight(Integer.parseInt(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Maximum tree height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setBranchChance")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setBranchChance <double>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setBranchChance(Double.parseDouble(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Branch chance set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMinBranchLength")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMinBranchLength <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    TreeBranch.setMinLength(Integer.parseInt(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Minimum branch length set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMaxBranchLength")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMaxBranchLength <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    TreeBranch.setMaxLength(Integer.parseInt(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Maximum branch length set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setBaseHeight")) {
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setBaseHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setBaseHeight(Integer.parseInt(rawInput));
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Base height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    connection.injectImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            }
        });
    }
}
