package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.DURATION_MS
import com.chimbori.crux.Resource
import com.chimbori.crux.common.assertStartsWith
import com.chimbori.crux.common.fromTestData
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractorTest {
  @Test
  fun testCanParseArticleContent() {
    val wikipediaUrl = "https://en.wikipedia.org/wiki/Galileo_Galilei".toHttpUrl()
    val articleExtractor = ArticleExtractor()
    assertTrue(articleExtractor.canExtract(wikipediaUrl))

    runBlocking {
      articleExtractor.extract(Resource.fromTestData(wikipediaUrl, "wikipedia_galileo.html"))
    }?.let { parsed ->
      val readingTimeMinutes = (parsed.objects.get(DURATION_MS) as? Int)?.div(60_000)
      assertEquals(51, readingTimeMinutes)

      val extractedArticle = parsed.article
      assertNotNull(extractedArticle)
      assertStartsWith(
        """"Galileo" redirects here. For other uses, see Galileo (disambiguation) and Galileo Galilei (disambiguation).""",
        extractedArticle?.text()
      )
    }
  }
}
