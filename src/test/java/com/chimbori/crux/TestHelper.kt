package com.chimbori.crux

import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import org.junit.Assert
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset

fun extractFromTestFile(baseUri: String, testFile: String, charset: String? = "UTF-8"): Article? {
  return try {
    val article = ArticleExtractor(baseUri,
        File("test_data/$testFile").readText(charset = Charset.forName(charset)))
        .extractMetadata()
        .extractContent()
        .estimateReadingTime()
        .article
    // Log.i(article.document?.childNodes().toString())
    article
  } catch (e: FileNotFoundException) {
    Assert.fail(e.message)
    null
  }
}
