package com.chimbori.snacktroid;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Takes the top node and strips out junk for presentation to the user.
 */
class OutputFormatter implements Extractor.Formatter {
  /**
   * If a string is shorter than this limit, it is not considered a paragraph.
   */
  private static final int MIN_LENGTH_FOR_PARAGRAPHS = 50;

  private final Pattern UNLIKELY_CSS_CLASSES = Pattern.compile("display\\:none|visibility\\:hidden");

  private final String NODES_TO_KEEP_SELECTOR = "p";

  public String getFormattedText(Element topNode) {
    removeNodesWithNegativeScores(topNode);
    StringBuilder sb = new StringBuilder();
    append(topNode, sb, NODES_TO_KEEP_SELECTOR);
    String str = StringUtils.innerTrim(sb.toString());
    if (str.length() > 100) {
      return str;
    }

    // No DOM children?
    if (str.isEmpty() || !topNode.text().isEmpty() && str.length() <= topNode.ownText().length()) {
      str = topNode.text();
    }

    // If Jsoup failed to parse the whole HTML, now parse this smaller snippet again, to avoid
    // HTML tags disturbing our text.
    return Jsoup.parse(str).text();
  }

  public List<String> getTextList(Element topNode) {
    List<String> texts = new ArrayList<>();
    for (Element element : topNode.select(this.NODES_TO_KEEP_SELECTOR)) {
      // System.err.println(element);

      if (element.hasText()) {
        texts.add(element.text());
      }
    }
    return texts;
  }

  private void removeNodesWithNegativeScores(Element topNode) {
    Elements gravityItems = topNode.select("*[gravityScore]");
    for (Element item : gravityItems) {
      int score = Integer.parseInt(item.attr("gravityScore"));
      if (score < 0 || item.text().length() < MIN_LENGTH_FOR_PARAGRAPHS)
        item.remove();
    }
  }

  private void append(Element node, StringBuilder sb, String tagName) {
    // is select more costly then getElementsByTag?
    MAIN:
    for (Element e : node.select(tagName)) {
      Element tmpEl = e;
      // check all elements until 'node'
      while (tmpEl != null && !tmpEl.equals(node)) {
        if (unlikely(tmpEl))
          continue MAIN;
        tmpEl = tmpEl.parent();
      }

      String text = node2Text(e);
      if (text.isEmpty() || text.length() < MIN_LENGTH_FOR_PARAGRAPHS || text.length() > StringUtils.countLetters(text) * 2)
        continue;

      sb.append(text);
      sb.append("\n\n");
    }
  }

  private boolean unlikely(Node e) {
    if (e.attr("class") != null && e.attr("class").toLowerCase().contains("caption"))
      return true;

    String style = e.attr("style");
    String clazz = e.attr("class");
    return UNLIKELY_CSS_CLASSES.matcher(style).find() || UNLIKELY_CSS_CLASSES.matcher(clazz).find();
  }

  void appendTextSkipHidden(Element e, StringBuilder accum) {
    for (Node child : e.childNodes()) {
      if (unlikely(child))
        continue;
      if (child instanceof TextNode) {
        TextNode textNode = (TextNode) child;
        String txt = textNode.text();
        accum.append(txt);
      } else if (child instanceof Element) {
        Element element = (Element) child;
        if (accum.length() > 0 && element.isBlock() && !lastCharIsWhitespace(accum))
          accum.append(" ");
        else if (element.tagName().equals("br"))
          accum.append(" ");
        appendTextSkipHidden(element, accum);
      }
    }
  }

  private boolean lastCharIsWhitespace(StringBuilder accum) {
    return accum.length() != 0 && Character.isWhitespace(accum.charAt(accum.length() - 1));
  }

  private String node2Text(Element el) {
    StringBuilder sb = new StringBuilder(200);
    appendTextSkipHidden(el, sb);
    return sb.toString();
  }
}
