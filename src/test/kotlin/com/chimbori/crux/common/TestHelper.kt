package com.chimbori.crux.common

import com.chimbori.crux.api.Resource
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.junit.Assert.fail

internal fun fromFile(baseUrl: String, testFile: String, charset: String? = "UTF-8") =
  fromFile(baseUrl.toHttpUrl(), testFile, charset)

internal fun fromFile(baseUrl: HttpUrl, testFile: String, charset: String? = "UTF-8"): Article = try {
  ArticleExtractor(baseUrl, File("test_data/$testFile").readText(Charset.forName(charset)))
    .extractMetadata()
    .extractContent()
    .estimateReadingTime()
    .article
} catch (e: FileNotFoundException) {
  fail(e.message)
  throw e
}

internal fun Resource.Companion.fromTestData(url: HttpUrl, filename: String) = Resource(
  url = url,
  document = Jsoup.parse(File("test_data/$filename").readText(), url.toString())
)

internal fun assertStartsWith(expected: String, actual: String?) {
  if (actual?.startsWith(expected) == false) {
    fail("Expected \n[$expected]\n at start of \n[$actual]\n")
  }
}

internal fun assertContains(expected: String, actual: String?) {
  if (actual?.contains(expected) == false) {
    fail("Expected \n[$expected]\n in \n[$actual]\n")
  }
}
