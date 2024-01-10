package com.chimbori.crux_kmp.plugins

import com.chimbori.crux_kmp.api.Extractor
import com.chimbori.crux_kmp.api.Fields.CANONICAL_URL
import com.chimbori.crux_kmp.api.Resource
import com.chimbori.crux_kmp.common.fetchFromUrl
import com.chimbori.crux_kmp.common.isLikelyArticle
import com.chimbori.crux_kmp.common.nullIfBlank
import com.chimbori.crux_kmp.common.toUrlOrNull
import io.ktor.client.HttpClient
import io.ktor.http.Url

/**
 * If the current page is an AMP page, then [AmpRedirector] extracts the canonical URL & replaces the DOM tree for the AMP
 * page with the DOM tree for the canonical page.
 */
public class AmpRedirector(
  private val refetchContentFromCanonicalUrl: Boolean,
  private val httpClient: HttpClient
) : Extractor {
  /** Skip handling any file extensions that are unlikely to be an HTML page. */
  override fun canExtract(url: Url): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    request.document?.select("link[rel=canonical]")?.attr("abs:href")?.nullIfBlank()?.let {
      val canonicalUrl = it.toUrlOrNull()
      if (canonicalUrl != request.url) {  // Only redirect if this is not already the canonical URL.
        return if (refetchContentFromCanonicalUrl && canonicalUrl != null) {
          Resource.fetchFromUrl(url = canonicalUrl, okHttpClient = httpClient)
        } else {
          Resource(url = canonicalUrl, metadata = mapOf(CANONICAL_URL to canonicalUrl))
        }
      }
    }
    return null
  }
}
