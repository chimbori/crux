package com.chimbori.crux.common

import com.chimbori.crux.api.Resource
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val DEFAULT_BROWSER_VERSION = "100.0.0.0"

internal const val CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 11; Build/RQ2A.210505.003) AppleWebKit/537.36 " +
    "(KHTML, like Gecko) Version/4.0 Chrome/$DEFAULT_BROWSER_VERSION Mobile Safari/537.36"

public suspend fun OkHttpClient.safeCall(request: Request): Response? = withContext(Dispatchers.IO) {
  try {
    newCall(request).execute()
  } catch (e: IOException) {
    null
  } catch (e: NullPointerException) {
    // OkHttp sometimes tries to read a cookie which is null, causing an NPE here. The root cause
    // has not been identified, but this only happens with Twitter so far.
    null
  } catch (e: IllegalArgumentException) {
    // The URL is something like "https://" (no hostname, no path, etc.) which is clearly invalid.
    null
  } catch (e: UnknownHostException) {
    // Device is offline, or this host is unreachable.
    null
  } catch (t: Throwable) {
    // Something else really bad happened, e.g. [java.net.SocketTimeoutException].
    null
  }
}

public suspend fun OkHttpClient.safeHttpGet(url: HttpUrl): Response? =
  safeCall(Request.Builder().url(url).get().build())

public suspend fun OkHttpClient.safeHttpHead(url: HttpUrl): Response? =
  safeCall(Request.Builder().url(url).head().build())

public suspend fun OkHttpClient.httpGetContent(url: HttpUrl, onError: ((t: Throwable) -> Unit)? = null): String? =
  withContext(Dispatchers.IO) {
    safeHttpGet(url)?.use { response ->
      if (response.isSuccessful && response.body != null) {
        try {
          response.body!!.string()
        } catch (t: Throwable) {
          onError?.invoke(t)
          null
        }
      } else null
    }
  }

public suspend fun Resource.Companion.fetchFromUrl(url: HttpUrl, okHttpClient: OkHttpClient)
    : Resource = withContext(Dispatchers.IO) {

  val httpResponse = okHttpClient.safeHttpGet(url)

  // If the HTTP request resulted in an HTTP redirect, use the redirected URL.
  val urlToUse = if (httpResponse?.isSuccessful == true && httpResponse.request.url != url) {
    httpResponse.request.url
  } else url

  val docToUse: Document? = try {
    httpResponse?.body?.let {
      Jsoup.parse(it.byteStream(), "UTF-8", urlToUse.toString())
    }
  } catch (t: Throwable) {
    null
  }

  Resource(url = urlToUse, document = docToUse)
}
