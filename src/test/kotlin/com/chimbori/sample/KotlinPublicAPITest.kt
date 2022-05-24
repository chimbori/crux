package com.chimbori.sample

import com.chimbori.crux.Crux
import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.BANNER_IMAGE_URL
import com.chimbori.crux.api.Fields.FAVICON_URL
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Resource
import com.chimbori.crux.articles.ArticleExtractor
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.resolveRedirects
import com.chimbori.crux.extractors.ImageUrlExtractor
import com.chimbori.crux.extractors.LinkUrlExtractor
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * `com.chimbori.crux` package, so this is a separate package.
 */
class KotlinPublicAPITest {
  @Test
  fun testKotlinPluginApi() {
    // Create a reusable object configured with the default set of plugins.
    val crux = Crux()

    val httpURL = "https://chimbori.com/".toHttpUrl()

    // You can provide prefetched raw HTML content yourself, or have Crux fetch
    // it for you.
    val htmlContent = """
      |<html>
      |  <head>
      |    <title>Chimbori</title>
      |    <meta name="twitter:image" property="og:image" content="https://chimbori.com/media/cover-photo.png">
      |    <meta name="twitter:site" content="ChimboriApps">
      |    <link rel="apple-touch-icon-precomposed" sizes="192x192" href="https://chimbori.com/media/favicon.png">
      |  </head>
      |</html>
      |""".trimMargin()

    // Crux runs inside a `suspend` function as a Kotlin Coroutine.
    val extractedMetadata = runBlocking {
      crux.extractFrom(originalUrl = httpURL, parsedDoc = Jsoup.parse(htmlContent))
    }

    // Metadata fields such as the Title and Description are available from the
    // returned [Resource] object as an indexed collection.
    assertEquals("Chimbori", extractedMetadata[TITLE])

    // Well-known URLs related to this page are available either as strings or
    // OkHttp [HttpUrl]s.
    assertEquals("https://chimbori.com/media/favicon.png", extractedMetadata[FAVICON_URL])
    assertEquals("https://chimbori.com/media/favicon.png".toHttpUrl(), extractedMetadata.urls[FAVICON_URL])

    // Extra markup fields like Twitter Cards metadata or Open Graph metadata are
    // available as metadata fields as well.
    assertEquals("https://chimbori.com/media/cover-photo.png", extractedMetadata[BANNER_IMAGE_URL])
  }

  @Test
  fun testWithCustomPlugin() {
    // If you write a new plugin yourself, you can add any custom fields to the `Resource` object yourself,
    // and consume them in your own app.
    val customerNumberExtractorPlugin = object : Extractor {
      // Indicate that your plugin can handle all URLs on your site, but no others.
      override fun canExtract(url: HttpUrl): Boolean = url.topPrivateDomain() == "your-website.com"

      // Fields in the returned [Resource] overwrite those in the input [request]. If no changes are to be made, then
      // return null from your plugin. Otherwise, only return those fields that are new or changed from the input.
      override suspend fun extract(request: Resource) = Resource(
        fields = mapOf(CUSTOMER_NUMBER_FIELD to request.url?.queryParameter("customer-number"))
      )

      val CUSTOMER_NUMBER_FIELD = "customer-number"
    }

    val cruxWithCustomPlugin = Crux(listOf(customerNumberExtractorPlugin))
    val orderDetailsUrl = "https://www.your-website.com/orders?customer-number=42".toHttpUrl()

    val metadata = runBlocking {
      cruxWithCustomPlugin.extractFrom(orderDetailsUrl, Document(orderDetailsUrl.toString()))
    }
    // Input URL was unchanged and is available in the output metadata.
    assertEquals(orderDetailsUrl, metadata.url)
    // Data extracted by the custom plugin is available as a custom field.
    assertEquals("42", metadata[customerNumberExtractorPlugin.CUSTOMER_NUMBER_FIELD])
  }

  @Test
  fun testCallersCanAccessArticleExtractorAPI() {
    val httpURL = "https://chimbori.com/".toHttpUrl()
    val content = "<html><title>Crux"  // Intentionally malformed.
    if (httpURL.isLikelyArticle()) {
      ArticleExtractor(httpURL, content).extractMetadata().extractContent().article.run {
        assertEquals("Crux", title)
      }
    }
    val directURL = httpURL.resolveRedirects()
    assertEquals("https://chimbori.com/", directURL.toString())
  }

  @Test
  fun testCallersCanAccessImageExtractorAPI() {
    val url = "https://chimbori.com/".toHttpUrl()
    val content = "<img src=\"test.jpg\">" // Intentionally malformed.
    val imageUrl = ImageUrlExtractor(url, Jsoup.parse(content).body()).findImage().imageUrl
    assertEquals("https://chimbori.com/test.jpg".toHttpUrl(), imageUrl)
  }

  @Test
  fun testCallersCanAccessLinkExtractorAPI() {
    val url = "https://chimbori.com/".toHttpUrl()
    val content = "<img href=\"/test\" src=\"test.jpg\">" // Intentionally malformed.
    val linkUrl = LinkUrlExtractor(url, Jsoup.parse(content).body()).findLink().linkUrl
    assertEquals("https://chimbori.com/test".toHttpUrl(), linkUrl)
  }
}
