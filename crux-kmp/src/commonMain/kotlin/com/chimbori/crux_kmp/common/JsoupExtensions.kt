package com.chimbori.crux_kmp.common

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements

internal fun Element.parseAttrAsInt(attr: String) = try {
  attr(attr).toInt()
} catch (e: NumberFormatException) {
  0
}

internal fun Elements.anyChildTagWithAttr(attribute: String): String? =
  firstOrNull { element -> element.attr(attribute).isNotBlank() }
    ?.attr(attribute)
