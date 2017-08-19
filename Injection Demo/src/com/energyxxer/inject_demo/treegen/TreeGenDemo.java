package com.energyxxer.inject_demo.treegen;

import com.energyxxer.inject.InjectionMaster;
import com.energyxxer.inject.utils.Vector3D;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;

import java.io.File;

/**
 * Created by Energyxxer on 4/12/2017.
 */
public class TreeGenDemo implements SetupListener {

    private static InjectionMaster master;

    private TreeGenDemo() {
        new DisplayWindow("Tree Generator", Commons.WORLD_NAME, this);
    }

    public static void main(String[] args) {
        new TreeGenDemo();
    }

    @Override
    public void onSetup(File log, File world) {
        master = new InjectionMaster(world, log, "treegen");
        master.setLogCheckFrequency(500);
        master.setInjectionFrequency(500);

        master.injector.setImpulseSize(new Vector3D(14, 128, 14));
        master.injector.setRepeatingSize(new Vector3D());

        master.addSuccessListener("$genTree",l -> {
            //master.injector.insertImpulseCommand("tellraw @a {\"text\":\"Generating a tree...\"}");
            Tree gen = new Tree(master.injector);
            gen.generate();
            master.injector.insertImpulseCommand("kill @e[type=armor_stand,name=$genTree]");
        });

        master.addChatListener(l -> {
            String text = l.getMessage();
            String[] args = text.split(" ");
            if(args.length < 1) return;
            if(args[0].equalsIgnoreCase(".setMinTreeHeight")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMinTreeHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setMinTreeHeight(Integer.parseInt(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Minimum tree height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMaxTreeHeight")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMaxTreeHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setMaxTreeHeight(Integer.parseInt(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Maximum tree height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setBranchChance")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setBranchChance <double>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setBranchChance(Double.parseDouble(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Branch chance set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMinBranchLength")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMinBranchLength <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    TreeBranch.setMinLength(Integer.parseInt(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Minimum branch length set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setMaxBranchLength")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setMaxBranchLength <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    TreeBranch.setMaxLength(Integer.parseInt(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Maximum branch length set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            } else if(args[0].equalsIgnoreCase(".setBaseHeight")) {
                if(args.length < 2) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Usage: .setBaseHeight <int>\",\"color\":\"red\"}]");
                    return;
                }
                String rawInput = args[1];
                try {
                    Tree.setBaseHeight(Integer.parseInt(rawInput));
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Base height set to " + rawInput + "\",\"color\":\"dark_aqua\"}]");
                } catch(NumberFormatException x) {
                    master.injector.insertImpulseCommand("tellraw " + l.getSender() + " [{\"text\":\"Invalid input '" + rawInput + "'\",\"color\":\"red\"}]");
                }
            }
        });

        master.start();
    }
}
