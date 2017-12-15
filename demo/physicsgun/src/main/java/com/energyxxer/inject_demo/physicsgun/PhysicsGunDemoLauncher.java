package com.energyxxer.inject_demo.physicsgun;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Adrodoc55
 */
public class PhysicsGunDemoLauncher {
  public static void main(String[] args) throws Throwable {
    JarClassLoader jcl = new JarClassLoader();
    jcl.invokeMain("com.energyxxer.inject_demo.physicsgun.PhysicsGunDemo", args);
  }
}
