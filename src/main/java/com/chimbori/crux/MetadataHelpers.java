package com.chimbori.crux;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MetadataHelpers {
  private MetadataHelpers() {
  }

  private static final Set<String> IGNORED_TITLE_PARTS = new HashSet<>(Arrays.asList(
      "hacker news", "facebook"
  ));

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
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel=icon]").attr("href")))
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
      if (image.src.isEmpty() || StringUtils.isAdImage(image.src)) {
        continue;
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
//        int index = title.lastIndexOf("|");
//        if (index > 0 && title.length() / 2 < index)
//            title = title.substring(0, index + 1);

    int counter = 0;
    String[] strs = title.split("\\|");
    for (String part : strs) {
      if (IGNORED_TITLE_PARTS.contains(part.toLowerCase().trim())) {
        continue;
      }
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
