package com.chimbori.crux.plugins

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingParameterRemoverTest {
  @Test
  fun testThatParametersAreRemoved() {
    val trackingRemover = TrackingParameterRemover()
    mapOf(
      "https://example.org/" to null,
      "https://example.org?utm_source" to "https://example.org/",
      "https://www.example.com/?utm_source=summer-mailer&utm_medium=email&utm_campaign=summer-sale"
          to "https://www.example.com/",
      "http://www.example.com/?utm_source=newsletter1&utm_medium=email&utm_campaign=summer-sale&utm_content=toplink"
          to "http://www.example.com/",
      "https://www.example.com/?utm_source=tracker&non-tracking-parameter=dont-remove"
          to "https://www.example.com/?non-tracking-parameter=dont-remove",
    ).forEach { (key, value) ->
      assertEquals((value ?: key).toHttpUrl(), trackingRemover.rewrite(key.toHttpUrl()))
    }
  }
}
