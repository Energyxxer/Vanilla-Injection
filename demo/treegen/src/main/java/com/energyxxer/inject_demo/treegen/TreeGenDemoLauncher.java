package com.energyxxer.inject_demo.treegen;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Adrodoc55
 */
public class TreeGenDemoLauncher {
  public static void main(String[] args) throws Throwable {
    JarClassLoader jcl = new JarClassLoader();
    jcl.invokeMain("com.energyxxer.inject_demo.treegen.TreeGenDemo", args);
  }
}
