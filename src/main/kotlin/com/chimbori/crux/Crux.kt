package com.chimbori.crux

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Plugin
import com.chimbori.crux.api.Resource
import com.chimbori.crux.api.Rewriter
import com.chimbori.crux.common.CHROME_USER_AGENT
import com.chimbori.crux.plugins.AmpRedirector
import com.chimbori.crux.plugins.ArticleExtractor
import com.chimbori.crux.plugins.FacebookUrlRewriter
import com.chimbori.crux.plugins.FaviconExtractor
import com.chimbori.crux.plugins.GoogleUrlRewriter
import com.chimbori.crux.plugins.HtmlMetadataExtractor
import com.chimbori.crux.plugins.TrackingParameterRemover
import com.chimbori.crux.plugins.WebAppManifestParser
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document

/**
 * An ordered list of default plugins configured in Crux. Callers can override and provide their own list, or pick and
 * choose from the set of available default plugins to create their own configuration.
 */
public fun createDefaultPlugins(okHttpClient: OkHttpClient): List<Plugin> = listOf(
  // Static redirectors go first, to avoid getting stuck into CAPTCHAs.
  GoogleUrlRewriter(),
  FacebookUrlRewriter(),
  // Remove any tracking parameters remaining.
  TrackingParameterRemover(),
  // Prefer canonical URLs over AMP URLs.
  AmpRedirector(refetchContentFromCanonicalUrl = true, okHttpClient),
  // Parses many standard HTML metadata attributes.
  HtmlMetadataExtractor(okHttpClient),
  // Extracts the best possible favicon from all the markup available on the page itself.
  FaviconExtractor(),
  // Fetches and parses the Web Manifest. May replace existing favicon URL with one from the manifest.json.
  WebAppManifestParser(okHttpClient),
  // Parses the content of the page to remove ads, navigation, and all the other fluff.
  ArticleExtractor(okHttpClient),
)

/**
 * Crux can be configured with a set of plugins, including custom ones, in sequence. Each plugin can optionally process
 * resource metadata, can make additional HTTP requests if necessary, and pass along updated metadata to the next plugin
 * in the chain.
 */
public class Crux(
  /** Select from available plugins, or provide custom plugins for Crux to use. */
  private val plugins: List<Plugin>? = null,

  /** If the calling app has its own instance of [OkHttpClient], use it, otherwise Crux can create and use its own. */
  okHttpClient: OkHttpClient = createCruxOkHttpClient(),
) {

  private val activePlugins: List<Plugin> = plugins ?: createDefaultPlugins(okHttpClient)

  /**
   * Processes the provided URL, and returns a metadata object containing custom fields.
   * @param originalUrl the URL to extract metadata and content from.
   * @param parsedDoc if the calling app already has access to a parsed DOM tree, Crux can reuse it instead of
   * re-parsing it. If a custom [Document] is provided, Crux will not make any HTTP requests itself, and may not follow
   * HTTP redirects (but plugins may still optionally make additional HTTP requests themselves.)
   */
  public suspend fun extractFrom(originalUrl: HttpUrl, parsedDoc: Document? = null): Resource {
    val rewrittenUrl = activePlugins
      .filterIsInstance<Rewriter>()
      .fold(originalUrl) { rewrittenUrl, rewriter -> rewriter.rewrite(rewrittenUrl) }

    return activePlugins
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

private fun createCruxOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
  .followRedirects(true)
  .followSslRedirects(true)
  .retryOnConnectionFailure(true)
  .addNetworkInterceptor { chain ->
    chain.proceed(
      chain.request().newBuilder()
        .header("User-Agent", CHROME_USER_AGENT).build()
    )
  }
  .build()
