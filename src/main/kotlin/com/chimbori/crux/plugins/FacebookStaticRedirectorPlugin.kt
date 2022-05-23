package com.chimbori.crux.plugins

import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class FacebookStaticRedirectorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean =
    url.host.endsWith(".facebook.com") && url.encodedPath == "/l.php"

  override suspend fun handle(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = outputUrl.queryParameter("u")?.toHttpUrlOrNull() ?: outputUrl
    } while (canHandle(outputUrl))
    return Resource(outputUrl)
  }
}
