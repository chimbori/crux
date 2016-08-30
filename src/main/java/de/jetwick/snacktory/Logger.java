package de.jetwick.snacktory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple shim for a Logger class to minimize changing the code while removing the dependency
 * on SLF4J and Log4J. Prints all output to the console.
 */
class Logger {
  private static Logger instance;

  static Logger getInstance() {
    if (instance == null) {
      instance = new Logger();
    }
    return instance;
  }

  void info(String message) {
    log(message, null);
  }

  void debug(String message) {
    log(message, null);
  }

  void warn(String message, Throwable t) {
    log(message, t);
  }

  void warn(String message) {
    log(message, null);
  }

  boolean isDebugEnabled() {
    return false;
  }

  private void log(String message, Throwable t) {
    System.err.println(String.format("[%s] %s %s",
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date()),
        message,
        t != null ? t.toString() : ""));
  }
}
