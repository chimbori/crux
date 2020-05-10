package com.chimbori.crux.urls

import com.chimbori.crux.common.StringUtils.getQueryParameters
import java.net.URI

internal object Redirectors {
  val REDIRECT_PATTERNS = listOf(
      object : RedirectPattern {  // Facebook.
        override fun matches(uri: URI) = uri.host?.endsWith(".facebook.com") == true && uri.path == "/l.php"
        override fun resolve(uri: URI) = URI(getQueryParameters(uri)["u"])
      },
      object : RedirectPattern { // Google.
        override fun matches(uri: URI) = uri.host?.endsWith(".google.com") == true && uri.path == "/url"
        override fun resolve(uri: URI) = URI(getQueryParameters(uri)["q"])
      }
  )

  /**
   * Defines a pattern used by a specific service for URL redirection. This should be stateless, and will be called for
   * each URL that needs to be resolved.
   */
  internal interface RedirectPattern {
    /** @return true if this RedirectPattern can handle the provided URL, false if not. */
    fun matches(uri: URI): Boolean

    /** @return the actual URL that is pointed to by this redirector URL. */
    fun resolve(uri: URI): URI
  }
}
