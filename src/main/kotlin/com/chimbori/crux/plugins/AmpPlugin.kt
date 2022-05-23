package com.chimbori.crux.plugins

import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.fromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

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
