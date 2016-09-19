package com.chimbori.crux;

class Log {
  private static final boolean DEBUG = false;

  public static void i(String message, Object ... args) {
    if (DEBUG) {
      System.err.println(String.format(message, args));
    }
  }
}
