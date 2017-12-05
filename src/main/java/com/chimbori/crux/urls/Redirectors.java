package com.chimbori.crux.urls;

import com.chimbori.crux.common.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

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
    public abstract boolean matches(URI uri);

    /**
     * @return the actual URL that is pointed to by this redirector URL.
     */
    public abstract URI resolve(URI uri) throws URISyntaxException;

    /**
     * To avoid every implementing class wrap their own code with a try-catch, they are encouraged
     * to use the resolve() method which throws {@link URISyntaxException}, while callers can
     * use this method which simply wraps that method’s return value in a try-catch.
     */
    URI resolveHandlingException(URI uri) {
      try {
        return resolve(uri);
      } catch (URISyntaxException e) {
        return uri;
      }
    }
  }

  static final RedirectPattern[] REDIRECT_PATTERNS = {
      new RedirectPattern() {  // Facebook.
        @Override
        public boolean matches(URI uri) {
          return uri.getHost() != null
              && uri.getHost().endsWith(".facebook.com")
              && "/l.php".equals(uri.getPath());
        }

        @Override
        public URI resolve(URI uri) throws URISyntaxException {
          return new URI(StringUtils.getQueryParameters(uri).get("u"));
        }
      },
      new RedirectPattern() {  // Google.
        @Override
        public boolean matches(URI uri) {
          return uri.getHost() != null
              && uri.getHost().endsWith(".google.com")
              && "/url".equals(uri.getPath());
        }

        @Override
        public URI resolve(URI uri) throws URISyntaxException {
          return new URI(StringUtils.getQueryParameters(uri).get("q"));
        }
      }
  };
}
