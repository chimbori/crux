package com.chimbori.crux.articles

import com.chimbori.crux.common.Log
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.urls.isAdImage
import java.util.Locale
import kotlin.math.max
import okhttp3.HttpUrl
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

internal fun findLargestIcon(iconNodes: Elements): String? {
  var largestIcon: Element? = null
  var maxSize: Long = -1
  for (iconNode in iconNodes) {
    val size = parseSize(iconNode.attr("sizes"))
    if (size > maxSize) {
      maxSize = size
      largestIcon = iconNode
    }
  }
  return largestIcon?.attr("href")?.nullIfBlank()
}

/**
 * Given a size represented by "WidthxHeight" or "WidthxHeight ...", will return the largest dimension found.
 *
 * Examples: "128x128" will return 128.
 * "128x64" will return 64.
 * "24x24 48x48" will return 48.
 *
 * @param sizes String representing the sizes.
 * @return largest dimension, or 0 if input could not be parsed.
 */
internal fun parseSize(sizeString: String?): Long {
  if (sizeString == null || sizeString.trim { it <= ' ' }.isEmpty()) {
    return 0
  }

  val sizes = sizeString.trim { it <= ' ' }.lowercase(Locale.getDefault())
  if (sizes.contains(" ")) {
    // For multiple sizes in the same String, split it and parse recursively.
    return sizes.split(" ").map { parseSize(it) }.maxOrNull() ?: 0
  } else if (sizes.contains("x")) {
    // For handling sizes of format 128x128 etc.
    val dimen = sizes.split("x")
    if (dimen.size == 2) {
      return try {
        val width = dimen[0].trim { it <= ' ' }.toLong()
        val height = dimen[1].trim { it <= ' ' }.toLong()
        max(width, height)
      } catch (e: NumberFormatException) {
        0
      }
    }
  }
  return 0
}

/**
 * Extracts a set of images from the article content itself. This extraction must be run before
 * the postprocess step, because that step removes tags that are useful for image extraction.
 */
internal fun extractImages(baseUrl: HttpUrl, topNode: Element?): List<Article.Image> {
  val images = mutableListOf<Article.Image>()
  if (topNode == null) {
    return images
  }

  val imgElements: Elements = when {
    topNode.select("img").isEmpty() && topNode.parent() != null -> topNode.parent()!!.select("img")
    else -> topNode.select("img")
  }

  var maxWeight = 0
  var score = 1.0
  for (imgElement in imgElements) {
    val image = Article.Image.from(baseUrl, imgElement)
    if (image.srcUrl == null || image.srcUrl?.isAdImage() == true) {
      continue
    }
    image.weight += if (image.height >= 50) 20 else -20
    image.weight += if (image.width >= 50) 20 else -20
    image.weight += if (image.srcUrl?.scheme == "data") -50 else 0
    image.weight += if (image.srcUrl?.encodedPath?.endsWith(".gif") == true) -20 else 0
    image.weight += if (image.srcUrl?.encodedPath?.endsWith(".jpg") == true) 5 else 0
    image.weight += if ((image.alt?.length ?: 0) > 35) 20 else 0
    image.weight += if ((image.title?.length ?: 0) > 35) 20 else 0
    image.weight += if (image.noFollow) -40 else 0
    image.weight = (image.weight * score).toInt()
    if (image.weight > maxWeight) {
      maxWeight = image.weight
      score = score / 2
    }
    images.add(image)
  }
  Log.i("images: %s", images)
  return images.sortedByDescending { it.weight }
}
