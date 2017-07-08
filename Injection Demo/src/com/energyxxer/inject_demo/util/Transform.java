package com.energyxxer.inject_demo.util;

import com.energyxxer.inject.utils.Vector3D;

/**
 * Created by User on 4/11/2017.
 */
public class Transform {
    public double x,y,z,yaw,pitch;

    public Transform() {
        this(0, 0, 0, 0, 0);
    }

    public Transform(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Transform(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector3D.Double forward(double distance) {
        double yawRad = Math.toRadians(360-yaw);
        double pitchRad = Math.toRadians(pitch);

        double sinYaw = Math.sin(yawRad);
        double cosYaw = Math.cos(yawRad);
        double sinPitch = Math.sin(pitchRad);
        double cosPitch = Math.cos(pitchRad);

        Vector3D.Double p = new Vector3D.Double(0, distance * -sinPitch, distance * cosPitch);

        return new Vector3D.Double(x + p.z * sinYaw, y + p.y, z + p.x * -sinYaw + p.z * cosYaw);
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z + " " + yaw + " " + pitch;
    }

    public String toSimplifiedString() {
        return (int) x + " " + (int) y + " " + (int) z;
    }

    public Vector3D.Double asVector() {
        return new Vector3D.Double(x,y,z);
    }
}
