package com.chimbori.crux.common;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  private static final String WHITESPACE = "[ \r\t\n]+";

  private static final String UTF8 = "UTF-8";

  private StringUtils() {
    // Prevent instantiation.
  }

  public static String urlEncodeSpaceCharacter(String url) {
    return url.isEmpty() ? url : url.trim().replaceAll(WHITESPACE, "%20");
  }

  public static int countMatches(String str, String substring) {
    int count = 0;
    int indexOf = str.indexOf(substring);
    if (indexOf >= 0) {
      count++;
      count += countMatches(str.substring(indexOf + substring.length()), substring);
    }
    return count;
  }

  /**
   * remove more than two spaces or newlines
   */
  public static String innerTrim(String str) {
    return str.replaceAll(WHITESPACE, " ").trim();
  }

  /**
   * Starts reading the encoding from the first valid character until an
   * invalid encoding character occurs.
   */
  static String encodingCleanup(String str) {
    StringBuilder sb = new StringBuilder();
    boolean startedWithCorrectString = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isDigit(c) || Character.isLetter(c) || c == '-' || c == '_') {
        startedWithCorrectString = true;
        sb.append(c);
        continue;
      }

      if (startedWithCorrectString)
        break;
    }
    return sb.toString().trim();
  }

  /**
   * @return the longest substring as str1.substring(result[0], result[1]);
   */
  static String getLongestSubstring(String str1, String str2) {
    int res[] = longestSubstring(str1, str2);
    if (res == null || res[0] >= res[1])
      return "";

    return str1.substring(res[0], res[1]);
  }

  private static int[] longestSubstring(String str1, String str2) {
    if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty())
      return null;

    // dynamic programming => save already identical length into array
    // to understand this algo simply print identical length in every entry of the array
    // i+1, j+1 then reuses information from i,j
    // java initializes them already with 0
    int[][] num = new int[str1.length()][str2.length()];
    int maxlen = 0;
    int lastSubstrBegin = 0;
    int endIndex = 0;
    for (int i = 0; i < str1.length(); i++) {
      for (int j = 0; j < str2.length(); j++) {
        if (str1.charAt(i) == str2.charAt(j)) {
          if ((i == 0) || (j == 0))
            num[i][j] = 1;
          else
            num[i][j] = 1 + num[i - 1][j - 1];

          if (num[i][j] > maxlen) {
            maxlen = num[i][j];
            // generate substring from str1 => i
            lastSubstrBegin = i - num[i][j] + 1;
            endIndex = i + 1;
          }
        }
      }
    }
    return new int[]{lastSubstrBegin, endIndex};
  }

  static String estimateDate(String url) {
    int index = url.indexOf("://");
    if (index > 0)
      url = url.substring(index + 3);

    int year = -1;
    int yearCounter = -1;
    int month = -1;
    int monthCounter = -1;
    int day = -1;
    String strs[] = url.split("/");
    for (int counter = 0; counter < strs.length; counter++) {
      String str = strs[counter];
      if (str.length() == 4) {
        try {
          year = Integer.parseInt(str);
        } catch (Exception ex) {
          continue;
        }
        if (year < 1970 || year > 3000) {
          year = -1;
          continue;
        }
        yearCounter = counter;
      } else if (str.length() == 2) {
        if (monthCounter < 0 && counter == yearCounter + 1) {
          try {
            month = Integer.parseInt(str);
          } catch (Exception ex) {
            continue;
          }
          if (month < 1 || month > 12) {
            month = -1;
            continue;
          }
          monthCounter = counter;
        } else if (counter == monthCounter + 1) {
          try {
            day = Integer.parseInt(str);
          } catch (NumberFormatException ex) {
            // Ignore
          }
          if (day < 1 || day > 31) {
            day = -1;
            continue;
          }
          break;
        }
      }
    }

    if (year < 0)
      return null;

    StringBuilder str = new StringBuilder();
    str.append(year);
    if (month < 1)
      return str.toString();

    str.append('/');
    if (month < 10)
      str.append('0');
    str.append(month);
    if (day < 1)
      return str.toString();

    str.append('/');
    if (day < 10)
      str.append('0');
    str.append(day);
    return str.toString();
  }

  static String completeDate(String dateStr) {
    if (dateStr == null)
      return null;

    int index = dateStr.indexOf('/');
    if (index > 0) {
      index = dateStr.indexOf('/', index + 1);
      if (index > 0)
        return dateStr;
      else
        return dateStr + "/01";
    }
    return dateStr + "/01/01";
  }

  public static int countLetters(String str) {
    int len = str.length();
    int chars = 0;
    for (int i = 0; i < len; i++) {
      if (Character.isLetter(str.charAt(i))) {
        chars++;
      }
    }
    return chars;
  }

  public static int parseAttrAsInt(Element element, String attr) {
    try {
      return Integer.parseInt(element.attr(attr));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static Map<String, String> getQueryParameters(URI url) {
    Map<String, String> nameValuePairs = new LinkedHashMap<>();
    String[] queryParameters = url.getQuery().split("&");
    for (String nameValuePair : queryParameters) {
      int equalsSignPosition = nameValuePair.indexOf("=");
      try {
        nameValuePairs.put(
            URLDecoder.decode(nameValuePair.substring(0, equalsSignPosition), UTF8),
            URLDecoder.decode(nameValuePair.substring(equalsSignPosition + 1), UTF8));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();  // Quite unlikely to happen, so simplify the API by catching it here.
      }
    }
    return nameValuePairs;
  }

  public static String cleanTitle(String title) {
    StringBuilder res = new StringBuilder();
    int index = title.lastIndexOf("|");
    if (index > 0 && title.length() / 2 < index)
      title = title.substring(0, index + 1);

    int counter = 0;
    String[] strs = title.split("\\|");
    for (String part : strs) {
      if (counter == strs.length - 1 && res.length() > part.length()) {
        continue;
      }
      if (counter > 0) {
        res.append("|");
      }
      res.append(part);
      counter++;
    }
    return innerTrim(res.toString());
  }

  /**
   * Given a {@code baseUrl} and a {@code relativeUrl}, it creates a new fully-qualified URL
   * representing the {@code relativeUrl}. It is best-effort, and returns the original
   * {@code relativeUrl} if an absolute URL could not be determined. It guarantees that no
   * exceptions will be thrown.
   */
  public static String makeAbsoluteUrl(String baseUrl, String relativeUrl) {
    if (relativeUrl == null || relativeUrl.isEmpty()) {
      return null;
    }

    baseUrl = StringUtils.unescapeBackslashHex(baseUrl);
    relativeUrl = StringUtils.unescapeBackslashHex(relativeUrl);

    try {
      return new URL(new URL(baseUrl), relativeUrl).toString();
    } catch (MalformedURLException e) {
      return relativeUrl;
    }
  }

  public static String anyChildTagWithAttr(Elements elements, String attr) {
    for (Element element : elements) {
      String attrValue = element.attr(attr);
      if (attrValue == null || attrValue.isEmpty()) {
        continue;
      }

      return StringEscapeUtils.unescapeHtml4(attrValue);
    }
    return null;
  }

  private static final Pattern BACKSLASH_HEX_SPACE_PATTERN = Pattern.compile("\\\\([a-zA-Z0-9]+) "); // Space is included.

  /**
   * Unescapes backslash-hex escaped strings in URLs (typically found in URLs used in CSS).
   * The escaped pattern begins with a backslash and ends with a space. All characters between these
   * two delimiters must be valid hex digits.
   *
   * E.g.
   * "\3a " --> :
   * "\3d " --> =
   * "\26 " --> &
   */
  public static String unescapeBackslashHex(String input) {
    if (input == null) {
      return null;
    }
    String output = input;
    Matcher matcher = BACKSLASH_HEX_SPACE_PATTERN.matcher(input);
    while (matcher.find()) {
      String backSlashHexSpace = matcher.group(0);
      String hexUnicode = matcher.group(1);
      int decimalUnicode = Integer.parseInt(hexUnicode.trim(), 16);
      String unicode = new String(Character.toChars(decimalUnicode));
      output = output.replace(backSlashHexSpace, unicode);
    }
    return output;
  }
}
