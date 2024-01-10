package com.chimbori.crux_kmp.plugins

import com.chimbori.crux_kmp.api.Rewriter
import com.chimbori.crux_kmp.common.toUrlOrNull
import io.ktor.http.Url

public class FacebookUrlRewriter : Rewriter {
  private fun canRewrite(url: Url) = url.host.endsWith(".facebook.com") && url.encodedPath == "/l.php"

  override fun rewrite(url: Url): Url {
    if (!canRewrite(url)) return url

    var outputUrl: Url = url
    do {
      outputUrl = outputUrl.parameters["u"]?.toUrlOrNull() ?: outputUrl
    } while (canRewrite(outputUrl))
    return outputUrl
  }
}
