package com.chimbori.crux.articles

import com.chimbori.crux.articles.ExtractionHelpers.GRAVITY_SCORE_SELECTOR
import com.chimbori.crux.common.Log
import com.chimbori.crux.common.Log.printAndRemove
import com.chimbori.crux.common.countLetters
import java.util.ArrayDeque
import java.util.Collections
import java.util.IdentityHashMap
import java.util.Locale
import java.util.Queue
import java.util.regex.Pattern
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/** Cleans up the best-match Element after one has been picked, in order to provide a sanitized output tree to the caller. */
internal class PostprocessHelpers private constructor(private val keepers: Set<Node>) {
  private fun replaceLineBreaksWithSpaces(topNode: Element) {
    topNode.select("br + br").forEach { brNextToBrElement ->
      brNextToBrElement.remove()
    }
    topNode.select("br").forEach { brElement ->
      if (brElement.previousSibling() != null) {
        brElement?.previousSibling()?.after(" • ")
      } else {
        brElement.parent()?.append(" • ")
      }
      brElement.unwrap()
    }
  }

  private fun removeTopLevelTagsNotLikelyToBeParagraphs(element: Element) {
    element.children().forEach { childElement ->
      if (!RETAIN_TAGS_TOP_LEVEL.contains(childElement.tagName())) {
        if (!shouldKeep(childElement)) {
          printAndRemove("removeTopLevelTagsNotLikelyToBeParagraphs", childElement)
        }
      }
    }
  }

  private fun removeTagsNotLikelyToBeParagraphs(element: Element) {
    element.children().forEach { childElement ->
      if (!RETAIN_TAGS.contains(childElement.tagName())) {
        if (!shouldKeep(childElement)) {
          printAndRemove("removeTagsNotLikelyToBeParagraphs", childElement)
        }
      } else if (childElement.children().size > 0) {
        removeTagsNotLikelyToBeParagraphs(childElement)
      }
    }
  }

  private fun removeTagsButRetainContent(element: Element) {
    element.children().forEach { childElement ->
      removeTagsButRetainContent(childElement)
      if (REMOVE_TAGS_BUT_RETAIN_CONTENT.contains(childElement.tagName())) {
        Log.i("removeTagsButRetainContent: [%s] %s", childElement.tagName(), childElement.outerHtml())
        childElement.tagName("p") // Set the wrapper tag to <p> instead of unwrapping them.
      }
    }
  }

  private fun removeShortParagraphs(topNode: Element) {
    for (i in topNode.childNodeSize() - 1 downTo 0) {
      val childNode = topNode.childNode(i)
      var text: String? = null
      var isExemptFromMinTextLengthCheck = false
      if (childNode is TextNode) {
        text = childNode.text().trim { it <= ' ' }
      } else if (childNode is Element) {
        text = childNode.text().trim { it <= ' ' }
        isExemptFromMinTextLengthCheck = TAGS_EXEMPT_FROM_MIN_LENGTH_CHECK.contains(childNode.tagName())
      }
      Log.i(
        "removeShortParagraphs: [%s] isExemptFromMinTextLengthCheck : %b",
        childNode, isExemptFromMinTextLengthCheck
      )
      if (text == null
        || text.isEmpty()
        || !isExemptFromMinTextLengthCheck && text.length < MIN_LENGTH_FOR_PARAGRAPHS
        || text.length > text.countLetters() * 2
      ) {
        if (!shouldKeep(childNode)) printAndRemove("removeShortParagraphs:", childNode)
      }
    }
  }

  private fun removeUnlikelyChildNodes(element: Element) {
    element.children().forEach { childElement ->
      if (isUnlikely(childElement)) {
        if (!shouldKeep(childElement)) {
          printAndRemove("removeUnlikelyChildNodes", childElement)
        }
      } else if (childElement.children().size > 0) {
        removeUnlikelyChildNodes(childElement)
      }
    }
  }

  private fun removeNodesWithNegativeScores(topNode: Element) {
    topNode.select(GRAVITY_SCORE_SELECTOR).forEach { element ->
      val score = element.attr(ExtractionHelpers.GRAVITY_SCORE_ATTRIBUTE).toInt()
      if (score < 0 || element.text().length < MIN_LENGTH_FOR_PARAGRAPHS) {
        if (!shouldKeep(element)) {
          printAndRemove("removeNodesWithNegativeScores", element)
        }
      }
    }
  }

