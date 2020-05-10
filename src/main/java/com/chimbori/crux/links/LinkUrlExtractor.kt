package com.chimbori.crux.links

import com.chimbori.crux.common.HeuristicString
import com.chimbori.crux.common.HeuristicString.CandidateFound
import com.chimbori.crux.common.StringUtils
import org.jsoup.nodes.Element

/**
 * Given a single DOM Element root, this extractor inspects the sub-tree and returns the best possible link URL
 * available within it. The use case for this application is to pick a single representative link from a DOM sub-tree,
 * in a way that works without explicit CSS selector foo. Check out the test cases for markup that is supported.
 */
class LinkUrlExtractor(private val url: String, private val root: Element) {
  var linkUrl: String? = null
    private set

  fun findLink(): LinkUrlExtractor {
    try {
      HeuristicString()
          .or(root.attr("href"))
          .or(StringUtils.anyChildTagWithAttr(root.select("*"), "href"))
    } catch (candidateFound: CandidateFound) {
      linkUrl = candidateFound.candidate
    }
    linkUrl = StringUtils.makeAbsoluteUrl(url, linkUrl)
    return this
  }
}
