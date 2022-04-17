package com.chimbori.crux.extractors

import com.chimbori.crux.common.cleanTitle
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
}
