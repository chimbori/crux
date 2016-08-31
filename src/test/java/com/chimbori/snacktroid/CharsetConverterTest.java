package com.chimbori.snacktroid;

import junit.framework.TestCase;

import org.jsoup.Jsoup;

public class CharsetConverterTest extends TestCase {

  public CharsetConverterTest(String testName) {
    super(testName);
  }

  public void testDetermineEncoding() throws Exception {
    assertEncodingEquals("utf-8", "faz.html");
    assertEncodingEquals("shift_jis", "yomiuri.html");
    assertEncodingEquals("shift_jis", "yomiuri2.html");
    assertEncodingEquals("iso-8859-1", "spiegel.html");
    assertEncodingEquals("utf-8", "itunes.html");
    assertEncodingEquals("utf-8", "twitter.html");
    assertEncodingEquals("utf-8", "youtube.html");
    assertEncodingEquals("utf-8", "nyt.html");
    assertEncodingEquals("utf-8", "badenc.html");
    assertEncodingEquals("iso-8859-15", "br-online.html");
  }

  private void assertEncodingEquals(String encoding, String testFile) {
    assertEquals(encoding, CharsetConverter.readStream(getClass().getResourceAsStream(testFile), null).encoding);
  }

  public void testMaxBytesExceedingButGetTitleNevertheless() throws Exception {
    CharsetConverter.StringWithEncoding parsed = CharsetConverter.readStream(
        getClass().getResourceAsStream("faz.html"), null);
    assertEquals("utf-8", parsed.encoding);
    assertEquals("Im Gespräch: Umweltaktivist Stewart Brand: Ihr Deutschen steht allein da "
        + "- Atomdebatte - FAZ.NET", Jsoup.parse(parsed.content).select("title").text());
  }
}
