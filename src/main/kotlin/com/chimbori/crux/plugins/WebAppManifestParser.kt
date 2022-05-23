package com.chimbori.crux.plugins

import com.chimbori.crux.Fields.BACKGROUND_COLOR_HEX
import com.chimbori.crux.Fields.BACKGROUND_COLOR_HTML
import com.chimbori.crux.Fields.DISPLAY
import com.chimbori.crux.Fields.FAVICON_URL
import com.chimbori.crux.Fields.LANGUAGE
import com.chimbori.crux.Fields.ORIENTATION
import com.chimbori.crux.Fields.THEME_COLOR_HEX
import com.chimbori.crux.Fields.THEME_COLOR_HTML
import com.chimbori.crux.Fields.TITLE
import com.chimbori.crux.Fields.WEB_APP_MANIFEST_URL
import com.chimbori.crux.Plugin
import com.chimbori.crux.Resource
import com.chimbori.crux.common.cruxOkHttpClient
import com.chimbori.crux.common.httpGetContent
import com.chimbori.crux.common.isLikelyArticle
import com.chimbori.crux.common.nullIfBlank
import com.chimbori.crux.extractors.extractCanonicalUrl
import com.chimbori.crux.extractors.parseSize
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

public class WebAppManifestParser : Plugin {
  override fun canHandle(url: HttpUrl): Boolean = url.isLikelyArticle()

  override suspend fun handle(request: Resource): Resource? {
    val canonicalUrl = request.document?.extractCanonicalUrl()?.let { request.url?.resolve(it) } ?: request.url
    val webAppManifestUrl = request.document?.select("link[rel=manifest]")?.attr("href")?.nullIfBlank()
      ?.let { canonicalUrl?.resolve(it) ?: it.toHttpUrlOrNull() }
      ?: return null

    val manifest: JSONObject? = cruxOkHttpClient.httpGetContent(webAppManifestUrl)?.let {
      try {
        JSONObject(it)
      } catch (e: JSONException) {
        // Silently ignore all JSON errors, since they are not recoverable.
        null
      }
    }

    val themeColorHtml = manifest.element("theme_color")
    val backgroundColorHtml = manifest.element("background_color")
    return Resource(
      fields = mapOf(
        TITLE to manifest.element("name"),
        LANGUAGE to manifest.element("lang"),
        DISPLAY to manifest.element("display"),
        ORIENTATION to manifest.element("orientation"),
        (if (themeColorHtml?.startsWith("#") == true) THEME_COLOR_HEX else THEME_COLOR_HTML) to themeColorHtml,
        (if (backgroundColorHtml?.startsWith("#") == true) BACKGROUND_COLOR_HEX else BACKGROUND_COLOR_HTML) to backgroundColorHtml,
      ),
      urls = mapOf(
        WEB_APP_MANIFEST_URL to webAppManifestUrl,
        FAVICON_URL to getLargestIconUrl(webAppManifestUrl, manifest?.optJSONArray("icons"))
      )
    ).removeNullValues()
  }

  private fun getLargestIconUrl(baseUrl: HttpUrl?, icons: JSONArray?): HttpUrl? {
    icons
      ?.maxByOrNull { sizeElement -> parseSize((sizeElement as? JSONObject)?.optString("sizes")) }
      .let { iconElement -> (iconElement as? JSONObject)?.optString("src") }
      ?.let { iconUrl -> return if (baseUrl != null) baseUrl.resolve(iconUrl) else iconUrl.toHttpUrlOrNull() }
      ?: return null
  }

  private fun JSONObject?.element(name: String): String? = this?.opt(name)?.toString()?.trim()
}
