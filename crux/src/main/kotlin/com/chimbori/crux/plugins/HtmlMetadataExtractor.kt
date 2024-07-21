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
import com.chimbori.crux.common.fetchFromUrl
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
import okhttp3.OkHttpClient

/**
 * Extracts common well-defined metadata fields from an HTML DOM tree. Includes support for:
 * - Twitter Cards Metadata: https://developer.twitter.com/en/docs/twitter-for-websites/cards/overview/markup
 * - Open Graph Protocol: https://ogp.me/
 * - AMP Spec: https://amp.dev/documentation/guides-and-tutorials/learn/spec/amphtml/
 */
public class HtmlMetadataExtractor(private val okHttpClient: OkHttpClient) : Extractor {
  /** Skip handling any file extensions that are unlikely to be HTML pages. */
  public override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource {
    val resourceToUse = if (request.document != null) {
      request
    } else if (request.url != null) {
      Resource.fetchFromUrl(request.url, okHttpClient)
    } else {
      Resource()
    }

    val canonicalUrl = resourceToUse.document?.extractCanonicalUrl()
      ?.let { resourceToUse.url?.resolve(it) }
      ?: resourceToUse.url

    return Resource(
      url = canonicalUrl,
      document = resourceToUse.document,
      metadata = mapOf(
        CANONICAL_URL to canonicalUrl,
        TITLE to resourceToUse.document?.extractTitle(),
        DESCRIPTION to resourceToUse.document?.extractDescription(),
        SITE_NAME to resourceToUse.document?.extractSiteName(),
        THEME_COLOR_HEX to resourceToUse.document?.extractThemeColor(),
        PUBLISHED_AT to resourceToUse.document?.extractPublishedAt(),
        MODIFIED_AT to resourceToUse.document?.extractModifiedAt(),
        KEYWORDS_CSV to resourceToUse.document?.extractKeywords()?.joinToString(separator = ","),
        NEXT_PAGE_URL to resourceToUse.document?.extractPaginationUrl(resourceToUse.url, "next"),
        PREVIOUS_PAGE_URL to resourceToUse.document?.extractPaginationUrl(resourceToUse.url, "prev"),
        BANNER_IMAGE_URL to resourceToUse.document?.extractImageUrl(canonicalUrl),
        FEED_URL to resourceToUse.document?.extractFeedUrl(canonicalUrl),
        AMP_URL to resourceToUse.document?.extractAmpUrl(canonicalUrl),
        VIDEO_URL to resourceToUse.document?.extractVideoUrl(canonicalUrl),
      )
    ).removeNullValues()
  }
}
