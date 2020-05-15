package com.chimbori.crux.articles

import com.chimbori.crux.articles.ImageHelpers.findLargestIcon
import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.StringUtils.cleanTitle
import com.chimbori.crux.common.StringUtils.urlEncodeSpaceCharacter
import com.chimbori.crux.common.removeWhiteSpace
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Document

fun Document.extractTitle(): String? {
  return try {
    cleanTitle(HeuristicString()
        .or(title())
        .or(select("head title").text().removeWhiteSpace())
        .or(select("head meta[name=title]").attr("content").removeWhiteSpace())
        .or(select("head meta[property=og:title]").attr("content").removeWhiteSpace())
        .or(select("head meta[name=twitter:title]").attr("content").removeWhiteSpace())
        .toString())
  } catch (candidateFound: CandidateFound) {
    if (candidateFound.candidate != null) {
      cleanTitle(candidateFound.candidate)
    } else {
      null
    }
  }
}

fun Document.extractAmpUrl(): String? {
  try {
    HeuristicString()
        .or(urlEncodeSpaceCharacter(select("link[rel=amphtml]").attr("href")))
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractCanonicalUrl(): String? {
  try {
    HeuristicString()
        .or(urlEncodeSpaceCharacter(select("head link[rel=canonical]").attr("href")))
        .or(urlEncodeSpaceCharacter(select("head meta[property=og:url]").attr("content")))
        .or(urlEncodeSpaceCharacter(select("head meta[name=twitter:url]").attr("content")))
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractDescription(): String? {
  try {
    HeuristicString()
        .or(select("head meta[name=description]").attr("content").removeWhiteSpace())
        .or(select("head meta[property=og:description]").attr("content").removeWhiteSpace())
        .or(select("head meta[name=twitter:description]").attr("content").removeWhiteSpace())
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractSiteName(): String? {
  try {
    HeuristicString()
        .or(select("head meta[property=og:site_name]").attr("content").removeWhiteSpace())
        .or(select("head meta[name=application-name]").attr("content").removeWhiteSpace())
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractThemeColor() = select("meta[name=theme-color]").attr("content")

fun Document.extractImageUrl(): String? {
  try {
    HeuristicString() // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
        .or(urlEncodeSpaceCharacter(select("head meta[name=twitter:image]").attr("content")))
        .or(urlEncodeSpaceCharacter(select("head meta[property=og:image]").attr("content")))
        // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
        .or(urlEncodeSpaceCharacter(select("link[rel=image_src]").attr("href")))
        .or(urlEncodeSpaceCharacter(select("head meta[name=thumbnail]").attr("content")))
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractFeedUrl(): String? {
  try {
    HeuristicString()
        .or(select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"))
        .or(select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href"))
  } catch (candidateFound: CandidateFound) {
    return candidateFound.candidate
  }
  return null
}

fun Document.extractVideoUrl() = urlEncodeSpaceCharacter(select("head meta[property=og:video]").attr("content"))

fun Document.extractFaviconUrl(baseUrl: HttpUrl) = try {
  HeuristicString()
      .or(findLargestIcon(baseUrl, select("head link[rel~=icon]")))
      .or(findLargestIcon(baseUrl, select("head link[rel~=ICON]")))
      .or(findLargestIcon(baseUrl, select("head link[rel^=apple-touch-icon]")))
  null
} catch (candidateFound: CandidateFound) {
  candidateFound.candidate?.toHttpUrlOrNull()
}

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
