package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractFaviconUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl

public class FaviconExtractor : Plugin {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource = withContext(Dispatchers.IO) {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    Resource(urls = mapOf(FAVICON_URL to request.document?.extractFaviconUrl(canonicalUrl))).removeNullValues()
  }
}
