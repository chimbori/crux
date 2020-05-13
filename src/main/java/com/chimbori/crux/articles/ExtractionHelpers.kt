package com.chimbori.crux.articles

import com.chimbori.crux.common.StringUtils.countMatches
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

internal object ExtractionHelpers {
  const val GRAVITY_SCORE_ATTRIBUTE = "gravityScore"
  val GRAVITY_SCORE_SELECTOR = "*[$GRAVITY_SCORE_ATTRIBUTE]"
  private val IMPORTANT_NODES = Pattern.compile("p|div|td|h1|h2|article|section")
  private val UNLIKELY_CSS_CLASSES_AND_IDS = Pattern.compile("com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
      + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
      + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
      + "login|si(debar|gn|ngle)|facebook|twitter|email")
  private val POSITIVE_CSS_CLASSES_AND_IDS = Pattern.compile("(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
      + "|arti(cle|kel)|instapaper_body")
  val NEGATIVE_CSS_CLASSES_AND_IDS = Pattern.compile("nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
      + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
      + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard|post-ratings")
  private val NEGATIVE_CSS_STYLES = Pattern.compile("hidden|display: ?none|font-size: ?small")

  /**
   * Weights current element. By matching it with positive candidates and weighting child nodes. Since it's impossible
   * to predict which exactly names, ids or class names will be used in HTML, major role is played by child nodes.
   *
   * @param e Element to weight, along with child nodes
   */
  fun getWeight(e: Element) = calcWeight(e) + (e.ownText().length / 100.0 * 10).roundToInt() + weightChildNodes(e)

  /**
   * Weights a child nodes of given Element. During tests some difficulties were met. For instanance, not every single
   * document has nested paragraph tags inside of the major article tag. Sometimes people are adding one more nesting
   * level. So, we're adding 4 points for every 100 symbols contained in tag nested inside of the current weighted
   * element, but only 3 points for every element that's nested 2 levels deep. This way we give more chances to extract
   * the element that has less nested levels, increasing probability of the correct extraction.
   * @param rootEl Element, who's child nodes will be weighted
   */
  private fun weightChildNodes(rootEl: Element): Int {
    var weight = 0
    var caption: Element? = null
    val pEls: MutableList<Element> = ArrayList(5)
    for (child in rootEl.children()) {
      var ownText = child.ownText()
      // if you are on a paragraph, grab all the text including that surrounded by additional formatting.
      if (child.tagName() == "p") ownText = child.text()
      val ownTextLength = ownText.length
      if (ownTextLength < 20) continue
      if (ownTextLength > 200) weight += Math.max(50, ownTextLength / 10)
      if (child.tagName() == "h1" || child.tagName() == "h2") {
        weight += 30
      } else if (child.tagName() == "div" || child.tagName() == "p") {
        weight += calcWeightForChild(child, ownText)
        if (child.tagName() == "p" && ownTextLength > 50) pEls.add(child)
        if (child.className().toLowerCase() == "caption") caption = child
      }
    }

    // use caption and image
    if (caption != null) weight += 30
    if (pEls.size >= 2) {
      for (subEl in rootEl.children()) {
        if ("h1;h2;h3;h4;h5;h6".contains(subEl.tagName())) {
          weight += 20
          // headerEls.add(subEl);
        } else if ("table;li;td;th".contains(subEl.tagName())) {
          addScore(subEl, -30)
        }
        if ("p".contains(subEl.tagName())) {
          addScore(subEl, 30)
        }
      }
    }
    return weight
  }

  private fun addScore(el: Element, score: Int) = setScore(el, getScore(el) + score)

  private fun getScore(el: Element) = try {
    el.attr(GRAVITY_SCORE_ATTRIBUTE).toInt()
  } catch (ex: NumberFormatException) {
    0
  }

  private fun setScore(el: Element, score: Int) {
    el.attr(GRAVITY_SCORE_ATTRIBUTE, Integer.toString(score))
  }

  private fun calcWeightForChild(child: Element, ownText: String): Int {
    var c = countMatches(ownText, "&quot;")
    c += countMatches(ownText, "&lt;")
    c += countMatches(ownText, "&gt;")
    c += countMatches(ownText, "px")
    val `val`: Int
    `val` = if (c > 5) {
      -30
    } else {
      Math.round(ownText.length / 25.0).toInt()
    }
    addScore(child, `val`)
    return `val`
  }

  private fun calcWeight(element: Element): Int {
    val className = element.className()
    val id = element.id()
    val style = element.attr("style")
    var weight = 0
    if (POSITIVE_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight += 35
    }
    if (POSITIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight += 40
    }
    if (UNLIKELY_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight -= 20
    }
    if (UNLIKELY_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight -= 20
    }
    if (NEGATIVE_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight -= 50
    }
    if (NEGATIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight -= 50
    }
    if (style != null && !style.isEmpty() && NEGATIVE_CSS_STYLES.matcher(style).find()) {
      weight -= 50
    }
    return weight
  }

  /** @return a set of all important nodes */
  fun getNodes(doc: Document): Collection<Element> {
    val nodes: MutableMap<Element, Any?> = LinkedHashMap(64)
    var score = 100
    for (el in doc.select("body").select("*")) {
      if (IMPORTANT_NODES.matcher(el.tagName()).matches()) {
        nodes[el] = null
        setScore(el, score)
        score = score / 2
      }
    }
    return nodes.keys
  }
}
