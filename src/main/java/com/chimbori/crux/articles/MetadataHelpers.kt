package com.chimbori.crux.articles

import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.cleanTitle
import com.chimbori.crux.common.removeWhiteSpace
import okhttp3.HttpUrl
import org.jsoup.nodes.Document

fun Document.extractTitle() = try {
  HeuristicString()
      .or(title())
      .or(select("head title").text())
      .or(select("head meta[name=title]").attr("content"))
      .or(select("head meta[property=og:title]").attr("content"))
      .or(select("head meta[name=twitter:title]").attr("content"))
      .toString()
      .cleanTitle()
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.cleanTitle()
}

fun Document.extractCanonicalUrl() = try {
  HeuristicString()
      .or(select("head link[rel=canonical]").attr("href"))
      .or(select("head meta[property=og:url]").attr("content"))
      .or(select("head meta[name=twitter:url]").attr("content"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.removeWhiteSpace()
}

fun Document.extractDescription() = try {
  HeuristicString()
      .or(select("head meta[name=description]").attr("content"))
      .or(select("head meta[property=og:description]").attr("content"))
      .or(select("head meta[name=twitter:description]").attr("content"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.removeWhiteSpace()
}

fun Document.extractSiteName() = try {
  HeuristicString()
      .or(select("head meta[property=og:site_name]").attr("content"))
      .or(select("head meta[name=application-name]").attr("content"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.removeWhiteSpace()
}

fun Document.extractThemeColor() = select("meta[name=theme-color]").attr("content")

fun Document.extractKeywords(): List<String> {
  var content = select("head meta[name=keywords]").attr("content").removeWhiteSpace()
  if (content.startsWith("[") && content.endsWith("]")) {
    content = content.substring(1, content.length - 1)
  }
  val split = content.split("\\s*,\\s*".toRegex())
  return if (split.size > 1 || split.size > 0 && split[0] != "") {
    split
  } else emptyList()
}

fun Document.extractFaviconUrl(baseUrl: HttpUrl) = try {
  HeuristicString()
      .or(findLargestIcon(select("head link[rel~=icon]")))
      .or(findLargestIcon(select("head link[rel~=ICON]")))
      .or(findLargestIcon(select("head link[rel^=apple-touch-icon]")))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.let { baseUrl.resolve(it) }
}

fun Document.extractImageUrl(baseUrl: HttpUrl) = try {
  HeuristicString() // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
      .or(select("head meta[name=twitter:image]").attr("content"))
      .or(select("head meta[property=og:image]").attr("content"))
      // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
      .or(select("link[rel=image_src]").attr("href"))
      .or(select("head meta[name=thumbnail]").attr("content"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.let { baseUrl.resolve(it) }
}

fun Document.extractAmpUrl(baseUrl: HttpUrl) = try {
  HeuristicString()
      .or(select("link[rel=amphtml]").attr("href"))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.let { baseUrl.resolve(it) }
}

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
