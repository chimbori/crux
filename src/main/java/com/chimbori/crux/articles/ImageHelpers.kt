package com.chimbori.crux.articles

import com.chimbori.crux.articles.Article.Image.Companion.from
import com.chimbori.crux.common.Log
import com.chimbori.crux.common.StringUtils
import com.chimbori.crux.urls.CruxURL.Companion.parse
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

internal object ImageHelpers {
  fun findLargestIcon(iconNodes: Elements): String {
    var largestIcon: Element? = null
    var maxSize: Long = -1
    for (iconNode in iconNodes) {
      val size = parseSize(iconNode.attr("sizes"))
      if (size > maxSize) {
        maxSize = size
        largestIcon = iconNode
      }
    }
    return if (largestIcon != null) {
      StringUtils.urlEncodeSpaceCharacter(largestIcon.attr("href"))
    } else ""
  }

  /**
   * Given a size represented by "WidthxHeight" or "WidthxHeight ...", will return the largest dimension found.
   *
   * Examples: "128x128" will return 128.
   * "128x64" will return 64.
   * "24x24 48x48" will return 48.
   *
   * If a non supported input is given, will return 0.
   *
   * @param sizes String representing the sizes.
   * @return largest dimension.
   */
  fun parseSize(sizeString: String?): Long {
    if (sizeString == null || sizeString.trim { it <= ' ' }.isEmpty()) {
      return 0
    }

    val sizes = sizeString.trim { it <= ' ' }.toLowerCase()
    if (sizes.contains(" ")) { // Some sizes can be "16x16 24x24", so we split them with space and process each one.
      val multiSizes = sizes.split(" ").toTypedArray()
      var maxSize: Long = 0
      for (size in multiSizes) {
        val currentSize = parseSize(size)
        if (currentSize > maxSize) {
          maxSize = currentSize
        }
      }
      return maxSize
    } else if (sizes.contains("x")) { // For handling sizes of format 128x128 etc.
      val dimen = sizes.split("x").toTypedArray()
      if (dimen.size == 2) {
        return try {
          val width = dimen[0].trim { it <= ' ' }.toLong()
          val height = dimen[1].trim { it <= ' ' }.toLong()
          Math.max(width, height)
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
  fun extractImages(topNode: Element?): List<Article.Image> {
    val images: MutableList<Article.Image> = ArrayList()
    if (topNode == null) {
      return images
    }
    var imgElements = topNode.select("img")
    if (imgElements.isEmpty() && topNode.parent() != null) {
      imgElements = topNode.parent().select("img")
    }
    var maxWeight = 0
    var score = 1.0
    for (imgElement in imgElements) {
      val image = from(imgElement!!)
      if (image.src!!.isEmpty()) {
        continue
      }
      val cruxURL = parse(image.src)
      if (cruxURL != null && cruxURL.isAdImage) {
        continue
      }
      // cruxURL may be null if trying to pass a "data://" URI, which the Java URL parser can’t handle.
      image.weight += if (image.height >= 50) 20 else -20
      image.weight += if (image.width >= 50) 20 else -20
      image.weight += if (image.src!!.startsWith("data:")) -50 else 0
      image.weight += if (image.src!!.endsWith(".gif")) -20 else 0
      image.weight += if (image.src!!.endsWith(".jpg")) 5 else 0
      image.weight += if (image.alt!!.length > 35) 20 else 0
      image.weight += if (image.title!!.length > 35) 20 else 0
      image.weight += if (image.noFollow) -40 else 0
      image.weight = (image.weight * score).toInt()
      if (image.weight > maxWeight) {
        maxWeight = image.weight
        score = score / 2
      }
      images.add(image)
    }
    Collections.sort(images, ImageWeightComparator())
    Log.i("images: %s", images)
    return images
  }

  /**
   * Returns the highest-scored image first.
   */
  private class ImageWeightComparator : Comparator<Article.Image> {
    override fun compare(o1: Article.Image, o2: Article.Image): Int {
      return o2.weight - o1.weight
    }
  }
}
