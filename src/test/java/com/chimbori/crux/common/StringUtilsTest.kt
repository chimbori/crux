package com.chimbori.crux.common

import com.chimbori.crux.common.StringUtils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

class StringUtilsTest {
  @Test
  fun testInnerTrim() {
    assertEquals("", innerTrim("   "))
    assertEquals("t", innerTrim("  t "))
    assertEquals("t t t", innerTrim("t t t "))
    assertEquals("t t", innerTrim("t    \nt "))
    assertEquals("t peter", innerTrim("t  peter "))
    assertEquals("t t", innerTrim("t    \n     t "))
  }

  @Test
  fun testCount() {
    assertEquals(1, countMatches("hi wie &test; gehts", "&test;").toLong())
    assertEquals(1, countMatches("&test;", "&test;").toLong())
    assertEquals(2, countMatches("&test;&test;", "&test;").toLong())
    assertEquals(2, countMatches("&test; &test;", "&test;").toLong())
    assertEquals(3, countMatches("&test; test; &test; plu &test;", "&test;").toLong())
  }

  @Test
  fun testLongestSubstring() {
    assertEquals("hello how", getLongestSubstring("hi hello how are you?", "hello how"))
    assertEquals(" people if ", getLongestSubstring("x now if people if todo?", "I know people if you"))
    assertEquals("", getLongestSubstring("?", "people"))
    assertEquals("people", getLongestSubstring(" people ", "people"))
  }

  @Test
  fun testImageProtocolRelative() {
    val result = try {
      URL(URL("http://de.wikipedia.org/wiki/Griechenland"), "//upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png").toString()
    } catch (e: MalformedURLException) {
      "//upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png"
    }
    assertEquals("http://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png", result)
  }

  @Test
  fun testEncodingCleanup() {
    assertEquals("utf-8", encodingCleanup("utf-8"))
    assertEquals("utf-8", encodingCleanup("utf-8\""))
    assertEquals("utf-8", encodingCleanup("utf-8'"))
    assertEquals("test-8", encodingCleanup(" test-8 &amp;"))
  }

  @Test
  fun testEstimateDate() {
    assertNull(estimateDate("http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketin"))
    assertEquals("2010/02/15", estimateDate("http://www.vogella.de/blog/2010/02/15/twitter-android/"))
    assertEquals("2010/02", estimateDate("http://www.vogella.de/blog/2010/02/twitter-android/12"))
    assertEquals("2009/11/05", estimateDate("http://cagataycivici.wordpress.com/2009/11/05/mobile-twitter-client-with-jsf/"))
    assertEquals("2009", estimateDate("http://cagataycivici.wordpress.com/2009/sf/12/1/"))
    assertEquals("2011/06", estimateDate("http://bdoughan.blogspot.com/2011/06/using-jaxbs-xmlaccessortype-to.html"))
    assertEquals("2011", estimateDate("http://bdoughan.blogspot.com/2011/13/using-jaxbs-xmlaccessortype-to.html"))
  }

  @Test
  fun testCompleteDate() {
    assertNull(completeDate(null))
    assertEquals("2001/01/01", completeDate("2001"))
    assertEquals("2001/11/01", completeDate("2001/11"))
    assertEquals("2001/11/02", completeDate("2001/11/02"))
  }

  @Test
  fun testMakeAbsoluteUrl() {
    assertEquals("http://example.com/test", makeAbsoluteUrl("http://example.com", "/test"))
    assertEquals("http://example.com/test", makeAbsoluteUrl("http\\3a //example.com", "/test"))
  }

  @Test
  fun testUnescapeBackslashHexUrl() {
    assertEquals(null, unescapeBackslashHex(null))
    assertEquals("", unescapeBackslashHex(""))
    assertEquals(":", unescapeBackslashHex("\\3a "))
    assertEquals("::", unescapeBackslashHex("\\3a \\3a "))
    assertEquals("=:", unescapeBackslashHex("\\3d \\3a "))
    assertEquals("https://scontent-sjc2-1.xx.fbcdn.net/v/t1.0-1/cp0/e15/q65/p120x120/00000000_00000000000000000_0000000000000000000_n.jpg?efg=aaaaaaaaaaaa&oh=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa&oe=abcdefgh",
        unescapeBackslashHex("https\\3a //scontent-sjc2-1.xx.fbcdn.net/v/t1.0-1/cp0/e15/q65/p120x120/00000000_00000000000000000_0000000000000000000_n.jpg?efg\\3d aaaaaaaaaaaa\\26 oh\\3d aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\\26 oe\\3d abcdefgh"))
  }
}
