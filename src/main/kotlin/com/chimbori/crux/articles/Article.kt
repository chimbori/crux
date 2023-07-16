package com.chimbori.crux.articles

import com.chimbori.crux.common.parseAttrAsInt
import okhttp3.HttpUrl
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Parsed result from a web page. */
@Deprecated("Use [com.chimbori.crux.api.Resource] instead. See README.md for examples.")
public data class Article(
  var canonicalUrl: HttpUrl,
  var title: String? = null,
  var description: String? = null,
  var siteName: String? = null,
  var themeColor: String? = null,
  var ampUrl: HttpUrl? = null,
  var imageUrl: HttpUrl? = null,
  var videoUrl: HttpUrl? = null,
  var feedUrl: HttpUrl? = null,
  var faviconUrl: HttpUrl? = null,

  /** Estimated reading time, in minutes. This is not populated unless explicitly requested by the caller. */
  var estimatedReadingTimeMinutes: Int? = null,
  var document: Document? = null,
  var keywords: List<String>? = null,
  var images: List<Image>? = null
) {

  /** Encapsulates the data from an image found under an element */
  public data class Image(
    var srcUrl: HttpUrl? = null,
    var weight: Int = 0,
    var title: String? = null,
    var height: Int = 0,
    var width: Int = 0,
    var alt: String? = null,
    var noFollow: Boolean = false,
    var element: Element? = null
  ) {
    public companion object {
      public fun from(baseUrl: HttpUrl, imgElement: Element): Image = Image().apply {
        element = imgElement
        // Some sites use data-src to load images lazily, so prefer the data-src attribute if it exists.
        srcUrl = if (imgElement.attr("data-src").isNotEmpty()) {
          baseUrl.resolve(imgElement.attr("data-src"))
        } else {
          baseUrl.resolve(imgElement.attr("src"))
        }
        width = imgElement.parseAttrAsInt("width")
        height = imgElement.parseAttrAsInt("height")
        alt = imgElement.attr("alt")
        title = imgElement.attr("title")
        noFollow = imgElement.parent()?.attr("rel")?.contains("nofollow") == true
      }
    }
  }
}
