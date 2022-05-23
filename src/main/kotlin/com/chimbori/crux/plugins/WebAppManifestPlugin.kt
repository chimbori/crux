package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.WEB_APP_MANIFEST_URL
import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.extractors.extractCanonicalUrl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class WebAppManifestPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    val webAppManifestUrl = request.document?.select("link[rel=manifest]")?.attr("href")?.nullIfBlank()
      ?.let { canonicalUrl?.resolve(it) ?: it.toHttpUrlOrNull() }
    return Resource(urls = mapOf(WEB_APP_MANIFEST_URL to webAppManifestUrl)).removeNullValues()
  }
}
