package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Resource
import com.chimbori.crux.common.fromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FaviconExtractorTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var faviconExtractor: FaviconExtractor

  @Before
  fun setUp() {
    faviconExtractor = FaviconExtractor()
    mockWebServer = MockWebServer().apply { start() }
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testFaviconPlugin() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = MockResponse().setBody(
        """|
          |<html>
          |  <head>
          |    <link rel="apple-touch-icon-precomposed" sizes="192x192" href="/favicon.png">
          |    <link rel="apple-touch-icon-precomposed" sizes="48x48" href="/favicon-too-small.png">
          |  </head>
          |</html>
          |""".trimMargin()
      )
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(faviconExtractor.canExtract(candidateUrl))

    runBlocking {
      val parsed = faviconExtractor.extract(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true)
      )
      assertEquals(mockWebServer.url("/favicon.png"), parsed.urls[FAVICON_URL])
    }
  }
}
