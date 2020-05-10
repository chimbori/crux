package com.chimbori.crux.urls

import com.chimbori.crux.common.StringUtils.countMatches
import com.chimbori.crux.urls.Redirectors.REDIRECT_PATTERNS
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/**
 * Checks heuristically whether a given URL is likely to be an article, video, image, or other types. Can optionally
 * resolve redirects such as when Facebook or Google show an interstitial page instead of redirecting the user to the
 * actual URL.
 */
class CruxURL private constructor(private var uri: URI) {
  private val fileName: String

  /** Private constructor, so that the wrapping static method can perform some validation before invoking the constructor. */
  init {
    val path = uri.path
    fileName = if (path?.isNotEmpty() == true) {
      path.substring(path.lastIndexOf('/') + 1)
    } else ""
  }

  val isAdImage: Boolean
    get() = countMatches(uri.toString(), "ad") >= 2

  val isWebScheme: Boolean
    get() {
      val scheme = uri.scheme.toLowerCase()
      return scheme == "http" || scheme == "https"
    }

  val isLikelyArticle: Boolean
    get() = !isLikelyBinaryDocument && !isLikelyExecutable && !isLikelyArchive && !isLikelyImage && !isLikelyVideo && !isLikelyAudio

  val isLikelyVideo: Boolean
    get() = listOf(".mp4", ".mpg", ".mpeg", ".avi", ".mov", ".mpg4", ".flv", ".wmv")
        .firstOrNull { fileName.endsWith(it) } != null

  val isLikelyAudio: Boolean
    get() = listOf(".mp3", ".ogg", ".m3u", ".wav")
        .firstOrNull { fileName.endsWith(it) } != null

  val isLikelyBinaryDocument: Boolean
    get() = listOf(".pdf", ".ppt", ".doc", ".swf", ".rtf", ".xls")
        .firstOrNull { fileName.endsWith(it) } != null

  val isLikelyArchive: Boolean
    get() = listOf(".gz", ".tgz", ".zip", ".rar", ".deb", ".rpm", ".7z")
        .firstOrNull { fileName.endsWith(it) } != null

  val isLikelyExecutable: Boolean
    get() = listOf(".exe", ".bin", ".bat", ".dmg")
        .firstOrNull { fileName.endsWith(it) } != null

  val isLikelyImage: Boolean
    get() = listOf(".png", ".jpeg", ".gif", ".jpg", ".bmp", ".ico", ".eps")
        .firstOrNull { fileName.endsWith(it) } != null

  fun resolveRedirects(): CruxURL? {
    REDIRECT_PATTERNS.forEach { redirect ->
      if (redirect.matches(uri)) {
        uri = redirect.resolve(uri)
      }
    }
    return this
  }

  override fun toString() = uri.toString()

  companion object {
    /** Validate, initialize, and create a new [CruxURL], without invoking a lenient URL parser. */
    fun parseStrict(url: String?): CruxURL? = parseInternal(url, isStrict = true)

    /**
     * Validate, initialize, and create a new [CruxURL], invoking a lenient URL parser, since Java’s java.net.URI is a
     * stricter parser than real-world usage requires. Many characters used in valid URLs are rejected by it.
     */
    fun parse(url: String?): CruxURL? = parseInternal(url, isStrict = false)

    private fun parseInternal(url: String?, isStrict: Boolean): CruxURL? {
      if (url.isNullOrBlank()) {
        return null
      }

      var javaNetUri: URI? = null
      try {
        javaNetUri = URI(url)
      } catch (e: URISyntaxException) {
        // Java’s java.net.URI is a stricter parser than real-world usage requires. Many characters
        // used in valid URLs are rejected by it. So, if we encounter a URISyntaxException here, it
        // doesn’t necessarily mean the URL is invalid. Instead of giving up, we try a more lenient
        // parse.
        if (!isStrict) {
          // Code below is inspired by Android’s {@code toURILenient()}, and only the required bits
          // have been included in this project.
          try {
            javaNetUri = LenientURLParser.toURILenient(URL(url))
          } catch (e1: URISyntaxException) {
            // Ignore; we tried it parsing it in two ways, and we couldn’t do much, so give up now.
          } catch (e1: MalformedURLException) {
          }
        }
      }
      if (javaNetUri != null && (javaNetUri.scheme == null || javaNetUri.scheme.isEmpty())) {
        try {
          javaNetUri = URI("http://$url")
        } catch (e: URISyntaxException) {
          // Ignore.
        }
      }
      return javaNetUri?.let { CruxURL(it) }
    }
  }
}
