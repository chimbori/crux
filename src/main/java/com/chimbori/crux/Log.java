package com.chimbori.crux;

public class Log {
  private static final boolean DEBUG = true;

  public static void i(String message, Object ... args) {
    if (DEBUG) {
      System.err.println(String.format(message, args));
    }
  }
}
