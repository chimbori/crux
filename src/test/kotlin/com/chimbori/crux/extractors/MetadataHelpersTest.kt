package com.chimbori.crux.extractors

import com.chimbori.crux.common.cleanTitle
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataHelpersTest {
  @Test
  fun testCleanTitle() {
    assertEquals("mytitle irgendwas", "mytitle irgendwas | Facebook".cleanTitle())
    assertEquals("mytitle irgendwas", "mytitle irgendwas | Irgendwas".cleanTitle())

    // This should fail as most sites do store their name after the post.
    assertEquals("Irgendwas | mytitle irgendwas", "Irgendwas | mytitle irgendwas".cleanTitle())
  }

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

  @Test
  fun testFindLargestIcon() {
    assertEquals(
      "/144.png",
      findLargestIcon(
        Jsoup.parse(
          """
          |<link rel="icon" sizes="57x57"   href="/57.png">
          |<link rel="icon" sizes="72x72"   href="/72.png">
          |<link rel="icon" sizes="114x114" href="/114.png">
          |<link rel="icon" sizes="144x144" href="/144.png">
          |<link rel="icon" href="/no-size.png">
        """.trimMargin(), "https://example.org/"
        ).select("link[rel~=icon]")
      )
    )
  }
}
