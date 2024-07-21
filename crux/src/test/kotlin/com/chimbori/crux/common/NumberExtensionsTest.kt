package com.chimbori.crux.common

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberExtensionsTest {
  @Test
  fun testMillisecondsToMinutes() {
    assertEquals(0, 0.millisecondsToMinutes())
    assertEquals(1, 1.millisecondsToMinutes())
    assertEquals(10, (10 * 60 * 1000).millisecondsToMinutes())
    assertEquals(-1, (-1 * 60 * 1000).millisecondsToMinutes())
  }
}
