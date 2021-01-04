package com.chimbori.crux.common

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
  fun testImageProtocolRelative() {
    val result = try {
      URL(URL("http://de.wikipedia.org/wiki/Griechenland"), "//upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png").toString()
    } catch (e: MalformedURLException) {
      "//upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png"
    }
    assertEquals("http://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Flag_of_Greece.svg/150px-Flag_of_Greece.svg.png", result)
  }

  @Test
  fun testCleanTitle() {
    assertEquals("World stock markets surge amid confidence Clinton will win US election",
        "World stock markets surge amid confidence Clinton will win US election | Business | The Guardian".cleanTitle())
  }
}
