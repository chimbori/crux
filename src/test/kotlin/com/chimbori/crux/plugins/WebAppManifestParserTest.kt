package com.chimbori.crux.plugins

import com.chimbori.crux.api.Fields.BACKGROUND_COLOR_HEX
import com.chimbori.crux.api.Fields.BACKGROUND_COLOR_HTML
import com.chimbori.crux.api.Fields.DISPLAY
import com.chimbori.crux.api.Fields.FAVICON_URL
import com.chimbori.crux.api.Fields.LANGUAGE
import com.chimbori.crux.api.Fields.ORIENTATION
import com.chimbori.crux.api.Fields.THEME_COLOR_HEX
import com.chimbori.crux.api.Fields.THEME_COLOR_HTML
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Fields.WEB_APP_MANIFEST_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.loggingOkHttpClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebAppManifestParserTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var webAppManifestParser: WebAppManifestParser

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply { start() }
    webAppManifestParser = WebAppManifestParser(loggingOkHttpClient)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testWebAppManifestLinkTag() {
    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = MockResponse().setBody(
        """|
          |<html lang="en">
          |<head>
          |  <link rel="manifest" href="/static/sub/directory/manifest.json">
          |</head>
          |""".trimMargin()
      )
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(webAppManifestParser.canExtract(candidateUrl))

    runBlocking {
      val parsedResource = webAppManifestParser.extract(
        Resource.fetchFromUrl(candidateUrl, loggingOkHttpClient)
      )
      assertEquals(
        mockWebServer.url("/static/sub/directory/manifest.json"),
        parsedResource?.urls?.get(WEB_APP_MANIFEST_URL)
      )
    }
  }

  @Test
  fun testWebManifestJson() {
    // Example JSON from https://w3c.github.io/manifest/#typical-structure
    val manifestJson = """|
      |{
      |  "lang": "en",
      |  "dir": "ltr",
      |  "name": "Super Racer 3000",
      |  "short_name": "Racer3K",
      |  "icons": [
      |    {
      |      "src": "icon/lowres.webp",
      |      "sizes": "48x48",
      |      "type": "image/webp"
      |    },{
      |      "src": "icon/lowres",
      |      "sizes": "48x48"
      |    },{
      |      "src": "icon/hd_hi.ico",
      |      "sizes": "72x72 96x96 128x128 256x256"
      |    },{
      |      "src": "icon/hd_hi.svg",
      |      "sizes": "257x257"
      |    }
      |  ],
      |  "scope": "/",
      |  "id": "superracer",
      |  "start_url": "/start.html",
      |  "display": "fullscreen",
      |  "orientation": "landscape",
      |  "theme_color": "aliceblue",
      |  "background_color": "red"
      |}
    """.trimMargin()

    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest) = when (request.path) {
        "/" -> MockResponse().setBody("""<link rel="manifest" href="/static/manifest.json">""")
        "/static/manifest.json" -> MockResponse().setBody(manifestJson)
        else -> MockResponse().setResponseCode(404)
      }
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(webAppManifestParser.canExtract(candidateUrl))

    runBlocking {
      val parsedResource: Resource? = webAppManifestParser.extract(
        Resource.fetchFromUrl(candidateUrl, loggingOkHttpClient)
      )
      assertNotNull(parsedResource)
      assertEquals("Super Racer 3000", parsedResource?.get(TITLE))
      assertEquals("en", parsedResource?.get(LANGUAGE))
      assertEquals("fullscreen", parsedResource?.get(DISPLAY))
      assertEquals("landscape", parsedResource?.get(ORIENTATION))
      assertEquals("aliceblue", parsedResource?.get(THEME_COLOR_HTML))
      assertEquals("red", parsedResource?.get(BACKGROUND_COLOR_HTML))
      assertNull(parsedResource?.get(THEME_COLOR_HEX))
      assertNull(parsedResource?.get(BACKGROUND_COLOR_HEX))
      assertEquals(mockWebServer.url("/static/manifest.json"), parsedResource?.urls?.get(WEB_APP_MANIFEST_URL))
      assertEquals(mockWebServer.url("/static/icon/hd_hi.svg"), parsedResource?.urls?.get(FAVICON_URL))
    }
  }
}
