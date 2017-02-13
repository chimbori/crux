package com.chimbori.sample;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.Extractor;
import com.chimbori.crux.images.ImageUrlExtractor;
import com.chimbori.crux.urls.CandidateURL;

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
    CandidateURL candidateURL = new CandidateURL(url);
    if (candidateURL.isLikelyArticle()) {
      Article article = Extractor.with(candidateURL, content).extractMetadata().extractContent().article();
      assertEquals("Crux", article.title);
    }
    CandidateURL directURL = candidateURL.resolveRedirects();
    assertEquals("https://chimbori.com/", directURL.toString());
  }

  @Test
  public void testCallersCanAccessImageExtractorAPI() {
    String url = "https://chimbori.com/";
    String content = "<img src=\"test.jpg\">";  // Intentionally malformed.

    String imageUrl = ImageUrlExtractor.with(url, Jsoup.parse(content).body()).findImage().imageUrl();
    assertEquals("https://chimbori.com/test.jpg", imageUrl);
  }
}
