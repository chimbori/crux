package com.chimbori.crux.urls;

import com.chimbori.crux.common.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Checks heuristically whether a given URL is likely to be an article, video, image, or other
 * types. Can optionally resolve redirects such as when Facebook or Google show an interstitial
 * page instead of redirecting the user to the actual URL.
 */
@SuppressWarnings("WeakerAccess")
public class CandidateURL {
  private final String fileName;
  public URL url;

  /**
   * Defines a pattern used by a specific service for URL redirection. This should be stateless,
   * and will be called for each URL that needs to be resolved.
   */
  private static abstract class RedirectPattern {
    /**
     * @return true if this RedirectPattern can handle the provided URL, false if not.
     */
    public abstract boolean matches(URL url);

    /**
     * @return the actual URL that is pointed to by this redirector URL.
     */
    public abstract URL resolve(URL url) throws MalformedURLException;

    /**
     * To avoid every implementing class wrap their own code with a try-catch, they are encouraged
     * to use the resolve() method which throws {@link MalformedURLException}, while callers can
     * use this method which simply wraps that method’s return value in a try-catch.
     */
    public URL resolveHandlingException(URL url) {
      try {
        return resolve(url);
      } catch (MalformedURLException e) {
        return url;
      }
    }
  }

  private static RedirectPattern[] REDIRECT_PATTERNS = {
      new RedirectPattern() {  // Facebook.
        @Override
        public boolean matches(URL url) {
          return url.getHost().endsWith(".facebook.com") && url.getPath().equals("/l.php");
        }

        @Override
        public URL resolve(URL url) throws MalformedURLException {
          return new URL(StringUtils.getQueryParameters(url).get("u"));
        }
      },
      new RedirectPattern() {  // Google.
        @Override
        public boolean matches(URL url) {
          return url.getHost().endsWith(".google.com") && url.getPath().equals("/url");
        }

        @Override
        public URL resolve(URL url) throws MalformedURLException {
          return new URL(StringUtils.getQueryParameters(url).get("q"));
        }
      }
  };

  @SuppressWarnings("unused")
  public CandidateURL(String candidateUrl) {
    if (candidateUrl == null || candidateUrl.isEmpty()) {
      throw new IllegalArgumentException(String.format("URL: [%s]", candidateUrl));
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
      throw new IllegalArgumentException(String.format("Unable to parse URL: [%s]", candidateUrl));
    }

    fileName = url.getFile();
  }

  public boolean isAdImage() {
    return StringUtils.countMatches(url.toString(), "ad") >= 2;
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

  public CandidateURL resolveRedirects() {
    for (RedirectPattern redirect : REDIRECT_PATTERNS) {
      if (redirect.matches(url)) {
        url = redirect.resolveHandlingException(url);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return url.toString();
  }
}
