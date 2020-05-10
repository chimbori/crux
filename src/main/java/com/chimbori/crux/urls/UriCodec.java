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

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Encodes and decodes “application/x-www-form-urlencoded” content.
 * <p>
 * Subclasses define “isRetained”, which decides which chars need to be escaped and which don’t.
 * Output is encoded as UTF-8 by default. I.e, each character (or surrogate pair) is converted to
 * its equivalent UTF-8 encoded byte sequence, which is then converted to it’s escaped form.
 * e.g a 4 byte sequence might look like” %c6%ef%e0%e8”
 */
public abstract class UriCodec {
  /**
   * Returns true iff. ‘c’ does not need to be escaped.
   * 'a’ - ‘z’ , ‘A’ - ‘Z’ and ‘0’ - ‘9’ are always considered valid (i.e, don’t need to be
   * escaped. This set is referred to as the ``whitelist''.
   */
  protected abstract boolean isRetained(char c);

  private static boolean isWhitelisted(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
  }

  private boolean isWhitelistedOrRetained(char c) {
    return isWhitelisted(c) || isRetained(c);
  }

  /**
   * Throw URISyntaxException if any of the characters in the range [start, end) are not valid
   * according to this codec.
   * - If a char is in the whitelist or retained, it is valid both escaped and unescaped.
   * - All escaped octets appearing in the input are structurally valid hex, i.e convertible to
   * decimals.
   * <p>
   * On success, the substring [start, end) is returned.
   * {@code name} is not used, except to generate debugging info.
   */
  public final String validate(String uri, int start, int end, String name)
      throws URISyntaxException {
    int i = start;
    while (i < end) {
      char c = uri.charAt(i++);
      if (isWhitelistedOrRetained(c)) {
        continue;
      }
      // c is either '%' or character not allowed in a uri.
      if (c != '%') {
        throw unexpectedCharacterException(uri, name, c, i - 1);
      }
      // Expect two characters representing a number in hex.
      for (int j = 0; j < 2; j++) {
        c = getNextCharacter(uri, i++, end, name);
        if (hexCharToValue(c) < 0) {
          throw unexpectedCharacterException(uri, name, c, i - 1);
        }
      }
    }
    return uri.substring(start, end);
  }

  /**
   * Interprets a char as hex digits, returning a number from -1 (invalid char) to 15 ('f').
   */
  private static int hexCharToValue(char c) {
    if ('0' <= c && c <= '9') {
      return c - '0';
    }
    if ('a' <= c && c <= 'f') {
      return 10 + c - 'a';
    }
    if ('A' <= c && c <= 'F') {
      return 10 + c - 'A';
    }
    return -1;
  }

  private static URISyntaxException unexpectedCharacterException(
      String uri, String name, char unexpected, int index) {
    String nameString = (name == null) ? "" : " in [" + name + "]";
    return new URISyntaxException(
        uri, "Unexpected character" + nameString + ": " + unexpected, index);
  }

  private static char getNextCharacter(String uri, int index, int end, String name)
      throws URISyntaxException {
    if (index >= end) {
      String nameString = (name == null) ? "" : " in [" + name + "]";
      throw new URISyntaxException(
          uri, "Unexpected end of string" + nameString, index);
    }
    return uri.charAt(index);
  }

  /**
   * Throws {@link URISyntaxException} if any character in {@code uri} is neither whitelisted nor
   * in {@code legal}.
   */
  public static void validateSimple(String uri, String legal) throws URISyntaxException {
    for (int i = 0; i < uri.length(); i++) {
      char c = uri.charAt(i);
      if (!isWhitelisted(c) && legal.indexOf(c) < 0) {
        throw unexpectedCharacterException(uri, null /* name */, c, i);
      }
    }
  }

  /**
   * Encodes the string {@code s} as per the rules of this encoder (see class level comment).
   *
   * @throws IllegalArgumentException if the encoder is unable to encode a sequence of bytes.
   */
  public final String encode(String s, Charset charset) {
    StringBuilder builder = new StringBuilder(s.length());
    appendEncoded(builder, s, charset, false);
    return builder.toString();
  }

