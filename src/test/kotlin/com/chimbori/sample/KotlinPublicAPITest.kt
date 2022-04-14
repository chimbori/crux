package com.chimbori.sample

import com.chimbori.crux.articles.ArticleExtractor
import com.chimbori.crux.images.ImageUrlExtractor
import com.chimbori.crux.links.LinkUrlExtractor
import com.chimbori.crux.urls.isLikelyArticle
import com.chimbori.crux.urls.resolveRedirects
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * `com.chimbori.crux` package, so this is a separate package.
 */
class KotlinPublicAPITest {
  @Test
  fun testCallersCanAccessArticleExtractorAPI() {
    val url = "https://chimbori.com/".toHttpUrl()
    val content = "<html><title>Crux"  // Intentionally malformed.
    val httpURL = "https://chimbori.com/".toHttpUrl()
    if (httpURL.isLikelyArticle()) {
      ArticleExtractor(url, content).extractMetadata().extractContent().article.run {
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
