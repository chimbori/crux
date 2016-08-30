package com.chimbori.snacktroid;

import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlFetcherIntegrationTest {

  @Test
  public void testNoException() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://www.tumblr.com/xeb22gs619", 10000, true);
//        System.out.println("tumblr:" + res.url);

//        res = new HtmlFetcher().fetchAndExtract("http://www.faz.net/-01s7fc", 10000, true);
//        System.out.println("faz:" + res.url);

    res = new HtmlFetcher().fetchAndExtract("http://www.google.com/url?sa=x&q=http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/&ct=ga&cad=caeqargbiaaoataaoabaltmh7qrialaawabibwrllurf&cd=d5glzns5m_4&usg=afqjcnetx___sph8sjwhjwi-_mmdnhilra&utm_source=twitterfeed&utm_medium=twitter", 10000, true);
    assertEquals("http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/", res.url);
//        System.out.println("google redirect:" + res.url);

    res = new HtmlFetcher().fetchAndExtract("http://bit.ly/gyFxfv", 10000, true);
    assertEquals("http://www.obiavi-bg.com/obiava_688245-6|260|262|/%D0%BF%D1%80%D0%BE%D0%BB%D0%B5%D1%82%D0%BD%D0%B0-%D0%BF%D1%80%D0%BE%D0%BC%D0%BE%D1%86%D0%B8%D1%8F-%D0%B4%D0%B0-%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%B8%D1%80%D0%B0%D0%BC%D0%B5-%D1%81-java.html?utm_source=twitterfeed&utm_medium=twitter",
        res.url);
  }

  @Test
  public void testWithTitle() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://www.midgetmanofsteel.com/2011/03/its-only-matter-of-time-before-fox-news.html", 10000, true);
    assertEquals("It's Only a Matter of Time Before Fox News Takes Out a Restraining Order", res.title);
    assertEquals("2011/03", res.dateString);
  }

  // do not support this uglyness
//    @Test
//    public void doubleRedirect() throws Exception {
//        ParsedResult res = new HtmlFetcher().fetchAndExtract("http://bit.ly/eZPI1c", 10000, true);
//        assertEquals("12 Minuten Battlefield 3 Gameplay - ohne Facebook-Bedingungen | Spaß und Spiele", res.title);
//    }
  // not available anymore
//    @Test
//    public void testTwitpicGzipDoesNOTwork() throws Exception {
//        ParsedResult res = new HtmlFetcher().fetchAndExtract("http://twitpic.com/4kuem8", 12000, true);
//        assertTrue(res.text, res.text.contains("*Not* what you want to see"));
//    }
  @Test
  public void testEncoding() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://www.yomiuri.co.jp/science/", 10000, true);
    assertEquals("科学・ＩＴニュース：読売新聞(YOMIURI ONLINE)", res.title);
  }

  //    @Test
  public void testHashbang() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://www.facebook.com/democracynow", 10000, true);
    assertTrue(res.title, res.title.startsWith("Democracy Now!"));

    // not available anymore
    //       res = new HtmlFetcher().fetchAndExtract("http://twitter.com/#!/th61/status/57141697720745984", 10000, true);
    //       assertTrue(res.title, res.title.startsWith("Twitter / TH61: “@AntiAtomPiraten:"));
  }

  public void testImage() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://grfx.cstv.com/schools/okla/graphics/auto/20110505_schedule.jpg", 10000, true);
    assertEquals("http://grfx.cstv.com/schools/okla/graphics/auto/20110505_schedule.jpg", res.imageUrl);
    assertTrue(res.title.isEmpty());
    assertTrue(res.text.isEmpty());
  }

  //    @Test
  public void testFurther() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("https://linksunten.indymedia.org/de/node/41619?utm_source=twitterfeed&utm_medium=twitter", 10000, true);
    assertTrue(res.text, res.text.startsWith("Es gibt kein ruhiges Hinterland! Schon wieder den "));
  }

  @Test
  public void testDoubleResolve() throws Exception {
    ParsedResult res = new HtmlFetcher().fetchAndExtract("http://t.co/eZRKcEYI", 10000, true);
    assertTrue(res.title, res.title.startsWith("GitHub - teleject/Responsive-Web-Design-Artboards"));
  }

  @Test
  public void testXml() throws Exception {
    String str = new HtmlFetcher().fetchAsString("https://karussell.wordpress.com/feed/", 10000);
    assertTrue(str, str.startsWith("<?xml version="));
  }
}
