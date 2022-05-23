package com.chimbori.crux.plugins

import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.extractors.PostprocessHelpers
import com.chimbori.crux.extractors.PreprocessHelpers
import com.chimbori.crux.extractors.getNodes
import com.chimbori.crux.extractors.getWeight
import com.chimbori.crux.urls.isLikelyArticle
import okhttp3.HttpUrl
import org.jsoup.nodes.Element

public class ArticleExtractorPlugin : Plugin {
  override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource? {
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

    return Resource(document = PostprocessHelpers.postprocess(bestMatchElement))
  }
}
