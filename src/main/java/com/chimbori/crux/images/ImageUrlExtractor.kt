@file:Suppress("DEPRECATION")

package com.chimbori.crux.images

import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.StringUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.regex.Pattern

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible image URL
 * candidate available within it. The use case for this application is to pick a single representative image from a DOM
 * sub-tree, in a way that works without explicit CSS selector foo. Check out the test cases for markup that is
 * supported.
 */
class ImageUrlExtractor(private val url: String, private val root: Element) {
  var imageUrl: String? = null
    private set

  fun findImage(): ImageUrlExtractor {
    try {
      HeuristicString()
          .or(root.attr("src"))
          .or(root.attr("data-src"))
          .or(StringUtils.anyChildTagWithAttr(root.select("img"), "src"))
          .or(StringUtils.anyChildTagWithAttr(root.select("img"), "data-src"))
          .or(StringUtils.anyChildTagWithAttr(root.select("*"), "src"))
          .or(StringUtils.anyChildTagWithAttr(root.select("*"), "data-src"))
          .or(parseImageUrlFromStyleAttr(root.select("[role=img]")))
          .or(parseImageUrlFromStyleAttr(root.select("*")))
    } catch (candidateFound: CandidateFound) {
      imageUrl = candidateFound.candidate
    }
    imageUrl = StringUtils.makeAbsoluteUrl(url, imageUrl)
    return this
  }

  private fun parseImageUrlFromStyleAttr(elements: Elements): String? {
    elements.forEach { element ->
      var styleAttr = element.attr("style")
      if (styleAttr.isNullOrEmpty()) {
        return@forEach
      }
      @Suppress("DEPRECATION")
      styleAttr = StringEscapeUtils.unescapeHtml4(styleAttr)
      val cssUrlMatcher = CSS_URL.matcher(styleAttr)
      if (cssUrlMatcher.find()) {
        return cssUrlMatcher.group(1)
      }
    }
    return null
  }

  companion object {
    private val CSS_URL = Pattern.compile("url\\([\\\"']{0,1}(.+?)[\\\"']{0,1}\\)")
  }
}
