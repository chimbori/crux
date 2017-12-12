package com.chimbori.crux.urls;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Code adapted from Android’s Lenient URI Parser.
 *
 * Android added code directly to their implementation of java.net.URI and java.net.URL, which are
 * both final classes. This makes the Android implementations fundamentally incompatible with the
 * standard JDK.
 *
 * Instead of this approach, we add side-car classes that provide the same functionality without
 * altering the JDK API.
 */
class LenientURLParser {
  private static final String UNRESERVED = "_-!.~\'()*";
  private static final String PUNCTUATION = ",;:$&+=";

  private static final UriCodec AUTHORITY_ENCODER = new PartEncoder("@[]");
  /**
   * for java.net.URL, which foolishly combines these two parts
   */
  private static final UriCodec FILE_AND_QUERY_ENCODER = new PartEncoder("/@?");
  /**
   * for query, fragment, and scheme-specific part
   */
  private static final UriCodec ALL_LEGAL_ENCODER = new PartEncoder("?/[]@");

  /**
   * Encodes the unescaped characters of {@code s} that are not permitted.
   * Permitted characters are:
   * <ul>
   * <li>Unreserved characters in <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>.
   * <li>{@code extraOkayChars},
   * <li>non-ASCII, non-control, non-whitespace characters
   * </ul>
   */
  private static class PartEncoder extends UriCodec {
    private final String extraLegalCharacters;

    PartEncoder(String extraLegalCharacters) {
      this.extraLegalCharacters = extraLegalCharacters;
    }

    @Override
    protected boolean isRetained(char c) {
      return UNRESERVED.indexOf(c) != -1
          || PUNCTUATION.indexOf(c) != -1
          || extraLegalCharacters.indexOf(c) != -1
          || (c > 127 && !Character.isSpaceChar(c) && !Character.isISOControl(c));
    }
  }

  /**
   * Encodes this URL to the equivalent URI after escaping characters that are
   * not permitted by URI.
   */
  public static URI toURILenient(URL url) throws URISyntaxException {
    return new URI(toExternalForm(url, true));
  }

  private static String toExternalForm(URL url, boolean escapeIllegalCharacters) {
    StringBuilder result = new StringBuilder();
    result.append(url.getProtocol());
    result.append(':');
    String authority = url.getAuthority();
    if (authority != null) {
      result.append("//");
      if (escapeIllegalCharacters) {
        AUTHORITY_ENCODER.appendPartiallyEncoded(result, authority);
      } else {
        result.append(authority);
      }
    }
    String fileAndQuery = url.getFile();
    if (fileAndQuery != null) {
      if (escapeIllegalCharacters) {
        FILE_AND_QUERY_ENCODER.appendPartiallyEncoded(result, fileAndQuery);
      } else {
        result.append(fileAndQuery);
      }
    }
    String ref = url.getRef();
    if (ref != null) {
      result.append('#');
      if (escapeIllegalCharacters) {
        ALL_LEGAL_ENCODER.appendPartiallyEncoded(result, ref);
      } else {
        result.append(ref);
      }
    }
    return result.toString();
  }
}
