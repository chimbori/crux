package com.chimbori.crux_kmp.extractors

import com.chimbori.crux_kmp.common.anyChildTagWithAttr
import com.chimbori.crux_kmp.common.nullIfBlank
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parser.Parser.Companion.unescapeEntities
import com.fleeksoft.ksoup.select.Elements
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.takeFrom

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible image URL
 * candidate available within it. The use case for this application is to pick a single representative image from a DOM
 * sub-tree, in a way that works without explicit CSS selector foo. Check out the test cases for markup that is
 * supported.
 */
@Suppress("unused")
public class ImageUrlExtractor(private val url: Url, private val root: Element) {
  public var imageUrl: Url? = null
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
        )?.let { imageUrl = URLBuilder(url).takeFrom(it).build() }
    return this
  }

  private fun parseImageUrlFromStyleAttr(elements: Elements): String? {
    elements.forEach { element ->
      var styleAttr = element.attr("style")
      if (styleAttr.isEmpty()) {
        return@forEach
      }
      styleAttr = unescapeEntities(styleAttr, true)
      return CSS_URL.find(styleAttr)?.groupValues?.get(1)
    }
    return null
  }

  public companion object {
    private val CSS_URL = Regex("url\\([\\\"']{0,1}(.+?)[\\\"']{0,1}\\)")
  }
}