  /**
   * Encodes the string {@code s} as per the rules of this encoder (see class level comment).
   * <p>
   * Encoded output is appended to {@code builder}. This uses the default output encoding (UTF-8).
   */
  public final void appendEncoded(StringBuilder builder, String s) {
    appendEncoded(builder, s, Charset.forName("UTF-8"), false);
  }

  /**
   * Encodes the string {@code s} as per the rules of this encoder (see class level comment).
   * <p>
   * Encoded output is appended to {@code builder}. This uses the default output encoding (UTF-8).
   * This method must produce partially encoded output. What this means is that if encoded octets
   * appear in the input string, they are passed through unmodified, instead of being double
   * escaped. Consider a decoder operating on the global whitelist dealing with a string
   * “foo%25bar”. With this method, the output will be “foo%25bar”, but with appendEncoded, it
   * will be double encoded into “foo%2525bar”.
   */
  public final void appendPartiallyEncoded(StringBuilder builder, String s) {
    appendEncoded(builder, s, Charset.forName("UTF-8"), true);
  }

  private void appendEncoded(
      StringBuilder builder, String s, Charset charset, boolean partiallyEncoded) {
    CharsetEncoder encoder = charset.newEncoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);
    CharBuffer cBuffer = CharBuffer.allocate(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '%' && partiallyEncoded) {
        // In case there are characters waiting to be encoded.
        flushEncodingCharBuffer(builder, encoder, cBuffer);
        builder.append('%');
        continue;
      }
      if (c == ' ' && isRetained(' ')) {
        flushEncodingCharBuffer(builder, encoder, cBuffer);
        builder.append('+');
        continue;
      }
      if (isWhitelistedOrRetained(c)) {
        flushEncodingCharBuffer(builder, encoder, cBuffer);
        builder.append(c);
        continue;
      }
      // Put the character in the queue for encoding.
      cBuffer.put(c);
    }
    flushEncodingCharBuffer(builder, encoder, cBuffer);
  }

  private static void flushEncodingCharBuffer(
      StringBuilder builder,
      CharsetEncoder encoder,
      CharBuffer cBuffer) {
    if (cBuffer.position() == 0) {
      return;
    }
    // We are reading from the buffer now.
    cBuffer.flip();
    ByteBuffer byteBuffer = ByteBuffer.allocate(
        cBuffer.remaining() * (int) Math.ceil(encoder.maxBytesPerChar()));
    byteBuffer.position(0);
    CoderResult result = encoder.encode(cBuffer, byteBuffer, true /* endOfInput */);
    // According to the {@code CharsetEncoder#encode} spec, the method returns underflow
    // and leaves an empty output when all bytes were processed correctly.
    if (result != CoderResult.UNDERFLOW) {
      throw new IllegalArgumentException(
          "Error encoding, unexpected result ["
              + result.toString()
              + "] using encoder for ["
              + encoder.charset().name()
              + "]");
    }
    if (cBuffer.hasRemaining()) {
      throw new IllegalArgumentException(
          "Encoder for [" + encoder.charset().name() + "] failed with underflow with "
              + "remaining input [" + cBuffer + "]");
    }
    // Need to flush in case the encoder saves internal state.
    encoder.flush(byteBuffer);
    if (result != CoderResult.UNDERFLOW) {
      throw new IllegalArgumentException(
          "Error encoding, unexpected result ["
              + result.toString()
              + "] flushing encoder for ["
              + encoder.charset().name()
              + "]");
    }
    encoder.reset();
    byteBuffer.flip();
    // Write the encoded bytes.
    while (byteBuffer.hasRemaining()) {
      byte b = byteBuffer.get();
      builder.append('%');
      builder.append(intToHexDigit((b & 0xf0) >>> 4));
      builder.append(intToHexDigit(b & 0x0f));
    }
    // Use the character buffer to write again.
    cBuffer.flip();
    cBuffer.limit(cBuffer.capacity());
  }

  private static char intToHexDigit(int b) {
    if (b < 10) {
      return (char) ('0' + b);
    } else {
      return (char) ('A' + b - 10);
    }
  }

  /**
   * Decode a string according to the rules of this decoder.
   * <p>
   * - if {@code convertPlus == true} all ‘+’ chars in the decoded output are converted to ‘ ‘
   * (white space)
   * - if {@code throwOnFailure == true}, an {@link IllegalArgumentException} is thrown for
   * invalid inputs. Else, U+FFFd is emitted to the output in place of invalid input octets.
   */
  public static String decode(
      String s, boolean convertPlus, Charset charset, boolean throwOnFailure) {
    StringBuilder builder = new StringBuilder(s.length());
    appendDecoded(builder, s, convertPlus, charset, throwOnFailure);
    return builder.toString();
  }

  /**
   * Character to be output when there's an error decoding an input.
   */
  private static final char INVALID_INPUT_CHARACTER = '\ufffd';

  private static void appendDecoded(
      StringBuilder builder,
      String s,
      boolean convertPlus,
      Charset charset,
      boolean throwOnFailure) {
    CharsetDecoder decoder = charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .replaceWith("\ufffd")
        .onUnmappableCharacter(CodingErrorAction.REPORT);
    // Holds the bytes corresponding to the escaped chars being read (empty if the last char
    // wasn't a escaped char).
    ByteBuffer byteBuffer = ByteBuffer.allocate(s.length());
    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      i++;
      switch (c) {
        case '+':
          flushDecodingByteAccumulator(
              builder, decoder, byteBuffer, throwOnFailure);
          builder.append(convertPlus ? ' ' : '+');
          break;
        case '%':
          // Expect two characters representing a number in hex.
          byte hexValue = 0;
          for (int j = 0; j < 2; j++) {
            try {
              c = getNextCharacter(s, i, s.length(), null /* name */);
            } catch (URISyntaxException e) {
              // Unexpected end of input.
              if (throwOnFailure) {
                throw new IllegalArgumentException(e);
              } else {
                flushDecodingByteAccumulator(
                    builder, decoder, byteBuffer, throwOnFailure);
                builder.append(INVALID_INPUT_CHARACTER);
                return;
              }
            }
            i++;
            int newDigit = hexCharToValue(c);
            if (newDigit < 0) {
              if (throwOnFailure) {
                throw new IllegalArgumentException(
                    unexpectedCharacterException(s, null /* name */, c, i - 1));
              } else {
                flushDecodingByteAccumulator(
                    builder, decoder, byteBuffer, throwOnFailure);
                builder.append(INVALID_INPUT_CHARACTER);
                break;
              }
            }
            hexValue = (byte) (hexValue * 0x10 + newDigit);
          }
          byteBuffer.put(hexValue);
          break;
        default:
          flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
          builder.append(c);
      }
    }
    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
  }

  private static void flushDecodingByteAccumulator(
      StringBuilder builder,
      CharsetDecoder decoder,
      ByteBuffer byteBuffer,
      boolean throwOnFailure) {
    if (byteBuffer.position() == 0) {
      return;
    }
    byteBuffer.flip();
    try {
      builder.append(decoder.decode(byteBuffer));
    } catch (CharacterCodingException e) {
      if (throwOnFailure) {
        throw new IllegalArgumentException(e);
      } else {
        builder.append(INVALID_INPUT_CHARACTER);
      }
    } finally {
      // Use the byte buffer to write again.
      byteBuffer.flip();
      byteBuffer.limit(byteBuffer.capacity());
    }
  }

  /**
   * Equivalent to {@code decode(s, false, UTF_8, true)}
   */
  public static String decode(String s) {
    return decode(
        s, false /* convertPlus */, Charset.forName("UTF-8"), true /* throwOnFailure */);
  }
}
