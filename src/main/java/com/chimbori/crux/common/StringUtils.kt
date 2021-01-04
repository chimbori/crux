@file:Suppress("DEPRECATION")

package com.chimbori.crux.common

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.regex.Pattern

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
fun String.removeWhiteSpace() = replace(WHITESPACE, " ").trim { it <= ' ' }

private val WHITESPACE = "\\s+".toRegex()

object StringUtils {
  fun countLetters(str: String) = str.count { Character.isLetter(it) }

  fun parseAttrAsInt(element: Element, attr: String?) = try {
    element.attr(attr).toInt()
  } catch (e: NumberFormatException) {
    0
  }

  fun cleanTitle(title: String) = if (title.lastIndexOf("|") > title.length / 2) {
    title.substring(0, title.indexOf("|")).trim()
  } else {
    title.removeWhiteSpace()
  }

  fun anyChildTagWithAttr(elements: Elements, attribute: String?): String? {
    return elements
        .firstOrNull { element -> element.attr(attribute).isNotBlank() }
        ?.attr(attribute)
  }

  private val BACKSLASH_HEX_SPACE_PATTERN = Pattern.compile("\\\\([a-zA-Z0-9]+) ") // Space is included.
}
