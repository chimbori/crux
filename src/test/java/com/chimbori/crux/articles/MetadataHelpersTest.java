package com.chimbori.crux.articles;

import com.chimbori.crux.common.StringUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataHelpersTest {
  @Test
  public void testCleanTitle() {
    assertEquals("mytitle irgendwas", StringUtils.cleanTitle("mytitle irgendwas | Facebook"));
    assertEquals("mytitle irgendwas", StringUtils.cleanTitle("mytitle irgendwas | Irgendwas"));

    // This should fail as most sites do store their name after the post.
    assertEquals("Irgendwas | mytitle irgendwas", StringUtils.cleanTitle("Irgendwas | mytitle irgendwas"));
  }
}
