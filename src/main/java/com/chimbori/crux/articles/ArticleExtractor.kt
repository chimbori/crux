package com.chimbori.crux.articles

import com.chimbori.crux.articles.ImageHelpers.extractImages
import okhttp3.HttpUrl
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
constructor(val url: HttpUrl, private val document: Document) {
  val article = Article(url)

  /** Create an [ArticleExtractor] from a raw HTML string. The HTML must exist and should be non-empty. */
  constructor(url: HttpUrl, html: String) : this(url, Jsoup.parse(html))

  fun extractMetadata(): ArticleExtractor {
    document.extractCanonicalUrl()?.let {
      article.canonicalUrl = url.resolve(it) ?: url
    }

    article.title = document.extractTitle()
    article.description = document.extractDescription()
    article.siteName = document.extractSiteName()
    article.themeColor = document.extractThemeColor()

    document.extractImageUrl()?.let {
      article.imageUrl = article.canonicalUrl.resolve(it)
    }
    document.extractAmpUrl()?.let {
      article.ampUrl = article.canonicalUrl.resolve(it)
    }
    document.extractFeedUrl()?.let {
      article.feedUrl = article.canonicalUrl.resolve(it)
    }
    document.extractVideoUrl()?.let {
      article.videoUrl = article.canonicalUrl.resolve(it)
    }
    document.extractFaviconUrl()?.let {
      article.faviconUrl = article.canonicalUrl.resolve(it)
    }

    article.keywords = document.extractKeywords()
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
    article.images = extractImages(article.canonicalUrl, bestMatchElement)

    article.document = PostprocessHelpers.postprocess(bestMatchElement)
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
