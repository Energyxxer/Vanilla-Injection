package com.energyxxer.inject_demo.warps;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.energyxxer.inject.InjectionConnection;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;
import com.energyxxer.inject_demo.util.Transform;

/**
 * Created by User on 4/11/2017.
 */
public class WarpSystem implements SetupListener {

    private File warpLogFile = null;

    private static InjectionConnection connection;
    private static HashMap<String, Transform> warps;

    private static int commandID = 0;

    private static List<String> reservedKeys = Arrays.asList("set", "remove", "list");

    private WarpSystem() {
        new DisplayWindow("Warps", Commons.WORLD_NAME, this);
    }

    public static void main(String[] a) {
        new WarpSystem();
    }

    @Override
    public void onSetup(File log, File world) {
        try {
          connection = new InjectionConnection(log.toPath(), world.toPath(), "warp");
        } catch (IOException | InterruptedException ex) {
          throw new UndeclaredThrowableException(ex);
        }
        warpLogFile = new File(world.getAbsolutePath() + File.separator + "warps.txt");
        connection.getLogObserver().setLogCheckFrequency(500, MILLISECONDS);
        connection.setFlushFrequency(500, MILLISECONDS);

        warps = new HashMap<>();

        try {
            if(!warpLogFile.exists()) warpLogFile.createNewFile();
        } catch(IOException x) {
            x.printStackTrace();
        }

        load();

        connection.getLogObserver().addChatListener(m -> {
            if(m.getMessage().split(" ",2)[0].equals(".warp")) {
                String[] args = m.getMessage().split(" ");
                if(args.length < 2) {
                    connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Usage:\n    warp <warp name>\n    warp set <warp name>\n    warp remove <warp name>\n    warp list\",\"color\":\"red\"}");
                } else if(args[1].equals("set")) {
                    if(args.length < 3) {
                        connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Usage: warp set <warp name>\",\"color\":\"red\"}");
                    } else {
                        if(reservedKeys.contains(args[2])) {
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Name '" + args[2] + "' is a reserved keyword.\",\"color\":\"red\"}");
                        } else if(warps.containsKey(args[2])) {
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"A warp by the name '" + args[2] + "' already exists!\",\"color\":\"red\"}");
                        } else {
                            int currentCommandID = commandID++;
                            String name = "$warpSet" + currentCommandID;
                            connection.injectCommand("execute " + m.getSender() + " ~ ~ ~ summon area_effect_cloud ~ ~ ~ {CustomName:\"" + name + "\"}");
                            connection.injectCommand("tp @e[type=area_effect_cloud,name=" + name + "] " + m.getSender());
                            connection.injectCommand("entitydata @e[type=area_effect_cloud,name=" + name + "] {fe:tch}", l -> {
                                Transform warpTransform = new Transform();

                                //Pos
                                int posIndex = l.getMessage().indexOf(",Pos:[");
                                String rawPos = l.getMessage().substring(posIndex + ",Pos:[".length());
                                rawPos = rawPos.substring(0,rawPos.indexOf("]"));
                                rawPos = rawPos.replaceAll("\\d:","").replace("d","");
                                String[] pos = rawPos.split(",");
                                warpTransform.x = Double.parseDouble(pos[0]);
                                warpTransform.y = Double.parseDouble(pos[1]);
                                warpTransform.z = Double.parseDouble(pos[2]);

                                //Rot
                                int rotIndex = l.getMessage().indexOf(",Rotation:[");
                                String rawRot = l.getMessage().substring(rotIndex + ",Rotation:[".length());
                                rawRot = rawRot.substring(0,rawRot.indexOf("]"));
                                rawRot = rawRot.replaceAll("\\d:","").replace("f","");
                                String[] rot = rawRot.split(",");
                                warpTransform.yaw = Float.parseFloat(rot[0]);
                                warpTransform.pitch = Float.parseFloat(rot[1]);

                                warps.put(args[2], warpTransform);
                                connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Warp '" + args[2] + "' has been set.\",\"color\":\"green\"}");
                                save();
                            });
                        }
                    }
                } else if(args[1].equals("remove")) {
                    if(args.length < 3) {
                        connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Usage: warp remove <warp name>\",\"color\":\"red\"}");
                    } else {
                        if(warps.containsKey(args[2])) {
                            warps.remove(args[2]);
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Warp '" + args[2] + "' has been removed.\",\"color\":\"green\"}");
                            save();
                        } else {
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"A warp by the name '" + args[2] + "' doesn't exist!\",\"color\":\"red\"}");
                        }
                    }
                } else if(args[1].equals("list")) {
                    if(warps.isEmpty()) connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"There are no warps.\",\"color\":\"yellow\"}");
                    else {
                        StringBuilder list = new StringBuilder();
                        for(String key : warps.keySet()) {
                            list.append(",{\"text\":\"");
                            list.append("\n    ");
                            list.append(key);
                            list.append(" (");
                            list.append(warps.get(key).toSimplifiedString());
                            list.append(")\",");
                            list.append("\"clickEvent\":{\"action\":\"run_command\",\"value\":\".warp ");
                            list.append(key);
                            list.append("\"},");
                            list.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Warp to '");
                            list.append(key);
                            list.append("'\"},");
                            list.append("\"color\":\"dark_aqua\"}");
                        }
                        connection.injectImpulseCommand("tellraw " + m.getSender() + " [{\"text\":\"List of warps (" + warps.size() + "):\",\"color\":\"aqua\"}" + list + "]");
                    }
                } else {
                    if(args.length < 2) {
                        connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Usage: warp <warp name>\",\"color\":\"red\"}");
                    } else {
                        if(warps.containsKey(args[1])) {
                            connection.injectImpulseCommand("tp " + m.getSender() + " " + warps.get(args[1]));
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"Warping to '" + args[1] + "'\",\"color\":\"yellow\"}");
                        } else {
                            connection.injectImpulseCommand("tellraw " + m.getSender() + " {\"text\":\"A warp by the name '" + args[1] + "' doesn't exist!\",\"color\":\"red\"}");
                        }
                    }
                }
            }
        });

        connection.injectImpulseCommand("tellraw @a {\"text\":\"§3[§bWarps§3] §3Warp systems online.\"}");
    }

    private void load() {
        FileInputStream inputStream;
        Scanner sc;
        try {
            inputStream = new FileInputStream(warpLogFile.getPath());
            sc = new Scanner(inputStream, "UTF-8");

            while(sc.hasNextLine()) {
                String line = sc.nextLine();

                if(line.length() > 0) {
                    String[] segments = line.split(":",2);
                    String name = segments[0];
                    String[] rawTransform = segments[1].split(" ");

                    warps.put(name, new Transform(
                            Double.parseDouble(rawTransform[0]),
                            Double.parseDouble(rawTransform[1]),
                            Double.parseDouble(rawTransform[2]),
                            Double.parseDouble(rawTransform[3]),
                            Double.parseDouble(rawTransform[4])
                    ));
                }
            }
        } catch(IOException x) {
            x.printStackTrace();
        }
    }

    private void save() {
        try(PrintWriter pw = new PrintWriter(warpLogFile)) {
            for(String key : warps.keySet()) {
                pw.print(key);
                pw.print(":");
                pw.print(warps.get(key).toString());
                pw.println();
            }
        } catch(IOException x) {
            x.printStackTrace();
        }
    }
}
