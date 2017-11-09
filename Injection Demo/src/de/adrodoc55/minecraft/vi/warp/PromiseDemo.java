package de.adrodoc55.minecraft.vi.warp;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.energyxxer.inject.utils.Vector3D;
import com.energyxxer.inject.v2.InjectionConnection;
import com.google.common.base.Splitter;

public class PromiseDemo {
  private static final Logger LOGGER = LogManager.getLogger();

  public static void main(String[] args) throws IOException, InterruptedException {
    Path logFile = Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/logs/latest.log");
    Path worldDir = Paths.get("C:/Users/Adrian/AppData/Roaming/.minecraft/saves/New World-");
    String prefix = "promise";

    InjectionConnection connection = new InjectionConnection(logFile, worldDir, prefix);
    connection.getLogObserver().addChatListener(e -> {
      LOGGER.info("Processing Chat Message: {}", e);
      String message = e.getMessage();
      if (message.startsWith(".pos")) {
        getPlayerPosition(connection, e.getSender()).thenAccept(pos -> {
          LOGGER.info("Processing Player Position");
          connection.injectImpulseCommand("say x = " + pos.x);
          connection.injectImpulseCommand("say y = " + pos.y);
          connection.injectImpulseCommand("say z = " + pos.z);
        });
      }
      switch (message) {
        case ".pig":
          getEntityData(connection, "@e[type=pig,c=1]").thenAccept(data -> {
            connection.injectCommand("say data = " + data);
          });
        case ".pigpos":
          getEntityPosition(connection, "@e[type=pig,c=1]").thenAccept(pos -> {
            connection.injectCommand("say x = " + pos.x);
            connection.injectCommand("say y = " + pos.y);
            connection.injectCommand("say z = " + pos.z);
          });
      }
    });
  }

  private static CompletionStage<Vector3D.Double> getPlayerPosition(InjectionConnection connection,
      String selector) {
    UUID id = UUID.randomUUID();
    connection.injectImpulseCommand("summon area_effect_cloud ~ ~ ~ {CustomName:" + id + ",Duration:1}");
    String aec = "@e[name=" + id + "]";
    connection.injectImpulseCommand("tp " + aec + " " + selector);
    return getEntityPosition(connection, aec);
  }

  private static CompletionStage<Vector3D.Double> getEntityPosition(InjectionConnection connection,
      String selector) {
    return getEntityData(connection, selector).thenApply(PromiseDemo::extractPos);
  }

  private static Vector3D.Double extractPos(String entityData) {
    String posHeader = "Pos:[";
    int beginIndex = entityData.indexOf(posHeader) + posHeader.length();
    int endIndex = entityData.indexOf(']', beginIndex);
    String pos = entityData.substring(beginIndex, endIndex);
    List<String> xyz = Splitter.on(',').splitToList(pos);
    double x = Double.parseDouble(xyz.get(0));
    double y = Double.parseDouble(xyz.get(1));
    double z = Double.parseDouble(xyz.get(2));
    return new Vector3D.Double(x, y, z);
  }

  private static CompletionStage<String> getEntityData(InjectionConnection connection,
      String selector) {
    CompletableFuture<String> result = new CompletableFuture<>();
    connection.injectImpulseCommand("entitydata " + selector + " {fetch:nbt}", e -> {
      String output = e.getMessage();
      String prefix = "Entity data updated to: ";
      checkState(output.startsWith(prefix), "Unexpected entitydata prefix: " + output);
      String entityData = output.substring(prefix.length());
      // Remove "fetch:nbt,"
      int a = entityData.indexOf("fetch");
      int b = entityData.indexOf(',', a) + 1;
      result.complete(entityData.substring(0, a).concat(entityData.substring(b)));
    });
    return result;
  }
}
