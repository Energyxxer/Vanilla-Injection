package com.energyxxer.inject_demo.jarbot;

import com.energyxxer.inject.InjectionMaster;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;

import java.io.File;

/**
 * Created by User on 4/11/2017.
 */
public class ChatDemo implements SetupListener {

    private static InjectionMaster master;

    private ChatDemo() {
        new DisplayWindow("Jarbot", Commons.WORLD_NAME, this);
    }

    public static void main(String[] args) {
        new ChatDemo();
    }

    @Override
    public void onSetup(String directory, String worldName) {
        master = new InjectionMaster(new File(directory + File.separator + "saves" + File.separator + worldName), new File(directory + File.separator + "logs" + File.separator + "latest.log"), "jarbot");
        master.setLogCheckFrequency(500);
        master.setInjectionFrequency(500);

        master.addChatListener(l -> {
            if(l.getMessage().charAt(0) != '.') answer(Jarbot.ask(l.getMessage()));
        });

        master.start();
    }

    private static void answer(String message) {
        answer(message, null);
    }

    private static void answer(String message, String username) {
        master.injector.insertImpulseCommand("tellraw " + ((username != null) ? username : "@a") + " [{\"text\":\"§8[§bJarbot§8] \"},{\"text\":\"" + message.replace("\\","\\\\").replace("\"","\\\"") + "\",\"color\":\"gray\"}]");
        master.injector.insertImpulseCommand("function lab:jarbot_graphics/voice/talk");
    }
}
