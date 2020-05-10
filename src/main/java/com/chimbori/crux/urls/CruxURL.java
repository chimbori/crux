package com.chimbori.crux.urls;

import com.chimbori.crux.common.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Checks heuristically whether a given URL is likely to be an article, video, image, or other
 * types. Can optionally resolve redirects such as when Facebook or Google show an interstitial
 * page instead of redirecting the user to the actual URL.
 */
public class CruxURL {
  private final String fileName;
  private URI uri;

  /**
   * Validate, initialize, and create a new {@link CruxURL}, without invoking a lenient URL parser.
   */
  public static CruxURL parseStrict(String url) {
    return parseInternal(url, true);
  }

  /**
   * Validate, initialize, and create a new {@link CruxURL}, invoking a lenient URL parser, since
   * Java’s java.net.URI is a stricter parser than real-world usage requires. Many characters used
   * in valid URLs are rejected by it.
   */
  public static CruxURL parse(String url) {
    return parseInternal(url, false);
  }

  private static CruxURL parseInternal(String url, boolean isStrict) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    URI javaNetUri = null;
    try {
      javaNetUri = new URI(url);
    } catch (URISyntaxException e) {
      // Java’s java.net.URI is a stricter parser than real-world usage requires. Many characters
      // used in valid URLs are rejected by it. So, if we encounter a URISyntaxException here, it
      // doesn’t necessarily mean the URL is invalid. Instead of giving up, we try a more lenient
      // parse.
      if (!isStrict) {
        // Code below is inspired by Android’s {@code toURILenient()}, and only the required bits
        // have been included in this project.
        try {
          javaNetUri = LenientURLParser.toURILenient(new URL(url));
        } catch (URISyntaxException | MalformedURLException e1) {
          // Ignore; we tried it parsing it in two ways, and we couldn’t do much, so give up now.
        }
      }
    }

    if (javaNetUri != null &&
        (javaNetUri.getScheme() == null || javaNetUri.getScheme().isEmpty())) {
      try {
        url = "http://" + url;
        javaNetUri = new URI(url);
      } catch (URISyntaxException e) {
        // Ignore.
      }
    }

    if (javaNetUri == null) {
      return null;
    }

    return new CruxURL(javaNetUri);
  }

  /**
   * Private constructor, so that the wrapping static method can perform some validation before
   * invoking the constructor.
   */
  private CruxURL(URI uri) {
    this.uri = uri;
    String path = uri.getPath();
    fileName = path != null && !path.isEmpty()
        ? path.substring(path.lastIndexOf('/') + 1)
        : "";
  }

  public boolean isAdImage() {
    return StringUtils.countMatches(uri.toString(), "ad") >= 2;
  }

  public boolean isWebScheme() {
    String scheme = uri.getScheme().toLowerCase();
    return scheme.equals("http") || scheme.equals("https");
  }

  public boolean isLikelyArticle() {
    return !isLikelyBinaryDocument() &&
        !isLikelyExecutable() &&
        !isLikelyArchive() &&
        !isLikelyImage() &&
        !isLikelyVideo() &&
        !isLikelyAudio();
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyVideo() {
    return fileName.endsWith(".mpeg") || fileName.endsWith(".mpg") || fileName.endsWith(".avi") || fileName.endsWith(".mov")
        || fileName.endsWith(".mpg4") || fileName.endsWith(".mp4") || fileName.endsWith(".flv") || fileName.endsWith(".wmv");
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyAudio() {
    return fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".m3u") || fileName.endsWith(".wav");
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyBinaryDocument() {
    return fileName.endsWith(".pdf") || fileName.endsWith(".ppt") || fileName.endsWith(".doc")
        || fileName.endsWith(".swf") || fileName.endsWith(".rtf") || fileName.endsWith(".xls");
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyArchive() {
    return fileName.endsWith(".gz") || fileName.endsWith(".tgz") || fileName.endsWith(".zip")
        || fileName.endsWith(".rar") || fileName.endsWith(".deb") || fileName.endsWith(".rpm") || fileName.endsWith(".7z");
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyExecutable() {
    return fileName.endsWith(".exe") || fileName.endsWith(".bin") || fileName.endsWith(".bat") || fileName.endsWith(".dmg");
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLikelyImage() {
    return fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")
        || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".ico") || fileName.endsWith(".eps");
  }

  public CruxURL resolveRedirects() {
    for (Redirectors.RedirectPattern redirect : Redirectors.INSTANCE.getREDIRECT_PATTERNS()) {
      if (redirect.matches(uri)) {
        uri = redirect.resolve(uri);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return uri.toString();
  }
}
