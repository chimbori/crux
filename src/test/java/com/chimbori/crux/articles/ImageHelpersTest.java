package com.chimbori.crux.articles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImageHelpersTest {
  @Test
  public void testParseSize() {
    assertEquals(0, ImageHelpers.parseSize(null));
    assertEquals(0, ImageHelpers.parseSize(""));
    assertEquals(0, ImageHelpers.parseSize(" "));
    assertEquals(0, ImageHelpers.parseSize("x"));
    assertEquals(0, ImageHelpers.parseSize("1"));
    assertEquals(128, ImageHelpers.parseSize("128x128"));
    assertEquals(128, ImageHelpers.parseSize("128x64"));
    assertEquals(256, ImageHelpers.parseSize("128x256"));
    assertEquals(128, ImageHelpers.parseSize("128X128"));
    assertEquals(0, ImageHelpers.parseSize("x 16"));
    assertEquals(48, ImageHelpers.parseSize("16x16 24x24 32x32 48x48"));
    assertEquals(128, ImageHelpers.parseSize("16x16 24x24 128x32 48x48"));
    assertEquals(48, ImageHelpers.parseSize("16x16 24x48"));
    assertEquals(16, ImageHelpers.parseSize("16x16 24"));
    assertEquals(0, ImageHelpers.parseSize("Some string with a 'x' in between"));
  }
}
