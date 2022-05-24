package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Resource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class GoogleStaticRedirector : Extractor {
  override fun canExtract(url: HttpUrl): Boolean =
    url.host.endsWith(".google.com") && url.encodedPath == "/url"

  override suspend fun extract(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = (outputUrl.queryParameter("q") ?: outputUrl.queryParameter("url"))
        ?.toHttpUrlOrNull()
        ?: outputUrl
    } while (canExtract(outputUrl))
    return Resource(outputUrl)
  }
}
