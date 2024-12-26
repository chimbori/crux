package com.chimbori.crux.plugins

import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.AMP_URL
import com.chimbori.crux.api.Fields.BANNER_IMAGE_URL
import com.chimbori.crux.api.Fields.CANONICAL_URL
import com.chimbori.crux.api.Fields.DESCRIPTION
import com.chimbori.crux.api.Fields.FEED_URL
import com.chimbori.crux.api.Fields.KEYWORDS_CSV
import com.chimbori.crux.api.Fields.MODIFIED_AT
import com.chimbori.crux.api.Fields.NEXT_PAGE_URL
import com.chimbori.crux.api.Fields.PREVIOUS_PAGE_URL
import com.chimbori.crux.api.Fields.PUBLISHED_AT
import com.chimbori.crux.api.Fields.SITE_NAME
import com.chimbori.crux.api.Fields.THEME_COLOR_HEX
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Fields.VIDEO_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.extractors.extractAmpUrl
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.extractDescription
import com.chimbori.crux.extractors.extractFeedUrl
import com.chimbori.crux.extractors.extractImageUrl
import com.chimbori.crux.extractors.extractKeywords
import com.chimbori.crux.extractors.extractModifiedAt
import com.chimbori.crux.extractors.extractPaginationUrl
import com.chimbori.crux.extractors.extractPublishedAt
import com.chimbori.crux.extractors.extractSiteName
import com.chimbori.crux.extractors.extractThemeColor
import com.chimbori.crux.extractors.extractTitle
import com.chimbori.crux.extractors.extractVideoUrl
import okhttp3.HttpUrl

/**
 * Extracts common well-defined metadata fields from an HTML DOM tree. Includes support for:
 * - Twitter Cards Metadata: https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/markup
 * - Open Graph Protocol: https://ogp.me/
 * - AMP Spec: https://amp.dev/documentation/guides-and-tutorials/learn/spec/amphtml/
 */
public class HtmlMetadataExtractor : Extractor {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource {
    val canonicalUrl = request.document?.extractCanonicalUrl()
      ?.let { request.url?.resolve(it) }
      ?: request.url

    return Resource(
      url = canonicalUrl,
      document = request.document,
      metadata = mapOf(
        CANONICAL_URL to canonicalUrl,
        TITLE to request.document?.extractTitle(),
        DESCRIPTION to request.document?.extractDescription(),
        SITE_NAME to request.document?.extractSiteName(),
        THEME_COLOR_HEX to request.document?.extractThemeColor(),
        PUBLISHED_AT to request.document?.extractPublishedAt(),
        MODIFIED_AT to request.document?.extractModifiedAt(),
        KEYWORDS_CSV to request.document?.extractKeywords()?.joinToString(separator = ","),
        NEXT_PAGE_URL to request.document?.extractPaginationUrl(request.url, "next"),
        PREVIOUS_PAGE_URL to request.document?.extractPaginationUrl(request.url, "prev"),
        BANNER_IMAGE_URL to request.document?.extractImageUrl(canonicalUrl),
        FEED_URL to request.document?.extractFeedUrl(canonicalUrl),
        AMP_URL to request.document?.extractAmpUrl(canonicalUrl),
        VIDEO_URL to request.document?.extractVideoUrl(canonicalUrl),
      )
    ).removeNullValues()
  }
}
