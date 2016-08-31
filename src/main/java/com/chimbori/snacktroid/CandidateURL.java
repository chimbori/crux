package com.chimbori.snacktroid;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CandidateURL {
  private static final String UTF8 = "UTF-8";
  public String url;

  public CandidateURL(String url) {
    this.url = url;
  }

  CandidateURL resolveRedirects() {
    return removeHashbang().resolveGoogleRedirect().resolveFacebookRedirect();
  }

  public boolean isLikelyArticle() {
    return !isLikelyBinaryDocument() &&
        !isLikelyExecutable() &&
        !isLikelyArchive() &&
        !isLikelyImage() &&
        !isLikelyVideo() &&
        !isLikelyAudio();
  }

  public boolean isLikelyVideo() {
    return url.endsWith(".mpeg") || url.endsWith(".mpg") || url.endsWith(".avi") || url.endsWith(".mov")
        || url.endsWith(".mpg4") || url.endsWith(".mp4") || url.endsWith(".flv") || url.endsWith(".wmv");
  }

  public boolean isVideoHost() {
    url = StringUtils.extractDomain(url, true);
    return url.startsWith("youtube.com") || url.startsWith("video.yahoo.com")
        || url.startsWith("vimeo.com") || url.startsWith("blip.tv");
  }

  public boolean isLikelyAudio() {
    return url.endsWith(".mp3") || url.endsWith(".ogg") || url.endsWith(".m3u") || url.endsWith(".wav");
  }

  public boolean isLikelyBinaryDocument() {
    return url.endsWith(".pdf") || url.endsWith(".ppt") || url.endsWith(".doc")
        || url.endsWith(".swf") || url.endsWith(".rtf") || url.endsWith(".xls");
  }

  public boolean isLikelyArchive() {
    return url.endsWith(".gz") || url.endsWith(".tgz") || url.endsWith(".zip")
        || url.endsWith(".rar") || url.endsWith(".deb") || url.endsWith(".rpm") || url.endsWith(".7z");
  }

  public boolean isLikelyExecutable() {
    return url.endsWith(".exe") || url.endsWith(".bin") || url.endsWith(".bat") || url.endsWith(".dmg");
  }

  public boolean isLikelyImage() {
    return url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".gif")
        || url.endsWith(".jpg") || url.endsWith(".bmp") || url.endsWith(".ico") || url.endsWith(".eps");
  }

  private CandidateURL resolveGoogleRedirect() {
    if (url.startsWith("http://www.google.com/url?")) {
      url = url.substring("http://www.google.com/url?".length());
      String arr[] = decodeUrl(url).split("\\&");
      for (String str : arr) {
        if (str.startsWith("q=")) {
          url = str.substring("q=".length());
        }
      }
    }
    return this;
  }

  private CandidateURL resolveFacebookRedirect() {
    if (url.startsWith("http://www.facebook.com/l.php?u=")) {
      url = decodeUrl(url.substring("http://www.facebook.com/l.php?u=".length()));
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

  /**
   * Popular sites uses the #! to indicate the importance of the following
   * chars. Ugly but true. Such as: facebook, twitter, gizmodo, ...
   */
  CandidateURL removeHashbang() {
    url = url.replaceFirst("#!", "");
    return this;
  }

  @Override
  public String toString() {
    return url;
  }
}
