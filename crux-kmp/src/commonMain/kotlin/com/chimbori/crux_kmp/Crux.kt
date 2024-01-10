package com.chimbori.crux

import com.chimbori.crux_kmp.api.Extractor
import com.chimbori.crux_kmp.api.Plugin
import com.chimbori.crux_kmp.api.Resource
import com.chimbori.crux_kmp.api.Rewriter
import com.chimbori.crux_kmp.common.CHROME_USER_AGENT
import com.chimbori.crux_kmp.plugins.AmpRedirector
import com.chimbori.crux_kmp.plugins.FacebookUrlRewriter
import com.chimbori.crux_kmp.plugins.FaviconExtractor
import com.chimbori.crux_kmp.plugins.GoogleUrlRewriter
import com.chimbori.crux_kmp.plugins.HtmlMetadataExtractor
import com.chimbori.crux_kmp.plugins.TrackingParameterRemover
import com.chimbori.crux_kmp.plugins.WebAppManifestParser
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * An ordered list of default plugins configured in Crux. Callers can override and provide their own list, or pick and
 * choose from the set of available default plugins to create their own configuration.
 */
public fun createDefaultPlugins(httpClient: HttpClient): List<Plugin> = listOf(
  // Rewriters

  // Static redirectors go first, to avoid getting stuck into CAPTCHAs.
  GoogleUrlRewriter(),
  FacebookUrlRewriter(),
  // Remove any tracking parameters remaining.
  TrackingParameterRemover(),

  // Extractors

  // Parses many standard HTML metadata attributes. Fetches the Web page, so this must be the first [Extractor].
  HtmlMetadataExtractor(httpClient),
  // Prefer canonical URLs over AMP URLs.
  AmpRedirector(refetchContentFromCanonicalUrl = true, httpClient),
  // Fetches and parses the Web Manifest. May replace existing favicon URL with one from the manifest.json.
  WebAppManifestParser(httpClient),
  // Extracts the best possible favicon from all the markup available on the page itself.
  FaviconExtractor(),
)

/**
 * Crux can be configured with a set of plugins, including custom ones, in sequence. Each plugin can optionally process
 * resource metadata, can make additional HTTP requests if necessary, and pass along updated metadata to the next plugin
 * in the chain.
 */
public class Crux(
  /** Select from available plugins, or provide custom plugins for Crux to use. */
  private val plugins: List<Plugin>? = null,

  /** If the calling app has its own instance of [HttpClient], use it, otherwise Crux can create and use its own. */
  httpClient: HttpClient = createCruxOkHttpClient(),
) {

  private val activePlugins: List<Plugin> = plugins ?: createDefaultPlugins(httpClient)

  /**
   * Processes the provided URL, and returns a metadata object containing custom fields.
   * @param originalUrl the URL to extract metadata and content from.
   * @param parsedDoc if the calling app already has access to a parsed DOM tree, Crux can reuse it instead of
   * re-parsing it. If a custom [Document] is provided, Crux will not make any HTTP requests itself, and may not follow
   * HTTP redirects (but plugins may still optionally make additional HTTP requests themselves.)
   */
  public suspend fun extractFrom(originalUrl: Url, parsedDoc: Document? = null): Resource =
    withContext(Dispatchers.IO) {
      val rewrittenUrl = activePlugins
        .filterIsInstance<Rewriter>()
        .fold(originalUrl) { rewrittenUrl, rewriter -> rewriter.rewrite(rewrittenUrl) }

      activePlugins
        .filterIsInstance<Extractor>()
        .fold(Resource(url = rewrittenUrl, document = parsedDoc)) { resource, extractor ->
          if (extractor.canExtract(resource.url ?: rewrittenUrl)) {
            resource + extractor.extract(resource)
          } else {
            resource
          }
        }.removeNullValues()
    }
}

internal fun createCruxOkHttpClient(): HttpClient = HttpClient {
  followRedirects = true
  headers {
    append("User-Agent", CHROME_USER_AGENT)
  }
}
