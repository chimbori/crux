package com.chimbori.crux;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

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

  CandidateURL resolveRedirects() {
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

  public boolean isVideoHost() {
    String host = url.getHost();
    return host.endsWith("youtube.com") ||
        host.endsWith("video.yahoo.com") ||
        host.endsWith("vimeo.com") ||
        host.endsWith(".blip.tv");
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
      String arr[] = decodeUrl(url.getPath()).split("\\&");
      for (String str : arr) {
        if (str.startsWith("q=")) {
          try {
            url = new URL(str.substring("q=".length()));
          } catch (MalformedURLException e) {
            // Keep URL as is.
          }
        }
      }
    }
    return this;
  }

  private CandidateURL resolveFacebookRedirect() {
    if (url.getHost().endsWith(".facebook.com") && url.getPath().equals("/l.php")) {
      try {
        url = new URL(decodeUrl(url.getQuery().substring("u=".length())));
      } catch (MalformedURLException e) {
        // Keep URL as is.
      }
    }
    return this;
  }

  private static String decodeUrl(String str) {
    try {
      return URLDecoder.decode(str, UTF8);
    } catch (UnsupportedEncodingException ex) {
      return str;
    }
  }

  @Override
  public String toString() {
    return url.toString();
  }
}
