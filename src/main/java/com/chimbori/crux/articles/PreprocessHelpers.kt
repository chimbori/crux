package com.chimbori.crux.articles

import com.chimbori.crux.articles.ExtractionHelpers.NEGATIVE_CSS_CLASSES_AND_IDS
import com.chimbori.crux.common.Log
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node

/** Performs basic sanitization before starting the extraction process.  */
internal object PreprocessHelpers {
  fun preprocess(doc: Document) {
    Log.i("preprocess")
    stripUnlikelyCandidates(doc)
    removeScriptsStyles(doc)
    removeComments(doc.body())
  }

  /**
   * Removes unlikely candidates from HTML. It often ends up removing more than just the unlikely
   * candidates, so exercise caution when enabling this.
   */
  private fun stripUnlikelyCandidates(doc: Document) {
    if (true) {
      return  // Temporarily disabled; see comment above.
    }
    doc.select("body").select("*").forEach { child ->
      val className = child.className().toLowerCase()
      val id = child.id().toLowerCase()
      if (NEGATIVE_CSS_CLASSES_AND_IDS.matcher(className).find() ||
          NEGATIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
        Log.printAndRemove(child, "stripUnlikelyCandidates")
      }
    }
  }

  private fun removeScriptsStyles(doc: Document) {
    doc.getElementsByTag("script").forEach { item ->
      Log.printAndRemove(item, "removeScriptsStyles('script')")
    }
    doc.getElementsByTag("noscript").forEach { item ->
      Log.printAndRemove(item, "removeScriptsStyles('noscript')")
    }
    doc.getElementsByTag("style").forEach { item ->
      Log.printAndRemove(item, "removeScriptsStyles('style')")
    }
  }

  private fun removeComments(node: Node) {
    var i = 0
    while (i < node.childNodes().size) {
      val child = node.childNode(i)
      if (child.nodeName() == "#comment") Log.printAndRemove(child, "removeComments") else {
        removeComments(child)
        i++
      }
    }
  }
}
