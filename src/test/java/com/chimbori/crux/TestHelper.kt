package com.chimbori.crux

import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import com.chimbori.crux.common.CharsetConverter
import com.chimbori.crux.common.Log
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

fun extractFromTestFile(baseUri: String?, testFile: String): Article? {
  return try {
    val article = ArticleExtractor.with(baseUri,
        CharsetConverter.readStream(FileInputStream(File("test_data/$testFile"))).content)
        .extractMetadata()
        .extractContent()
        .estimateReadingTime()
        .article()
    Log.i("%s", article.document.childNodes().toString())
    article
  } catch (e: FileNotFoundException) {
    Assert.fail(e.message)
    null
  }
}
