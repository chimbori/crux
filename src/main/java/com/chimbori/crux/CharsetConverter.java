package com.chimbori.crux;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * This class is not thread safe. Use one new instance every time due to encoding variable.
 */
@SuppressWarnings("WeakerAccess")
public class CharsetConverter {
  private final static String UTF8 = "UTF-8";
  private final static String ISO = "ISO-8859-1";
  private final static int K2 = 2048;
  private static final int DEFAULT_MAX_BYTES = 500 * 1024;

  public static class StringWithEncoding {
    public final String content;
    public final String encoding;

    public StringWithEncoding(String content, String encoding) {
      this.content = content;
      this.encoding = encoding;
    }
  }

  private CharsetConverter() {
  }

  public static String extractEncoding(String contentType) {
    String[] values;
    if (contentType != null)
      values = contentType.split(";");
    else
      values = new String[0];

    String charset = "";

    for (String value : values) {
      value = value.trim().toLowerCase();

      if (value.startsWith("charset="))
        charset = value.substring("charset=".length());
    }

    // http1.1 says ISO-8859-1 is the default charset
    if (charset.length() == 0)
      charset = ISO;

    return charset;
  }

  static StringWithEncoding readStream(InputStream inputStream, String encoding) {
    // HTTP 1.1 standard is iso-8859-1 not utf8 but we force utf-8 as YouTube assumes it.
    encoding = encoding == null || encoding.isEmpty() ? UTF8 : encoding.toLowerCase();

    BufferedInputStream in = null;
    try {
      in = new BufferedInputStream(inputStream, K2);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // detect encoding with the help of meta tag
      try {
        in.mark(K2 * 2);
        String tmpEnc = detectCharset("charset=", outputStream, in, encoding);
        if (tmpEnc != null)
          encoding = tmpEnc;
        else {
          Log.i("no charset found in first stage");
          // detect with the help of xml beginning ala encoding="charset"
          tmpEnc = detectCharset("encoding=", outputStream, in, encoding);
          if (tmpEnc != null) {
            encoding = tmpEnc;
          } else {
            Log.i("no charset found in second stage");
          }
        }

        if (!Charset.isSupported(encoding))
          throw new UnsupportedEncodingException(encoding);
      } catch (UnsupportedEncodingException e) {
        Log.i("Using default encoding:" + UTF8 + " problem:" + e.getMessage() + " encoding:" + encoding);
        encoding = UTF8;
      }

      // SocketException: Connection reset
      // IOException: missing CR    => problem on server (probably some xml character thing?)
      // IOException: Premature EOF => socket unexpectedly closed from server
      int bytesRead = outputStream.size();
      byte[] arr = new byte[K2];
      while (true) {
        if (bytesRead >= DEFAULT_MAX_BYTES) {
          Log.i("maxBytes " + DEFAULT_MAX_BYTES + " exceeded. HTML may be broken.");
          break;
        }

        int n = in.read(arr);
        if (n < 0)
          break;
        bytesRead += n;
        outputStream.write(arr, 0, n);
      }

      return new StringWithEncoding(outputStream.toString(encoding), encoding.toLowerCase());

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          // Ignore.
        }
      }
    }
    return null;
  }

  /**
   * This method detects the charset even if the first call only returns some
   * bytes. It will read until 4K bytes are reached and then try to determine
   * the encoding
   *
   * @throws IOException
   */
  private static String detectCharset(String key, ByteArrayOutputStream bos, BufferedInputStream in,
                               String enc) throws IOException {

    // Grab better encoding from stream
    byte[] arr = new byte[K2];
    int nSum = 0;
    while (nSum < K2) {
      int n = in.read(arr);
      if (n < 0)
        break;

      nSum += n;
      bos.write(arr, 0, n);
    }

    String str = bos.toString(enc);
    int encIndex = str.indexOf(key);
    int clength = key.length();
    if (encIndex > 0) {
      char startChar = str.charAt(encIndex + clength);
      int lastEncIndex;
      if (startChar == '\'')
        // if we have charset='something'
        lastEncIndex = str.indexOf("'", ++encIndex + clength);
      else if (startChar == '\"')
        // if we have charset="something"
        lastEncIndex = str.indexOf("\"", ++encIndex + clength);
      else {
        // if we have "text/html; charset=utf-8"
        int first = str.indexOf("\"", encIndex + clength);
        if (first < 0)
          first = Integer.MAX_VALUE;

        // or "text/html; charset=utf-8 "
        int sec = str.indexOf(" ", encIndex + clength);
        if (sec < 0)
          sec = Integer.MAX_VALUE;
        lastEncIndex = Math.min(first, sec);

        // or "text/html; charset=utf-8 '
        int third = str.indexOf("'", encIndex + clength);
        if (third > 0)
          lastEncIndex = Math.min(lastEncIndex, third);
      }

      // re-read byte array with different encoding
      // assume that the encoding string cannot be greater than 40 chars
      if (lastEncIndex > encIndex + clength && lastEncIndex < encIndex + clength + 40) {
        String tmpEnc = StringUtils.encodingCleanup(str.substring(encIndex + clength, lastEncIndex));
        try {
          in.reset();
          bos.reset();
          return tmpEnc;
        } catch (IOException e) {
          Log.i("Couldn't reset stream to re-read with new encoding %s %s %s", tmpEnc, e.toString(), e);
        }
      }
    }
    return null;
  }
}
