package com.energyxxer.inject_demo.jarbot;

import static com.energyxxer.inject.InjectionBuffer.InjectionType.IMPULSE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import com.energyxxer.inject.InjectionConnection;
import com.energyxxer.inject_demo.common.Commons;
import com.energyxxer.inject_demo.common.DisplayWindow;
import com.energyxxer.inject_demo.common.SetupListener;

/**
 * Created by User on 4/11/2017.
 */
public class ChatDemo implements SetupListener {

    private static InjectionConnection connection;

    private ChatDemo() {
        new DisplayWindow("Jarbot", Commons.WORLD_NAME, this);
    }

    public static void main(String[] args) {
        new ChatDemo();
    }

    @Override
    public void onSetup(File log, File world) {
        try {
          connection = new InjectionConnection(log.toPath(), world.toPath(), "jarbot");
        } catch (IOException | InterruptedException ex) {
          throw new UndeclaredThrowableException(ex);
        }
        connection.getLogObserver().setLogCheckFrequency(500, MILLISECONDS);
        connection.setFlushFrequency(500, MILLISECONDS);

        connection.getLogObserver().addChatListener(l -> {
            if(l.getMessage().charAt(0) != '.') answer(Jarbot.ask(l.getMessage()));
        });
    }

    private static void answer(String message) {
        answer(message, null);
    }

    private static void answer(String message, String username) {
        connection.inject(IMPULSE, "tellraw " + ((username != null) ? username : "@a") + " [{\"text\":\"§8[§bJarbot§8] \"},{\"text\":\"" + message.replace("\\","\\\\").replace("\"","\\\"") + "\",\"color\":\"gray\"}]");
        connection.inject(IMPULSE, "function lab:jarbot_graphics/voice/talk");
    }
}
