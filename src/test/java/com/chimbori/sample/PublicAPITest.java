package com.chimbori.sample;

import com.chimbori.crux.Article;
import com.chimbori.crux.CandidateURL;
import com.chimbori.crux.Extractor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that Crux classes have the proper visibility to be used outside of the
 * {@code com.chimbori.crux} package, so this is a separate package.
 */
public class PublicAPITest {
  @Test
  public void testCallersCanAccessPublicAPIMethods() {
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
}
