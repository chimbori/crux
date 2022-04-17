package com.chimbori.crux.common

import kotlin.math.ceil
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

internal fun Element.parseAttrAsInt(attr: String) = try {
  attr(attr).toInt()
} catch (e: NumberFormatException) {
  0
}

internal fun Elements.anyChildTagWithAttr(attribute: String): String? =
  firstOrNull { element -> element.attr(attribute).isNotBlank() }
    ?.attr(attribute)

internal fun Document.estimatedReadingTimeMinutes(): Int {
  // TODO: Consider handling badly-punctuated text such as missing spaces after periods.
  val wordCount = text().split("\\s+".toRegex()).size
  return ceil((wordCount / AVERAGE_WORDS_PER_MINUTE).toDouble()).toInt()
}

/** Number of words that can be read by an average person in one minute. */
private const val AVERAGE_WORDS_PER_MINUTE = 275
