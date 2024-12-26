package com.chimbori.crux.common

import com.chimbori.crux.api.Resource
import java.io.File
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import org.jsoup.Jsoup
import org.junit.Assert.fail

internal val loggingOkHttpClient: OkHttpClient = OkHttpClient.Builder()
  .followRedirects(true)
  .followSslRedirects(true)
  .retryOnConnectionFailure(true)
  .addNetworkInterceptor { chain ->
    chain.proceed(
      chain.request().newBuilder()
        .header("User-Agent", CHROME_USER_AGENT).build()
    )
  }
  .addInterceptor(HttpLoggingInterceptor().apply { level = BASIC })
  .build()

internal fun Resource.Companion.fromTestData(url: HttpUrl, testFile: String) = Resource(
  url = url,
  document = Jsoup.parse(File("test_data/$testFile"), "UTF-8", url.toString()),
)

internal fun assertStartsWith(expected: String, actual: String?) {
  if (actual?.startsWith(expected) == false) {
    fail("Expected \n[$expected]\n at start of \n[$actual]\n")
  }
}

internal fun assertContains(expected: String, actual: String?) {
  if (actual?.contains(expected) == false) {
    fail("Expected \n[$expected]\n in \n[$actual]\n")
  }
}
