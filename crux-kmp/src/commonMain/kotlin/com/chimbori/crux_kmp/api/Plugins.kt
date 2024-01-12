package com.chimbori.crux_kmp.api

import io.ktor.http.Url

public sealed interface Plugin

/**
 * Rewriters are plugins that can modify the URL before it’s processed by other plugins. They should not have access
 * to the network, and should execute quickly on the main thread if necessary.
 */
public fun interface Rewriter : Plugin {
  public fun rewrite(url: Url): Url
}

/**
 * Crux is designed as a chain of plugins, each of which can optionally handle URLs passed to it. Each plugin is
 * provided a fully-parsed HTML DOM to extract fields from, and can also make additional HTTP requests if necessary to
 * retrieve additional metadata or to follow redirects.
 *
 * Metadata fields can be set via the [Resource.metadata] property. Plugins can also rewrite the canonical URL, and can
 * provide an updated DOM tree if the canonical URL is changed. The updated URL and DOM tree will be passed on to the
 * next plugin in sequence, so the exact ordering of plugins is important.
 */
public interface Extractor : Plugin {
  /**
   * @param url URL for the resource being processed by Crux.
   * @return true if this plugin can handle the URL, false otherwise. Plugins can only inspect the [HttpUrl], without
   * being able to peek at the content.
   */
  public fun canExtract(url: Url): Boolean

  /**
   * @param request metadata & DOM content for the request being handled.
   * @return a partially populated [Resource] with newly-extracted fields. Include only those fields that need to be
   * set or updated; they will be merged with the set of previously-extracted fields. If no fields need to be updated,
   * return `null`.
   */
  public suspend fun extract(request: Resource): Resource?
}
