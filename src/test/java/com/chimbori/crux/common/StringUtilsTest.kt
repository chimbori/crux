package com.chimbori.crux.common

import com.chimbori.crux.common.StringUtils.cleanTitle
import com.chimbori.crux.common.StringUtils.countMatches
import com.chimbori.crux.common.StringUtils.encodingCleanup
import com.chimbori.crux.common.StringUtils.getLongestSubstring
import com.chimbori.crux.common.StringUtils.innerTrim
import com.chimbori.crux.common.StringUtils.makeAbsoluteUrl
import com.chimbori.crux.common.StringUtils.unescapeBackslashHex
import org.junit.Assert.assertEquals
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

  @Test
  fun testCleanTitle() {
    assertEquals("World stock markets surge amid confidence Clinton will win US election",
        cleanTitle("World stock markets surge amid confidence Clinton will win US election | Business | The Guardian"))
  }
}
