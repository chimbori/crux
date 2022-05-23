package com.chimbori.crux.plugins

import com.chimbori.crux.Resource
import com.chimbori.crux.common.assertStartsWith
import com.chimbori.crux.common.fromTestData
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleExtractorTest {
  @Test
  fun testCanParseArticleContent() {
    val wikipediaUrl = "https://en.wikipedia.org/wiki/Galileo_Galilei".toHttpUrl()
    val articleExtractor = ArticleExtractor()
    assertTrue(articleExtractor.canHandle(wikipediaUrl))

    runBlocking {
      articleExtractor.handle(Resource.fromTestData(wikipediaUrl, "wikipedia_galileo.html"))
    }?.let {
      val parsedArticle = it.document
      assertNotNull(parsedArticle)
      assertStartsWith(
        """"Galileo" redirects here. For other uses, see Galileo (disambiguation) and Galileo Galilei (disambiguation).""",
        parsedArticle?.text()
      )
    }
  }
}
