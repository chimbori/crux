package com.chimbori.crux_kmp.extractors

import com.chimbori.crux_kmp.common.cleanTitle
import com.chimbori.crux_kmp.common.nullIfBlank
import com.chimbori.crux_kmp.common.removeWhiteSpace
import com.chimbori.crux_kmp.common.toUrlOrNull
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.copy
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom

public fun Document.extractTitle(): String? = (
    title().nullIfBlank()
      ?: select("title").text().nullIfBlank()
      ?: select("meta[name=title]").attr("content").nullIfBlank()
      ?: select("meta[property=og:title]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:title]").attr("content").nullIfBlank()
    )?.cleanTitle()?.nullIfBlank()

public fun Document.extractCanonicalUrl(): String? = (
    select("link[rel=canonical]").attr("abs:href").nullIfBlank()
      ?: select("meta[property=og:url]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:url]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

public fun Document.extractPaginationUrl(baseUrl: Url?, nextOrPrev: String): Url? = (
    select("link[rel=$nextOrPrev]").attr("abs:href").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()
  ?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }

public fun Document.extractDescription(): String? = (
    select("meta[name=description]").attr("content").nullIfBlank()
      ?: select("meta[property=og:description]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:description]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

public fun Document.extractSiteName(): String? = (
    select("meta[property=og:site_name]").attr("content").nullIfBlank()
      ?: select("meta[name=application-name]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

public fun Document.extractThemeColor(): String? =
  select("meta[name=theme-color]").attr("content").nullIfBlank()

public fun Document.extractPublishedAt(): String? = (
    select("meta[itemprop=dateCreated]").attr("content").nullIfBlank()
      ?: select("meta[property=article:published_time]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

public fun Document.extractModifiedAt(): String? = (
    select("meta[itemprop=dateModified]").attr("content").nullIfBlank()
      ?: select("meta[property=article:modified_time]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

public fun Document.extractKeywords(): List<String> =
  select("meta[name=keywords]").attr("content")
    .removeWhiteSpace()
    .removePrefix("[")
    .removeSuffix("]")
    .split("\\s*,\\s*".toRegex())
    .filter { it.isNotBlank() }

public fun Document.extractFaviconUrl(baseUrl: Url?): Url? {
  val allPossibleIconElements = listOf(
    select("link[rel~=apple-touch-icon]"),
    select("link[rel~=apple-touch-icon-precomposed]"),
    select("link[rel~=icon]"),
    select("link[rel~=ICON]"),
  )
  return findLargestIcon(allPossibleIconElements.flatten())
    ?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }
    ?: baseUrl?.let {
        URLBuilder(it).apply {
            encodedPath = "/favicon.ico"
        }.build()
    }
}

public fun Document.extractImageUrl(baseUrl: Url?): Url? = (
    // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
    select("meta[name=twitter:image]").attr("content").nullIfBlank()
      ?: select("meta[property=og:image]").attr("content").nullIfBlank()
      // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
      ?: select("link[rel=image_src]").attr("href").nullIfBlank()
      ?: select("meta[name=thumbnail]").attr("content").nullIfBlank()
    )?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }

public fun Document.extractFeedUrl(baseUrl: Url?): Url? = (
    select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href").nullIfBlank()
      ?: select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href").nullIfBlank()
    )?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }

public fun Document.extractAmpUrl(baseUrl: Url?): Url? =
  select("link[rel=amphtml]").attr("href").nullIfBlank()
    ?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }

public fun Document.extractVideoUrl(baseUrl: Url?): Url? =
  select("meta[property=og:video]").attr("content").nullIfBlank()
    ?.let { relativeUrl -> baseUrl?.let { URLBuilder(it).takeFrom(relativeUrl).build() } ?: relativeUrl.toUrlOrNull() }

internal fun findLargestIcon(iconElements: List<Element>): String? =
  iconElements.maxByOrNull { parseSize(it.attr("sizes")) }?.attr("abs:href")?.nullIfBlank()

/**
 * Given a size represented by "WidthxHeight" or "WidthxHeight ...", will return the largest dimension found.
 *
 * Examples: "128x128" will return 128.
 * "128x64" will return 64.
 * "24x24 48x48" will return 48.
 *
 * @param sizes String representing the sizes.
 * @return largest dimension, or 0 if input could not be parsed.
 */
internal fun parseSize(sizeString: String?): Int {
  if (sizeString.isNullOrBlank()) return 0

  val sizes = sizeString.trim(' ').lowercase()
  return when {
    // For multiple sizes in the same String, split and parse recursively.
    sizes.contains(" ") -> sizes.split(" ").maxOfOrNull { parseSize(it) } ?: 0
    // For handling sizes of format 128x128 etc.
    sizes.contains("x") -> try {
      sizes.split("x").maxOf { it.trim().toInt() }
    } catch (e: NumberFormatException) {
      0
    }
    else -> 0
  }
}
