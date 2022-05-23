package com.chimbori.crux.plugins

import com.chimbori.crux.Fields
import com.chimbori.crux.Fields.CANONICAL_URL
import com.chimbori.crux.Fields.NEXT_PAGE_URL
import com.chimbori.crux.Fields.PREVIOUS_PAGE_URL
import com.chimbori.crux.Resource
import com.chimbori.crux.common.fromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HtmlMetadataExtractorTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var htmlMetadataExtractor: HtmlMetadataExtractor

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply { start() }
    htmlMetadataExtractor = HtmlMetadataExtractor()
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
    assertTrue(htmlMetadataExtractor.canHandle(candidateUrl))

    runBlocking {
      val parsed = htmlMetadataExtractor.handle(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true)
      )
      assertNull(parsed.url)
      assertEquals("Crux Test", parsed[Fields.TITLE])
      assertFalse(parsed.fields.containsKey(Fields.DESCRIPTION))
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
    assertTrue(htmlMetadataExtractor.canHandle(candidateUrl))

    runBlocking {
      val parsed = htmlMetadataExtractor.handle(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true)
      )
      assertEquals("http://www.example.com/page=2", parsed[CANONICAL_URL])
      assertEquals("http://www.example.com/page=3", parsed[NEXT_PAGE_URL])
      assertEquals("http://www.example.com/page=1", parsed[PREVIOUS_PAGE_URL])
    }
  }
}
