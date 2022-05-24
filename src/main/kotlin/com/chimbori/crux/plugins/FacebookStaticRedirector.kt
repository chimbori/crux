package com.chimbori.crux.plugins

import com.chimbori.crux.Extractor
import com.chimbori.crux.Resource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class FacebookStaticRedirector : Extractor {
  override fun canExtract(url: HttpUrl): Boolean =
    url.host.endsWith(".facebook.com") && url.encodedPath == "/l.php"

  override suspend fun extract(request: Resource): Resource {
    var outputUrl: HttpUrl = request.url ?: return request
    do {
      outputUrl = outputUrl.queryParameter("u")?.toHttpUrlOrNull() ?: outputUrl
    } while (canExtract(outputUrl))
    return Resource(outputUrl)
  }
}
