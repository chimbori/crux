package com.chimbori.crux.plugins

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.chimbori.crux.api.Extractor
import com.chimbori.crux.api.Fields.BACKGROUND_COLOR_HEX
import com.chimbori.crux.api.Fields.BACKGROUND_COLOR_HTML
import com.chimbori.crux.api.Fields.DISPLAY
import com.chimbori.crux.api.Fields.FAVICON_URL
import com.chimbori.crux.api.Fields.LANGUAGE
import com.chimbori.crux.api.Fields.ORIENTATION
import com.chimbori.crux.api.Fields.THEME_COLOR_HEX
import com.chimbori.crux.api.Fields.THEME_COLOR_HTML
import com.chimbori.crux.api.Fields.TITLE
import com.chimbori.crux.api.Fields.WEB_APP_MANIFEST_URL
import com.chimbori.crux.api.Resource
import com.chimbori.crux.common.httpGetContent
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.parseSize
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient

public class WebAppManifestParser(private val okHttpClient: OkHttpClient) : Extractor {
  override fun canExtract(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun extract(request: Resource): Resource? {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    val webAppManifestUrl = request.document?.select("link[rel=manifest]")?.attr("abs:href")?.nullIfBlank()
      ?.let { canonicalUrl?.resolve(it) ?: it.toHttpUrlOrNull() }
      ?: return null

    val manifest: JsonObject? = okHttpClient.httpGetContent(webAppManifestUrl)?.let { rawJSON ->
      try {
        Parser.default().parse(StringBuilder(rawJSON)) as JsonObject
      } catch (t: Throwable) {
        // Silently ignore all JSON errors, since they are not recoverable.
        null
      }
    }

    val themeColorHtml = manifest.element("theme_color")
    val backgroundColorHtml = manifest.element("background_color")
    return Resource(
      metadata = mapOf(
        WEB_APP_MANIFEST_URL to webAppManifestUrl,
        TITLE to manifest.element("name"),
        LANGUAGE to manifest.element("lang"),
        DISPLAY to manifest.element("display"),
        ORIENTATION to manifest.element("orientation"),
        FAVICON_URL to getLargestIconUrl(webAppManifestUrl, manifest?.array<JsonObject>("icons")),
        (if (themeColorHtml?.startsWith("#") == true) THEME_COLOR_HEX else THEME_COLOR_HTML) to themeColorHtml,
        (if (backgroundColorHtml?.startsWith("#") == true) BACKGROUND_COLOR_HEX else BACKGROUND_COLOR_HTML) to backgroundColorHtml,
      )
    ).removeNullValues()
  }

  private fun getLargestIconUrl(baseUrl: HttpUrl?, icons: JsonArray<JsonObject>?): HttpUrl? {
    icons
      ?.maxByOrNull { sizeElement -> parseSize((sizeElement as? JsonObject)?.string("sizes")) }
      .let { iconElement -> iconElement?.string("src") }
      ?.let { iconUrl -> return if (baseUrl != null) baseUrl.resolve(iconUrl) else iconUrl.toHttpUrlOrNull() }
      ?: return null
  }

  private fun JsonObject?.element(name: String): String? = this?.string(name)?.trim()
}
