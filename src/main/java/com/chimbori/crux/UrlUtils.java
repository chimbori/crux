package com.chimbori.crux;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class to work with URLs.
 */
class UrlUtils {
  private UrlUtils() {
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
    try {
      return new URL(new URL(baseUrl), relativeUrl).toString();
    } catch (MalformedURLException e) {
      return relativeUrl;
    }
  }
}
