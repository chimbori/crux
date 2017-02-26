package com.chimbori.crux.articles;

import com.chimbori.crux.common.Log;
import com.chimbori.crux.common.StringUtils;
import com.chimbori.crux.urls.CruxURL;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ImageHelpers {
  private ImageHelpers() {
    // Prevent instantiation.
  }

  static String findLargestIcon(Elements iconNodes) {
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

      CruxURL cruxURL = CruxURL.parse(image.src);
      if (cruxURL != null && cruxURL.isAdImage()) {
        continue;
      }
      // cruxURL may be null if trying to pass a "data://" URI, which the Java URL parser can’t handle.

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
