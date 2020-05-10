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
    CruxURL cruxURL = CruxURL.Companion.parse(url);
    if (cruxURL.isLikelyArticle()) {
      Article article = new ArticleExtractor(url, content).extractMetadata().extractContent().getArticle();
      assertEquals("Crux", article.getTitle());
    }
    CruxURL directURL = cruxURL.resolveRedirects();
    assertEquals("https://chimbori.com/", directURL.toString());
  }

  @Test
  public void testCallersCanAccessImageExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<img src=\"test.jpg\">";  // Intentionally malformed.

    String imageUrl = new ImageUrlExtractor(url, Jsoup.parse(content).body()).findImage().getImageUrl();
    assertEquals("https://chimbori.com/test.jpg", imageUrl);
  }

  @Test
  public void testCallersCanAccessLinkExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<img href=\"/test\" src=\"test.jpg\">";  // Intentionally malformed.

    String linkUrl = new LinkUrlExtractor(url, Jsoup.parse(content).body()).findLink().getLinkUrl();
    assertEquals("https://chimbori.com/test", linkUrl);
  }
}
