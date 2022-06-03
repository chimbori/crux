package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.FAVICON_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractFaviconUrl
import okhttp3.HttpUrl

public class FaviconExtractor : Extractor {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    return Resource(urls = mapOf(FAVICON_URL to request.document?.extractFaviconUrl(canonicalUrl))).removeNullValues()
  }
}
