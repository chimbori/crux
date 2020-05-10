package com.chimbori.crux.links

import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.IOException

class LinkUrlExtractorTest {
  @Test
  fun testFindLink() {
    assertEquals("https://m.facebook.com/story.php?story_fbid=11111111111111111&id=1111111111&comment_id=11111111111111111&notif_t=comment_mention&notif_id=1111111111111111&ref=m_notif#11111111111111111",
        extractFromTestFile("https://m.facebook.com/notifications", "facebook_notification_single.html")!!.findLink().linkUrl())
    assertEquals("https://hermit.chimbori.com/test-url",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_simple_img.html")!!.findLink().linkUrl())
    assertEquals("https://hermit.chimbori.com/test",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_css_style.html")!!.findLink().linkUrl())
  }

  private fun extractFromTestFile(baseUrl: String, testFile: String) = try {
    val doc = Jsoup.parse(File("test_data/$testFile"), "UTF-8")
    LinkUrlExtractor.with(baseUrl, doc.body())
  } catch (e: IOException) {
    Assert.fail(e.message)
    null
  }
}
