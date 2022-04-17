package com.chimbori.crux.articles

import com.chimbori.crux.common.cleanTitle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.common.removeWhiteSpace
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.nodes.Document

internal fun Document.extractTitle(): String? = (
    title().nullIfBlank()
      ?: select("head title").text().nullIfBlank()
      ?: select("head meta[name=title]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:title]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:title]").attr("content").nullIfBlank()
    )?.cleanTitle()?.nullIfBlank()

internal fun Document.extractCanonicalUrl(): String? = (
    select("head link[rel=canonical]").attr("href").nullIfBlank()
      ?: select("head meta[property=og:url]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:url]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractDescription(): String? = (
    select("head meta[name=description]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:description]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:description]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractSiteName(): String? = (
    select("head meta[property=og:site_name]").attr("content").nullIfBlank()
      ?: select("head meta[name=application-name]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()?.nullIfBlank()

internal fun Document.extractThemeColor(): String? =
  select("meta[name=theme-color]").attr("content").nullIfBlank()

internal fun Document.extractKeywords(): List<String> =
  select("head meta[name=keywords]").attr("content")
    .removeWhiteSpace()
    .removePrefix("[")
    .removeSuffix("]")
    .split("\\s*,\\s*".toRegex())
    .filter { it.isNotBlank() }

internal fun Document.extractFaviconUrl(baseUrl: HttpUrl?): HttpUrl? = (
    findLargestIcon(select("head link[rel~=icon]"))
      ?: findLargestIcon(select("head link[rel~=ICON]"))
      ?: findLargestIcon(select("head link[rel^=apple-touch-icon]"))
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrl() }

internal fun Document.extractImageUrl(baseUrl: HttpUrl?): HttpUrl? = (
    // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
    select("head meta[name=twitter:image]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:image]").attr("content").nullIfBlank()
      // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
      ?: select("link[rel=image_src]").attr("href").nullIfBlank()
      ?: select("head meta[name=thumbnail]").attr("content").nullIfBlank()
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrl() }

internal fun Document.extractFeedUrl(baseUrl: HttpUrl?): HttpUrl? = (
    select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href").nullIfBlank()
      ?: select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href").nullIfBlank()
    )?.let { baseUrl?.resolve(it) ?: it.toHttpUrl() }

internal fun Document.extractAmpUrl(baseUrl: HttpUrl?): HttpUrl? =
  select("link[rel=amphtml]").attr("href").nullIfBlank()
    ?.let { baseUrl?.resolve(it) ?: it.toHttpUrl() }

internal fun Document.extractVideoUrl(baseUrl: HttpUrl?): HttpUrl? =
  select("head meta[property=og:video]").attr("content").nullIfBlank()
    ?.let { baseUrl?.resolve(it) ?: it.toHttpUrl() }
