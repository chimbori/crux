package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.estimatedReadingTimeMs
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.PostprocessHelpers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * AbsoluteUriResolver is a post-process plugin that replaces all relative
 * URIs (src and href attributes) with absolute URIs
 */
public class AbsoluteUriResolver(
  private val okHttpClient: OkHttpClient? = null

) : Extractor {
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    val resourceToUse = if (request.document != null) {
      request
    } else if (request.url != null && okHttpClient != null) {
      Resource.fetchFromUrl(request.url, okHttpClient)
    } else {
      Resource()
    }

    resourceToUse.url ?: return null
    resourceToUse.document ?: return null

    // if article is already fetched this plugin has probably been placed
    // after an article extractor - use article in that case
    val extractedDoc = if (resourceToUse.article != null) {
      PostprocessHelpers.toAbsoluteUri(resourceToUse.article)
    } else {
      PostprocessHelpers.toAbsoluteUri(resourceToUse.document)
    }

    return Resource(
      metadata = mapOf(Fields.DURATION_MS to extractedDoc.text().estimatedReadingTimeMs()),
      article = extractedDoc
    )
  }
}
