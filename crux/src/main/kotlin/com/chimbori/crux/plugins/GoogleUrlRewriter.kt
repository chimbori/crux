package com.chimbori.crux.plugins

import com.chimbori.crux.api.Rewriter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class GoogleUrlRewriter : Rewriter {
  private fun canRewrite(url: HttpUrl) = url.host.endsWith(".google.com") && url.encodedPath == "/url"

  override fun rewrite(url: HttpUrl): HttpUrl {
    if (!canRewrite(url)) return url

    var outputUrl: HttpUrl = url
    do {
      outputUrl = (outputUrl.queryParameter("q") ?: outputUrl.queryParameter("url"))
        ?.toHttpUrlOrNull()
        ?: outputUrl
    } while (canRewrite(outputUrl))
    return outputUrl
  }
}
