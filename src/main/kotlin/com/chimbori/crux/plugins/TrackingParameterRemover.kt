package com.chimbori.crux.plugins

import com.chimbori.crux.api.Rewriter
import okhttp3.HttpUrl

public class TrackingParameterRemover(private val trackingParameters: Array<String> = TRACKING_PARAMETERS) : Rewriter {
  override fun rewrite(url: HttpUrl): HttpUrl = url.newBuilder().apply {
    url.queryParameterNames.filter { it in trackingParameters }.forEach {
      removeAllQueryParameters(it)
    }
  }.build()

  public companion object {
    public val TRACKING_PARAMETERS: Array<String> = arrayOf(
      "_openstat",
      "action_object_map",
      "action_ref_map",
      "action_type_map",
      "fb_action_ids",
      "fb_action_types",
      "fb_ref",
      "fb_source",
      "ga_campaign",
      "ga_content",
      "ga_medium",
      "ga_place",
      "ga_source",
      "ga_term",
      "gs_l",
      "utm_campaign",
      "utm_cid",
      "utm_content",
      "utm_id",
      "utm_medium",
      "utm_name",
      "utm_place",
      "utm_pubreferrer",
      "utm_reader",
      "utm_source",
      "utm_swu",
      "utm_term",
      "utm_userid",
      "utm_viz_id",
      "yclid"
    )
  }
}
