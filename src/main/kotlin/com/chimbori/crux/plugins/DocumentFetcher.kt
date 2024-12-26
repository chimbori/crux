package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.CANONICAL_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.extractCanonicalUrl
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Fetches an HTML document from a remote URL, if not already fetched.
 * If a parsed JSoup Document is already available, this is a no-op.
 */
public class DocumentFetcher(private val okHttpClient: OkHttpClient) : Extractor {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource {
    val resourceToUse = if (request.document != null) {
      request
    } else if (request.url != null) {
      Resource.fetchFromUrl(request.url, okHttpClient)
    } else {
      Resource()
    }

    val canonicalUrl = resourceToUse.document?.extractCanonicalUrl()
      ?.let { resourceToUse.url?.resolve(it) }
      ?: resourceToUse.url

    return Resource(
      url = canonicalUrl,
      document = resourceToUse.document,
      metadata = mapOf(CANONICAL_URL to canonicalUrl)
    ).removeNullValues()
  }
}
