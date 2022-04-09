package com.chimbori.crux.links

import java.io.File
import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class LinkUrlExtractorTest {
  @Test
  fun testFindLink() {
    assertEquals(
      "https://m.facebook.com/story.php?story_fbid=11111111111111111&id=1111111111&comment_id=11111111111111111&notif_t=comment_mention&notif_id=1111111111111111&ref=m_notif#11111111111111111".toHttpUrlOrNull()!!,
      extractFromTestFile(
        "https://m.facebook.com/notifications".toHttpUrlOrNull()!!,
        "facebook_notification_single.html"
      )?.findLink()?.linkUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/test-url".toHttpUrlOrNull()!!,
      extractFromTestFile(
        "https://hermit.chimbori.com".toHttpUrlOrNull()!!,
        "image_extractor_simple_img.html"
      )?.findLink()?.linkUrl
    )
    assertEquals(
      "https://hermit.chimbori.com/test".toHttpUrlOrNull()!!,
      extractFromTestFile(
        "https://hermit.chimbori.com".toHttpUrlOrNull()!!,
        "image_extractor_css_style.html"
      )?.findLink()?.linkUrl
    )
  }

  private fun extractFromTestFile(baseUrl: HttpUrl, testFile: String) = try {
    val doc = Jsoup.parse(File("test_data/$testFile"), "UTF-8")
    LinkUrlExtractor(baseUrl, doc.body())
  } catch (e: IOException) {
    fail(e.message)
    null
  }
}
