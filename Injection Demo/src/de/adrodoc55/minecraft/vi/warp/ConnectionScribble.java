package de.adrodoc55.minecraft.vi.warp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.energyxxer.inject.v2.InjectionConnection;

public class ConnectionScribble {
  private static final Logger LOGGER = LogManager.getLogger();

  public static void main(String[] args) throws IOException, InterruptedException {
    Path logFile = Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/logs/latest.log");
    Path worldDirectory =
        Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/saves/New World-");
    InjectionConnection connection = new InjectionConnection(logFile, worldDirectory, "scribble");
    connection.getLogObserver().addLogListener(e -> {
      System.out.println(e);
      if (e.getLine().endsWith(".close")) {
        try {
          connection.close();
        } catch (IOException ex) {
          LOGGER.error("Closing encountered an error", ex);
        }
      }
    });
  }
}
