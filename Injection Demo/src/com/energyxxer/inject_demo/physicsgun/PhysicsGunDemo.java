package com.energyxxer.inject_demo.physicsgun;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;

import javax.swing.Timer;

import com.energyxxer.inject.InjectionConnection;
import com.energyxxer.inject.structure.Command;
import com.energyxxer.inject.utils.Vector3D;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;
import com.energyxxer.inject_demo.util.Transform;

/**
 * Created by User on 4/19/2017.
 */
public class PhysicsGunDemo implements SetupListener {

    private static final double MAX_REACH = 10;
    private static final double MIN_REACH = 1;

    private static final double SCROLL_STEP = 0.5;

    private static InjectionConnection connection;

    private static HashMap<String, PGPlayerInfo> playerInfo = new HashMap<>();

    private PhysicsGunDemo() {
        new DisplayWindow("Physics Gun", Commons.WORLD_NAME, this);
    }

    public static void main(String[] a) {
        new PhysicsGunDemo();
    }

    @Override
    public void onSetup(File log, File world) {
        try {
          connection = new InjectionConnection(log.toPath(), world.toPath(), "physicsgun");
        } catch (IOException | InterruptedException ex) {
          throw new UndeclaredThrowableException(ex);
        }
        connection.getLogObserver().setLogCheckFrequency(100, MILLISECONDS);
        connection.setFlushFrequency(50, MILLISECONDS);

        //When player state has changed (1: Attempt to grab an entity, 3: Attempt to launch/drop an entity)
        connection.getLogObserver().addLogListener(l -> {
            String text = l.getLine();
            String leadingText = "Set score of pg_state for player ";
            if(!text.startsWith("[Server thread/INFO]: [",11) || !text.contains(leadingText)) return;
            text = text.substring(text.indexOf(leadingText));
            String username = text.substring(leadingText.length(),text.indexOf(' ',leadingText.length()));
            int stage = -1;
            if(text.indexOf('1',leadingText.length() + username.length()) >= 0) stage = 1;
            else if(text.indexOf('3',leadingText.length() + username.length()) >= 0) stage = 3;

            //Player attempted to begin controlling an entity
            if(stage == 1) {
                if(playerInfo.containsKey(username)) {
                    PGPlayerInfo player = playerInfo.get(username);
                    //Raytracing to find possible entities
                    for(double i = 1; i <= MAX_REACH; i++) {
                        Vector3D.Double vec = player.transform.forward(i);
                        vec.translate(0,PGPlayerInfo.EYE_LEVEL,0);
                        connection.injectImpulseCommand("execute " + username + " " + vec + " scoreboard players tag @e[name=!" + username + ",r=2] add pg_control_" + username + "$0");
                    }
                    //Tagging the closest matching entity
                    connection.injectImpulseCommand("execute " + username + " ~ ~ ~ scoreboard players tag @e[tag=pg_control_" + username + "$0,c=1] add pg_control_" + username);
                    //Set player's state to 2 (if entity found)
                    connection.injectImpulseCommand("execute @e[tag=pg_control_" + username + "] ~ ~ ~ scoreboard players set " + username + " pg_state 2");
                    //Setting entity to have no gravity (if entity found)
                    connection.injectImpulseCommand("entitydata @e[tag=pg_control_" + username + "] {NoGravity:1b}");
                    //Setting up entity's position getter
                    connection.injectImpulseCommand("execute @e[tag=pg_control_" + username + "] ~ ~ ~ summon area_effect_cloud ~ ~ ~ {CustomName:\"$pgControlTransform#" + username + "\",Duration:2}");
                    connection.injectImpulseCommand("execute @e[tag=pg_control_" + username + "] ~ ~ ~ teleport @e[type=area_effect_cloud,name=$pgControlTransform#" + username + "] ~ ~ ~ ~ ~");
                    //Commands from this point on will see their output in the logs
                    connection.injectImpulseCommand("gamerule logAdminCommands true");
                    //Remove temporary tags from matching entities (keeping the closest one with its own tag). If this succeeds, this player's "active" field will be set to true
                    connection.injectImpulseCommand(new Command("$pgEnable:" + username, "scoreboard players tag @e[tag=pg_control_" + username + "$0] remove pg_control_" + username + "$0"));
                    //Get the controlled entity's position
                    connection.injectImpulseCommand(new Command("pg_control_" + username, "entitydata @e[type=area_effect_cloud,name=$pgControlTransform#" + username + "] {pg:\"transform2\"}"));
                    //Set player's state back to 0 if no entities were found
                    connection.injectImpulseCommand(new Command("$pgDisable:" + username, "scoreboard players set @a[name=" + username + ",score_pg_state_min=1,score_pg_state=1] pg_state 0"));
                    //Disable command logging
                    connection.injectImpulseCommand("gamerule logAdminCommands false");
                } else {
                    //Technically this block should never run
                    playerInfo.put(username, new PGPlayerInfo(username));
                    connection.injectImpulseCommand("tellraw @a {\"text\":\"[WARNING]: Player '" + username + "' requested an action but wasn't found on the player database.\",\"color\":\"yellow\"}");
                }
            } else if(stage == 3) {
                if(playerInfo.containsKey(username)) {
                    PGPlayerInfo player = playerInfo.get(username);
                    player.active = false;

                    Vector3D.Double forward = player.transform.forward(1);
                    forward.x -= player.transform.x;
                    forward.y -= player.transform.y;
                    forward.z -= player.transform.z;

                    forward.x *= 2 * MAX_REACH / player.distance;
                    forward.y *= 2 * MAX_REACH / player.distance;
                    forward.z *= 2 * MAX_REACH / player.distance;

                    connection.injectImpulseCommand("execute @a[name=" + username + ",tag=!pg_sneaking] ~ ~ ~ playsound minecraft:entity.ghast.shoot master @a ~ ~ ~ 1 1 0");
                    connection.injectImpulseCommand("execute @a[name=" + username + ",tag=!pg_sneaking] ~ ~ ~ playsound minecraft:entity.zombie.attack_iron_door master @a ~ ~ ~ 1 1 0");
                    connection.injectImpulseCommand("entitydata @e[tag=pg_control_" + username + "] {NoGravity:0b}");
                    connection.injectImpulseCommand("execute @a[name=" + username + ",tag=!pg_sneaking] ~ ~ ~ entitydata @e[tag=pg_control_" + username + "] {Motion:[" + forward.x + "," + forward.y + "," + forward.z + "]}");
                    connection.injectImpulseCommand("execute @a[name=" + username + ",tag=!pg_sneaking] ~ ~ ~ execute @e[tag=pg_control_" + username + "] ~ ~ ~ particle largesmoke ~ ~0.5 ~ 0 0 0 " + (MAX_REACH / player.distance) + " " + (int) (2 * (MAX_REACH - player.distance)) + " force");
                    connection.injectImpulseCommand("scoreboard players set " + username + " pg_state 0");
                    connection.injectImpulseCommand("scoreboard players tag @e[tag=pg_control_" + username + "] remove pg_control_" + username);
                } else {
                    playerInfo.put(username, new PGPlayerInfo(username));
                    connection.injectImpulseCommand("tellraw @a {\"text\":\"[WARNING]: Player '" + username + "' requested an action but wasn't found on the player database.\",\"color\":\"yellow\"}");
                }
            }
        });

        connection.getLogObserver().addLogListener(l -> {
            String line = l.getLine();
            String leadingText = "Set score of pg_distChange for player ";
            if(!line.startsWith("[Server thread/INFO]: [",11) || !line.contains(leadingText)) return;
            line = line.substring(line.indexOf(leadingText), line.length()-1);
            String username = line.substring(38, line.indexOf(" ",38));
            String rawChange = line.substring(38 + username.length() + 4);
            int change = Integer.parseInt(rawChange);

            PGPlayerInfo player;

            if(playerInfo.containsKey(username)) {
                player = playerInfo.get(username);
            } else {
                player = new PGPlayerInfo(username);
                playerInfo.put(username, player);
            }

            player.distance += change * SCROLL_STEP;
            player.distance = Math.max(Math.min(player.distance, MAX_REACH), MIN_REACH);
        });

        connection.getLogObserver().addLogListener(e -> {
            String text = e.getLine();
            if(text.startsWith("[Server thread/INFO]: [",11) && text.contains(",pg:\"transform\",")) {
                String username = text.substring(34,text.indexOf(':',34));

                PGPlayerInfo player;

                if(playerInfo.containsKey(username)) {
                    player = playerInfo.get(username);
                } else {
                    player = new PGPlayerInfo(username);
                    playerInfo.put(username, player);
                }

                Transform tr = getTransform(text);

                if(tr != null) player.transform = tr;
            } else if(text.startsWith("[Server thread/INFO]: [",11) && text.contains(",pg:\"transform2\"")) {
                String name = text.substring(34,text.indexOf(':',34));

                if(!name.startsWith("pg_control_")) return;
                String username = name.substring("pg_control_".length());

                PGPlayerInfo player;

                if(playerInfo.containsKey(username)) {
                    player = playerInfo.get(username);
                } else {
                    player = new PGPlayerInfo(username);
                    playerInfo.put(username, player);
                }

                Transform controller = player.transform;
                Transform controlled = getTransform(text);
                if(controlled == null) return;

                player.distance = Math.sqrt(Math.pow(controlled.x - controller.x, 2) + Math.pow(controlled.y - controller.y, 2) + Math.pow(controlled.z - controller.z, 2));
            }
            String enable = e.getReturnValueFor("$pgEnable:",true);
            if(enable != null) {
                String username = enable.substring(0,enable.indexOf(':'));

                if(playerInfo.containsKey(username)) {
                    playerInfo.get(username).active = true;
                } else {
                    playerInfo.put(username, new PGPlayerInfo(username));
                }
            }
            String disable = e.getReturnValueFor("$pgDisable:",true);
            if(disable != null) {
                String username = disable.substring(0,disable.indexOf(':'));

                if(playerInfo.containsKey(username)) {
                    playerInfo.get(username).active = false;
                } else {
                    playerInfo.put(username, new PGPlayerInfo(username));
                }
            }
        });

        Timer timer = new Timer(100, e -> {
            for(PGPlayerInfo player : playerInfo.values()) {
                if(player.active) {
                    Vector3D.Double forward = player.transform.forward(player.distance).translated(0,PGPlayerInfo.EYE_LEVEL - 0.5,0);
                    forward.x -= player.transform.x;
                    forward.y -= player.transform.y;
                    forward.z -= player.transform.z;

                    connection.injectRepeatCommand("execute @a[name=" + player.username + ",score_pg_state_min=2,score_pg_state=2] ~ ~ ~ teleport @e[tag=pg_control_" + player.username + "] ~" + forward.x + " ~" + forward.y + " ~" + forward.z);
                    connection.injectRepeatCommand("execute @a[name=" + player.username + ",score_pg_state_min=2,score_pg_state=2] ~ ~ ~ execute @e[tag=pg_control_" + player.username + "] ~ ~ ~ particle reddust ~ ~0.5 ~ 0.0001 0.75 1 1 0 force");
                    connection.injectRepeatCommand("execute @a[name=" + player.username + ",score_pg_state_min=2,score_pg_state=2] ~ ~ ~ playsound minecraft:entity.guardian.ambient player @a ~ ~ ~ 1.0 1.5 0.0");
                }
            }
        });

        timer.start();
    }

    private static Transform getTransform(String line) {
        Transform transform = new Transform();
        {
            int posIndex = line.indexOf(",Pos:[", 34) + ",Pos:[".length();
            if (posIndex < ",Pos:[".length()) return null;
            String rawPos = line.substring(posIndex, line.indexOf(']', posIndex)).replaceAll("\\d:","").replace("d", "").replace(",", " ");
            String[] pos = rawPos.split(" ");
            transform.x = Double.parseDouble(pos[0]);
            transform.y = Double.parseDouble(pos[1]);
            transform.z = Double.parseDouble(pos[2]);
        }

        {
            int rotIndex = line.indexOf(",Rotation:[", 34) + ",Rotation:[".length();
            if (rotIndex < ",Rotation:[".length()) return null;
            String rawRot = line.substring(rotIndex, line.indexOf(']', rotIndex)).replaceAll("\\d:","").replace("f", "").replace(",", " ");
            String[] rot = rawRot.split(" ");
            transform.yaw = Double.parseDouble(rot[0]);
            transform.pitch = Double.parseDouble(rot[1]);
        }
        return transform;
    }
}
