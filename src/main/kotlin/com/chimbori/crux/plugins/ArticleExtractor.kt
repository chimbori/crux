package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.DURATION_MS
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.estimatedReadingTimeMs
import com.chimbori.crux.common.fetchFromUrl
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.articles.PostprocessHelpers
import com.chimbori.crux.articles.PreprocessHelpers
import com.chimbori.crux.articles.getNodes
import com.chimbori.crux.articles.getWeight
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jsoup.nodes.Element

public class ArticleExtractor(private val okHttpClient: OkHttpClient) : Extractor {
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    val resourceToUse = if (request.document != null) {
      request
    } else if (request.url != null) {
      Resource.fetchFromUrl(request.url, okHttpClient)
    } else {
      Resource()
    }

    resourceToUse.document
      ?: return null

    PreprocessHelpers.preprocess(resourceToUse.document)
    val nodes = resourceToUse.document.getNodes()
    var maxWeight = 0
    var bestMatchElement: Element? = null
    for (element in nodes) {
      val currentWeight = element.getWeight()
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight
        bestMatchElement = element
        if (maxWeight > 200) {
          break
        }
      }
    }

    val extractedDoc = PostprocessHelpers.postprocess(bestMatchElement)
    return Resource(
      metadata = mapOf(DURATION_MS to extractedDoc.text().estimatedReadingTimeMs()),
      article = extractedDoc
    )
  }
}
