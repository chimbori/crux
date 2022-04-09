package com.chimbori.crux

import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert

fun extractFromTestFile(baseUrl: String, testFile: String, charset: String? = "UTF-8"): Article? {
  return extractFromTestFile(baseUrl.toHttpUrlOrNull()!!, testFile, charset)
}

fun extractFromTestFile(baseUrl: HttpUrl, testFile: String, charset: String? = "UTF-8"): Article? {
  return try {
    val article = ArticleExtractor(
      baseUrl,
      File("test_data/$testFile").readText(charset = Charset.forName(charset))
    )
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
