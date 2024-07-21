package com.chimbori.crux_kmp.plugins

import com.chimbori.crux_kmp.api.Extractor
import com.chimbori.crux_kmp.api.Fields.FAVICON_URL
import com.chimbori.crux_kmp.api.Resource
import com.chimbori.crux_kmp.common.isLikelyArticle
import com.chimbori.crux_kmp.extractors.extractCanonicalUrl
import com.chimbori.crux_kmp.extractors.extractFaviconUrl
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.takeFrom

public class FaviconExtractor : Extractor {
    /** Skip handling any file extensions that are unlikely to be HTML pages. */
    public override fun canExtract(url: Url): Boolean = url.isLikelyArticle()

    override suspend fun extract(request: Resource): Resource {
        val canonicalUrl = request.document?.extractCanonicalUrl()
            ?.let { request.url?.let { requestUrl -> URLBuilder(requestUrl).takeFrom(it).build() } }
            ?: request.url
        return Resource(
            metadata = mapOf(
                FAVICON_URL to request.document?.extractFaviconUrl(
                    canonicalUrl
                )
            )
        ).removeNullValues()
    }
}
