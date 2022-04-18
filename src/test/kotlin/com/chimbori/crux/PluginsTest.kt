package com.chimbori.crux

import com.chimbori.crux.Fields.DESCRIPTION
import com.chimbori.crux.Fields.TITLE
import com.chimbori.crux.articles.extractTitle
import com.chimbori.crux.common.assertStartsWith
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.fromTestData
import com.chimbori.crux.common.fromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PluginsTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var htmlMetadataPlugin: HtmlMetadataPlugin
  private lateinit var ampPlugin: AmpPlugin
  private lateinit var okHttpClientWithLogging: OkHttpClient

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply {
      dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest) = MockResponse().setBody("${request.path}")
      }
      start()
    }
    okHttpClientWithLogging = cruxOkHttpClient.newBuilder()
      .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
      .build()
    htmlMetadataPlugin = HtmlMetadataPlugin()
    ampPlugin = AmpPlugin(refetchContentFromCanonicalUrl = true, okHttpClient = okHttpClientWithLogging)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testHtmlPluginCanParseValidTitleAndBlankDescription() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) =
        MockResponse().setBody("<title>Crux Test</title><description>\r\t\n </description>")
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(htmlMetadataPlugin.canHandle(candidateUrl))

    runBlocking {
      val parsedResource = htmlMetadataPlugin.handle(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true, okHttpClientWithLogging)
      )
      assertNull(parsedResource.url)
      assertEquals("Crux Test", parsedResource[TITLE])
      assertFalse(parsedResource.fields.containsKey(DESCRIPTION))
    }
  }

  @Test
  fun testArticleExtractorPluginCanParseArticleContent() {
    val wikipediaUrl = "https://en.wikipedia.org/wiki/Galileo_Galilei".toHttpUrl()
    val articleExtractorPlugin = ArticleExtractorPlugin()
    assertTrue(articleExtractorPlugin.canHandle(wikipediaUrl))

    runBlocking {
      articleExtractorPlugin.handle(Resource.fromTestData(wikipediaUrl, "wikipedia_galileo.html"))
    }?.let {
      val parsedArticle = it.document
      assertNotNull(parsedArticle)
      assertStartsWith(
        """"Galileo" redirects here. For other uses, see Galileo (disambiguation) and Galileo Galilei (disambiguation).""",
        parsedArticle?.text()
      )
    }
  }

  @Test
  fun testAmpRedirectorPlugin_extractsCanonicalUrl() {
    val canonicalUrl = mockWebServer.url("/canonical-url")
    val ampUrl = mockWebServer.url("/amp-url")
    val ampHtml = """<!doctype html><html amp>
      |<head>
      |<link rel="canonical" href="$canonicalUrl"/>
      |</head>
      |</html>""".trimMargin()

    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        ampUrl.encodedPath -> MockResponse().setBody(
          """<html amp><link rel="canonical" href="${mockWebServer.url("/canonical-url")}"/></html>"""
        )
        canonicalUrl.encodedPath -> MockResponse().setBody("<title>CanonicalUrl</title>")
        else -> MockResponse().setResponseCode(404)
      }
    }

    runBlocking {
      val parsedResource = ampPlugin.handle(Resource(url = ampUrl, document = Jsoup.parse(ampHtml)))
      assertEquals(canonicalUrl, parsedResource?.url)
    }
  }

  @Test
  fun testAmpRedirectorPlugin_returnsNullWhenCanonicalUrlIsAbsent() {
    val ampUrl = mockWebServer.url("/amp-url")
    val ampHtmlWithNoCanonicalUrl = """<!doctype html><html amp><head></head></html>""".trimMargin()

    runBlocking {
      val parsedResource = ampPlugin.handle(Resource(url = ampUrl, document = Jsoup.parse(ampHtmlWithNoCanonicalUrl)))
      assertNull(parsedResource?.url)
    }
  }

  @Test
  fun testAmpPlugin_fetchesContentFromCanonicalUrl() {
    val canonicalUrl = mockWebServer.url("/canonical-url")
    val ampUrl = mockWebServer.url("/amp-url")

    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        ampUrl.encodedPath -> MockResponse().setBody(
          """<html amp>
            |<title>AmpUrl</title>
            |<link rel="canonical" href="${mockWebServer.url("/canonical-url")}"/>
            |</html>""".trimMargin()
        )
        canonicalUrl.encodedPath -> MockResponse().setBody("<title>CanonicalUrl</title>")
        else -> MockResponse().setResponseCode(404)
      }
    }

    runBlocking {
      val parsedResource = ampPlugin.handle(
        Resource.fromUrl(
          url = ampUrl, shouldFetchContent = true,
          okHttpClient = okHttpClientWithLogging
        )
      )
      assertEquals(canonicalUrl, parsedResource?.url)
      assertEquals("CanonicalUrl", parsedResource?.document?.extractTitle())
    }
  }
}
