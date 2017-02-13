package com.chimbori.crux.urls;

import com.chimbori.crux.common.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("WeakerAccess")
public class CandidateURL {
  private static final String UTF8 = "UTF-8";
  private final String fileName;
  public URL url;

  @SuppressWarnings("unused")
  public CandidateURL(String candidateUrl) {
    if (candidateUrl == null || candidateUrl.isEmpty()) {
      throw new IllegalArgumentException();
    }

    try {
      url = new URL(candidateUrl);
    } catch (MalformedURLException e) {
      if (e.getMessage().startsWith("no protocol")) {
        try {
          url = new URL("http://" + candidateUrl);
        } catch (MalformedURLException e1) {
          // Uninitialized!
          e1.printStackTrace();
        }
      }
    }

    if (url == null) {
      throw new IllegalArgumentException(String.format("Unable to parse [%s]", candidateUrl));
    }

    fileName = url.getFile();
  }

  public boolean isAdImage() {
    return StringUtils.countMatches(url.toString(), "ad") >= 2;
  }

  public CandidateURL resolveRedirects() {
    return resolveGoogleRedirect().resolveFacebookRedirect();
  }

  public boolean isHttp() {
    String scheme = url.getProtocol().toLowerCase();
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

  private CandidateURL resolveGoogleRedirect() {
    if (url.getHost().endsWith(".google.com") && url.getPath().equals("/url")) {
      try {
        String actualURL = StringUtils.getQueryParameters(url).get("q");
        url = new URL(actualURL);
      } catch (MalformedURLException e) {
        // Keep URL as is.
      }
    }
    return this;
  }

  private CandidateURL resolveFacebookRedirect() {
    if (url.getHost().endsWith(".facebook.com") && url.getPath().equals("/l.php")) {
      try {
        String actualURL = StringUtils.getQueryParameters(url).get("u");
        url = new URL(actualURL);
      } catch (MalformedURLException e) {
        // Keep URL as is.
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return url.toString();
  }
}
