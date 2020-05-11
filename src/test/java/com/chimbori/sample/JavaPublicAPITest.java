package com.chimbori.sample;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.images.ImageUrlExtractor;
import com.chimbori.crux.links.LinkUrlExtractor;
import com.chimbori.crux.urls.HttpUrlExtensionsKt;

import org.jsoup.Jsoup;
import org.junit.Test;

import okhttp3.HttpUrl;

import static org.junit.Assert.assertEquals;

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * {@code com.chimbori.crux} package, so this is a separate package.
 */
public class JavaPublicAPITest {
  @Test
  public void testCallersCanAccessArticleExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<html><title>Crux";  // Intentionally malformed.

    HttpUrl httpURL = HttpUrl.Companion.parse(url);
    if (HttpUrlExtensionsKt.isLikelyArticle(httpURL)) {
      Article article = new ArticleExtractor(url, content).extractMetadata().extractContent().getArticle();
      assertEquals("Crux", article.getTitle());
    }
    HttpUrl directURL = HttpUrlExtensionsKt.resolveRedirects(httpURL);
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
