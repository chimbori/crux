package com.chimbori.crux.api

import com.chimbori.crux.api.Fields.BANNER_IMAGE_URL
import com.chimbori.crux.api.Fields.CANONICAL_URL
import com.chimbori.crux.api.Fields.DESCRIPTION
import com.chimbori.crux.api.Fields.TITLE
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResourceTest {
  @Test
  fun testResourceMetadataApiExamples() {
    val resource = Resource(
      url = "https://chimbori.com/".toHttpUrl(),
      metadata = mapOf(
        TITLE to "Life, the Universe, and Everything",
        DESCRIPTION to "42",
        CANONICAL_URL to "https://chimbori.com/".toHttpUrl()
      )
    )
    assertEquals("Life, the Universe, and Everything", resource[TITLE])
    assertEquals("42", resource[DESCRIPTION])
    assertEquals("https://chimbori.com/".toHttpUrl(), resource[CANONICAL_URL])
    assertNull(resource[BANNER_IMAGE_URL])
  }
}
