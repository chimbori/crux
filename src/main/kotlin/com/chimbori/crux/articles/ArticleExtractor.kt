package com.chimbori.crux.articles

import com.chimbori.crux.common.estimatedReadingTimeMinutes
import com.chimbori.crux.extractors.PostprocessHelpers
import com.chimbori.crux.extractors.PreprocessHelpers
import com.chimbori.crux.extractors.extractAmpUrl
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractDescription
import com.chimbori.crux.extractors.extractFaviconUrl
import com.chimbori.crux.extractors.extractFeedUrl
import com.chimbori.crux.extractors.extractImageUrl
import com.chimbori.crux.extractors.extractImages
import com.chimbori.crux.extractors.extractKeywords
import com.chimbori.crux.extractors.extractSiteName
import com.chimbori.crux.extractors.extractThemeColor
import com.chimbori.crux.extractors.extractTitle
import com.chimbori.crux.extractors.extractVideoUrl
import com.chimbori.crux.extractors.getNodes
import com.chimbori.crux.extractors.getWeight
import okhttp3.HttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Suppress("unused")
public class ArticleExtractor
/**
 * Create an [ArticleExtractor] from an already-parsed JSoup document, to be used when a
 * JSoup document has already been parsed outside this library, and saves a second duplicate
 * re-parse of the same content.
 */
constructor(public val url: HttpUrl, private val document: Document) {
  public val article: Article = Article(url)

  /** Create an [ArticleExtractor] from a raw HTML string. The HTML must exist and should be non-empty. */
  public constructor(url: HttpUrl, html: String) : this(url, Jsoup.parse(html))

  public fun extractMetadata(): ArticleExtractor {
    document.extractCanonicalUrl()?.let {
      article.canonicalUrl = url.resolve(it) ?: url
    }

    article.title = document.extractTitle()
    article.description = document.extractDescription()
    article.siteName = document.extractSiteName()
    article.themeColor = document.extractThemeColor()

    document.extractFaviconUrl(article.canonicalUrl)?.let { article.faviconUrl = it }
    document.extractImageUrl(article.canonicalUrl)?.let { article.imageUrl = it }
    document.extractAmpUrl(article.canonicalUrl)?.let { article.ampUrl = it }
    document.extractFeedUrl(article.canonicalUrl)?.let { article.feedUrl = it }
    document.extractVideoUrl(article.canonicalUrl)?.let { article.videoUrl = it }

    article.keywords = document.extractKeywords()
    return this
  }

  public fun extractContent(): ArticleExtractor {
    PreprocessHelpers.preprocess(document)
    val nodes = document.getNodes()
    var maxWeight = 0
    var bestMatchElement: Element? = null
    for (element in nodes) {
      val currentWeight = element.getWeight()
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
  public fun estimateReadingTime(): ArticleExtractor {
    article.estimatedReadingTimeMinutes = document.estimatedReadingTimeMinutes()
    return this
  }
}
