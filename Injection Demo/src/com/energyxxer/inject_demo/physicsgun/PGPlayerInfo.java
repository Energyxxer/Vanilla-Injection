package com.energyxxer.inject_demo.physicsgun;

import com.energyxxer.inject_demo.util.Transform;

/**
 * Created by User on 4/19/2017.
 */
public class PGPlayerInfo {
    private static int NEXT_ID = 0;
    public static double EYE_LEVEL = 1.68125;

    final String username;
    final int id;
    Transform transform = new Transform();
    double distance = 2;
    boolean active = false;

    public PGPlayerInfo(String username) {
        this.username = username;
        this.id = NEXT_ID++;
    }
}
