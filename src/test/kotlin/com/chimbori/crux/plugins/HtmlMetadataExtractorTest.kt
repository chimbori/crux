package com.chimbori.crux.plugins

import com.chimbori.crux.api.Fields.CANONICAL_URL
import com.chimbori.crux.api.Fields.DESCRIPTION
import com.chimbori.crux.api.Fields.NEXT_PAGE_URL
import com.chimbori.crux.api.Fields.PREVIOUS_PAGE_URL
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.loggingOkHttpClient
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HtmlMetadataExtractorTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var htmlMetadataExtractor: HtmlMetadataExtractor

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply { start() }
    htmlMetadataExtractor = HtmlMetadataExtractor(loggingOkHttpClient)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testParseValidTitleAndBlankDescription() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) =
        MockResponse().setBody("<title>Crux Test</title><description>\r\t\n </description>")
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(htmlMetadataExtractor.canExtract(candidateUrl))

    runBlocking {
      val parsed = htmlMetadataExtractor.extract(
        Resource.fetchFromUrl(candidateUrl, loggingOkHttpClient)
      )
      assertEquals(candidateUrl, parsed.url)
      assertEquals("Crux Test", parsed[TITLE])
      assertFalse(parsed.metadata.containsKey(DESCRIPTION))
    }
  }

  @Test
  fun testPaginationLinks() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) =
        MockResponse().setBody(
          """<link rel="canonical" href="http://www.example.com/page=2"/>
            |<link rel="prev" href="http://www.example.com/page=1"/>
            |<link rel="next" href="http://www.example.com/page=3"/>
            |""".trimMargin()
        )
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(htmlMetadataExtractor.canExtract(candidateUrl))

    runBlocking {
      val parsed = htmlMetadataExtractor.extract(
        Resource.fetchFromUrl(candidateUrl, loggingOkHttpClient)
      )
      assertEquals("http://www.example.com/page=2".toHttpUrl(), parsed[CANONICAL_URL])
      assertEquals("http://www.example.com/page=3".toHttpUrl(), parsed[NEXT_PAGE_URL])
      assertEquals("http://www.example.com/page=1".toHttpUrl(), parsed[PREVIOUS_PAGE_URL])
    }
  }
}
