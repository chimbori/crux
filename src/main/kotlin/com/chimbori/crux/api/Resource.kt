package com.chimbori.crux.api

import okhttp3.HttpUrl
import org.jsoup.nodes.Document

/** A [Resource] encapculates metadata and content related to an HTTP resource. */
public data class Resource(
  /** Canonical URL for this resource. */
  val url: HttpUrl? = null,

  /** Parsed DOM tree for this resource, if available. */
  val document: Document? = null,

  /** Extracted and cleaned-up DOM tree for this resource, if available. */
  val article: Document? = null,

  /**
   * Text fields extracted from this resource, stored as key-value pairs. It is recommended to use well-defined keys
   * from [com.chimbori.crux.Fields] for all standard fields. Custom fields are also supported, in case none of the
   * pre-defined keys are applicable.
   */
  val fields: Map<String, String?> = emptyMap(),

  /**
   * URL fields extracted from this resource. Storing these as key-value pairs of [HttpUrl]s avoids re-parsing the same
   * URLs multiple times. URLs can also be retrieved as strings via the [get] indexed accessor.
   */
  val urls: Map<String, HttpUrl?> = emptyMap(),

  /** A holder for any kind of custom objects that library users may want to use. */
  val objects: Map<String, Any?> = emptyMap(),
) {
  /**
   * @return value of a named field. If there’s no named [String] field corresponding to this key in [Resource.fields],
   * but a [HttpUrl] exists in [Resource.urls], the latter will be stringified and returned instead.
   */
  public operator fun get(key: String): String? = fields[key] ?: urls[key]?.toString()

  /**
   * Merges non-null fields from another [Resource] with this object, and returns a new immutable object. Prefer to use
   * this operator instead of manually merging the two objects, so that [fields] and [urls] are correctly merged and
   * not clobbered.
   */
  public operator fun plus(anotherResource: Resource?): Resource = Resource(
    url = anotherResource?.url ?: url,
    document = anotherResource?.document ?: document,
    article = anotherResource?.article ?: article,
    fields = if (anotherResource?.fields == null) fields else fields + anotherResource.fields,
    urls = if (anotherResource?.urls == null) urls else urls + anotherResource.urls,
    objects = if (anotherResource?.objects == null) objects else objects + anotherResource.objects,
  )

  /**
   * Removes an immutable copy of this [Resource] that only contains non-null values for each key in both [fields]
   * and [urls].
   */
  public fun removeNullValues(): Resource = copy(
    fields = fields.filterValues { !it.isNullOrBlank() },
    urls = urls.filterValues { it != null },
    objects = objects.filterValues { it != null },
  )

  /** For any potential extension functions to be defined on the companion object. */
  public companion object
}
