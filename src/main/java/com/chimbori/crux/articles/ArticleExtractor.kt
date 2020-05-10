package com.chimbori.crux.articles

import com.chimbori.crux.articles.ImageHelpers.extractImages
import com.chimbori.crux.articles.MetadataHelpers.*
import com.chimbori.crux.common.StringUtils.makeAbsoluteUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.math.ceil

class ArticleExtractor
/**
 * Create an [ArticleExtractor] from an already-parsed JSoup document, to be used when a
 * JSoup document has already been parsed outside this library, and saves a second duplicate
 * re-parse of the same content.
 */
constructor(val givenUrl: String, private val document: Document) {
  val article = Article().apply {
    canonicalUrl = givenUrl
  }

  /** Create an [ArticleExtractor] from a raw HTML string. The HTML must exist and should be non-empty. */
  constructor(url: String, html: String) : this(url, Jsoup.parse(html))

  fun extractMetadata(): ArticleExtractor {
    article.title = extractTitle(document)
    article.description = extractDescription(document)
    article.siteName = extractSiteName(document)
    article.themeColor = extractThemeColor(document)
    val extractedCanonicalUrl = extractCanonicalUrl(document)
    if (extractedCanonicalUrl != null) {
      article.canonicalUrl = makeAbsoluteUrl(article.canonicalUrl, extractedCanonicalUrl)
    }
    article.ampUrl = makeAbsoluteUrl(article.canonicalUrl, extractAmpUrl(document))
    article.feedUrl = makeAbsoluteUrl(article.canonicalUrl, extractFeedUrl(document))
    article.videoUrl = makeAbsoluteUrl(article.canonicalUrl, extractVideoUrl(document))
    article.faviconUrl = makeAbsoluteUrl(article.canonicalUrl, extractFaviconUrl(document))
    article.keywords = extractKeywords(document)
    return this
  }

  fun extractContent(): ArticleExtractor {
    PreprocessHelpers.preprocess(document)
    val nodes = ExtractionHelpers.getNodes(document)
    var maxWeight = 0
    var bestMatchElement: Element? = null
    for (element in nodes) {
      val currentWeight = ExtractionHelpers.getWeight(element)
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight
        bestMatchElement = element
        if (maxWeight > 200) {
          break
        }
      }
    }

    // Extract images before post-processing, because that step may remove images.
    article.images = extractImages(bestMatchElement)
    article.document = PostprocessHelpers.postprocess(bestMatchElement)
    article.imageUrl = makeAbsoluteUrl(article.canonicalUrl, MetadataHelpers.extractImageUrl(document, article.images))
    return this
  }

  /**
   * Populates [Article.estimatedReadingTimeMinutes] based on the parsed content. This method
   * must only be called after [.extractContent] has already been performed.
   */
  fun estimateReadingTime(): ArticleExtractor {
    // TODO: Consider handling badly-punctuated text such as missing spaces after periods.
    val wordCount = document.text().split("\\s+".toRegex()).size
    article.estimatedReadingTimeMinutes = ceil((wordCount / AVERAGE_WORDS_PER_MINUTE).toDouble()).toInt()
    return this
  }

  companion object {
    /** Number of words that can be read by an average person in one minute. */
    private const val AVERAGE_WORDS_PER_MINUTE = 275
  }
}
