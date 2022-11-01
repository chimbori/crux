package com.chimbori.crux.api

import okhttp3.HttpUrl
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** A [Resource] encapculates metadata and content related to an HTTP resource. */
public data class Resource(
  /** Canonical URL for this resource. */
  val url: HttpUrl? = null,

  /** Parsed DOM tree for this resource, if available. */
  val document: Document? = null,

  /**
   * Extracted and cleaned-up DOM tree for this resource, if available.
   * If this is null, then article extraction has not been performed, or has failed.
   */
  val article: Element? = null,

  /** A holder for any kind of custom objects that library users may want to use. */
  val metadata: Map<String, Any?> = emptyMap(),
) {
  /** @return value of a named field in [Resource.metadata]. */
  public operator fun get(key: String): Any? = metadata[key]

  /**
   * Merges non-null fields from another [Resource] with this object, and returns a new immutable object. Prefer to use
   * this operator instead of manually merging the two objects, so that all fields are correctly merged and not clobbered.
   */
  public operator fun plus(anotherResource: Resource?): Resource = Resource(
    url = anotherResource?.url ?: url,
    document = anotherResource?.document ?: document,
    article = anotherResource?.article ?: article,
    metadata = if (anotherResource?.metadata == null) metadata else metadata + anotherResource.metadata,
  )

  /** Removes an immutable copy of this [Resource] that only contains non-null values for each key in [metadata]. */
  public fun removeNullValues(): Resource = copy(
    metadata = metadata.filterValues { it != null },
  )

  /** For any potential extension functions to be defined on the companion object. */
  public companion object
}
