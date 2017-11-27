package com.chimbori.crux.common;

import org.jsoup.nodes.Node;

@SuppressWarnings("WeakerAccess")
public class Log {
  private static final boolean DEBUG = false;

  private static final boolean TRUNCATE = true;

  public static void i(String message, Object... args) {
    if (DEBUG) {
      System.err.println(String.format(message, args));
    }
  }

  public static void i(String reason, Node node) {
    if (DEBUG) {
      String nodeToString = TRUNCATE
          ? node.outerHtml().substring(0, Math.min(node.outerHtml().length(), 80)).replace("\n", "")
          : "\n------\n" + node.outerHtml() + "\n------\n";
      i("%s [%s]", reason, nodeToString);
    }
  }

  public static void printAndRemove(Node node, String reason) {
    i(reason, node);
    node.remove();
  }
}
