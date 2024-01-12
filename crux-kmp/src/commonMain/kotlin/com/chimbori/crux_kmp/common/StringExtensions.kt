package com.chimbori.crux_kmp.common

import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

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

internal fun String.countLetters() = count { it.isLetter() }

public fun String.nullIfBlank(): String? = ifBlank { null }

internal fun String.cleanTitle() = if (lastIndexOf("|") > length / 2) {
  substring(0, indexOf("|")).trim()
} else {
  removeWhiteSpace()
}

@OptIn(ExperimentalTime::class)
public fun String.estimatedReadingTimeMs(): Int {
  val wordCount = split("\\s+".toRegex()).size
  return ((wordCount * Duration.convert(1.0, DurationUnit.MINUTES, DurationUnit.MILLISECONDS)) / AVERAGE_WORDS_PER_MINUTE).toInt()
}

public fun String.estimatedReadingTimeMinutes(): Int {
  val wordCount = split("\\s+".toRegex()).size
  return ceil((wordCount / AVERAGE_WORDS_PER_MINUTE).toDouble()).toInt()
}

/** Number of words that can be read by an average person in one minute. */
internal const val AVERAGE_WORDS_PER_MINUTE = 275
