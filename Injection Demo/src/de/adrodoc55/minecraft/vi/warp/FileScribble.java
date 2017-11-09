package de.adrodoc55.minecraft.vi.warp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

import com.energyxxer.inject.utils.LogFileReader;
import com.google.common.base.Charsets;

public class FileScribble {
  public static void main(String[] args) throws InterruptedException, IOException {
     File file = new File("C:/Users/Adrian/AppData/Roaming/.minecraft/logs/latest.log");
//    File file = new File("C:/Users/Adrian/AppData/Roaming/.minecraft/logs/test1.txt");
    Path path = file.toPath();
    LogFileReader reader = new LogFileReader(path);
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        reader.readAddedLines(Charsets.UTF_8, System.out::println);
      }
    };
    new Timer().scheduleAtFixedRate(task, 0, 50);
  }
}
