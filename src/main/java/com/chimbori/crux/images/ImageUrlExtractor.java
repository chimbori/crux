package com.chimbori.crux.images;

import com.chimbori.crux.common.HeuristicString;
import com.chimbori.crux.common.StringUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chimbori.crux.common.StringUtils.anyChildTagWithAttr;

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best
 * possible image URL candidate available within it. The use case for this application is to pick
 * a single representative image from a DOM sub-tree, in a way that works without explicit CSS
 * selector foo.
 *
 * Check out the test cases for markup that is supported.
 */
public class ImageUrlExtractor {
  private final String url;
  private final Element root;

  private String imageUrl;

  private static final Pattern CSS_URL = Pattern.compile("url\\([\\\"']{0,1}(.+?)[\\\"']{0,1}\\)");

  private ImageUrlExtractor(String url, Element root) {
    this.url = url;
    this.root = root;
  }

  public static ImageUrlExtractor with(String url, Element root) {
    return new ImageUrlExtractor(url, root);
  }

  public ImageUrlExtractor findImage() {
    try {
      imageUrl = new HeuristicString(root.attr("src"))
          .or(root.attr("data-src"))
          .or(anyChildTagWithAttr(root.select("img"), "src"))
          .or(anyChildTagWithAttr(root.select("img"), "data-src"))
          .or(anyChildTagWithAttr(root.select("*"), "src"))
          .or(anyChildTagWithAttr(root.select("*"), "data-src"))
          .or(parseImageUrlFromStyleAttr(root.select("[role=img]")))
          .or(parseImageUrlFromStyleAttr(root.select("*")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      imageUrl = candidateFound.candidate;
    }
    imageUrl = StringUtils.makeAbsoluteUrl(url, imageUrl);
    return this;
  }

  private String parseImageUrlFromStyleAttr(Elements elements) {
    for (Element element : elements) {
      String styleAttr = element.attr("style");
      if (styleAttr == null || styleAttr.isEmpty()) {
        continue;
      }

      styleAttr = StringEscapeUtils.unescapeHtml4(styleAttr);

      Matcher cssUrlMatcher = CSS_URL.matcher(styleAttr);
      if (cssUrlMatcher.find()) {
        return cssUrlMatcher.group(1);
      }
    }
    return null;
  }

  public String imageUrl() {
    return imageUrl;
  }
}
