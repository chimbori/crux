package com.chimbori.crux.common

import kotlin.math.ceil
import kotlin.math.roundToInt

/** Cannot use [TimeUnit.MILLISECONDS.toMinutes()]; it rounds down, so anything under 1 min is reported as 0. */
public fun Int?.millisecondsToMinutes(): Int = this?.let { milliseconds ->
  ceil(milliseconds.toDouble() / 60_000).roundToInt()
} ?: 0
