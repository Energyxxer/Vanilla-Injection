package de.adrodoc55.minecraft.vi.warp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.energyxxer.inject.v2.InjectionConnection;

public class CounterScribble {
  private static int i;

  public static void main(String[] args) throws IOException, InterruptedException {
    Path logFile = Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/logs/latest.log");
    Path worldDir = Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/saves/New World-");
    String identifier = "counter";
    InjectionConnection connection = new InjectionConnection(logFile, worldDir, identifier);
    connection.getLogObserver().addChatListener(e -> {
      if ("close".equals(e.getMessage())) {
        try {
          connection.close();
        } catch (IOException ex) {
          throw new InternalError(ex);
        }
      }
      if ("c".equals(e.getMessage())) {
        connection.injectCommand("say " + i++);
      }
    });
  }
}
