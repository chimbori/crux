package com.chimbori.crux.api

import okhttp3.HttpUrl

public sealed interface Plugin

/**
 * Crux is designed as a chain of plugins, each of which can optionally handle URLs passed to it. Each plugin is
 * provided a fully-parsed HTML DOM to extract fields from, and can also make additional HTTP requests if necessary to
 * retrieve additional metadata or to follow redirects.
 *
 * Text fields can be set via the [Resource.fields] property, and URLs via the [Resource.urls] property. Plugins
 * can also rewrite the canonical URL, and can provide an updated DOM tree if the canonical URL is changed. The
 * updated URL and DOM tree will be passed on to the next plugin in sequence, so the exact ordering of plugins is
 * important.
 */
public interface Extractor : Plugin {
  /**
   * @param url URL for the resource being processed by Crux.
   * @return true if this plugin can handle the URL, false otherwise. Plugins can only inspect the [HttpUrl], without
   * being able to peek at the content.
   */
  public fun canExtract(url: HttpUrl): Boolean

  /**
   * @param request metadata & DOM content for the request being handled.
   * @return a partially populated [Resource] with newly-extracted fields. Include only those fields that need to be
   * set or updated; they will be merged with the set of previously-extracted fields. If no fields need to be updated,
   * return `null`.
   */
  public suspend fun extract(request: Resource): Resource?
}
