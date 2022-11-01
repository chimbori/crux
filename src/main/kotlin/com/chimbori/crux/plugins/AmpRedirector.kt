package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.CANONICAL_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

/**
 * If the current page is an AMP page, then [AmpRedirector] extracts the canonical URL & replaces the DOM tree for the AMP
 * page with the DOM tree for the canonical page.
 */
public class AmpRedirector(
  private val refetchContentFromCanonicalUrl: Boolean,
  private val okHttpClient: OkHttpClient
) : Extractor {
  /** Skip handling any file extensions that are unlikely to be an HTML page. */
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    request.document?.select("link[rel=canonical]")?.attr("abs:href")?.nullIfBlank()?.let {
      val canonicalUrl = it.toHttpUrlOrNull()
      if (canonicalUrl != request.url) {  // Only redirect if this is not already the canonical URL.
        return if (refetchContentFromCanonicalUrl && canonicalUrl != null) {
          Resource.fetchFromUrl(url = canonicalUrl, okHttpClient = okHttpClient)
        } else {
          Resource(url = canonicalUrl, metadata = mapOf(CANONICAL_URL to canonicalUrl))
        }
      }
    }
    return null
  }
}
