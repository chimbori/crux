package com.chimbori.crux.articles;

import com.chimbori.crux.common.HeuristicString;
import com.chimbori.crux.common.Log;
import com.chimbori.crux.common.StringUtils;
import com.chimbori.crux.urls.CandidateURL;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MetadataHelpers {
  private MetadataHelpers() {
  }

  static String extractTitle(Document doc) {
    try {
      return cleanTitle(new HeuristicString(doc.title())
          .or(StringUtils.innerTrim(doc.select("head title").text()))
          .or(StringUtils.innerTrim(doc.select("head meta[name=title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=twitter:title]").attr("content")))
          .toString());
    } catch (HeuristicString.CandidateFound candidateFound) {
      return cleanTitle(candidateFound.candidate);
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
          .or(StringUtils.urlEncodeSpaceCharacter(largestIconNode(doc.select("head link[rel=icon]"))))
          .or(StringUtils.urlEncodeSpaceCharacter(largestIconNode(doc.select("head link[rel^=apple-touch-icon]"))))
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

  /**
   * Extracts a set of images from the article content itself. This extraction must be run before
   * the postprocess step, because that step removes tags that are useful for image extraction.
   */
  static List<Article.Image> extractImages(Element topNode) {
    List<Article.Image> images = new ArrayList<>();
    if (topNode == null) {
      return images;
    }

    Elements imgElements = topNode.select("img");
    if (imgElements.isEmpty() && topNode.parent() != null) {
      imgElements = topNode.parent().select("img");
    }

    int maxWeight = 0;
    double score = 1;
    for (Element imgElement : imgElements) {
      Article.Image image = Article.Image.from(imgElement);
      if (image.src.isEmpty()) {
        continue;
      }
      try {
        if (new CandidateURL(image.src).isAdImage()) {
          continue;
        }
      } catch (IllegalArgumentException e) {
        // Quite likely trying to pass a "data://" URI, which the Java URL parser can’t handle.
      }

      image.weight += image.height >= 50 ? 20 : -20;
      image.weight += image.width >= 50 ? 20 : -20;
      image.weight += image.src.startsWith("data:") ? -50 : 0;
      image.weight += image.src.endsWith(".gif") ? -20 : 0;
      image.weight += image.src.endsWith(".jpg") ? 5 : 0;
      image.weight += image.alt.length() > 35 ? 20 : 0;
      image.weight += image.title.length() > 35 ? 20 : 0;
      image.weight += image.noFollow ? -40 : 0;

      image.weight = (int) (image.weight * score);
      if (image.weight > maxWeight) {
        maxWeight = image.weight;
        score = score / 2;
      }

      images.add(image);
    }

    Collections.sort(images, new ImageWeightComparator());
    Log.i("images: %s", images);
    return images;
  }

  static String cleanTitle(String title) {
    StringBuilder res = new StringBuilder();
    int index = title.lastIndexOf("|");
    if (index > 0 && title.length() / 2 < index)
      title = title.substring(0, index + 1);

    int counter = 0;
    String[] strs = title.split("\\|");
    for (String part : strs) {
      if (counter == strs.length - 1 && res.length() > part.length()) {
        continue;
      }
      if (counter > 0) {
        res.append("|");
      }
      res.append(part);
      counter++;
    }
    return StringUtils.innerTrim(res.toString());
  }

  private static String largestIconNode(Elements iconNodes) {
    Element largestIcon = null;
    long maxSize = -1;
    for (Element iconNode : iconNodes) {
      final long size = parseSize(iconNode.attr("sizes"));
      if (size > maxSize) {
        maxSize = size;
        largestIcon = iconNode;
      }
    }
    if (largestIcon != null) {
      return StringUtils.urlEncodeSpaceCharacter(largestIcon.attr("href"));
    }
    return "";
  }

  /**
   * Given a size represented by "WidthxHeight" or "WidthxHeight ...", will return the largest dimension found.
   * <p>
   * Examples: "128x128" will return 128.
   * "128x64" will return 64.
   * "24x24 48x48" will return 48.
   * <p>
   * If a non supported input is given, will return 0.
   *
   * @param sizes String representing the sizes.
   * @return largest dimension.
   */
  static long parseSize(String sizes) {
    if (sizes == null || sizes.trim().isEmpty()) {
      return 0;
    }
    sizes = sizes.trim().toLowerCase();
    if (sizes.contains(" ")) { // Some sizes can be "16x16 24x24", so we split them with space and process each one.
      final String[] multiSizes = sizes.split(" ");
      long maxSize = 0;
      for (String size : multiSizes) {
        long currentSize = parseSize(size);
        if (currentSize > maxSize) {
          maxSize = currentSize;
        }
      }
      return maxSize;
    } else if (sizes.contains("x")) { // For handling sizes of format 128x128 etc.
      final String[] dimen = sizes.split("x");
      if (dimen.length == 2) {
        try {
          final long width = Long.parseLong(dimen[0].trim());
          final long height = Long.parseLong(dimen[1].trim());
          return Math.max(width, height);
        } catch (NumberFormatException e) {
          return 0;
        }
      }
    }
    return 0;
  }

  /**
   * Returns the highest-scored image first.
   */
  private static class ImageWeightComparator implements Comparator<Article.Image> {
    @Override
    public int compare(Article.Image o1, Article.Image o2) {
      return o2.weight - o1.weight;
    }
  }
}
