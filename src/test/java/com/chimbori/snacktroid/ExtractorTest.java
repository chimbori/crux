package com.chimbori.snacktroid;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtractorTest {
  private Extractor extractor;

  @Before
  public void setup() {
    extractor = new Extractor();
  }

  @Test
  public void testCleanTitle() {
    String title = "Hacker News | Ask HN: Apart from Hacker News, what else you read?";
    assertEquals("Ask HN: Apart from Hacker News, what else you read?", ExtractionHelpers.cleanTitle(title));
    assertEquals("mytitle irgendwas", ExtractionHelpers.cleanTitle("mytitle irgendwas | Facebook"));
    assertEquals("mytitle irgendwas", ExtractionHelpers.cleanTitle("mytitle irgendwas | Irgendwas"));

    // this should fail as most sites do store their name after the post
    assertEquals("Irgendwas | mytitle irgendwas", ExtractionHelpers.cleanTitle("Irgendwas | mytitle irgendwas"));
  }

  @Test
  public void testRetainSpaceInsideTags() {
    Article article = extractor.extractContent("<html><body><div> aaa<a> bbb </a>ccc</div></body></html>");
    assertEquals(3, article.content.childNodeSize());
    assertEquals("aaa", article.content.childNode(0).outerHtml().trim());
    assertEquals("<a> bbb </a>", article.content.childNode(1).outerHtml().trim());
    assertEquals("ccc", article.content.childNode(2).outerHtml().trim());

    article = extractor.extractContent("<html><body><div> aaa <strong>bbb </strong>ccc</div></body></html>");
    assertEquals(3, article.content.childNodeSize());
    assertEquals("aaa", article.content.childNode(0).outerHtml().trim());
    assertEquals("<strong>bbb </strong>", article.content.childNode(1).outerHtml().trim());
    assertEquals("ccc", article.content.childNode(2).outerHtml().trim());

    article = extractor.extractContent("<html><body><div> aaa <strong> bbb </strong>ccc</div></body></html>");
    assertEquals(3, article.content.childNodeSize());
    assertEquals("aaa", article.content.childNode(0).outerHtml().trim());
    assertEquals("<strong> bbb </strong>", article.content.childNode(1).outerHtml().trim());
    assertEquals("ccc", article.content.childNode(2).outerHtml().trim());
  }

  @Test
  public void testThatHiddenTextIsNotExtracted() {
    Article article = extractor.extractContent("<div style=\"margin: 5px; display:none; padding: 5px;\">Hidden Text</div>\n" +
        "<div style=\"margin: 5px; display:block; padding: 5px;\">Visible Text</div>\n" +
        "<div>Default Text</div>");
    assertEquals("Visible Text", article.content.text());
  }

  @Test
  public void testThatLongerTextIsPreferred() {
    Article article = extractor.extractContent("<div style=\"margin: 5px; display:none; padding: 5px;\">Hidden Text</div>\n" +
        "<div style=\"margin: 5px; display:block; padding: 5px;\">Visible Text</div>\n" +
        "<div>Default Text But Longer</div>");
    assertEquals("Default Text But Longer", article.content.text());
  }
}
