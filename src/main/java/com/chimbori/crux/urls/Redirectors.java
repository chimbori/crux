package com.chimbori.crux.urls;

import com.chimbori.crux.common.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

class Redirectors {
  private Redirectors() {
  }

  /**
   * Defines a pattern used by a specific service for URL redirection. This should be stateless,
   * and will be called for each URL that needs to be resolved.
   */
  abstract static class RedirectPattern {
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
    URL resolveHandlingException(URL url) {
      try {
        return resolve(url);
      } catch (MalformedURLException e) {
        return url;
      }
    }
  }

  static RedirectPattern[] REDIRECT_PATTERNS = {
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
}
