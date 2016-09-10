package com.chimbori.snacktroid;

public class Log {
  private static final boolean DEBUG = true;

  public static void i(String message) {
    if (DEBUG) {
      System.err.println(message);
    }
  }
}
