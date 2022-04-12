@file:Suppress("DEPRECATION")

package com.chimbori.crux.common

import java.lang.Character.isLetter
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun String.countMatches(substring: String): Int {
  var count = 0
  val indexOf = indexOf(substring)
  if (indexOf >= 0) {
    count++
    count += substring(indexOf + substring.length).countMatches(substring)
  }
  return count
}

/** Remove more than two spaces or newlines */
fun String.removeWhiteSpace() = replace("\\s+".toRegex(), " ").trim { it <= ' ' }

fun String.countLetters() = count { isLetter(it) }

fun String.nullIfBlank(): String? = ifBlank { null }

fun Element.parseAttrAsInt(attr: String) = try {
  attr(attr).toInt()
} catch (e: NumberFormatException) {
  0
}

fun String.cleanTitle() = if (lastIndexOf("|") > length / 2) {
  substring(0, indexOf("|")).trim()
} else {
  removeWhiteSpace()
}

fun Elements.anyChildTagWithAttr(attribute: String): String? =
  firstOrNull { element -> element.attr(attribute).isNotBlank() }
    ?.attr(attribute)
