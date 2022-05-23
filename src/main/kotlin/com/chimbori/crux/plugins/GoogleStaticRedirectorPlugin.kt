package com.chimbori.crux.plugins

import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class GoogleStaticRedirectorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean =
    url.host.endsWith(".google.com") && url.encodedPath == "/url"

  override suspend fun handle(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = (outputUrl.queryParameter("q") ?: outputUrl.queryParameter("url"))
        ?.toHttpUrlOrNull()
        ?: outputUrl
    } while (canHandle(outputUrl))
    return Resource(outputUrl)
  }
}
