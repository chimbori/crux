package com.chimbori.crux.extractors

import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fromTestData
import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ImageUrlExtractorTest {
  @Test
  fun testFindImage() {
    assertEquals(
      "https://scontent-sea1-1.xx.fbcdn.net/v/t1.0-1/cp0/e15/q65/c30.0.120.120/p120x120/1111111_11111111111111111_1111111111_n.jpg?efg=abcdefghijk1&oh=1234567890abcdef1234567890abcdef&oe=ABCDEF12".toHttpUrl(),
      extractFromTestFile(
        baseUrl = "https://m.facebook.com/notifications".toHttpUrl(),
        testFile = "facebook_notification_single.html"
      )?.findImage()?.imageUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/static/media/test.jpg".toHttpUrl(),
      extractFromTestFile(
        baseUrl = "https://hermit.chimbori.com".toHttpUrl(),
        testFile = "image_extractor_simple_img.html"
      )?.findImage()?.imageUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/static/media/test.jpg".toHttpUrl(),
      extractFromTestFile(
        baseUrl = "https://hermit.chimbori.com".toHttpUrl(),
        testFile = "image_extractor_css_style.html"
      )?.findImage()?.imageUrl
    )
  }

  private fun extractFromTestFile(baseUrl: HttpUrl, testFile: String) = try {
    val resource = Resource.fromTestData(baseUrl, testFile)
    ImageUrlExtractor(baseUrl, resource.document!!.body())
  } catch (e: IOException) {
    fail(e.message)
    null
  }
}
