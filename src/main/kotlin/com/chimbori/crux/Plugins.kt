package com.chimbori.crux

import com.chimbori.crux.Fields.AMP_URL
import com.chimbori.crux.Fields.BANNER_IMAGE_URL
import com.chimbori.crux.Fields.CANONICAL_URL
import com.chimbori.crux.Fields.DESCRIPTION
import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Fields.FEED_URL
import com.chimbori.crux.Fields.KEYWORDS_CSV
import com.chimbori.crux.Fields.SITE_NAME
import com.chimbori.crux.Fields.THEME_COLOR_HEX
import com.chimbori.crux.Fields.TITLE
import com.chimbori.crux.Fields.VIDEO_URL
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.fromUrl
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.extractors.PostprocessHelpers.Companion.postprocess
import com.chimbori.crux.extractors.PreprocessHelpers.preprocess
import com.chimbori.crux.extractors.extractAmpUrl
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractDescription
import com.chimbori.crux.extractors.extractFaviconUrl
import com.chimbori.crux.extractors.extractFeedUrl
import com.chimbori.crux.extractors.extractImageUrl
import com.chimbori.crux.extractors.extractKeywords
import com.chimbori.crux.extractors.extractSiteName
import com.chimbori.crux.extractors.extractThemeColor
import com.chimbori.crux.extractors.extractTitle
import com.chimbori.crux.extractors.extractVideoUrl
import com.chimbori.crux.extractors.getNodes
import com.chimbori.crux.extractors.getWeight
import com.chimbori.crux.urls.isLikelyArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.jsoup.nodes.Element

/**
 * An ordered list of default plugins configured in Crux. Callers can override and provide their own list, or pick and
 * choose from the set of available default plugins to create their own configuration.
 */
public val DEFAULT_PLUGINS: List<Plugin> = listOf(
  GoogleStaticRedirectorPlugin(),
  FacebookStaticRedirectorPlugin(),
  AmpPlugin(refetchContentFromCanonicalUrl = true),
  HtmlMetadataPlugin(),  // Fallback extractor that parses many standard HTML attributes.
  ArticleExtractorPlugin(),
)

/**
 * Extracts common well-defined metadata fields from an HTML DOM tree. Includes support for:
 * - Twitter Cards Metadata: https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/markup
 * - Open Graph Protocol: https://ogp.me/
 * - AMP Spec: https://amp.dev/documentation/guides-and-tutorials/learn/spec/amphtml/
 */
public class HtmlMetadataPlugin : Plugin {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource = withContext(Dispatchers.IO) {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    Resource(
      fields = mapOf(
        TITLE to request.document?.extractTitle(),
        CANONICAL_URL to request.document?.extractCanonicalUrl(),
        DESCRIPTION to request.document?.extractDescription(),
        SITE_NAME to request.document?.extractSiteName(),
        THEME_COLOR_HEX to request.document?.extractThemeColor(),
        KEYWORDS_CSV to request.document?.extractKeywords()?.joinToString(separator = ","),
      ),
      urls = mapOf(
        FAVICON_URL to request.document?.extractFaviconUrl(canonicalUrl),
        BANNER_IMAGE_URL to request.document?.extractImageUrl(canonicalUrl),
        FEED_URL to request.document?.extractFeedUrl(canonicalUrl),
        AMP_URL to request.document?.extractAmpUrl(canonicalUrl),
        VIDEO_URL to request.document?.extractVideoUrl(canonicalUrl),
      )
    ).removeNullValues()
  }
}

public class ArticleExtractorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource? {
    request.document
      ?: return null

    preprocess(request.document)
    val nodes = request.document.getNodes()
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

    return Resource(document = postprocess(bestMatchElement))
  }
}

/**
 * If the current page is an AMP page, then [AmpPlugin] extracts the canonical URL & replaces the DOM tree for the AMP
 * page with the DOM tree for the canonical page.
 */
public class AmpPlugin(
  private val refetchContentFromCanonicalUrl: Boolean,
  private val okHttpClient: OkHttpClient = cruxOkHttpClient
) : Plugin {
  /** Skip handling any file extensions that are unlikely to be an HTML page. */
  override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource? {
    request.document?.select("link[rel=canonical]")?.attr("href")?.nullIfBlank()?.let {
      return Resource.fromUrl(
        url = it.toHttpUrl(),
        shouldFetchContent = refetchContentFromCanonicalUrl,
        okHttpClient = okHttpClient
      )
    }
    return null
  }
}

public class GoogleStaticRedirectorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean =
    url.host.endsWith(".google.com") && url.encodedPath == "/url"

  override suspend fun handle(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = (outputUrl.queryParameter("q") ?: outputUrl.queryParameter("url"))
        ?.toHttpUrlOrNull()
        ?: outputUrl
    } while (canHandle(outputUrl))
    return Resource(outputUrl)
  }
}

public class FacebookStaticRedirectorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean =
    url.host.endsWith(".facebook.com") && url.encodedPath == "/l.php"

  override suspend fun handle(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = outputUrl.queryParameter("u")?.toHttpUrlOrNull() ?: outputUrl
    } while (canHandle(outputUrl))
    return Resource(outputUrl)
  }
}
