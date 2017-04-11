package com.chimbori.crux.articles;

import com.chimbori.crux.common.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class ExtractionHelpers {
  private ExtractionHelpers() {
  }

  public static final String GRAVITY_SCORE_ATTRIBUTE = "gravityScore";
  public static final String GRAVITY_SCORE_SELECTOR = String.format("*[%s]", GRAVITY_SCORE_ATTRIBUTE);

  private static final Pattern IMPORTANT_NODES =
      Pattern.compile("p|div|td|h1|h2|article|section");

  private static final Pattern UNLIKELY_CSS_CLASSES_AND_IDS =
      Pattern.compile("com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
          + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
          + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
          + "login|si(debar|gn|ngle)|facebook|twitter|email");

  private static final Pattern POSITIVE_CSS_CLASSES_AND_IDS =
      Pattern.compile("(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
          + "|arti(cle|kel)|instapaper_body");

  public static final Pattern NEGATIVE_CSS_CLASSES_AND_IDS =
      Pattern.compile("nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
          + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
          + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard|post-ratings");

  private static final Pattern NEGATIVE_CSS_STYLES =
      Pattern.compile("hidden|display: ?none|font-size: ?small");

  /**
   * Weights current element. By matching it with positive candidates and
   * weighting child nodes. Since it's impossible to predict which exactly
   * names, ids or class names will be used in HTML, major role is played by
   * child nodes
   *
   * @param e Element to weight, along with child nodes
   */
  static int getWeight(Element e) {
    int weight = calcWeight(e);
    weight += (int) Math.round(e.ownText().length() / 100.0 * 10);
    weight += weightChildNodes(e);
    return weight;
  }

  /**
   * Weights a child nodes of given Element. During tests some difficulties
   * were met. For instanance, not every single document has nested paragraph
   * tags inside of the major article tag. Sometimes people are adding one
   * more nesting level. So, we're adding 4 points for every 100 symbols
   * contained in tag nested inside of the current weighted element, but only
   * 3 points for every element that's nested 2 levels deep. This way we give
   * more chances to extract the element that has less nested levels,
   * increasing probability of the correct extraction.
   *
   * @param rootEl Element, who's child nodes will be weighted
   */
  private static int weightChildNodes(Element rootEl) {
    int weight = 0;
    Element caption = null;
    List<Element> pEls = new ArrayList<>(5);
    for (Element child : rootEl.children()) {
      String ownText = child.ownText();

      // if you are on a paragraph, grab all the text including that surrounded by additional formatting.
      if (child.tagName().equals("p"))
        ownText = child.text();

      int ownTextLength = ownText.length();
      if (ownTextLength < 20)
        continue;

      if (ownTextLength > 200)
        weight += Math.max(50, ownTextLength / 10);

      if (child.tagName().equals("h1") || child.tagName().equals("h2")) {
        weight += 30;
      } else if (child.tagName().equals("div") || child.tagName().equals("p")) {
        weight += calcWeightForChild(child, ownText);
        if (child.tagName().equals("p") && ownTextLength > 50)
          pEls.add(child);

        if (child.className().toLowerCase().equals("caption"))
          caption = child;
      }
    }

    // use caption and image
    if (caption != null)
      weight += 30;

    if (pEls.size() >= 2) {
      for (Element subEl : rootEl.children()) {
        if ("h1;h2;h3;h4;h5;h6".contains(subEl.tagName())) {
          weight += 20;
          // headerEls.add(subEl);
        } else if ("table;li;td;th".contains(subEl.tagName())) {
          addScore(subEl, -30);
        }

        if ("p".contains(subEl.tagName())) {
          addScore(subEl, 30);
        }
      }
    }
    return weight;
  }

  private static void addScore(Element el, int score) {
    setScore(el, getScore(el) + score);
  }

  private static int getScore(Element el) {
    try {
      return Integer.parseInt(el.attr(GRAVITY_SCORE_ATTRIBUTE));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  private static void setScore(Element el, int score) {
    el.attr(GRAVITY_SCORE_ATTRIBUTE, Integer.toString(score));
  }

  private static int calcWeightForChild(Element child, String ownText) {
    int c = StringUtils.countMatches(ownText, "&quot;");
    c += StringUtils.countMatches(ownText, "&lt;");
    c += StringUtils.countMatches(ownText, "&gt;");
    c += StringUtils.countMatches(ownText, "px");
    int val;
    if (c > 5) {
      val = -30;
    } else {
      val = (int) Math.round(ownText.length() / 25.0);
    }

    addScore(child, val);
    return val;
  }

  private static int calcWeight(Element element) {
    String className = element.className();
    String id = element.id();
    String style = element.attr("style");

    int weight = 0;
    if (POSITIVE_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight += 35;
    }
    if (POSITIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight += 40;
    }
    if (UNLIKELY_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight -= 20;
    }
    if (UNLIKELY_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight -= 20;
    }
    if (NEGATIVE_CSS_CLASSES_AND_IDS.matcher(className).find()) {
      weight -= 50;
    }
    if (NEGATIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
      weight -= 50;
    }
    if (style != null && !style.isEmpty() && NEGATIVE_CSS_STYLES.matcher(style).find()) {
      weight -= 50;
    }
    return weight;
  }

  /**
   * @return a set of all important nodes
   */
  static Collection<Element> getNodes(Document doc) {
    Map<Element, Object> nodes = new LinkedHashMap<>(64);
    int score = 100;
    for (Element el : doc.select("body").select("*")) {
      if (IMPORTANT_NODES.matcher(el.tagName()).matches()) {
        nodes.put(el, null);
        setScore(el, score);
        score = score / 2;
      }
    }
    return nodes.keySet();
  }
}
