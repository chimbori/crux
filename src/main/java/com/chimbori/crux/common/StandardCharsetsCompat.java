package com.chimbori.crux.common;

import java.nio.charset.Charset;

/**
 * {@code java.nio.charset.StandardCharsets} is only available on Android SDK &gt;= 19, so this is a
 * compatibility wrapper for the constants used from that class.
 *
 * @see <a href="https://developer.android.com/reference/java/nio/charset/StandardCharsets.html">StandardCharsets</a>
 */
public class StandardCharsetsCompat {
  /**
   * Eight-bit UCS Transformation Format
   */
  public static final Charset UTF_8 = Charset.forName("UTF-8");
}
