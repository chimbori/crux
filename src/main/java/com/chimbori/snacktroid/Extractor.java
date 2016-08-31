package com.chimbori.snacktroid;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Extractor {
  private static final Logger logger = Logger.getInstance();

  private static final Pattern NODES =
      Pattern.compile("p|div|td|h1|h2|article|section");
  private static final Pattern UNLIKELY =
      Pattern.compile("com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
      + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
      + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
      + "login|si(debar|gn|ngle)");
  private static final Pattern POSITIVE =
      Pattern.compile("(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
      + "|arti(cle|kel)|instapaper_body");
  private static final Pattern NEGATIVE =
      Pattern.compile("nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
      + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
      + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard|post-ratings");

  private static final Pattern NEGATIVE_STYLE = Pattern.compile("hidden|display: ?none|font-size: ?small");

  private static final Set<String> IGNORED_TITLE_PARTS = new LinkedHashSet<String>() {
    {
      add("hacker news");
      add("facebook");
    }
  };

  private static final OutputFormatter DEFAULT_FORMATTER = new OutputFormatter();
  private OutputFormatter formatter = DEFAULT_FORMATTER;

  public Extractor setOutputFormatter(OutputFormatter formatter) {
    this.formatter = formatter;
    return this;
  }

  public ParsedResult extractContent(String html) {
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return extractContent(Jsoup.parse(html), formatter);
  }

  private ParsedResult extractContent(Document doc, OutputFormatter formatter) {
    if (doc == null) {
      throw new IllegalArgumentException();
    }

    ParsedResult res = new ParsedResult();
    res.title = extractTitle(doc);
    res.description = extractDescription(doc);
    res.canonicalUrl = extractCanonicalUrl(doc);

    // now remove the clutter
    prepareDocument(doc);

    // init elements
    Collection<Element> nodes = getNodes(doc);
    int maxWeight = 0;
    Element bestMatchElement = null;
    for (Element entry : nodes) {
      int currentWeight = getWeight(entry);
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight;
        bestMatchElement = entry;
        if (maxWeight > 200)
          break;
      }
    }

    if (bestMatchElement != null) {
      List<ParsedResult.ImageResult> images = new ArrayList<>();
      Element imgEl = determineImageSource(bestMatchElement, images);
      if (imgEl != null) {
        res.imageUrl = StringUtils.replaceSpaces(imgEl.attr("src"));
        // TODO remove parent container of image if it is contained in bestMatchElement
        // to avoid image subtitles flooding in

        res.images = images;
      }

      // clean before grabbing text
      String text = formatter.getFormattedText(bestMatchElement);
      // this fails for short facebook post and probably tweets: text.length() > res.description.length()
      if (text.length() > res.title.length()) {
        res.text = text;
        // print("best element:", bestMatchElement);
      }
      res.textList = formatter.getTextList(bestMatchElement);
    }

    if (res.imageUrl.isEmpty()) {
      res.imageUrl = extractImageUrl(doc);
    }

    res.rssUrl = extractRssUrl(doc);
    res.videoUrl = extractVideoUrl(doc);
    res.faviconUrl = extractFaviconUrl(doc);
    res.keywords = extractKeywords(doc);
    return res;
  }

  private static String extractTitle(Document doc) {
    String title = cleanTitle(doc.title());
    if (title.isEmpty()) {
      title = StringUtils.innerTrim(doc.select("head title").text());
      if (title.isEmpty()) {
        title = StringUtils.innerTrim(doc.select("head meta[name=title]").attr("content"));
        if (title.isEmpty()) {
          title = StringUtils.innerTrim(doc.select("head meta[property=og:title]").attr("content"));
          if (title.isEmpty()) {
            title = StringUtils.innerTrim(doc.select("head meta[name=twitter:title]").attr("content"));
          }
        }
      }
    }
    return title;
  }

  private static String extractCanonicalUrl(Document doc) {
    String url = StringUtils.replaceSpaces(doc.select("head link[rel=canonical]").attr("href"));
    if (url.isEmpty()) {
      url = StringUtils.replaceSpaces(doc.select("head meta[property=og:url]").attr("content"));
      if (url.isEmpty()) {
        url = StringUtils.replaceSpaces(doc.select("head meta[name=twitter:url]").attr("content"));
      }
    }
    return url;
  }

  private static String extractDescription(Document doc) {
    String description = StringUtils.innerTrim(doc.select("head meta[name=description]").attr("content"));
    if (description.isEmpty()) {
      description = StringUtils.innerTrim(doc.select("head meta[property=og:description]").attr("content"));
      if (description.isEmpty()) {
        description = StringUtils.innerTrim(doc.select("head meta[name=twitter:description]").attr("content"));
      }
    }
    return description;
  }

  private static Collection<String> extractKeywords(Document doc) {
    String content = StringUtils.innerTrim(doc.select("head meta[name=keywords]").attr("content"));

    if (content.startsWith("[") && content.endsWith("]")) {
      content = content.substring(1, content.length() - 1);
    }

    String[] split = content.split("\\s*,\\s*");
    if (split.length > 1 || (split.length > 0 && !"".equals(split[0]))) {
      return Arrays.asList(split);
    }
    return Collections.emptyList();
  }

  private static String extractImageUrl(Document doc) {
    // use open graph tag to get image
    String imageUrl = StringUtils.replaceSpaces(doc.select("head meta[property=og:image]").attr("content"));
    if (imageUrl.isEmpty()) {
      imageUrl = StringUtils.replaceSpaces(doc.select("head meta[name=twitter:image]").attr("content"));
      if (imageUrl.isEmpty()) {
        // prefer link over thumbnail-meta if empty
        imageUrl = StringUtils.replaceSpaces(doc.select("link[rel=image_src]").attr("href"));
        if (imageUrl.isEmpty()) {
          imageUrl = StringUtils.replaceSpaces(doc.select("head meta[name=thumbnail]").attr("content"));
        }
      }
    }
    return imageUrl;
  }

  private String extractRssUrl(Document doc) {
    return StringUtils.replaceSpaces(doc.select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"));
  }

  private String extractVideoUrl(Document doc) {
    return StringUtils.replaceSpaces(doc.select("head meta[property=og:video]").attr("content"));
  }

  private String extractFaviconUrl(Document doc) {
    String faviconUrl = StringUtils.replaceSpaces(doc.select("head link[rel=icon]").attr("href"));
    if (faviconUrl.isEmpty()) {
      faviconUrl = StringUtils.replaceSpaces(doc.select("head link[rel^=shortcut],link[rel$=icon]").attr("href"));
    }
    return faviconUrl;
  }

  /**
   * Weights current element. By matching it with positive candidates and
   * weighting child nodes. Since it's impossible to predict which exactly
   * names, ids or class names will be used in HTML, major role is played by
   * child nodes
   *
   * @param e Element to weight, along with child nodes
   */
  private static int getWeight(Element e) {
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

        if ("p".contains(subEl.tagName()))
          addScore(subEl, 30);
      }
    }
    return weight;
  }

  private static void addScore(Element el, int score) {
    int old = getScore(el);
    setScore(el, score + old);
  }

  private static int getScore(Element el) {
    int old = 0;
    try {
      old = Integer.parseInt(el.attr("gravityScore"));
    } catch (NumberFormatException ex) {
      // Ignore.
    }
    return old;
  }

  private static void setScore(Element el, int score) {
    el.attr("gravityScore", Integer.toString(score));
  }

  private static int calcWeightForChild(Element child, String ownText) {
    int c = StringUtils.count(ownText, "&quot;");
    c += StringUtils.count(ownText, "&lt;");
    c += StringUtils.count(ownText, "&gt;");
    c += StringUtils.count(ownText, "px");
    int val;
    if (c > 5)
      val = -30;
    else
      val = (int) Math.round(ownText.length() / 25.0);

    addScore(child, val);
    return val;
  }

  private static int calcWeight(Element e) {
    int weight = 0;
    if (POSITIVE.matcher(e.className()).find())
      weight += 35;

    if (POSITIVE.matcher(e.id()).find())
      weight += 40;

    if (UNLIKELY.matcher(e.className()).find())
      weight -= 20;

    if (UNLIKELY.matcher(e.id()).find())
      weight -= 20;

    if (NEGATIVE.matcher(e.className()).find())
      weight -= 50;

    if (NEGATIVE.matcher(e.id()).find())
      weight -= 50;

    String style = e.attr("style");
    if (style != null && !style.isEmpty() && NEGATIVE_STYLE.matcher(style).find())
      weight -= 50;
    return weight;
  }

  private static Element determineImageSource(Element el, List<ParsedResult.ImageResult> images) {
    int maxWeight = 0;
    Element maxNode = null;
    Elements els = el.select("img");
    if (els.isEmpty())
      els = el.parent().select("img");

    double score = 1;
    for (Element e : els) {
      String sourceUrl = e.attr("src");
      if (sourceUrl.isEmpty() || StringUtils.isAdImage(sourceUrl))
        continue;

      int weight = 0;
      int height = 0;
      try {
        height = Integer.parseInt(e.attr("height"));
        if (height >= 50)
          weight += 20;
        else
          weight -= 20;
      } catch (NumberFormatException ex) {
        // Ignore.
      }

      int width = 0;
      try {
        width = Integer.parseInt(e.attr("width"));
        if (width >= 50)
          weight += 20;
        else
          weight -= 20;
      } catch (NumberFormatException ex) {
        // Ignore.
      }
      String alt = e.attr("alt");
      if (alt.length() > 35)
        weight += 20;

      String title = e.attr("title");
      if (title.length() > 35)
        weight += 20;

      String rel;
      boolean noFollow = false;
      if (e.parent() != null) {
        rel = e.parent().attr("rel");
        if (rel != null && rel.contains("nofollow")) {
          noFollow = rel.contains("nofollow");
          weight -= 40;
        }
      }

      weight = (int) (weight * score);
      if (weight > maxWeight) {
        maxWeight = weight;
        maxNode = e;
        score = score / 2;
      }

      ParsedResult.ImageResult image = new ParsedResult.ImageResult(sourceUrl, weight, title, height, width, alt, noFollow);
      images.add(image);
    }

    Collections.sort(images, new ImageWeightComparator());
    return maxNode;
  }

  /**
   * Prepares document. Currently only stipping unlikely candidates, since
   * from time to time they're getting more score than good ones especially in
   * cases when major text is short.
   *
   * @param doc document to prepare. Passed as reference, and changed inside
   *            of function
   */
  private void prepareDocument(Document doc) {
//        stripUnlikelyCandidates(doc);
    removeScriptsAndStyles(doc);
  }

  /**
   * Removes unlikely candidates from HTML. Currently takes id and class name
   * and matches them against list of patterns
   *
   * @param doc document to strip unlikely candidates from
   */
  protected void stripUnlikelyCandidates(Document doc) {
    for (Element child : doc.select("body").select("*")) {
      String className = child.className().toLowerCase();
      String id = child.id().toLowerCase();

      if (NEGATIVE.matcher(className).find()
          || NEGATIVE.matcher(id).find()) {
//                print("REMOVE:", child);
        child.remove();
      }
    }
  }

  private Document removeScriptsAndStyles(Document doc) {
    Elements scripts = doc.getElementsByTag("script");
    for (Element item : scripts) {
      item.remove();
    }

    Elements noscripts = doc.getElementsByTag("noscript");
    for (Element item : noscripts) {
      item.remove();
    }

    Elements styles = doc.getElementsByTag("style");
    for (Element style : styles) {
      style.remove();
    }

    return doc;
  }

  /**
   * @return a set of all important nodes
   */
  private static Collection<Element> getNodes(Document doc) {
    Map<Element, Object> nodes = new LinkedHashMap<>(64);
    int score = 100;
    for (Element el : doc.select("body").select("*")) {
      if (NODES.matcher(el.tagName()).matches()) {
        nodes.put(el, null);
        setScore(el, score);
        score = score / 2;
      }
    }
    return nodes.keySet();
  }

  public static String cleanTitle(String title) {
    StringBuilder res = new StringBuilder();
//        int index = title.lastIndexOf("|");
//        if (index > 0 && title.length() / 2 < index)
//            title = title.substring(0, index + 1);

    int counter = 0;
    String[] strs = title.split("\\|");
    for (String part : strs) {
      if (IGNORED_TITLE_PARTS.contains(part.toLowerCase().trim()))
        continue;

      if (counter == strs.length - 1 && res.length() > part.length())
        continue;

      if (counter > 0)
        res.append("|");

      res.append(part);
      counter++;
    }

    return StringUtils.innerTrim(res.toString());
  }

  private static class ImageWeightComparator implements Comparator<ParsedResult.ImageResult> {
    @Override
    public int compare(ParsedResult.ImageResult o1, ParsedResult.ImageResult o2) {
      // Returns the highest weight first
      return o2.weight.compareTo(o1.weight);
    }
  }
}