  private fun isUnlikely(element: Element): Boolean {
    val styleAttribute = element.attr("style")
    val classAttribute = element.attr("class")
    return ((classAttribute.lowercase(Locale.getDefault()).contains("caption")
        || UNLIKELY_CSS_STYLES.matcher(styleAttribute).find())
        || UNLIKELY_CSS_STYLES.matcher(classAttribute).find())
  }

  private fun removeDisallowedAttributes(node: Element) {
    node.children().forEach { childElement ->
      removeDisallowedAttributes(childElement)
    }
    val keysToRemove = mutableListOf<String>()
    node.attributes().forEach { (key) ->
      if (!ATTRIBUTES_TO_RETAIN_IN_HTML.contains(key)) {
        keysToRemove.add(key)
      }
    }
    keysToRemove.forEach { key ->
      node.removeAttr(key)
    }
  }

  private fun shouldKeep(node: Node) = keepers.contains(node)

  companion object {
    /** If a string is shorter than this limit, it is not considered a paragraph. */
    private const val MIN_LENGTH_FOR_PARAGRAPHS = 50

    private val UNLIKELY_CSS_STYLES = Pattern.compile("display\\:none|visibility\\:hidden")

    /** Tags that should not be output, but still may contain interesting content. */
    private val REMOVE_TAGS_BUT_RETAIN_CONTENT =
      setOf("font", "table", "tbody", "tr", "td", "div", "ol", "ul", "li", "span")

    /**
     * Tags that should be retained in the output. This list should be fairly minimal, and equivalent
     * to the list of tags that callers can be expected to be able to handle.
     */
    private val RETAIN_TAGS =
      setOf("p", "b", "i", "u", "strong", "em", "a", "pre", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote")

    /**
     * Tags that can contain really short content, because they are not paragraph-level tags. Content
     * within these tags is not subject to the `MIN_LENGTH_FOR_PARAGRAPHS` requirement.
     */
    private val TAGS_EXEMPT_FROM_MIN_LENGTH_CHECK =
      setOf("b", "i", "u", "strong", "em", "a", "pre", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote")

    /** The whitelist of attributes that should be retained in the output. No other attributes will be retained. */
    private val ATTRIBUTES_TO_RETAIN_IN_HTML = setOf("href")

    /**
     * After a final set of top-level nodes has been extracted, all tags except these are removed.
     * This ensures that while inline tags containing shorter text, e.g. [one word](…)
     * are kept as part of a larger paragraph, those same short tags are not allowed to be
     * top-level children.
     */
    private val RETAIN_TAGS_TOP_LEVEL = setOf("p", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "li")

    fun postprocess(topNode: Element?): Document {
      Log.i("postprocess")
      val doc = Document("")
      if (topNode == null) {
        return doc
      }
      val keepers = Collections.newSetFromMap(IdentityHashMap<Node, Boolean>())
      for (element in topNode.select("[crux-keep]")) {
        keepers.addAll(getAncestorsSelfAndDescendants(topNode, element))
      }
      val helper = PostprocessHelpers(keepers)
      helper.removeNodesWithNegativeScores(topNode)
      helper.replaceLineBreaksWithSpaces(topNode)
      helper.removeUnlikelyChildNodes(topNode)
      helper.removeTagsButRetainContent(topNode)
      helper.removeTagsNotLikelyToBeParagraphs(topNode)
      helper.removeTopLevelTagsNotLikelyToBeParagraphs(topNode)
      helper.removeShortParagraphs(topNode)
      helper.removeDisallowedAttributes(topNode)
      for (node in topNode.childNodes()) {
        doc.appendChild(node.clone()) // TODO: Don’t copy each item separately.
      }
      return doc
    }

    private fun getAncestorsSelfAndDescendants(root: Element, e: Element): Collection<Node> {
      val result: MutableList<Node> = ArrayList()

      // Add all ancestors, up to the top node
      var n: Node? = e
      while (n !== root && n != null) {
        result.add(n)
        n = n.parentNode()
      }

      // Add all descendants
      val nodes: Queue<Node> = ArrayDeque(e.childNodes())
      while (!nodes.isEmpty()) {
        val node = nodes.poll()
        result.add(node)
        for (childNode in node.childNodes()) {
          nodes.offer(childNode)
        }
      }
      return result
    }
  }
}
