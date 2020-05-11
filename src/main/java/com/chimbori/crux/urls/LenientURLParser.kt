package com.chimbori.crux.urls

import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

/**
 * Code adapted from Android’s Lenient URI Parser.
 *
 * Android added code directly to their implementation of java.net.URI and java.net.URL, which are both final classes.
 * This makes the Android implementations fundamentally incompatible with the standard JDK.
 *
 * Instead of this approach, we add side-car classes that provide the same functionality without altering the JDK API.
 */
internal object LenientURLParser {
  private const val UNRESERVED = "_-!.~\'()*"
  private const val PUNCTUATION = ",;:$&+="
  private val AUTHORITY_ENCODER: UriCodec = PartEncoder("@[]")

  /** for java.net.URL, which foolishly combines these two parts */
  private val FILE_AND_QUERY_ENCODER: UriCodec = PartEncoder("/@?")

  /** for query, fragment, and scheme-specific part */
  private val ALL_LEGAL_ENCODER: UriCodec = PartEncoder("?/[]@")

  /** Encodes this URL to the equivalent URI after escaping characters that are not permitted by URI. */
  @Throws(URISyntaxException::class)
  fun toURILenient(url: URL): URI {
    return URI(toExternalForm(url, true))
  }

  private fun toExternalForm(url: URL, escapeIllegalCharacters: Boolean): String {
    val result = StringBuilder()
    result.append(url.protocol)
    result.append(':')
    val authority = url.authority
    if (authority != null) {
      result.append("//")
      if (escapeIllegalCharacters) {
        AUTHORITY_ENCODER.appendPartiallyEncoded(result, authority)
      } else {
        result.append(authority)
      }
    }
    val fileAndQuery = url.file
    if (fileAndQuery != null) {
      if (escapeIllegalCharacters) {
        FILE_AND_QUERY_ENCODER.appendPartiallyEncoded(result, fileAndQuery)
      } else {
        result.append(fileAndQuery)
      }
    }
    val ref = url.ref
    if (ref != null) {
      result.append('#')
      if (escapeIllegalCharacters) {
        ALL_LEGAL_ENCODER.appendPartiallyEncoded(result, ref)
      } else {
        result.append(ref)
      }
    }
    return result.toString()
  }

  /**
   * Encodes the unescaped characters of `s` that are not permitted.
   * Permitted characters are:
   *  * Unreserved characters in [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt).
   *  * `extraOkayChars`,
   *  * non-ASCII, non-control, non-whitespace characters
   */
  private class PartEncoder internal constructor(private val extraLegalCharacters: String) : UriCodec() {
    override fun isRetained(c: Char) =
        UNRESERVED.indexOf(c) != -1 || PUNCTUATION.indexOf(c) != -1 || extraLegalCharacters.indexOf(c) != -1 ||
            c.toInt() > 127 && !Character.isSpaceChar(c) && !Character.isISOControl(c)
  }
}
