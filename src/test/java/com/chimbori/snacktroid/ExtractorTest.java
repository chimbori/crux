package com.chimbori.snacktroid;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
  public void testGaltimeWhereUrlContainsSpaces() {
    //String url = "http://galtime.com/article/entertainment/37/22938/kris-humphries-avoids-kim-talk-gma";
    ParsedResult article = getContentFromTestFile("galtime.com.html");
    assertEquals("http://vnetcdn.dtsph.com/files/vnet3/imagecache/opengraph_ogimage/story-images/Kris%20Humphries%20Top%20Bar.JPG", article.imageUrl);
  }

  @Test
  public void testRetainSpaceInsideTags() {
    ParsedResult res = extractor.extractContent("<html><body><div> aaa<a> bbb </a>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", res.text);

    res = extractor.extractContent("<html><body><div> aaa <strong>bbb </strong>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", res.text);

    res = extractor.extractContent("<html><body><div> aaa <strong> bbb </strong>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", res.text);
  }

  @Test
  public void testHideHiddenText() {
    ParsedResult res = getContentFromTestFile("no-hidden.html");
    assertEquals("This is the text which is shorter but visible", res.text);
  }

  @Test
  public void testShowOnlyNonHiddenText() {
    ParsedResult res = getContentFromTestFile("no-hidden2.html");
    assertEquals("This is the NONE-HIDDEN text which shouldn't be shown and it is a bit longer so normally prefered", res.text);
  }

  @Test
  public void testImagesList() {
    // http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803
    ParsedResult res = getContentFromTestFile("reuters.html");
    assertEquals(1, res.images.size());
    assertEquals(res.imageUrl, res.images.get(0).src);
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00",
        res.images.get(0).src);

    // http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/
    res = getContentFromTestFile("thevacationgals.html");
    assertEquals(3, res.images.size());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
        res.images.get(0).src);
    assertEquals("../wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg",
        res.images.get(1).src);
    assertEquals("http://www.linkwithin.com/pixel.png", res.images.get(2).src);
  }

  @Test
  public void testTextList() {
    ParsedResult res = getContentFromTestFile("npr.html");
    String text = res.text;
    List<String> textList = res.textList;
    assertEquals(23, textList.size());
    assertTrue(textList.get(0).startsWith(text.substring(0, 15)));
    assertTrue(textList.get(22).endsWith(text.substring(text.length() - 15, text.length())));
  }

  private ParsedResult getContentFromTestFile(String testFile) {
    try {
      return extractor.extractContent(CharsetConverter.readStream(new FileInputStream(new File("test_data/" + testFile)), null).content);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    return null;
  }
}
