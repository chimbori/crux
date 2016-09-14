package com.chimbori.crux;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataHelpersTest {
  @Test
  public void testCleanTitle() {
    String title = "Hacker News | Ask HN: Apart from Hacker News, what else you read?";
    assertEquals("Ask HN: Apart from Hacker News, what else you read?", MetadataHelpers.cleanTitle(title));
    assertEquals("mytitle irgendwas", MetadataHelpers.cleanTitle("mytitle irgendwas | Facebook"));
    assertEquals("mytitle irgendwas", MetadataHelpers.cleanTitle("mytitle irgendwas | Irgendwas"));

    // this should fail as most sites do store their name after the post
    assertEquals("Irgendwas | mytitle irgendwas", MetadataHelpers.cleanTitle("Irgendwas | mytitle irgendwas"));
  }


}
