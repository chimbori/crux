package com.chimbori.crux.common

import com.chimbori.crux.common.CharsetConverter.readStream
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class CharsetConverterTest {
  @Test
  fun testDetermineEncoding() {
    assertEncodingEquals("shift_jis", "yomiuri.html")
    assertEncodingEquals("shift_jis", "yomiuri2.html")
    assertEncodingEquals("iso-8859-1", "spiegel.html")
    assertEncodingEquals("utf-8", "itunes.html")
    assertEncodingEquals("utf-8", "twitter.html")
    assertEncodingEquals("utf-8", "youtube.html")
    assertEncodingEquals("utf-8", "nyt.html")
    assertEncodingEquals("utf-8", "badenc.html")
    assertEncodingEquals("iso-8859-15", "br-online.html")
  }

  @Test
  fun testMaxBytesExceedingButGetTitleNevertheless() {
    val parsed = readStream(FileInputStream(File("test_data/bbc.html")))
    assertEquals("utf-8", parsed.encoding)
    assertEquals("Baby born on Mediterranean rescue ship - BBC News BBC News", Jsoup.parse(parsed.content).select("title").text())
  }

  private fun assertEncodingEquals(encoding: String, testFile: String) {
    try {
      assertEquals(encoding, readStream(FileInputStream(File("test_data/$testFile"))).encoding)
    } catch (e: FileNotFoundException) {
      fail(e.message)
    }
  }
}
