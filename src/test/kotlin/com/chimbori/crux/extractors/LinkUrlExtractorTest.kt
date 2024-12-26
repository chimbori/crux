package com.chimbori.crux.extractors

import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.fromTestData
import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class LinkUrlExtractorTest {
  @Test
  fun testFindLink() {
    assertEquals(
      "https://m.facebook.com/story.php?story_fbid=11111111111111111&id=1111111111&comment_id=11111111111111111&notif_t=comment_mention&notif_id=1111111111111111&ref=m_notif#11111111111111111".toHttpUrl(),
      extractFromTestFile(
        "https://m.facebook.com/notifications".toHttpUrl(),
        "facebook_notification_single.html"
      )?.findLink()?.linkUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/test-url".toHttpUrl(),
      extractFromTestFile(
        "https://hermit.chimbori.com".toHttpUrl(),
        "image_extractor_simple_img.html"
      )?.findLink()?.linkUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/test".toHttpUrl(),
      extractFromTestFile(
        "https://hermit.chimbori.com".toHttpUrl(),
        "image_extractor_css_style.html"
      )?.findLink()?.linkUrl
    )
  }

  private fun extractFromTestFile(baseUrl: HttpUrl, testFile: String) = try {
    val resource = Resource.fromTestData(baseUrl, testFile)
    LinkUrlExtractor(baseUrl, resource.document!!.body())
  } catch (e: IOException) {
    fail(e.message)
    null
  }
}
