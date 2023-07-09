package com.chimbori.crux.plugins

import com.chimbori.crux.api.Resource
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AbsoluteUriResolverTest {
  private lateinit var absoluteUriResolver: AbsoluteUriResolver

  @Before
  fun setUp() {
    absoluteUriResolver = AbsoluteUriResolver()
  }

  private fun getBaseUriWithoutScheme(url : HttpUrl) : String {
    return "${url.host}${
      if (url.port != 80 && url.port != 443) {
      ":${url.port}/"
    } else {
      "/"
    }}"
  }

  private fun getBaseUriWithScheme(url : HttpUrl) : String {
    return "${url.scheme}://${url.host}${
      if (url.port != 80 && url.port != 443) {
        ":${url.port}/"
      } else {
        "/"
      }}"
  }

  private fun fixTrailingSlash(url : HttpUrl) : HttpUrl {
    return if (url.toUri().path.last() == '/') {
      url
    } else {
      "${url.toUri()}/".toHttpUrl()
    }
  }

  @Test
  fun testImageAbsoluteUriBaseUrl() {
    val inputUri = listOf(
      "http://test1.com".toHttpUrl(), // HTTP URL, domain, no trail '/'
      "http://test2.com/".toHttpUrl(), // HTTP URL, domain, trail '/'
      "https://test3.com".toHttpUrl(), // HTTPS URL, domain, no trail '/'
      "https://test4.com/".toHttpUrl(), // HTTPS URL, domain, trail '/'
      "http://test5.com:42".toHttpUrl(), // HTTP URL, domain, port, no trail '/'
      "http://test6.com:42/".toHttpUrl(), // HTTP URL, domain, port, trail '/'
      "https://test7.com:42".toHttpUrl(), // HTTPS URL, domain, port, no trail '/'
      "https://test8.com:42/".toHttpUrl(), // HTTPS URL, domain, port, trail '/'
      "http://test9.com/post/42/".toHttpUrl(), // HTTP URL, domain, post, trail '/'
      "http://test10.com:42/post/42/".toHttpUrl(), // HTTP URL, domain, port, post, trail '/'
      "http://192.168.1.1".toHttpUrl(), // IP address URL no trailing '/'
      "http://192.168.1.1/".toHttpUrl(), // IP address URL with trailing '/'
      "http://192.168.1.1/post/42/".toHttpUrl(), // IP address URL with trailing '/'
      "http://192.168.1.1:42".toHttpUrl(), // IP address URL with port no trailing '/'
      "http://192.168.1.1:42/".toHttpUrl(), // IP address URL with port with trailing '/'
    )

    inputUri.forEach { uri ->
      val inputHtmlToExpectedSrcUrl = mapOf(
        "<img src=http://not-your-site.com/image.png>" to "http://not-your-site.com/image.png", // already absolute, other domain
        "<img src=\"${uri}image1.png\">" to "${uri}image1.png", // already absolute
        "<img src=\"//${getBaseUriWithoutScheme(uri)}image2.png\">" to "${getBaseUriWithScheme(uri)}image2.png", // Scheme-rooted relative URI
        "<img src=\"/image3.png\">" to "${getBaseUriWithScheme(uri)}image3.png", // Pre-path-rooted relative URI.
        "<img src=\"./image4.png\">" to "${fixTrailingSlash(uri)}image4.png", // "Dotslash" relative URI.
        "<a href=\"${uri}\">" to "$uri", // already absolute
        "<a href=\"//${getBaseUriWithoutScheme(uri)}image2.png\">" to "${getBaseUriWithScheme(uri)}image2.png", // Scheme-rooted relative URI
        "<a href=\"/image3.png\">" to "${getBaseUriWithScheme(uri)}image3.png", // Pre-path-rooted relative URI.
        "<a href=\"./image4.png\">" to "${fixTrailingSlash(uri)}image4.png", // "Dotslash" relative URI.
      )

      inputHtmlToExpectedSrcUrl.forEach { (input, exp) ->

        val document = Jsoup.parse(input)

        // in real use-case this is set when fetching article
        document.setBaseUri(uri.toUri().toString())
        val resource = runBlocking {
          absoluteUriResolver.extract(
            Resource(url = uri, document = document)
          )
        }

        val expectedHtml = if (document.select("img[src]").size > 0) {
          Element("img").attr("src", exp)
        } else if (document.select("a[href]").size > 0) {
          Element("a").attr("href", exp)
        } else {
          Element("fail")
        }
        assertEquals(expectedHtml.toString(), resource?.article?.select("img[src],a[href]")?.get(0).toString())
      }
    }
  }
}
