package com.chimbori.crux.articles;

import com.chimbori.crux.common.HeuristicString;
import com.chimbori.crux.common.StringUtils;

import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MetadataHelpers {
  private MetadataHelpers() {
    // Prevent instantiation.
  }

  static String extractTitle(Document doc) {
    try {
      return StringUtils.cleanTitle(new HeuristicString(doc.title())
          .or(StringUtils.innerTrim(doc.select("head title").text()))
          .or(StringUtils.innerTrim(doc.select("head meta[name=title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=twitter:title]").attr("content")))
          .toString());
    } catch (HeuristicString.CandidateFound candidateFound) {
      return StringUtils.cleanTitle(candidateFound.candidate);
    }
  }

  static String extractAmpUrl(Document doc) {
    try {
      return new HeuristicString(StringUtils.urlEncodeSpaceCharacter(doc.select("link[rel=amphtml]").attr("href")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractCanonicalUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel=canonical]").attr("href")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:url]").attr("content")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:url]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractDescription(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.innerTrim(doc.select("head meta[name=description]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:description]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=twitter:description]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractSiteName(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:site_name]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=application-name]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractThemeColor(Document doc) {
    return doc.select("meta[name=theme-color]").attr("content");
  }

  static String extractImageUrl(Document doc, List<Article.Image> images) {
    try {
      return new HeuristicString(null)
          // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:image]").attr("content")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:image]").attr("content")))
          // Then, grab any hero images from the article itself.
          .or(images != null && images.size() > 0 ? StringUtils.urlEncodeSpaceCharacter(images.get(0).src) : null)
          // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("link[rel=image_src]").attr("href")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=thumbnail]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractFeedUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(doc.select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"))
          .or(doc.select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href"))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractVideoUrl(Document doc) {
    return StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:video]").attr("content"));
  }

  static String extractFaviconUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.urlEncodeSpaceCharacter(ImageHelpers.findLargestIcon(doc.select("head link[rel=icon]"))))
          .or(StringUtils.urlEncodeSpaceCharacter(ImageHelpers.findLargestIcon(doc.select("head link[rel^=apple-touch-icon]"))))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel^=shortcut],link[rel$=icon]").attr("href")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static Collection<String> extractKeywords(Document doc) {
    String content = StringUtils.innerTrim(doc.select("head meta[name=keywords]").attr("content"));

    if (content.startsWith("[") && content.endsWith("]")) {
      content = content.substring(1, content.length() - 1);
    }

    String[] split = content.split("\\s*,\\s*");
    if (split.length > 1 || (split.length > 0 && !"".equals(split[0]))) {
      return Arrays.asList(split);
    }
    return Collections.emptyList();
  }
}
