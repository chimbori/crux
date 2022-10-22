package com.chimbori.crux.common

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

internal fun Element.parseAttrAsInt(attr: String) = try {
  attr(attr).toInt()
} catch (e: NumberFormatException) {
  0
}

internal fun Elements.anyChildTagWithAttr(attribute: String): String? =
  firstOrNull { element -> element.attr(attribute).isNotBlank() }
    ?.attr(attribute)
