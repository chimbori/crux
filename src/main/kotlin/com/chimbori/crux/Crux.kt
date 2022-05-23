package com.chimbori.crux

import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.safeHttpGet
import com.chimbori.crux.plugins.AmpPlugin
import com.chimbori.crux.plugins.ArticleExtractorPlugin
import com.chimbori.crux.plugins.FacebookStaticRedirectorPlugin
import com.chimbori.crux.plugins.FaviconPlugin
import com.chimbori.crux.plugins.GoogleStaticRedirectorPlugin
import com.chimbori.crux.plugins.HtmlMetadataPlugin
import com.chimbori.crux.plugins.TrackingParameterRemover
import com.chimbori.crux.plugins.WebAppManifestPlugin
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * An ordered list of default plugins configured in Crux. Callers can override and provide their own list, or pick and
 * choose from the set of available default plugins to create their own configuration.
 */
public val DEFAULT_PLUGINS: List<Plugin> = listOf(
  // Static redirectors go first, to avoid getting stuck into CAPTCHAs.
  GoogleStaticRedirectorPlugin(),
  FacebookStaticRedirectorPlugin(),
  // Remove any tracking parameters remaining.
  TrackingParameterRemover(),
  // Prefer canonical URLs over AMP URLs.
  AmpPlugin(refetchContentFromCanonicalUrl = true),
  // Parses many standard HTML metadata attributes.
  HtmlMetadataPlugin(),
  // Extracts the best possible favicon from all the markup available on the page itself.
  FaviconPlugin(),
  // Fetches and parses the Web Manifest. May replace existing favicon URL with one from the manifest.json.
  WebAppManifestPlugin(),
  // Parses the content of the page to remove ads, navigation, and all the other fluff.
  ArticleExtractorPlugin(),
)

/**
 * Crux can be configured with a set of plugins, including custom ones, in sequence. Each plugin can optionally process
 * resource metadata, can make additional HTTP requests if necessary, and pass along updated metadata to the next plugin
 * in the chain.
 */
public class Crux(
  /** Select from available plugins, or provide custom plugins for Crux to use. */
  private val plugins: List<Plugin> = DEFAULT_PLUGINS,

  /** If the calling app has its own instance of [OkHttpClient], use it, otherwise Crux can create and use its own. */
  private val okHttpClient: OkHttpClient = cruxOkHttpClient,
) {

  /**
   * Processes the provided URL, and returns a metadata object containing custom fields.
   * @param originalUrl the URL to extract metadata and content from.
   * @param parsedDoc if the calling app already has access to a parsed DOM tree, Crux can reuse it instead of
   * re-parsing it. If a custom [Document] is provided, Crux will not make any HTTP requests itself, and may not follow
   * HTTP redirects (but plugins may still optionally make additional HTTP requests themselves.)
   */
  public suspend fun extractFrom(originalUrl: HttpUrl, parsedDoc: Document? = null): Resource {
    var resource: Resource = if (parsedDoc != null) {
      // If a [Document] is provided by the caller, then do not make any additional HTTP requests.
      Resource(url = originalUrl, document = parsedDoc)
    } else {
      val httpResponse = okHttpClient.safeHttpGet(originalUrl)
      val urlToUse = if (httpResponse?.isSuccessful == true) {
        // If the HTTP request resulted in an HTTP redirect, use the redirected URL.
        httpResponse.request.url
      } else {
        originalUrl
      }

      val downloadedDoc = httpResponse?.body?.string()?.let {
        Jsoup.parse(it, urlToUse.toString())
      } ?: Document(urlToUse.toString())  // Create an empty [Document] if none could be downloaded/parsed.

      Resource(url = urlToUse, document = downloadedDoc)
    }

    for (plugin in plugins) {
      if (plugin.canHandle(resource.url ?: originalUrl)) {
        plugin.handle(resource)?.let {
          resource += it
        }
      }
    }
    return resource.removeNullValues()
  }
}

