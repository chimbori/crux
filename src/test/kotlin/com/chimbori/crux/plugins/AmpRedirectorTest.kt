package com.chimbori.crux.plugins

import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fromUrl
import com.chimbori.crux.extractors.extractTitle
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AmpRedirectorTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var ampRedirector: AmpRedirector

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply { start() }
    ampRedirector = AmpRedirector(refetchContentFromCanonicalUrl = true)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testExtractsCanonicalUrl() {
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
      val parsedResource = ampRedirector.extract(Resource(url = ampUrl, document = Jsoup.parse(ampHtml)))
      assertEquals(canonicalUrl, parsedResource?.url)
    }
  }

  @Test
  fun testReturnsNullWhenCanonicalUrlIsAbsent() {
    val ampUrl = mockWebServer.url("/amp-url")
    val ampHtmlWithNoCanonicalUrl = """<!doctype html><html amp><head></head></html>""".trimMargin()

    runBlocking {
      val parsedResource =
        ampRedirector.extract(Resource(url = ampUrl, document = Jsoup.parse(ampHtmlWithNoCanonicalUrl)))
      assertNull(parsedResource?.url)
    }
  }

  @Test
  fun testReturnsOriginalWhenAlreadyOnCanonicalUrl() {
    val canonicalUrl = mockWebServer.url("/canonical-url")
    val canonicalHtml = """<!doctype html><html amp>
      |<head>
      |<link rel="canonical" href="$canonicalUrl"/>
      |</head>
      |</html>""".trimMargin()

    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        canonicalUrl.encodedPath -> MockResponse().setBody(canonicalHtml)
        else -> MockResponse().setResponseCode(404)
      }
    }

    runBlocking {
      val canonicalResource = Resource(url = canonicalUrl, document = Jsoup.parse(canonicalHtml))
      val parsedResource = ampRedirector.extract(canonicalResource)
      assertNull(parsedResource)
    }
  }

  @Test
  fun testFetchesContentFromCanonicalUrl() {
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
      val parsed = ampRedirector.extract(
        Resource.fromUrl(url = ampUrl, shouldFetchContent = true)
      )
      assertEquals(canonicalUrl, parsed?.url)
      assertEquals("CanonicalUrl", parsed?.document?.extractTitle())
    }
  }
}
