package com.energyxxer.inject.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * A {@link LogFileReader} is used to read new lines that have been added to a file since the last
 * call to {@link #readAddedLines(Charset, Consumer)} or alternatively the construction of the
 * {@link LogFileReader}.
 * <p>
 * To function properly the {@link #logFile} must use {@link System#lineSeparator()} as it's line
 * seperator and every line must be terminated by a line seperator. Otherwise the file content would
 * be shorter than expected which is considered to be a log file rotation.<br>
 * These two requirements are met by Minecraft's log file.
 * <p>
 * A {@link LogFileReader} can properly handle log file rotation (for instance when Minecraft is
 * restarted) and does not lock the log file.
 *
 * @author Adrodoc55
 */
public class LogFileReader {
  private final Path logFile;
  private long bytesRead;

  /**
   * Create a new {@link LogFileReader} and check for the existence of {@code logFile}.
   *
   * @param logFile
   * @throws IOException if an I/O error occurs during the determination of the size of the
   *         {@code logFile}
   */
  public LogFileReader(Path logFile) throws IOException {
    this.logFile = checkNotNull(logFile, "logFile == null!");
    bytesRead = Files.size(logFile);
  }

  /**
   * Read all new lines that have been added to a file since the last call to
   * {@link #readAddedLines(Charset, Consumer)} or alternatively the construction of the
   * {@link LogFileReader}.
   *
   * @param charset
   * @param lineConsumer the {@link Consumer} to be called for each line (without line seperator)
   */
  public void readAddedLines(Charset charset, Consumer<String> lineConsumer) {
    try (// Open file without locking it
        InputStream is = Files.newInputStream(logFile, StandardOpenOption.READ);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));) {
      if (bytesRead > Files.size(logFile)) {
        handleLogFileRotation();
      } else { // Skip previously read lines
        is.skip(bytesRead);
      }
      String line;
      while ((line = reader.readLine()) != null) {
        // Every line in the log file must be terminated by a line separator
        bytesRead += line.getBytes(charset).length + System.lineSeparator().length();
        lineConsumer.accept(line);
      }
    } catch (IOException ex) {
      handleLogFileRotation();
    }
  }

  private void handleLogFileRotation() {
    bytesRead = 0;
  }
}
