package com.chimbori.crux.articles;

import org.junit.Test;

import static com.chimbori.crux.TestHelperKt.extractFromTestFile;
import static org.junit.Assert.assertEquals;

public class ArticleExtractorTest {
  private static final String EXAMPLE_URL = "http://example.com/";

  @Test
  public void testRetainSpaceInsideTags() {
    final String As =  "aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa";
    final String Bs =  "bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb";
    final String Cs =  "ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc";

    Article article = ArticleExtractor.with(EXAMPLE_URL, String.format("<html><body><div> %s <p> %s</p>%s </div></body></html>", As, Bs, Cs)).extractContent().article();
    assertEquals(3, article.document.childNodeSize());
    assertEquals(As, article.document.childNode(0).outerHtml().trim());
    assertEquals(String.format("<p> %s</p>", Bs), article.document.childNode(1).outerHtml().trim());
    assertEquals(Cs, article.document.childNode(2).outerHtml().trim());

    article = ArticleExtractor.with(EXAMPLE_URL, String.format("<html><body><div> %s <p>%s </p>%s</div></body></html>", As, Bs, Cs)).extractContent().article();
    assertEquals(3, article.document.childNodeSize());
    assertEquals(As, article.document.childNode(0).outerHtml().trim());
    assertEquals(String.format("<p>%s </p>", Bs), article.document.childNode(1).outerHtml().trim());
    assertEquals(Cs, article.document.childNode(2).outerHtml().trim());

    article = ArticleExtractor.with(EXAMPLE_URL, String.format("<html><body><div> %s <p> %s </p>%s</div></body></html>", As, Bs, Cs)).extractContent().article();
    assertEquals(3, article.document.childNodeSize());
    assertEquals(As, article.document.childNode(0).outerHtml().trim());
    assertEquals(String.format("<p> %s </p>", Bs), article.document.childNode(1).outerHtml().trim());
    assertEquals(Cs, article.document.childNode(2).outerHtml().trim());
  }

  @Test
  public void testThatHiddenTextIsNotExtracted() {
    Article article = ArticleExtractor.with(EXAMPLE_URL, "<div style=\"margin: 5px; display:none; padding: 5px;\">Hidden Text</div>\n" +
        "<div style=\"margin: 5px; display:block; padding: 5px;\">Visible Text that has to be longer than X characters so it’s not stripped out for being too short.</div>\n" +
        "<div>Default Text</div>").extractContent().article();
    assertEquals("Visible Text that has to be longer than X characters so it’s not stripped out for being too short.", article.document.text());
  }

  @Test
  public void testThatLongerTextIsPreferred() {
    Article article = ArticleExtractor.with(EXAMPLE_URL, "<div style=\"margin: 5px; display:none; padding: 5px;\">Hidden Text</div>\n" +
        "<div style=\"margin: 5px; display:block; padding: 5px;\">Visible Text that’s still longer than our minimum text size limits</div>\n" +
        "<div>Default Text but longer that’s still longer than our minimum text size limits</div>").extractContent().article();
    assertEquals("Default Text but longer that’s still longer than our minimum text size limits", article.document.text());
  }

  @Test
  public void testThatShortTextIsDiscarded() {
    Article article = ArticleExtractor.with(EXAMPLE_URL,
        "<div>"
        + "Default Text but longer that’s still longer than our minimum text size limits"
        + "<div>short text</div>"
        + "</div>").extractContent().article();
    assertEquals("Default Text but longer that’s still longer than our minimum text size limits", article.document.text());
  }

  @Test
  public void testThatImportantShortTextIsRetained() {
      Article article = ArticleExtractor.with(EXAMPLE_URL,
        "<div>"
        + "Default Text but longer that’s still longer than our minimum text size limits"
        + "<div crux-keep>short text</div>"
        + "</div>").extractContent().article();
      assertEquals("Default Text but longer that’s still longer than our minimum text size limits short text", article.document.text());
  }

  @Test
  public void testReadingTimeEstimates() {
    Article washingtonPostArticle = extractFromTestFile("https://www.washingtonpost.com/lifestyle/style/the-nearly-forgotten-story-of-the-black-women-who-helped-land-a-man-on-the-moon/2016/09/12/95f2d356-7504-11e6-8149-b8d05321db62_story.html", "washingtonpost.html");
    assertEquals(8, washingtonPostArticle.estimatedReadingTimeMinutes);

    Article galileoArticle = extractFromTestFile("https://en.wikipedia.org/wiki/Galileo_Galilei", "wikipedia_galileo.html");
    assertEquals(53, galileoArticle.estimatedReadingTimeMinutes);
  }
}
