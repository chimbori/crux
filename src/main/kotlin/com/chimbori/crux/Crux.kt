package com.chimbori.crux

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.safeHttpGet
import com.chimbori.crux.plugins.AmpRedirector
import com.chimbori.crux.plugins.ArticleExtractor
import com.chimbori.crux.plugins.FacebookStaticRedirector
import com.chimbori.crux.plugins.FaviconExtractor
import com.chimbori.crux.plugins.GoogleStaticRedirector
import com.chimbori.crux.plugins.HtmlMetadataExtractor
import com.chimbori.crux.plugins.TrackingParameterRemover
import com.chimbori.crux.plugins.WebAppManifestParser
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * An ordered list of default plugins configured in Crux. Callers can override and provide their own list, or pick and
 * choose from the set of available default plugins to create their own configuration.
 */
public val DEFAULT_PLUGINS: List<Extractor> = listOf(
  // Static redirectors go first, to avoid getting stuck into CAPTCHAs.
  GoogleStaticRedirector(),
  FacebookStaticRedirector(),
  // Remove any tracking parameters remaining.
  TrackingParameterRemover(),
  // Prefer canonical URLs over AMP URLs.
  AmpRedirector(refetchContentFromCanonicalUrl = true),
  // Parses many standard HTML metadata attributes.
  HtmlMetadataExtractor(),
  // Extracts the best possible favicon from all the markup available on the page itself.
  FaviconExtractor(),
  // Fetches and parses the Web Manifest. May replace existing favicon URL with one from the manifest.json.
  WebAppManifestParser(),
  // Parses the content of the page to remove ads, navigation, and all the other fluff.
  ArticleExtractor(),
)

/**
 * Crux can be configured with a set of plugins, including custom ones, in sequence. Each plugin can optionally process
 * resource metadata, can make additional HTTP requests if necessary, and pass along updated metadata to the next plugin
 * in the chain.
 */
public class Crux(
  /** Select from available plugins, or provide custom plugins for Crux to use. */
  private val plugins: List<Extractor> = DEFAULT_PLUGINS,

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
      if (plugin.canExtract(resource.url ?: originalUrl)) {
        plugin.extract(resource)?.let {
          resource += it
        }
      }
    }
    return resource.removeNullValues()
  }
}
