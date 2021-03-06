package com.energyxxer.inject.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.CountingInputStream;

/**
 * A {@link LogFileReader} is used to read new lines that have been added to a file since the last
 * call to {@link #readAddedLines(Charset, Consumer)} or alternatively the construction of the
 * {@link LogFileReader}.
 * <p>
 * To function properly every line in the {@link #logFile} must be terminated by a line seperator.
 * Otherwise a line might be cut in half if content is added to the last line between calls to
 * {@link #readAddedLines(Charset, Consumer)}.<br>
 * This requirement is met by Minecraft's {@link #logFile}.
 * <p>
 * A {@link LogFileReader} does not lock the {@link #logFile} and can properly handle log file
 * rotation (for instance when Minecraft is restarted).
 *
 * @author Adrodoc55
 */
public class LogFileReader {
  private static final Logger LOGGER = LogManager.getLogger();

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
        CountingInputStream is =
            new CountingInputStream(Files.newInputStream(logFile, StandardOpenOption.READ));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {

      // Skip previously read bytes if there was no log file rotation
      if (bytesRead <= Files.size(logFile)) {
        is.skip(bytesRead);
      } else {
        LOGGER.info("Detected log file rotation due to change in size of log file: {}", logFile);
      }
      try {
        String line;
        while ((line = reader.readLine()) != null) {
          lineConsumer.accept(line);
        }
      } catch (Exception ex) {
        LOGGER.warn(
            "Exception during log line processing, some bytes might be skipped due to buffering!",
            ex);
        throw ex;
      } finally {
        // Even though we use a BufferedReader the count is correct when the stream is exhausted
        bytesRead = is.getCount();
      }
    } catch (IOException ex) {
      LOGGER.info("Interpreting exception as a log file rotation", ex);
      // Log file rotation
      bytesRead = 0;
    }
  }
}
