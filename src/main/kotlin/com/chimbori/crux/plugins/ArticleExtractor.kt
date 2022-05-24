package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.DURATION_MS
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.estimatedReadingTimeMs
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.PostprocessHelpers
import com.chimbori.crux.extractors.PreprocessHelpers
import com.chimbori.crux.extractors.getNodes
import com.chimbori.crux.extractors.getWeight
import okhttp3.HttpUrl
import org.jsoup.nodes.Element

public class ArticleExtractor : Extractor {
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    request.document
      ?: return null

    PreprocessHelpers.preprocess(request.document)
    val nodes = request.document.getNodes()
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
      objects = mapOf(DURATION_MS to extractedDoc.estimatedReadingTimeMs()),
      article = extractedDoc
    )
  }
}
