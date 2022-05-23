package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Resource
import com.chimbori.crux.common.assertStartsWith
import com.chimbori.crux.common.fromTestData
import com.chimbori.crux.common.fromUrl
import com.chimbori.crux.extractors.extractTitle
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
      val parsedResource = ampRedirector.handle(Resource(url = ampUrl, document = Jsoup.parse(ampHtml)))
      assertEquals(canonicalUrl, parsedResource?.url)
    }
  }

  @Test
  fun testAmpRedirectorPlugin_returnsNullWhenCanonicalUrlIsAbsent() {
    val ampUrl = mockWebServer.url("/amp-url")
    val ampHtmlWithNoCanonicalUrl = """<!doctype html><html amp><head></head></html>""".trimMargin()

    runBlocking {
      val parsedResource =
        ampRedirector.handle(Resource(url = ampUrl, document = Jsoup.parse(ampHtmlWithNoCanonicalUrl)))
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
      val parsedResource = ampRedirector.handle(
        Resource.fromUrl(url = ampUrl, shouldFetchContent = true)
      )
      assertEquals(canonicalUrl, parsedResource?.url)
      assertEquals("CanonicalUrl", parsedResource?.document?.extractTitle())
    }
  }

  @Test
  fun testFacebookRedirectorPlugin() {
    val facebookPlugin = FacebookStaticRedirector()
    mapOf(
      "http://example.com" to null,
      "http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketingreps&h=42263"
          to "http://www.bet.com/collegemarketingreps",
      "https://lm.facebook.com/l.php?u=https%3A%2F%2Fwww.wired.com%2F2014%2F08%2Fmaryam-mirzakhani-fields-medal%2F&h=ATMfLBdoriaBcr9HOvzkEe68VZ4hLhTiFINvMmq5_e6fC9yi3xe957is3nl8VJSWhUO_7BdOp7Yv9CHx6MwQaTkwbZ1CKgSQCt45CROzUw0C37Tp4V-2EvDSBuBM2H-Qew&enc=AZPhspzfaWR0HGkmbExT_AfCFThsP829S0z2UWadB7ponM3YguqyJXgtn2E9BAv_-IdZvW583OnNC9M6WroEsV1jlilk3FXS4ppeydAzaJU_o9gq6HvoGMj0N_SiIKHRE_Gamq8xVdEGPnCJi078X8fTEW_jrkwpPC6P6p5Z3gv6YkFZfskU6J9qe3YRyarG4dgM25dJFnVgxxH-qyHlHsYbMD69i2MF8QNreww1J6S84y6VbIxXC-m9dVfFlNQVmtWMUvJKDLcPmYNysyQSYvkknfZ9SgwBhimurLFmKWhf39nNNVYjjCszCJ1XT57xX0Q&s=1"
          to "https://www.wired.com/2014/08/maryam-mirzakhani-fields-medal/",
      "http://lm.facebook.com/l.php?u=http%3A%2F%2Fwww.cnn.com%2F2017%2F01%2F25%2Fpolitics%2Fscientists-march-dc-trnd%2Findex.html&h=ATO7Ln_rl7DAjRcqSo8yfpOvrFlEmKZmgeYHsOforgXsUYPLDy3nC1KfCYE-hev5oJzz1zydvvzI4utABjHqU1ruwDfw49jiDGCTrjFF-EyE6xfcbWRmDacY_6_R-lSi9g&enc=AZP1hkQfMXuV0vOHa1VeY8kdip2N73EjbXMKx3Zf4Ytdb1MrGHL48by4cl9_DShGYj9nZXvNt9xad9_4jphO9QBpRJLNGoyrRMBHI09eoFyPmxxjw7hHBy5Ouez0q7psi1uvjiphzOKVxjxyYBWnTJKD7m8rvhFz0HespmfvCf-fUiCpi6NDpxwYEw7vZ99fcjOpkiQqaFM_Gvqeat7r0e8axnqM-pJGY0fkjgWvgwTyfiB4fNMRhH3IaAmyL7DXl0xeYMoYSHuITkjTY9aU5dkiETfDVwBABOO9FJi2nTnRMw92E-gMMbiHFoHENlaSVJc&s=1"
          to "http://www.cnn.com/2017/01/25/politics/scientists-march-dc-trnd/index.html",
    ).forEach { (key, value) ->
      assertEquals(value != null, facebookPlugin.canHandle(key.toHttpUrl()))
      assertEquals(
        value?.toHttpUrl() ?: key.toHttpUrl(),
        runBlocking { facebookPlugin.handle(Resource(url = key.toHttpUrl())).url }
      )
    }
  }

  @Test
  fun testGoogleRedirectorPlugin() {
    val googleRedirectorPlugin = GoogleStaticRedirector()
    mapOf(
      "http://example.com" to null,
      "https://plus.url.google.com/url?q=https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/&rct=j&ust=1485739059621000&usg=AFQjCNH6Cgp4iU0NB5OoDpT3OtOXds7HQg"
          to "https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/",
      "https://www.google.com/url?q=https://www.google.com/url?rct%3Dj%26sa%3Dt%26url%3Dhttps://www.facebook.com/permalink.php%253Fid%253D111262459538815%2526story_fbid%253D534292497235807%26ct%3Dga%26cd%3DCAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA%26usg%3DAFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw"
          to "https://www.facebook.com/permalink.php?id=111262459538815&story_fbid=534292497235807",
    ).forEach { (key, value) ->
      assertEquals(value != null, googleRedirectorPlugin.canHandle(key.toHttpUrl()))
      assertEquals(
        value?.toHttpUrl() ?: key.toHttpUrl(),
        runBlocking { googleRedirectorPlugin.handle(Resource(url = key.toHttpUrl())).url }
      )
    }
  }
}
