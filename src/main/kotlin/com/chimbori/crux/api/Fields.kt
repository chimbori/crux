package com.chimbori.crux.api

/** Well-known keys to use in [Resource.fields] & [Resource.urls]. */
public object Fields {
  public const val TITLE: String = "title"
  public const val DESCRIPTION: String = "description"
  public const val SITE_NAME: String = "site-name"
  public const val LANGUAGE: String = "language"
  public const val DISPLAY: String = "display"
  public const val ORIENTATION: String = "orientation"
  public const val CREATED: String = "created"
  public const val MODIFIED: String = "modified"

  public const val THEME_COLOR_HEX: String = "theme-color-hex"
  public const val THEME_COLOR_HTML: String = "theme-color-html"  // Named colors like "aliceblue"
  public const val BACKGROUND_COLOR_HEX: String = "background-color-hex"
  public const val BACKGROUND_COLOR_HTML: String = "background-color-html"  // Named colors like "aliceblue"

  public const val CANONICAL_URL: String = "canonical-url"
  public const val AMP_URL: String = "amp-url"
  public const val FAVICON_URL: String = "favicon-url"
  public const val BANNER_IMAGE_URL: String = "banner-image-url"
  public const val FEED_URL: String = "feed-url"
  public const val VIDEO_URL: String = "video-url"
  public const val WEB_APP_MANIFEST_URL: String = "web-app-manifest-url"  // https://www.w3.org/TR/appmanifest/
  public const val NEXT_PAGE_URL: String = "next-page-url"
  public const val PREVIOUS_PAGE_URL: String = "previous-page-url"

  // For image or video resources only.
  public const val ALT_TEXT: String = "alt-text"
  public const val WIDTH_PX: String = "width-px"
  public const val HEIGHT_PX: String = "height-px"

  // For articles (estimated reading time) and audio/video content (playback duration).
  public const val DURATION_MS: String = "duration-ms"

  public const val TWITTER_HANDLE: String = "twitter-handle"
  public const val KEYWORDS_CSV: String = "keywords-csv"
}
