@file:Suppress("DEPRECATION")

package com.chimbori.crux.common

import org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern

object StringUtils {
  private val WHITESPACE = "[ \r\t\n]+".toRegex()
  private const val UTF8 = "UTF-8"

  fun urlEncodeSpaceCharacter(url: String?) =
      if (url.isNullOrEmpty()) {
        null
      } else {
        url.trim { it <= ' ' }.replace(WHITESPACE, "%20")
      }

  fun countMatches(str: String, substring: String): Int {
    var count = 0
    val indexOf = str.indexOf(substring)
    if (indexOf >= 0) {
      count++
      count += countMatches(str.substring(indexOf + substring.length), substring)
    }
    return count
  }

  /** Remove more than two spaces or newlines */
  fun innerTrim(str: String) = str.replace(WHITESPACE, " ").trim { it <= ' ' }

  /** Starts reading the encoding from the first valid character until an invalid encoding character occurs. */
  fun encodingCleanup(str: String): String {
    val sb = StringBuilder()
    var startedWithCorrectString = false
    for (i in 0 until str.length) {
      val c = str[i]
      if (Character.isDigit(c) || Character.isLetter(c) || c == '-' || c == '_') {
        startedWithCorrectString = true
        sb.append(c)
        continue
      }
      if (startedWithCorrectString) break
    }
    return sb.toString().trim { it <= ' ' }
  }

  /** @return the longest substring as str1.substring(result[0], result[1]); */
  fun getLongestSubstring(str1: String, str2: String?): String {
    val res = longestSubstring(str1, str2)
    return if (res == null || res[0] >= res[1]) "" else str1.substring(res[0], res[1])
  }

  private fun longestSubstring(str1: String?, str2: String?): IntArray? {
    if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty()) return null

    // dynamic programming => save already identical length into array
    // to understand this algo simply print identical length in every entry of the array
    // i+1, j+1 then reuses information from i,j
    // java initializes them already with 0
    val num = Array(str1.length) { IntArray(str2.length) }
    var maxlen = 0
    var lastSubstrBegin = 0
    var endIndex = 0
    for (i in 0 until str1.length) {
      for (j in 0 until str2.length) {
        if (str1[i] == str2[j]) {
          if (i == 0 || j == 0) num[i][j] = 1 else num[i][j] = 1 + num[i - 1][j - 1]
          if (num[i][j] > maxlen) {
            maxlen = num[i][j]
            // generate substring from str1 => i
            lastSubstrBegin = i - num[i][j] + 1
            endIndex = i + 1
          }
        }
      }
    }
    return intArrayOf(lastSubstrBegin, endIndex)
  }

  fun countLetters(str: String) = str.count { Character.isLetter(it) }

  fun parseAttrAsInt(element: Element, attr: String?) = try {
    element.attr(attr).toInt()
  } catch (e: NumberFormatException) {
    0
  }

  fun getQueryParameters(url: URI): Map<String, String> {
    val nameValuePairs: MutableMap<String, String> = LinkedHashMap()
    val queryParameters = url.query.split("&").toTypedArray()
    for (nameValuePair in queryParameters) {
      val equalsSignPosition = nameValuePair.indexOf("=")
      try {
        nameValuePairs[URLDecoder.decode(nameValuePair.substring(0, equalsSignPosition), UTF8)] = URLDecoder.decode(nameValuePair.substring(equalsSignPosition + 1), UTF8)
      } catch (e: UnsupportedEncodingException) {
        e.printStackTrace() // Quite unlikely to happen, so simplify the API by catching it here.
      }
    }
    return nameValuePairs
  }

  fun cleanTitle(title: String) = if (title.lastIndexOf("|") > title.length / 2) {
    title.substring(0, title.indexOf("|")).trim()
  } else {
    innerTrim(title)
  }

  fun anyChildTagWithAttr(elements: Elements, attribute: String?): String? {
    return elements.firstOrNull { element ->
      element.attr(attribute).isNotBlank()
    }?.attr(attribute).apply {
      unescapeHtml4(this)
    }
  }


  private val BACKSLASH_HEX_SPACE_PATTERN = Pattern.compile("\\\\([a-zA-Z0-9]+) ") // Space is included.

  /**
   * Unescapes backslash-hex escaped strings in URLs (typically found in URLs used in CSS).
   * The escaped pattern begins with a backslash and ends with a space. All characters between these
   * two delimiters must be valid hex digits.
   * E.g.
   * "\3a " becomes ":"
   * "\3d " becomes "="
   * "\26 " becomes "&amp;"
   */
  fun unescapeBackslashHex(input: String?): String? {
    var output = input
        ?: return null
    val matcher = BACKSLASH_HEX_SPACE_PATTERN.matcher(input)
    while (matcher.find()) {
      val backSlashHexSpace = matcher.group(0)
      val hexUnicode = matcher.group(1)
      val decimalUnicode = hexUnicode.trim { it <= ' ' }.toInt(16)
      val unicode = String(Character.toChars(decimalUnicode))
      output = output.replace(backSlashHexSpace, unicode)
    }
    return output
  }
}
