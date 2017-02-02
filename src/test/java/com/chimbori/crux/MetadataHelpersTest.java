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

  @Test
  public void testParseSize() {
    assertEquals(0, MetadataHelpers.parseSize(null));
    assertEquals(0, MetadataHelpers.parseSize(""));
    assertEquals(0, MetadataHelpers.parseSize(" "));
    assertEquals(0, MetadataHelpers.parseSize("x"));
    assertEquals(0, MetadataHelpers.parseSize("1"));
    assertEquals(128, MetadataHelpers.parseSize("128x128"));
    assertEquals(128, MetadataHelpers.parseSize("128x64"));
    assertEquals(256, MetadataHelpers.parseSize("128x256"));
    assertEquals(128, MetadataHelpers.parseSize("128X128"));
    assertEquals(0, MetadataHelpers.parseSize("x 16"));
    assertEquals(48, MetadataHelpers.parseSize("16x16 24x24 32x32 48x48"));
    assertEquals(128, MetadataHelpers.parseSize("16x16 24x24 128x32 48x48"));
    assertEquals(48, MetadataHelpers.parseSize("16x16 24x48"));
    assertEquals(16, MetadataHelpers.parseSize("16x16 24"));
    assertEquals(0, MetadataHelpers.parseSize("Some string with a 'x' in between"));
  }
}
