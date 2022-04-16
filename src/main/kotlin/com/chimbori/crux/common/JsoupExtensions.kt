package com.chimbori.crux.common

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun Element.parseAttrAsInt(attr: String) = try {
  attr(attr).toInt()
} catch (e: NumberFormatException) {
  0
}

fun Elements.anyChildTagWithAttr(attribute: String): String? =
  firstOrNull { element -> element.attr(attribute).isNotBlank() }
    ?.attr(attribute)
