package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.WEB_APP_MANIFEST_URL
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

class WebAppManifestPluginTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var webAppManifestPlugin: WebAppManifestPlugin

  @Before
  fun setUp() {
    mockWebServer = MockWebServer().apply { start() }
    webAppManifestPlugin = WebAppManifestPlugin()
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
          |  <link rel="manifest" href="manifest.json">
          |</head>
          |""".trimMargin()
      )
    }

    val candidateUrl = mockWebServer.url("/")
    assertTrue(webAppManifestPlugin.canHandle(candidateUrl))

    runBlocking {
      val parsedResource = webAppManifestPlugin.handle(
        Resource.fromUrl(candidateUrl, shouldFetchContent = true)
      )
      assertEquals(mockWebServer.url("/manifest.json"), parsedResource.urls[WEB_APP_MANIFEST_URL])
    }
  }
}
