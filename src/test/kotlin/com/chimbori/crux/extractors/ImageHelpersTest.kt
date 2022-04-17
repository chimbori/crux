package com.chimbori.crux.extractors

import com.chimbori.crux.articles.parseSize
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageHelpersTest {
  @Test
  fun testParseSize() {
    assertEquals(0, parseSize(null))
    assertEquals(0, parseSize(""))
    assertEquals(0, parseSize(" "))
    assertEquals(0, parseSize("x"))
    assertEquals(0, parseSize("1"))
    assertEquals(128, parseSize("128x128"))
    assertEquals(128, parseSize("128x64"))
    assertEquals(256, parseSize("128x256"))
    assertEquals(128, parseSize("128X128"))
    assertEquals(0, parseSize("x 16"))
    assertEquals(48, parseSize("16x16 24x24 32x32 48x48"))
    assertEquals(128, parseSize("16x16 24x24 128x32 48x48"))
    assertEquals(48, parseSize("16x16 24x48"))
    assertEquals(16, parseSize("16x16 24"))
    assertEquals(0, parseSize("Some string with a 'x' in between"))
  }
}
