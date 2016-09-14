package com.chimbori.crux;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

class ExtractionHelpers {
  private ExtractionHelpers() {
  }

  public static final String GRAVITY_SCORE_ATTRIBUTE = "gravityScore";
  public static final String GRAVITY_SCORE_SELECTOR = String.format("*[%s]", GRAVITY_SCORE_ATTRIBUTE);

  private static final Pattern NODES =
      Pattern.compile("p|div|td|h1|h2|article|section");

  private static final Pattern UNLIKELY =
      Pattern.compile("com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
          + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
          + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
          + "login|si(debar|gn|ngle)|facebook|twitter|email");

  private static final Pattern POSITIVE =
      Pattern.compile("(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
          + "|arti(cle|kel)|instapaper_body");

  public static final Pattern NEGATIVE =
      Pattern.compile("nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
          + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
          + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard|post-ratings");

  private static final Pattern NEGATIVE_STYLE =
      Pattern.compile("hidden|display: ?none|font-size: ?small");

  private static final Set<String> IGNORED_TITLE_PARTS = new HashSet<>(Arrays.asList(
      "hacker news", "facebook"
  ));

  static String extractTitle(Document doc) {
    try {
      return cleanTitle(new HeuristicString(doc.title())
          .or(StringUtils.innerTrim(doc.select("head title").text()))
          .or(StringUtils.innerTrim(doc.select("head meta[name=title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:title]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=twitter:title]").attr("content")))
          .toString());
    } catch (HeuristicString.CandidateFound candidateFound) {
      return cleanTitle(candidateFound.candidate);
    }
  }

  static String extractAmpUrl(Document doc) {
    try {
      return new HeuristicString(StringUtils.urlEncodeSpaceCharacter(doc.select("link[rel=amphtml]").attr("href")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractCanonicalUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel=canonical]").attr("href")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:url]").attr("content")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:url]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractDescription(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.innerTrim(doc.select("head meta[name=description]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[property=og:description]").attr("content")))
          .or(StringUtils.innerTrim(doc.select("head meta[name=twitter:description]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractImageUrl(Document doc, List<Article.Image> images) {
    try {
      return new HeuristicString(null)
          // Twitter Cards and Open Graph images are usually higher quality, so rank them first.
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=twitter:image]").attr("content")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:image]").attr("content")))
          // Then, grab any hero images from the article itself.
          .or(images != null && images.size() > 0 ? StringUtils.urlEncodeSpaceCharacter(images.get(0).src) : null)
          // image_src or thumbnails are usually low quality, so prioritize them *after* article images.
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("link[rel=image_src]").attr("href")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[name=thumbnail]").attr("content")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractFeedUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(doc.select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"))
          .or(doc.select("link[rel=alternate]").select("link[type=application/atom+xml]").attr("href"))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static String extractVideoUrl(Document doc) {
    return StringUtils.urlEncodeSpaceCharacter(doc.select("head meta[property=og:video]").attr("content"));
  }

  static String extractFaviconUrl(Document doc) {
    try {
      return new HeuristicString(null)
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel=icon]").attr("href")))
          .or(StringUtils.urlEncodeSpaceCharacter(doc.select("head link[rel^=shortcut],link[rel$=icon]").attr("href")))
          .toString();
    } catch (HeuristicString.CandidateFound candidateFound) {
      return candidateFound.candidate;
    }
  }

  static Collection<String> extractKeywords(Document doc) {
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
    if (POSITIVE.matcher(className).find()) {
      weight += 35;
    }
    if (POSITIVE.matcher(id).find()) {
      weight += 40;
    }
    if (UNLIKELY.matcher(className).find()) {
      weight -= 20;
    }
    if (UNLIKELY.matcher(id).find()) {
      weight -= 20;
    }
    if (NEGATIVE.matcher(className).find()) {
      weight -= 50;
    }
    if (NEGATIVE.matcher(id).find()) {
      weight -= 50;
    }
    if (style != null && !style.isEmpty() && NEGATIVE_STYLE.matcher(style).find()) {
      weight -= 50;
    }
    return weight;
  }

  /**
   * Extracts a set of images from the article content itself. This extraction must be run before
   * the postprocess step, because that step removes tags that are useful for image extraction.
   */
  static List<Article.Image> extractImages(Element topNode) {
    List<Article.Image> images = new ArrayList<>();
    if (topNode == null) {
      return images;
    }

    Elements imgElements = topNode.select("img");
    if (imgElements.isEmpty() && topNode.parent() != null) {
      imgElements = topNode.parent().select("img");
    }

    int maxWeight = 0;
    double score = 1;
    for (Element imgElement : imgElements) {
      Article.Image image = Article.Image.from(imgElement);
      if (image.src.isEmpty() || StringUtils.isAdImage(image.src)) {
        continue;
      }

      image.weight += image.height >= 50 ? 20 : -20;
      image.weight += image.width >= 50 ? 20 : -20;
      image.weight += image.src.endsWith(".gif") ? -20 : 0;
      image.weight += image.src.endsWith(".jpg") ? 5 : 0;
      image.weight += image.alt.length() > 35 ? 20 : 0;
      image.weight += image.title.length() > 35 ? 20 : 0;
      image.weight += image.noFollow ? -40 : 0;

      image.weight = (int) (image.weight * score);
      if (image.weight > maxWeight) {
        maxWeight = image.weight;
        score = score / 2;
      }

      images.add(image);
    }

    Collections.sort(images, new ImageWeightComparator());
    return images;
  }

  /**
   * @return a set of all important nodes
   */
  static Collection<Element> getNodes(Document doc) {
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

  static String cleanTitle(String title) {
    StringBuilder res = new StringBuilder();
//        int index = title.lastIndexOf("|");
//        if (index > 0 && title.length() / 2 < index)
//            title = title.substring(0, index + 1);

    int counter = 0;
    String[] strs = title.split("\\|");
    for (String part : strs) {
      if (IGNORED_TITLE_PARTS.contains(part.toLowerCase().trim())) {
        continue;
      }
      if (counter == strs.length - 1 && res.length() > part.length()) {
        continue;
      }
      if (counter > 0) {
        res.append("|");
      }
      res.append(part);
      counter++;
    }
    return StringUtils.innerTrim(res.toString());
  }

  /**
   * Returns the highest-scored image first.
   */
  private static class ImageWeightComparator implements Comparator<Article.Image> {
    @Override
    public int compare(Article.Image o1, Article.Image o2) {
      return o2.weight - o1.weight;
    }
  }
}
