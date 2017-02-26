package com.chimbori.sample;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.images.ImageUrlExtractor;
import com.chimbori.crux.links.LinkUrlExtractor;
import com.chimbori.crux.urls.CruxURL;

import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * {@code com.chimbori.crux} package, so this is a separate package.
 */
public class PublicAPITest {
  @Test
  public void testCallersCanAccessArticleExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<html><title>Crux";  // Intentionally malformed.
    CruxURL cruxURL = CruxURL.parse(url);
    if (cruxURL.isLikelyArticle()) {
      Article article = ArticleExtractor.with(url, content).extractMetadata().extractContent().article();
      assertEquals("Crux", article.title);
    }
    CruxURL directURL = cruxURL.resolveRedirects();
    assertEquals("https://chimbori.com/", directURL.toString());
  }

  @Test
  public void testCallersCanAccessImageExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<img src=\"test.jpg\">";  // Intentionally malformed.

    String imageUrl = ImageUrlExtractor.with(url, Jsoup.parse(content).body()).findImage().imageUrl();
    assertEquals("https://chimbori.com/test.jpg", imageUrl);
  }

  @Test
  public void testCallersCanAccessLinkExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<img href=\"/test\" src=\"test.jpg\">";  // Intentionally malformed.

    String linkUrl = LinkUrlExtractor.with(url, Jsoup.parse(content).body()).findLink().linkUrl();
    assertEquals("https://chimbori.com/test", linkUrl);
  }
}
