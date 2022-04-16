package com.chimbori.crux.common

import java.lang.Character.isLetter

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

fun String.cleanTitle() = if (lastIndexOf("|") > length / 2) {
  substring(0, indexOf("|")).trim()
} else {
  removeWhiteSpace()
}
