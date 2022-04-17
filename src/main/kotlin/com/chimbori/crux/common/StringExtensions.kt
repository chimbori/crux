package com.chimbori.crux.common

import java.lang.Character.isLetter

internal fun String.countMatches(substring: String): Int {
  var count = 0
  val indexOf = indexOf(substring)
  if (indexOf >= 0) {
    count++
    count += substring(indexOf + substring.length).countMatches(substring)
  }
  return count
}

/** Remove more than two spaces or newlines */
internal fun String.removeWhiteSpace() = replace("\\s+".toRegex(), " ").trim { it <= ' ' }

internal fun String.countLetters() = count { isLetter(it) }

internal fun String.nullIfBlank(): String? = ifBlank { null }

internal fun String.cleanTitle() = if (lastIndexOf("|") > length / 2) {
  substring(0, indexOf("|")).trim()
} else {
  removeWhiteSpace()
}
