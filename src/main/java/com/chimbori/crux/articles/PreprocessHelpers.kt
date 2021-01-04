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
        Log.printAndRemove("stripUnlikelyCandidates", child)
      }
    }
  }

  private fun removeScriptsStyles(doc: Document) {
    doc.getElementsByTag("script").forEach { item ->
      Log.printAndRemove("removeScriptsStyles('script')", item)
    }
    doc.getElementsByTag("noscript").forEach { item ->
      Log.printAndRemove("removeScriptsStyles('noscript')", item)
    }
    doc.getElementsByTag("style").forEach { item ->
      Log.printAndRemove("removeScriptsStyles('style')", item)
    }
  }

  private fun removeComments(node: Node) {
    var i = 0
    while (i < node.childNodes().size) {
      val child = node.childNode(i)
      if (child.nodeName() == "#comment") Log.printAndRemove("removeComments", child) else {
        removeComments(child)
        i++
      }
    }
  }
}
