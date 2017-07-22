package com.energyxxer.inject.utils;

import java.io.File;

/**
 * Created by User on 4/11/2017.
 */
public class MinecraftUtils {
    public static String getDefaultMinecraftDir() {
        String workingDirectory;
        // here, we assign the name of the OS, according to Java, to a
        // variable...
        String OS = (System.getProperty("os.name")).toUpperCase();
        // to determine what the workingDirectory is.
        // if it is some version of Windows
        if (OS.contains("WIN")) {
            // it is simply the location of the "AppData" folder
            workingDirectory = System.getenv("AppData");
        }
        // Otherwise, we assume Linux or Mac
        else {
            // in either case, we would start in the user's home directory
            workingDirectory = System.getProperty("user.home");
            // if we are on a Mac, we are not done, we look for "Application
            // Support"
            if(OS.contains("MAC")) workingDirectory += "/Library/Application Support";
        }

        if(OS.contains("MAC")) workingDirectory += File.separator + "minecraft";
        else workingDirectory += File.separator + ".minecraft";

        return workingDirectory;
    }

    private MinecraftUtils() {
    }
}
