package com.chimbori.crux.links;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LinkUrlExtractorTest {
  @Test
  public void testFindLink() {
    assertEquals("https://m.facebook.com/story.php?story_fbid=11111111111111111&id=1111111111&comment_id=11111111111111111&notif_t=comment_mention&notif_id=1111111111111111&ref=m_notif#11111111111111111",
        extractFromTestFile("https://m.facebook.com/notifications", "facebook_notification_single.html").findLink().linkUrl());
    assertEquals("https://hermit.chimbori.com/test-url",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_simple_img.html").findLink().linkUrl());
    assertEquals("https://hermit.chimbori.com/test",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_css_style.html").findLink().linkUrl());
  }

  private LinkUrlExtractor extractFromTestFile(String baseUrl, String testFile) {
    try {
      Document doc = Jsoup.parse(new File("test_data/" + testFile), "UTF-8");
      return LinkUrlExtractor.with(baseUrl, doc.body());
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }
}
