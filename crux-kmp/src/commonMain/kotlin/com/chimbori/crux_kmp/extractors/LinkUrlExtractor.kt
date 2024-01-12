package com.chimbori.crux_kmp.extractors

import com.chimbori.crux_kmp.common.anyChildTagWithAttr
import com.chimbori.crux_kmp.common.nullIfBlank
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.takeFrom

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible link URL
 * available within it. The use case for this application is to pick a single representative link from a DOM sub-tree,
 * in a way that works without explicit CSS selector foo. Check out the test cases for markup that is supported.
 */
@Suppress("unused")
public class LinkUrlExtractor(private val url: Url, private val root: Element) {
  public var linkUrl: Url? = null
    private set

  public fun findLink(): LinkUrlExtractor {
    (
        root.attr("abs:href").nullIfBlank()
          ?: root.select("*").anyChildTagWithAttr("href")
        )?.let {
        linkUrl = URLBuilder(url).takeFrom(it).build()
      }
    return this
  }
}
