package com.chimbori.crux.common

import com.chimbori.crux.common.StringUtils.cleanTitle
import com.chimbori.crux.common.StringUtils.encodingCleanup
import com.chimbori.crux.common.StringUtils.getLongestSubstring
import com.chimbori.crux.common.StringUtils.unescapeBackslashHex
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

class StringUtilsTest {
  @Test
  fun testInnerTrim() {
    assertEquals("", "   ".removeWhiteSpace())
    assertEquals("t", "  t ".removeWhiteSpace())
    assertEquals("t t t", "t t t ".removeWhiteSpace())
    assertEquals("t t", "t    \nt ".removeWhiteSpace())
    assertEquals("t peter", "t  peter ".removeWhiteSpace())
    assertEquals("t t", "t    \n     t ".removeWhiteSpace())
  }

  @Test
  fun testCount() {
    assertEquals(1, "hi wie &test; gehts".countMatches("&test;"))
    assertEquals(1, "&test;".countMatches("&test;"))
    assertEquals(2, "&test;&test;".countMatches("&test;"))
    assertEquals(2, "&test; &test;".countMatches("&test;"))
    assertEquals(3, "&test; test; &test; plu &test;".countMatches("&test;"))
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
