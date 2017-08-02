package com.chimbori.crux.links;

import com.chimbori.crux.common.HeuristicString;
import com.chimbori.crux.common.StringUtils;

import org.jsoup.nodes.Element;

import static com.chimbori.crux.common.StringUtils.anyChildTagWithAttr;

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best
 * possible link URL available within it. The use case for this application is to pick
 * a single representative link from a DOM sub-tree, in a way that works without explicit CSS
 * selector foo.
 *
 * Check out the test cases for markup that is supported.
 */
public class LinkUrlExtractor {
  private final String url;
  private final Element root;

  private String linkUrl;

  private LinkUrlExtractor(String url, Element root) {
    this.url = url;
    this.root = root;
  }

  public static LinkUrlExtractor with(String url, Element root) {
    return new LinkUrlExtractor(url, root);
  }

  public LinkUrlExtractor findLink() {
    try {
      linkUrl = new HeuristicString(root.attr("href"))
          .or(anyChildTagWithAttr(root.select("*"), "href"))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      linkUrl = candidateFound.candidate;
    }
    linkUrl = StringUtils.makeAbsoluteUrl(url, linkUrl);
    return this;
  }

  public String linkUrl() {
    return linkUrl;
  }
}
