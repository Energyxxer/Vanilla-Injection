package com.energyxxer.inject_demo.worldedit;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Adrodoc55
 */
public class WorldEditLauncher {
  public static void main(String[] args) throws Throwable {
    JarClassLoader jcl = new JarClassLoader();
    jcl.invokeMain("com.energyxxer.inject_demo.worldedit.WorldEdit", args);
  }
}
