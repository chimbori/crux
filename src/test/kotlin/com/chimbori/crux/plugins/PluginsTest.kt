package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Resource
import com.chimbori.crux.common.assertStartsWith
import com.chimbori.crux.common.fromTestData
import com.chimbori.crux.common.fromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PluginsTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var htmlMetadataExtractor: HtmlMetadataExtractor
  private lateinit var faviconExtractor: FaviconExtractor
  private lateinit var ampRedirector: AmpRedirector

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply {
      dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest) = MockResponse().setBody("${request.path}")
      }
      start()
    }
    htmlMetadataExtractor = HtmlMetadataExtractor()
    faviconExtractor = FaviconExtractor()
    ampRedirector = AmpRedirector(refetchContentFromCanonicalUrl = true)
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
    assertTrue(faviconExtractor.canHandle(candidateUrl))

    runBlocking {
      val parsedResource = faviconExtractor.handle(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true)
      )
      assertEquals(mockWebServer.url("/favicon.png"), parsedResource.urls[FAVICON_URL])
    }
  }

  @Test
  fun testArticleExtractorPluginCanParseArticleContent() {
    val wikipediaUrl = "https://en.wikipedia.org/wiki/Galileo_Galilei".toHttpUrl()
    val articleExtractor = ArticleExtractor()
    assertTrue(articleExtractor.canHandle(wikipediaUrl))

    runBlocking {
      articleExtractor.handle(Resource.fromTestData(wikipediaUrl, "wikipedia_galileo.html"))
    }?.let {
      val parsedArticle = it.document
      assertNotNull(parsedArticle)
      assertStartsWith(
        """"Galileo" redirects here. For other uses, see Galileo (disambiguation) and Galileo Galilei (disambiguation).""",
        parsedArticle?.text()
      )
    }
  }

}
