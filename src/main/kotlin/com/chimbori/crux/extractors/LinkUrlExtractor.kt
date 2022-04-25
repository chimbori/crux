package com.chimbori.crux.extractors

import com.chimbori.crux.common.anyChildTagWithAttr
import com.chimbori.crux.common.nullIfBlank
import okhttp3.HttpUrl
import org.jsoup.nodes.Element

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible link URL
 * available within it. The use case for this application is to pick a single representative link from a DOM sub-tree,
 * in a way that works without explicit CSS selector foo. Check out the test cases for markup that is supported.
 */
@Suppress("unused")
public class LinkUrlExtractor(private val url: HttpUrl, private val root: Element) {
  public var linkUrl: HttpUrl? = null
    private set

  public fun findLink(): LinkUrlExtractor {
    (
        root.attr("href").nullIfBlank()
          ?: root.select("*").anyChildTagWithAttr("href")
        )?.let {
        linkUrl = url.resolve(it)
      }
    return this
  }
}
