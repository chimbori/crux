package com.chimbori.snacktroid;

import junit.framework.TestCase;

import org.jsoup.Jsoup;

public class ConverterTest extends TestCase {

  public ConverterTest(String testName) {
    super(testName);
  }

  public void testDetermineEncoding() throws Exception {
    Converter d = new Converter();
    d.streamToString(getClass().getResourceAsStream("faz.html"));
    assertEquals("utf-8", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("yomiuri.html"));
    assertEquals("shift_jis", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("yomiuri2.html"));
    assertEquals("shift_jis", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("spiegel.html"));
    assertEquals("iso-8859-1", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("itunes.html"));
    assertEquals("utf-8", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("twitter.html"));
    assertEquals("utf-8", d.getEncoding());

    // youtube DOES not specify the encoding AND assumes utf-8 !?
    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("youtube.html"));
    assertEquals("utf-8", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("nyt.html"));
    assertEquals("utf-8", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("badenc.html"));
    assertEquals("utf-8", d.getEncoding());

    d = new Converter();
    d.streamToString(getClass().getResourceAsStream("br-online.html"));
    assertEquals("iso-8859-15", d.getEncoding());
  }

  public void testMaxBytesExceedingButGetTitleNevertheless() throws Exception {
    Converter d = new Converter();
    d.setMaxBytes(10000);
    String str = d.streamToString(getClass().getResourceAsStream("faz.html"));
    assertEquals("utf-8", d.getEncoding());
    assertEquals("Im Gespräch: Umweltaktivist Stewart Brand: Ihr Deutschen steht allein da "
        + "- Atomdebatte - FAZ.NET", Jsoup.parse(str).select("title").text());
  }
}
