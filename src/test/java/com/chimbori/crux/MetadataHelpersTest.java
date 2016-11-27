package com.chimbori.crux;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataHelpersTest {
  @Test
  public void testCleanTitle() {
    assertEquals("mytitle irgendwas", MetadataHelpers.cleanTitle("mytitle irgendwas | Facebook"));
    assertEquals("mytitle irgendwas", MetadataHelpers.cleanTitle("mytitle irgendwas | Irgendwas"));

    // This should fail as most sites do store their name after the post.
    assertEquals("Irgendwas | mytitle irgendwas", MetadataHelpers.cleanTitle("Irgendwas | mytitle irgendwas"));
  }
}
