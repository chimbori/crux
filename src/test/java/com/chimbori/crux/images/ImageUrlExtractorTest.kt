package com.chimbori.crux.images

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.io.IOException

class ImageUrlExtractorTest {
  @Test
  fun testFindImage() {
    assertEquals(
        "https://scontent-sea1-1.xx.fbcdn.net/v/t1.0-1/cp0/e15/q65/c30.0.120.120/p120x120/1111111_11111111111111111_1111111111_n.jpg?efg=abcdefghijk1&oh=1234567890abcdef1234567890abcdef&oe=ABCDEF12".toHttpUrlOrNull()!!,
        extractFromTestFile(
            baseUrl = "https://m.facebook.com/notifications".toHttpUrlOrNull()!!,
            testFile = "facebook_notification_single.html")?.findImage()?.imageUrl)
    assertEquals(
        "https://hermit.chimbori.com/static/media/test.jpg".toHttpUrlOrNull()!!,
        extractFromTestFile(
            baseUrl = "https://hermit.chimbori.com".toHttpUrlOrNull()!!,
            testFile = "image_extractor_simple_img.html")?.findImage()?.imageUrl)
    assertEquals(
        "https://hermit.chimbori.com/static/media/test.jpg".toHttpUrlOrNull()!!,
        extractFromTestFile(
            baseUrl = "https://hermit.chimbori.com".toHttpUrlOrNull()!!,
            testFile = "image_extractor_css_style.html")?.findImage()?.imageUrl)
  }

  private fun extractFromTestFile(baseUrl: HttpUrl, testFile: String) = try {
    val doc = Jsoup.parse(File("test_data/$testFile"), "UTF-8")
    ImageUrlExtractor(baseUrl, doc.body())
  } catch (e: IOException) {
    fail(e.message)
    null
  }
}
