package com.chimbori.crux

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Resource
import com.chimbori.crux.plugins.GoogleUrlRewriter
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CruxTest {
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
  fun testRewritersAreCompletedBeforeExtraction() {
    val crux = Crux(plugins = listOf(GoogleUrlRewriter()))
    val metadata = runBlocking {
      crux.extractFrom(
        "https://www.google.com/url?q=https://www.google.com/url?rct%3Dj%26sa%3Dt%26url%3Dhttps://example.com/permalink%253Fid%253D1234567890%26ct%3Dga%26cd%3DCAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA%26usg%3DAFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw".toHttpUrl()
      )
    }
    assertNotNull(metadata)
    assertEquals("https://example.com/permalink?id=1234567890".toHttpUrl(), metadata.url)
  }

  @Test
  fun testPluginsAreNotAskedToHandleUrlsTheyCannotHandle() {
    val fooHandlerPlugin = object : Extractor {
      override fun canExtract(url: HttpUrl) = url.encodedPath == "/foo"
      override suspend fun extract(request: Resource) = Resource(
        url = request.url?.newBuilder()?.encodedPath("/rewritten-from-foo")?.build()
      )
    }

    val barHandlerPlugin = object : Extractor {
      override fun canExtract(url: HttpUrl) = url.encodedPath == "/bar"
      override suspend fun extract(request: Resource) = Resource(
        url = request.url?.newBuilder()?.encodedPath("/rewritten-from-bar")?.build()
      )
    }

    val cruxWithFooPlugin = Crux(plugins = listOf(fooHandlerPlugin))
    val fooMetadata = runBlocking {
      cruxWithFooPlugin.extractFrom(mockWebServer.url("/foo"))
    }
    assertEquals("/rewritten-from-foo", fooMetadata.url?.encodedPath)

    val cruxWithBarPlugin = Crux(plugins = listOf(barHandlerPlugin))
    val barMetadata = runBlocking {
      cruxWithBarPlugin.extractFrom(mockWebServer.url("/foo"))
    }
    assertEquals("/foo", barMetadata.url?.encodedPath)
  }

  @Test
  fun testDefaultPluginsCanParseTitle() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = MockResponse().setBody("<title>Mock Title</title>")
    }

    val crux = Crux()
    val metadata = runBlocking { crux.extractFrom(mockWebServer.url("/mock-title")) }
    assertNotNull(metadata)
    assertEquals("Mock Title", metadata[TITLE])
  }

  @Test
  fun testHttpRedirectUrlReturnedInsteadOfOriginalUrl() {
    val originalUrl = mockWebServer.url("/original")
    val redirectedUrl = mockWebServer.url("/redirected")
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        originalUrl.encodedPath -> MockResponse().setResponseCode(301).setHeader("Location", redirectedUrl)
        redirectedUrl.encodedPath -> MockResponse().setBody("")
        else -> MockResponse().setResponseCode(404)
      }
    }

    val metadata = runBlocking { Crux().extractFrom(originalUrl) }
    assertEquals(redirectedUrl, metadata.url)
  }

  @Test
  fun testLaterPluginOperatesOnRewrittenUrlFromPreviousPlugin() {
    val rewriteFooToBarPlugin = object : Extractor {
      override fun canExtract(url: HttpUrl) = url.encodedPath == "/foo"
      override suspend fun extract(request: Resource) =
        Resource(
          url = request.url?.newBuilder()?.encodedPath("/bar")?.build(),
          fields = mapOf(TITLE to "Foo Title")
        )
    }

    val generateTitleForBarPlugin = object : Extractor {
      override fun canExtract(url: HttpUrl) = url.encodedPath == "/bar"
      override suspend fun extract(request: Resource) = Resource(fields = mapOf(TITLE to "Bar Title"))
    }

    // Test Foo before Bar.
    val fooBeforeBarCrux = Crux(listOf(rewriteFooToBarPlugin, generateTitleForBarPlugin))
    val fooBeforeBar = runBlocking {
      fooBeforeBarCrux.extractFrom(mockWebServer.url("/foo"))
    }
    assertEquals("Bar Title", fooBeforeBar[TITLE])

    // Test Bar before Foo.
    val barBeforeFooCrux = Crux(listOf(generateTitleForBarPlugin, rewriteFooToBarPlugin))
    val barBeforeFoo = runBlocking {
      barBeforeFooCrux.extractFrom(mockWebServer.url("/foo"))
    }
    assertEquals("Foo Title", barBeforeFoo[TITLE])
  }

  @Test
  fun testNoHttpRequestsAreMadeWhenCallerProvidesParsedDocument() {
  }

  @Test
  fun testLaterPluginOverridesFieldsSetByPreviousPlugin() {
  }

  @Test
  fun testLaterPluginOverridesFieldsWithNull() {
  }

  @Test
  fun testLaterPluginOverridesFieldsWithBlanks() {
  }

  @Test
  fun testPluginProvidesUpdatedParsedDocument() {
  }
}
