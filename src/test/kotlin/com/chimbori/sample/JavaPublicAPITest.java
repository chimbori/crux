package com.chimbori.sample;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.images.ImageUrlExtractor;
import com.chimbori.crux.links.LinkUrlExtractor;
import com.chimbori.crux.urls.HttpUrlExtensionsKt;
import okhttp3.HttpUrl;
import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * {@code com.chimbori.crux} package, so this is a separate package.
 */
public class JavaPublicAPITest {
  @Test
  public void testCallersCanAccessArticleExtractorAPI() {
    HttpUrl url = HttpUrl.parse("https://chimbori.com/");
    String content = "<html><title>Crux";  // Intentionally malformed.

    assert url != null;
    if (HttpUrlExtensionsKt.isLikelyArticle(url)) {
      Article article = new ArticleExtractor(url, content).extractMetadata().extractContent().getArticle();
      assertEquals("Crux", article.getTitle());
    }
    HttpUrl directURL = HttpUrlExtensionsKt.resolveRedirects(url);
    assertEquals("https://chimbori.com/", directURL.toString());
  }

  @Test
  public void testCallersCanAccessImageExtractorAPI() {
    HttpUrl url = HttpUrl.parse("https://chimbori.com/");
    String content = "<img src=\"test.jpg\">";  // Intentionally malformed.

    assert url != null;
    HttpUrl imageUrl = new ImageUrlExtractor(url, Jsoup.parse(content).body()).findImage().getImageUrl();
    assertEquals(HttpUrl.parse("https://chimbori.com/test.jpg"), imageUrl);
  }

  @Test
  public void testCallersCanAccessLinkExtractorAPI() {
    HttpUrl url = HttpUrl.parse("https://chimbori.com/");
    String content = "<img href=\"/test\" src=\"test.jpg\">";  // Intentionally malformed.

    assert url != null;
    HttpUrl linkUrl = new LinkUrlExtractor(url, Jsoup.parse(content).body()).findLink().getLinkUrl();
    assertEquals(HttpUrl.parse("https://chimbori.com/test"), linkUrl);
  }
}
