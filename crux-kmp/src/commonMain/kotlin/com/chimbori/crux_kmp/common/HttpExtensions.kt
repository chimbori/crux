package com.chimbori.crux_kmp.common

import com.chimbori.crux_kmp.api.Resource
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.ported.BufferReader
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private const val DEFAULT_BROWSER_VERSION = "100.0.0.0"

internal const val CHROME_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 11; Build/RQ2A.210505.003) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Version/4.0 Chrome/$DEFAULT_BROWSER_VERSION Mobile Safari/537.36"

public suspend fun HttpClient.safeCall(builder: HttpRequestBuilder): HttpResponse? =
    withContext(Dispatchers.IO) {
        try {
            this@safeCall.request(builder)
        } catch (e: IOException) {
            null
        } catch (e: NullPointerException) {
            // OkHttp sometimes tries to read a cookie which is null, causing an NPE here. The root cause
            // has not been identified, but this only happens with Twitter so far.
            null
        } catch (e: IllegalArgumentException) {
            // The URL is something like "https://" (no hostname, no path, etc.) which is clearly invalid.
            null
        } catch (e: ResponseException) {
            // Device is offline, or this host is unreachable.
            null
        } catch (t: Throwable) {
            // Something else really bad happened, e.g. [java.net.SocketTimeoutException].
            null
        }
    }

public suspend fun HttpClient.safeHttpGet(url: Url): HttpResponse? {
    val builder = HttpRequestBuilder()
    builder.method = HttpMethod.Get
    builder.url(url)
    return safeCall(builder)
}

public suspend fun HttpClient.safeHttpHead(url: Url): HttpResponse? {
    val builder = HttpRequestBuilder()
    builder.method = HttpMethod.Head
    builder.url(url)
    return safeCall(builder)
}

public suspend fun HttpClient.httpGetContent(
    url: Url,
    onError: ((t: Throwable) -> Unit)? = null
): String? =
    withContext(Dispatchers.IO) {
        return@withContext safeHttpGet(url)?.use { response ->
            if (response.status == HttpStatusCode.OK) {
                try {
                    ""
                } catch (t: Throwable) {
                    onError?.invoke(t)
                    "null"
                }
            } else "null"
        }
    }

public suspend fun Resource.Companion.fetchFromUrl(url: Url, httpClient: HttpClient)
        : Resource = withContext(Dispatchers.IO) {

    val httpResponse = httpClient.safeHttpGet(url)

    // If the HTTP request resulted in an HTTP redirect, use the redirected URL.
    val urlToUse = if (httpResponse?.status == HttpStatusCode.OK && httpResponse.request.url != url) {
        httpResponse.request.url
    } else url

    val docToUse: Document? = try {
        httpResponse?.readBytes()?.let {
            Ksoup.parse(BufferReader(it), "UTF-8", urlToUse.toString())
        }
    } catch (t: Throwable) {
        null
    }

    Resource(url = urlToUse, document = docToUse)
}
