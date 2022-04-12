package com.chimbori.crux

import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.fail

fun fromFile(baseUrl: String, testFile: String, charset: String? = "UTF-8") =
  fromFile(baseUrl.toHttpUrl(), testFile, charset)

fun fromFile(baseUrl: HttpUrl, testFile: String, charset: String? = "UTF-8"): Article = try {
  ArticleExtractor(baseUrl, File("test_data/$testFile").readText(Charset.forName(charset)))
    .extractMetadata()
    .extractContent()
    .estimateReadingTime()
    .article
} catch (e: FileNotFoundException) {
  fail(e.message)
  throw e
}
