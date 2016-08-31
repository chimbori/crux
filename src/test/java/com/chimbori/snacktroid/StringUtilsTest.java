package com.chimbori.snacktroid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilsTest {
  @Test
  public void testInnerTrim() {
    assertEquals("", StringUtils.innerTrim("   "));
    assertEquals("t", StringUtils.innerTrim("  t "));
    assertEquals("t t t", StringUtils.innerTrim("t t t "));
    assertEquals("t t", StringUtils.innerTrim("t    \nt "));
    assertEquals("t peter", StringUtils.innerTrim("t  peter "));
    assertEquals("t t", StringUtils.innerTrim("t    \n     t "));
  }

  @Test
  public void testCount() {
    assertEquals(1, StringUtils.count("hi wie &test; gehts", "&test;"));
    assertEquals(1, StringUtils.count("&test;", "&test;"));
    assertEquals(2, StringUtils.count("&test;&test;", "&test;"));
    assertEquals(2, StringUtils.count("&test; &test;", "&test;"));
    assertEquals(3, StringUtils.count("&test; test; &test; plu &test;", "&test;"));
  }

  @Test
  public void longestSubstring() {
//        assertEquals(9, Extractor.longestSubstring("hi hello how are you?", "hello how"));
    assertEquals("hello how", StringUtils.getLongestSubstring("hi hello how are you?", "hello how"));
    assertEquals(" people if ", StringUtils.getLongestSubstring("x now if people if todo?", "I know people if you"));
    assertEquals("", StringUtils.getLongestSubstring("?", "people"));
    assertEquals("people", StringUtils.getLongestSubstring(" people ", "people"));
  }

  @Test
  public void testExctractHost() {
    assertEquals("techcrunch.com",
        StringUtils.extractHost("http://techcrunch.com/2010/08/13/gantto-takes-on-microsoft-project-with-web-based-project-management-application/"));
  }

  @Test
  public void testFavicon() {
    assertEquals("http://www.n24.de/news/../../../media/imageimport/images/content/favicon.ico",
        StringUtils.useDomainOfFirstArg4Second("http://www.n24.de/news/newsitem_6797232.html", "../../../media/imageimport/images/content/favicon.ico"));
    StringUtils.useDomainOfFirstArg4Second("http://www.n24.de/favicon.ico", "/favicon.ico");
    StringUtils.useDomainOfFirstArg4Second("http://www.n24.de/favicon.ico", "favicon.ico");
  }

  @Test
  public void testFaviconProtocolRelative() throws Exception {
    assertEquals("http://de.wikipedia.org/apple-touch-icon.png",
        StringUtils.useDomainOfFirstArg4Second("http://de.wikipedia.org/favicon", "//de.wikipedia.org/apple-touch-icon.png"));
  }

  @Test
  public void testImageProtocolRelative() throws Exception {
    assertEquals("http://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png",
        StringUtils.useDomainOfFirstArg4Second("http://de.wikipedia.org/wiki/Griechenland", "//upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png"));
  }

  @Test
  public void testEncodingCleanup() {
    assertEquals("utf-8", StringUtils.encodingCleanup("utf-8"));
    assertEquals("utf-8", StringUtils.encodingCleanup("utf-8\""));
    assertEquals("utf-8", StringUtils.encodingCleanup("utf-8'"));
    assertEquals("test-8", StringUtils.encodingCleanup(" test-8 &amp;"));
  }

  @Test
  public void testEstimateDate() {
    assertNull(StringUtils.estimateDate("http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketin"));
    assertEquals("2010/02/15", StringUtils.estimateDate("http://www.vogella.de/blog/2010/02/15/twitter-android/"));
    assertEquals("2010/02", StringUtils.estimateDate("http://www.vogella.de/blog/2010/02/twitter-android/12"));
    assertEquals("2009/11/05", StringUtils.estimateDate("http://cagataycivici.wordpress.com/2009/11/05/mobile-twitter-client-with-jsf/"));
    assertEquals("2009", StringUtils.estimateDate("http://cagataycivici.wordpress.com/2009/sf/12/1/"));
    assertEquals("2011/06", StringUtils.estimateDate("http://bdoughan.blogspot.com/2011/06/using-jaxbs-xmlaccessortype-to.html"));
    assertEquals("2011", StringUtils.estimateDate("http://bdoughan.blogspot.com/2011/13/using-jaxbs-xmlaccessortype-to.html"));
  }

  @Test
  public void testCompleteDate() {
    assertNull(StringUtils.completeDate(null));
    assertEquals("2001/01/01", StringUtils.completeDate("2001"));
    assertEquals("2001/11/01", StringUtils.completeDate("2001/11"));
    assertEquals("2001/11/02", StringUtils.completeDate("2001/11/02"));
  }
}
