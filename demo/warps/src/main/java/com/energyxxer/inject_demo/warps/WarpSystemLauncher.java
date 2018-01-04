package com.energyxxer.inject_demo.warps;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Adrodoc55
 */
public class WarpSystemLauncher {
  public static void main(String[] args) throws Throwable {
    JarClassLoader jcl = new JarClassLoader();
    jcl.invokeMain("com.energyxxer.inject_demo.warps.WarpSystem", args);
  }
}
