package com.chimbori.crux;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ImageUrlExtractorTest {
  @Test
  public void testFindImage() {
    assertEquals("https://scontent-sea1-1.xx.fbcdn.net/v/t1.0-1/cp0/e15/q65/c30.0.120.120/p120x120/1111111_11111111111111111_1111111111_n.jpg?efg=abcdefghijk1&oh=1234567890abcdef1234567890abcdef&oe=ABCDEF12",
        extractFromTestFile("https://m.facebook.com/notifications", "facebook_notification_single.html").findImage().imageUrl());
    assertEquals("https://hermit.chimbori.com/static/media/test.jpg",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_simple_img.html").findImage().imageUrl());
    assertEquals("https://hermit.chimbori.com/static/media/test.jpg",
        extractFromTestFile("https://hermit.chimbori.com", "image_extractor_css_style.html").findImage().imageUrl());
  }

  private ImageUrlExtractor extractFromTestFile(String baseUrl, String testFile) {
    try {
      Document doc = Jsoup.parse(new File("test_data/" + testFile), "UTF-8");
      return ImageUrlExtractor.with(baseUrl, doc.body());
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }
}
