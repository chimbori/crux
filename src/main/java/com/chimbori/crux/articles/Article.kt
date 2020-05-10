package com.chimbori.crux.articles

import com.chimbori.crux.common.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Parsed result from a web page. */
data class Article(
    val originalUrl: String? = null,
    var title: String? = null,
    var description: String? = null,
    var siteName: String? = null,
    var themeColor: String? = null,
    var ampUrl: String? = null,
    var canonicalUrl: String? = null,
    var imageUrl: String? = null,
    var videoUrl: String? = null,
    var feedUrl: String? = null,
    var faviconUrl: String? = null,

    /** Estimated reading time, in minutes. This is not populated unless explicitly requested by the caller. */
    var estimatedReadingTimeMinutes: Int? = null,
    var document: Document? = null,
    var keywords: Collection<String>? = null,
    var images: List<Image>? = null) {


  /** Encapsulates the data from an image found under an element */
  data class Image(
      var src: String? = null,
      public var weight: Int = 0,
      var title: String? = null,
      var height: Int = 0,
      var width: Int = 0,
      var alt: String? = null,
      var noFollow: Boolean = false,
      var element: Element? = null) {
    companion object {
      fun from(imgElement: Element) = Image().apply {
        element = imgElement
        // Some sites use data-src to load images lazily, so prefer the data-src attribute if it exists.
        src = if (!imgElement.attr("data-src").isEmpty()) imgElement.attr("data-src") else imgElement.attr("src")
        width = StringUtils.parseAttrAsInt(imgElement, "width")
        height = StringUtils.parseAttrAsInt(imgElement, "height")
        alt = imgElement.attr("alt")
        title = imgElement.attr("title")
        noFollow = imgElement.parent() != null && imgElement.parent().attr("rel") != null && imgElement.parent().attr("rel").contains("nofollow")
      }
    }
  }
}

