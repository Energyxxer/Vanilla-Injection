package com.energyxxer.inject_demo.jarbot;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Adrodoc55
 */
public class ChatDemoLauncher {
  public static void main(String[] args) throws Throwable {
    JarClassLoader jcl = new JarClassLoader();
    jcl.invokeMain("com.energyxxer.inject_demo.jarbot.ChatDemo", args);
  }
}
