package com.chimbori.crux.extractors

import com.chimbori.crux.common.anyChildTagWithAttr
import com.chimbori.crux.common.nullIfBlank
import java.util.regex.Pattern
import okhttp3.HttpUrl
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser.unescapeEntities
import org.jsoup.select.Elements

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible image URL
 * candidate available within it. The use case for this application is to pick a single representative image from a DOM
 * sub-tree, in a way that works without explicit CSS selector foo. Check out the test cases for markup that is
 * supported.
 */
@Suppress("unused")
public class ImageUrlExtractor(private val url: HttpUrl, private val root: Element) {
  public var imageUrl: HttpUrl? = null
    private set

  public fun findImage(): ImageUrlExtractor {
    (
        root.attr("src").nullIfBlank()
          ?: root.attr("data-src").nullIfBlank()
          ?: root.select("img").anyChildTagWithAttr("src")
          ?: root.select("img").anyChildTagWithAttr("data-src")
          ?: root.select("*").anyChildTagWithAttr("src")
          ?: root.select("*").anyChildTagWithAttr("data-src")
          ?: parseImageUrlFromStyleAttr(root.select("[role=img]"))
          ?: parseImageUrlFromStyleAttr(root.select("*"))
        )?.let { imageUrl = url.resolve(it) }
    return this
  }

  private fun parseImageUrlFromStyleAttr(elements: Elements): String? {
    elements.forEach { element ->
      var styleAttr = element.attr("style")
      if (styleAttr.isNullOrEmpty()) {
        return@forEach
      }
      styleAttr = unescapeEntities(styleAttr, true)
      val cssUrlMatcher = CSS_URL.matcher(styleAttr)
      if (cssUrlMatcher.find()) {
        return cssUrlMatcher.group(1)
      }
    }
    return null
  }

  public companion object {
    private val CSS_URL = Pattern.compile("url\\([\\\"']{0,1}(.+?)[\\\"']{0,1}\\)")
  }
}
