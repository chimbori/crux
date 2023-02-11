package com.chimbori.crux.common

import com.chimbori.crux.api.Resource
import com.chimbori.crux.createCruxOkHttpClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class OkHttpExtensionsTest {
  private val okHttpClient = createCruxOkHttpClient()
  private lateinit var mockWebServer: MockWebServer

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply {
      dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest) = MockResponse().setBody("${request.path}")
      }
      start()
    }
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testHttpRedirectUrlReturnedInsteadOfOriginalUrl() {
    val originalUrl = mockWebServer.url("/original")
    val redirectedUrl = mockWebServer.url("/redirected")
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        originalUrl.encodedPath -> MockResponse().setResponseCode(302).setHeader("Location", redirectedUrl)
        redirectedUrl.encodedPath -> MockResponse().setBody("")
        else -> MockResponse().setResponseCode(404)
      }
    }

    val resource = runBlocking {
      Resource.fetchFromUrl(originalUrl, okHttpClient)
    }
    assertNotEquals(originalUrl, resource.url)
    assertEquals(redirectedUrl, resource.url)
  }
}
