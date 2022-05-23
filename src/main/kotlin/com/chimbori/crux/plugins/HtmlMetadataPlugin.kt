package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.AMP_URL
import com.chimbori.crux.Fields.BANNER_IMAGE_URL
import com.chimbori.crux.Fields.CANONICAL_URL
import com.chimbori.crux.Fields.DESCRIPTION
import com.chimbori.crux.Fields.FEED_URL
import com.chimbori.crux.Fields.KEYWORDS_CSV
import com.chimbori.crux.Fields.SITE_NAME
import com.chimbori.crux.Fields.THEME_COLOR_HEX
import com.chimbori.crux.Fields.TITLE
import com.chimbori.crux.Fields.VIDEO_URL
import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.extractors.extractAmpUrl
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractDescription
import com.chimbori.crux.extractors.extractFeedUrl
import com.chimbori.crux.extractors.extractImageUrl
import com.chimbori.crux.extractors.extractKeywords
import com.chimbori.crux.extractors.extractSiteName
import com.chimbori.crux.extractors.extractThemeColor
import com.chimbori.crux.extractors.extractTitle
import com.chimbori.crux.extractors.extractVideoUrl
import com.chimbori.crux.urls.isLikelyArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl

/**
 * Extracts common well-defined metadata fields from an HTML DOM tree. Includes support for:
 * - Twitter Cards Metadata: https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/markup
 * - Open Graph Protocol: https://ogp.me/
 * - AMP Spec: https://amp.dev/documentation/guides-and-tutorials/learn/spec/amphtml/
 */
public class HtmlMetadataPlugin : Plugin {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource = withContext(Dispatchers.IO) {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    Resource(
      fields = mapOf(
        TITLE to request.document?.extractTitle(),
        DESCRIPTION to request.document?.extractDescription(),
        SITE_NAME to request.document?.extractSiteName(),
        THEME_COLOR_HEX to request.document?.extractThemeColor(),
        KEYWORDS_CSV to request.document?.extractKeywords()?.joinToString(separator = ","),
      ),
      urls = mapOf(
        CANONICAL_URL to canonicalUrl,
        BANNER_IMAGE_URL to request.document?.extractImageUrl(canonicalUrl),
        FEED_URL to request.document?.extractFeedUrl(canonicalUrl),
        AMP_URL to request.document?.extractAmpUrl(canonicalUrl),
        VIDEO_URL to request.document?.extractVideoUrl(canonicalUrl),
      )
    ).removeNullValues()
  }
}
