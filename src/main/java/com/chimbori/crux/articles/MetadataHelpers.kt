package com.chimbori.crux.articles

import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.cleanTitle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.common.removeWhiteSpace
import okhttp3.HttpUrl
import org.jsoup.nodes.Document

fun Document.extractTitle(): String? = (
    title().nullIfBlank()
      ?: select("head title").text().nullIfBlank()
      ?: select("head meta[name=title]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:title]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:title]").attr("content")
    )?.cleanTitle()

fun Document.extractCanonicalUrl(): String? = (
    select("head link[rel=canonical]").attr("href").nullIfBlank()
      ?: select("head meta[property=og:url]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:url]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()

fun Document.extractDescription(): String? = (
    select("head meta[name=description]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:description]").attr("content").nullIfBlank()
      ?: select("head meta[name=twitter:description]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()

fun Document.extractSiteName(): String? = (
    select("head meta[property=og:site_name]").attr("content").nullIfBlank()
      ?: select("head meta[name=application-name]").attr("content").nullIfBlank()
    )?.removeWhiteSpace()

fun Document.extractThemeColor(): String? = select("meta[name=theme-color]").attr("content")

fun Document.extractKeywords(): List<String> =
  select("head meta[name=keywords]").attr("content")
    .removeWhiteSpace()
    .removePrefix("[")
    .removeSuffix("]")
    .split("\\s*,\\s*".toRegex())
    .filter { it.isNotBlank() }

fun Document.extractFaviconUrl(baseUrl: HttpUrl): HttpUrl? = (
    findLargestIcon(select("head link[rel~=icon]"))
      ?: findLargestIcon(select("head link[rel~=ICON]"))
      ?: findLargestIcon(select("head link[rel^=apple-touch-icon]"))
    )?.let { baseUrl.resolve(it) }

fun Document.extractImageUrl(baseUrl: HttpUrl): HttpUrl? = (
    // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
    select("head meta[name=twitter:image]").attr("content").nullIfBlank()
      ?: select("head meta[property=og:image]").attr("content").nullIfBlank()
      // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
      ?: select("link[rel=image_src]").attr("href").nullIfBlank()
      ?: select("head meta[name=thumbnail]").attr("content").nullIfBlank()
    )?.let { baseUrl.resolve(it) }

fun Document.extractAmpUrl(baseUrl: HttpUrl): HttpUrl? =
  select("link[rel=amphtml]").attr("href").nullIfBlank()?.let { baseUrl.resolve(it) }

fun Document.extractFeedUrl(baseUrl: HttpUrl): HttpUrl? = try {
  HeuristicString()
    .or(select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"))
    .or(select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.let { baseUrl.resolve(it) }
}

fun Document.extractVideoUrl(baseUrl: HttpUrl) =
  baseUrl.resolve(select("head meta[property=og:video]").attr("content"))
