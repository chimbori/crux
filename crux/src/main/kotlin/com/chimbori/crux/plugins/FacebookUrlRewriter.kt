package com.chimbori.crux.plugins

import com.chimbori.crux.api.Rewriter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class FacebookUrlRewriter : Rewriter {
  private fun canRewrite(url: HttpUrl) = url.host.endsWith(".facebook.com") && url.encodedPath == "/l.php"

  override fun rewrite(url: HttpUrl): HttpUrl {
    if (!canRewrite(url)) return url

    var outputUrl: HttpUrl = url
    do {
      outputUrl = outputUrl.queryParameter("u")?.toHttpUrlOrNull() ?: outputUrl
    } while (canRewrite(outputUrl))
    return outputUrl
  }
}