/** A [Resource] encapculates metadata and content related to an HTTP resource. */
public data class Resource(
  /** Canonical URL for this resource. */
  val url: HttpUrl? = null,

  /** Parsed DOM tree for this resource, if available. */
  val document: Document? = null,

  /**
   * Text fields extracted from this resource, stored as key-value pairs. It is recommended to use well-defined keys
   * from [com.chimbori.crux.Fields] for all standard fields. Custom fields are also supported, in case none of the
   * pre-defined keys are applicable.
   */
  val fields: Map<String, String?> = emptyMap(),

  /**
   * URL fields extracted from this resource. Storing these as key-value pairs of [HttpUrl]s avoids re-parsing the same
   * URLs multiple times. URLs can also be retrieved as strings via the [get] indexed accessor.
   */
  val urls: Map<String, HttpUrl?> = emptyMap(),
) {
  /**
   * @return value of a named field. If there’s no named [String] field corresponding to this key in [Resource.fields],
   * but a [HttpUrl] exists in [Resource.urls], the latter will be stringified and returned instead.
   */
  public operator fun get(key: String): String? = fields[key] ?: urls[key]?.toString()

  /**
   * Merges non-null fields from another [Resource] with this object, and returns a new immutable object. Prefer to use
   * this operator instead of manually merging the two objects, so that [fields] and [urls] are correctly merged and
   * not clobbered.
   */
  public operator fun plus(anotherResource: Resource): Resource = Resource(
    url = anotherResource.url ?: url,
    document = anotherResource.document ?: document,
    fields = fields + anotherResource.fields,
    urls = urls + anotherResource.urls,
  )

  /**
   * Removes an immutable copy of this [Resource] that only contains non-null values for each key in both [fields]
   * and [urls].
   */
  public fun removeNullValues(): Resource = copy(
    fields = fields.filterValues { !it.isNullOrBlank() },
    urls = urls.filterValues { it != null },
  )

  /** For any potential extension functions to be defined on the companion object. */
  public companion object
}

/** Well-known keys to use in [Resource.fields] & [Resource.urls]. */
public object Fields {
  public const val TITLE: String = "title"
  public const val DESCRIPTION: String = "description"
  public const val SITE_NAME: String = "site-name"
  public const val LANGUAGE: String = "language"
  public const val DISPLAY: String = "display"
  public const val ORIENTATION: String = "orientation"

  public const val THEME_COLOR_HEX: String = "theme-color-hex"
  public const val THEME_COLOR_HTML: String = "theme-color-html"  // Named colors like "aliceblue"
  public const val BACKGROUND_COLOR_HEX: String = "background-color-hex"
  public const val BACKGROUND_COLOR_HTML: String = "background-color-html"  // Named colors like "aliceblue"

  public const val CANONICAL_URL: String = "canonical-url"
  public const val AMP_URL: String = "amp-url"
  public const val FAVICON_URL: String = "favicon-url"
  public const val BANNER_IMAGE_URL: String = "banner-image-url"
  public const val FEED_URL: String = "feed-url"
  public const val VIDEO_URL: String = "video-url"
  public const val WEB_APP_MANIFEST_URL: String = "web-app-manifest-url"  // https://www.w3.org/TR/appmanifest/

  // For image or video resources only.
  public const val ALT_TEXT: String = "alt-text"
  public const val WIDTH_PX: String = "width-px"
  public const val HEIGHT_PX: String = "height-px"

  // For articles (estimated reading time) and audio/video content (playback duration).
  public const val DURATION_MS: String = "duration-ms"

  public const val TWITTER_HANDLE: String = "twitter-handle"
  public const val KEYWORDS_CSV: String = "keywords-csv"
}

/**
 * Crux is designed as a chain of plugins, each of which can optionally handle URLs passed to it. Each plugin is
 * provided a fully-parsed HTML DOM to extract fields from, and can also make additional HTTP requests if necessary to
 * retrieve additional metadata or to follow redirects.
 *
 * Text fields can be set via the [Resource.fields] property, and URLs via the [Resource.urls] property. Plugins
 * can also rewrite the canonical URL, and can provide an updated DOM tree if the canonical URL is changed. The
 * updated URL and DOM tree will be passed on to the next plugin in sequence, so the exact ordering of plugins is
 * important.
 */
public interface Plugin {
  /**
   * @param url URL for the resource being processed by Crux.
   * @return true if this plugin can handle the URL, false otherwise. Plugins can only inspect the [HttpUrl], without
   * being able to peek at the content.
   */
  public fun canHandle(url: HttpUrl): Boolean

  /**
   * @param request metadata & DOM content for the request being handled.
   * @return a partially populated [Resource] with newly-extracted fields. Include only those fields that need to be
   * set or updated; they will be merged with the set of previously-extracted fields. If no fields need to be updated,
   * return `null`.
   */
  public suspend fun handle(request: Resource): Resource?
}
