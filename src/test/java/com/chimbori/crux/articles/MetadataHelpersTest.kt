package com.chimbori.crux.articles

import com.chimbori.crux.common.StringUtils.cleanTitle
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataHelpersTest {
  @Test
  fun testCleanTitle() {
    assertEquals("mytitle irgendwas", cleanTitle("mytitle irgendwas | Facebook"))
    assertEquals("mytitle irgendwas", cleanTitle("mytitle irgendwas | Irgendwas"))

    // This should fail as most sites do store their name after the post.
    assertEquals("Irgendwas | mytitle irgendwas", cleanTitle("Irgendwas | mytitle irgendwas"))
  }
}
