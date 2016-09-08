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
    StringBuilder buffer = new StringBuilder();
    appendContentFromNode(topNode, buffer, NODES_TO_KEEP_SELECTOR);
    String str = StringUtils.innerTrim(buffer.toString());
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
      if (score < 0 || item.text().length() < MIN_LENGTH_FOR_PARAGRAPHS) {
        item.remove();
      }
    }
  }

  private void appendContentFromNode(Element node, StringBuilder buffer, String tagName) {
    // is select more costly then getElementsByTag?
    MAIN:
    for (Element e : node.select(tagName)) {
      Element tmpEl = e;
      // check all elements until 'node'
      while (tmpEl != null && !tmpEl.equals(node)) {
        if (isUnlikely(tmpEl)) {
          continue MAIN;
        }
        tmpEl = tmpEl.parent();
      }

      String text = getTextFromElement(e);
      if (text.isEmpty() || text.length() < MIN_LENGTH_FOR_PARAGRAPHS || text.length() > StringUtils.countLetters(text) * 2) {
        continue;
      }

      buffer.append(text);
      buffer.append("\n\n");
    }
  }

  private boolean isUnlikely(Node e) {
    String styleAttribute = e.attr("style");
    String classAttribute = e.attr("class");
    if (classAttribute != null && classAttribute.toLowerCase().contains("caption")) {
      return true;
    }
    return UNLIKELY_CSS_CLASSES.matcher(styleAttribute).find() || UNLIKELY_CSS_CLASSES.matcher(classAttribute).find();
  }

  void appendTextSkipHidden(Element e, StringBuilder buffer) {
    for (Node child : e.childNodes()) {
      if (isUnlikely(child)) {
        continue;
      }
      if (child instanceof TextNode) {
        TextNode textNode = (TextNode) child;
        String txt = textNode.text();
        buffer.append(txt);
      } else if (child instanceof Element) {
        Element element = (Element) child;
        if (buffer.length() > 0 && element.isBlock() && !lastCharIsWhitespace(buffer)) {
          buffer.append(" ");
        } else if (element.tagName().equals("br")) {
          buffer.append(" ");
        }
        appendTextSkipHidden(element, buffer);
      }
    }
  }

  private boolean lastCharIsWhitespace(StringBuilder buffer) {
    return buffer.length() != 0 && Character.isWhitespace(buffer.charAt(buffer.length() - 1));
  }

  private String getTextFromElement(Element element) {
    StringBuilder buffer = new StringBuilder();
    appendTextSkipHidden(element, buffer);
    return buffer.toString();
  }
}
