package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.fromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

/**
 * If the current page is an AMP page, then [AmpRedirector] extracts the canonical URL & replaces the DOM tree for the AMP
 * page with the DOM tree for the canonical page.
 */
public class AmpRedirector(
  private val refetchContentFromCanonicalUrl: Boolean,
  private val okHttpClient: OkHttpClient = cruxOkHttpClient
) : Extractor {
  /** Skip handling any file extensions that are unlikely to be an HTML page. */
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    request.document?.select("link[rel=canonical]")?.attr("href")?.nullIfBlank()?.let {
      if (it.toHttpUrl() != request.url) {  // Only redirect if this is not already the canonical URL.
        return Resource.fromUrl(
          url = it.toHttpUrl(),
          shouldFetchContent = refetchContentFromCanonicalUrl,
          okHttpClient = okHttpClient
        )
      }
    }
    return null
  }
}
