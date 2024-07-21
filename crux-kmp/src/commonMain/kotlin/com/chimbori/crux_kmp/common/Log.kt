package com.chimbori.crux_kmp.common

import com.fleeksoft.ksoup.nodes.Node


internal object Log {
  private const val DEBUG = false

  private const val TRUNCATE = true

  fun i(message: String, vararg args: Any?) {
    if (DEBUG) {
      System.err.println(String.format(message, *args))
    }
  }

  fun i(reason: String, node: Node) {
    if (DEBUG) {
      val nodeToString = if (TRUNCATE) {
        node.outerHtml().take(80).replace("\n", "")
      } else {
        "\n------\n${node.outerHtml()}\n------\n"
      }
      i("%s [%s]", reason, nodeToString)
    }
  }

  fun printAndRemove(reason: String, node: Node) {
    i(reason, node)
    node.remove()
  }
}
