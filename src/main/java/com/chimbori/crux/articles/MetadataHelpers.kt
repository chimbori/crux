package com.chimbori.crux.articles

import com.chimbori.crux.articles.ImageHelpers.findLargestIcon
import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.StringUtils.cleanTitle
import com.chimbori.crux.common.StringUtils.innerTrim
import com.chimbori.crux.common.StringUtils.urlEncodeSpaceCharacter
import org.jsoup.nodes.Document

internal object MetadataHelpers {
  fun extractTitle(doc: Document): String? {
    return try {
      cleanTitle(HeuristicString()
          .or(doc.title())
          .or(innerTrim(doc.select("head title").text()))
          .or(innerTrim(doc.select("head meta[name=title]").attr("content")))
          .or(innerTrim(doc.select("head meta[property=og:title]").attr("content")))
          .or(innerTrim(doc.select("head meta[name=twitter:title]").attr("content")))
          .toString())
    } catch (candidateFound: CandidateFound) {
      if (candidateFound.candidate != null) {
        cleanTitle(candidateFound.candidate)
      } else {
        null
      }
    }
  }

  fun extractAmpUrl(doc: Document): String? {
    try {
      HeuristicString()
          .or(urlEncodeSpaceCharacter(doc.select("link[rel=amphtml]").attr("href")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractCanonicalUrl(doc: Document): String? {
    try {
      HeuristicString()
          .or(urlEncodeSpaceCharacter(doc.select("head link[rel=canonical]").attr("href")))
          .or(urlEncodeSpaceCharacter(doc.select("head meta[property=og:url]").attr("content")))
          .or(urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:url]").attr("content")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractDescription(doc: Document): String? {
    try {
      HeuristicString()
          .or(innerTrim(doc.select("head meta[name=description]").attr("content")))
          .or(innerTrim(doc.select("head meta[property=og:description]").attr("content")))
          .or(innerTrim(doc.select("head meta[name=twitter:description]").attr("content")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractSiteName(doc: Document): String? {
    try {
      HeuristicString()
          .or(innerTrim(doc.select("head meta[property=og:site_name]").attr("content")))
          .or(innerTrim(doc.select("head meta[name=application-name]").attr("content")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractThemeColor(doc: Document): String {
    return doc.select("meta[name=theme-color]").attr("content")
  }

  fun extractImageUrl(doc: Document): String? {
    try {
      HeuristicString() // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
          .or(urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:image]").attr("content")))
          .or(urlEncodeSpaceCharacter(doc.select("head meta[property=og:image]").attr("content")))
          // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
          .or(urlEncodeSpaceCharacter(doc.select("link[rel=image_src]").attr("href")))
          .or(urlEncodeSpaceCharacter(doc.select("head meta[name=thumbnail]").attr("content")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractFeedUrl(doc: Document): String? {
    try {
      HeuristicString()
          .or(doc.select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"))
          .or(doc.select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href"))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractVideoUrl(doc: Document): String? {
    return urlEncodeSpaceCharacter(doc.select("head meta[property=og:video]").attr("content"))
  }

  fun extractFaviconUrl(doc: Document): String? {
    try {
      HeuristicString()
          .or(urlEncodeSpaceCharacter(findLargestIcon(doc.select("head link[rel=icon]"))))
          .or(urlEncodeSpaceCharacter(findLargestIcon(doc.select("head link[rel^=apple-touch-icon]"))))
          .or(urlEncodeSpaceCharacter(doc.select("head link[rel^=shortcut],link[rel$=icon]").attr("href")))
    } catch (candidateFound: CandidateFound) {
      return candidateFound.candidate
    }
    return null
  }

  fun extractKeywords(doc: Document): List<String> {
    var content = innerTrim(doc.select("head meta[name=keywords]").attr("content"))
    if (content.startsWith("[") && content.endsWith("]")) {
      content = content.substring(1, content.length - 1)
    }
    val split = content.split("\\s*,\\s*".toRegex())
    return if (split.size > 1 || split.size > 0 && split[0] != "") {
      split
    } else emptyList()
  }
}
