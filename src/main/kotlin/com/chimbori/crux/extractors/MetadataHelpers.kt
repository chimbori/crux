package com.chimbori.crux.extractors

import com.chimbori.crux.common.cleanTitle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.common.removeWhiteSpace
import java.util.Locale
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

internal fun Document.extractTitle(): String? = (
    title().nullIfBlank()
      ?: select("title").text().nullIfBlank()
      ?: select("meta[name=title]").attr("content").nullIfBlank()
      ?: select("meta[property=og:title]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:title]").attr("content").nullIfBlank()
    )?.cleanTitle()?.nullIfBlank()

internal fun Document.extractCanonicalUrl(): String? = (
    select("link[rel=canonical]").attr("href").nullIfBlank()
      ?: select("meta[property=og:url]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:url]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractDescription(): String? = (
    select("meta[name=description]").attr("content").nullIfBlank()
      ?: select("meta[property=og:description]").attr("content").nullIfBlank()
      ?: select("meta[name=twitter:description]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractSiteName(): String? = (
    select("meta[property=og:site_name]").attr("content").nullIfBlank()
      ?: select("meta[name=application-name]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractThemeColor(): String? =
  select("meta[name=theme-color]").attr("content").nullIfBlank()

internal fun Document.extractKeywords(): List<String> =
  select("meta[name=keywords]").attr("content")
    .removeWhiteSpace()
    .removePrefix("[")
    .removeSuffix("]")
    .split("\\s*,\\s*".toRegex())
    .filter { it.isNotBlank() }

internal fun Document.extractFaviconUrl(baseUrl: HttpUrl?): HttpUrl? = (
    findLargestIcon(select("link[rel~=icon]"))
      ?: findLargestIcon(select("link[rel~=ICON]"))
      ?: findLargestIcon(select("link[rel~=apple-touch-icon]"))
      ?: findLargestIcon(select("link[rel~=apple-touch-icon-precomposed]"))
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrlOrNull() }
  ?: baseUrl?.newBuilder()?.encodedPath("/favicon.ico")?.build()

internal fun Document.extractImageUrl(baseUrl: HttpUrl?): HttpUrl? = (
    // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
    select("meta[name=twitter:image]").attr("content").nullIfBlank()
      ?: select("meta[property=og:image]").attr("content").nullIfBlank()
      // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
      ?: select("link[rel=image_src]").attr("href").nullIfBlank()
      ?: select("meta[name=thumbnail]").attr("content").nullIfBlank()
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrlOrNull() }

internal fun Document.extractFeedUrl(baseUrl: HttpUrl?): HttpUrl? = (
    select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href").nullIfBlank()
      ?: select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href").nullIfBlank()
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrlOrNull() }

internal fun Document.extractAmpUrl(baseUrl: HttpUrl?): HttpUrl? =
  select("link[rel=amphtml]").attr("href").nullIfBlank()
    ?.let { baseUrl?.resolve(it) ?: it.toHttpUrlOrNull() }

internal fun Document.extractVideoUrl(baseUrl: HttpUrl?): HttpUrl? =
  select("meta[property=og:video]").attr("content").nullIfBlank()
    ?.let { baseUrl?.resolve(it) ?: it.toHttpUrlOrNull() }

internal fun findLargestIcon(iconElements: Elements): String? =
  iconElements.maxByOrNull { parseSize(it.attr("sizes")) }?.attr("href")?.nullIfBlank()

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

  val sizes = sizeString.trim(' ').lowercase(Locale.getDefault())
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
