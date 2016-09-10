package com.chimbori.snacktroid;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Cleans up the best-match Element after one has been picked, in order to provide a sanitized
 * output tree to the caller.
 */
class PostprocessHelpers {
  /** If a string is shorter than this limit, it is not considered a paragraph. */
  private static final int MIN_LENGTH_FOR_PARAGRAPHS = 50;
  private static final Pattern UNLIKELY_CSS_CLASSES = Pattern.compile("display\\:none|visibility\\:hidden");
  private static final Set<String> REMOVE_TAGS_BUT_RETAIN_CONTENT = new HashSet<>(Arrays.asList(
      "font", "table", "tbody", "tr", "td"
  ));
  private static final Set<String> RETAIN_TAGS = new HashSet<>(Arrays.asList(
      "p", "li", "ol", "span", "b", "i", "u", "strong", "em", "a", "pre"
      // , "h1", "h2", "h3", "h4", "h5", "h6"
  ));
  private static final Set<String> ATTRIBUTES_TO_RETAIN_IN_HTML = new HashSet<>(Arrays.asList(
      "href"
  ));

  static Document postprocess(Element topNode) {
    Log.i(topNode.outerHtml());
    removeNodesWithNegativeScores(topNode);
    replaceLineBreaksWithSpaces(topNode);
    removeUnlikelyChildNodes(topNode);
    removeTagsButRetainContent(topNode);
    removeTagsNotLikelyToBeParagraphs(topNode);
    removeShortParagraphs(topNode);
    removeDisallowedAttributes(topNode);

    Document doc = new Document("");
    doc.html(topNode.html());
    return doc;
  }

  private static void replaceLineBreaksWithSpaces(Element topNode) {
    for (Element brNextToBrElement : topNode.select("br + br")) {
      brNextToBrElement.remove();
    }
    for (Element brElement : topNode.select("br")) {
      if (brElement.previousSibling() != null) {
        brElement.previousSibling().after(" • ");
      } else {
        brElement.parent().append(" • ");
      }
      brElement.unwrap();
    }
  }

  private static void removeTagsNotLikelyToBeParagraphs(Element element) {
    for (Element childElement: element.children()) {
      if (!RETAIN_TAGS.contains(childElement.tagName())) {
        printAndRemove(childElement, "removeTagsNotLikelyToBeParagraphs");
      } else if (childElement.children().size() > 0) {
        removeTagsNotLikelyToBeParagraphs(childElement);
      }
    }
  }

  private static void removeTagsButRetainContent(Element element) {
    for (Element childElement: element.children()) {
      removeTagsButRetainContent(childElement);
      if (REMOVE_TAGS_BUT_RETAIN_CONTENT.contains(childElement.tagName())) {
        childElement.unwrap();
      }
    }
  }

  private static void removeShortParagraphs(Element element) {
    List<Node> nodesToRemove = new ArrayList<>();
    for (Node childNode : element.childNodes()) {
      String text = null;
      if (childNode instanceof TextNode) {
        text = ((TextNode) childNode).text();
      } else if (childNode instanceof Element) {
        text = ((Element) childNode).text();
      }
      if (text == null ||
          text.isEmpty() ||
          text.length() < MIN_LENGTH_FOR_PARAGRAPHS ||
          text.length() > StringUtils.countLetters(text) * 2) {
        nodesToRemove.add(childNode);
      }
    }
    for (Node nodeToRemove : nodesToRemove) {
      printAndRemove(nodeToRemove, "removeShortParagraphs");
    }
  }

  private static void removeUnlikelyChildNodes(Element element) {
    for (Element childElement: element.children()) {
      if (isUnlikely(childElement)) {
        printAndRemove(childElement, "removeUnlikelyChildNodes");
      } else if (childElement.children().size() > 0) {
        removeUnlikelyChildNodes(childElement);
      }
    }
  }

  static private void removeNodesWithNegativeScores(Element topNode) {
    Elements elementsWithGravityScore = topNode.select(ExtractionHelpers.GRAVITY_SCORE_SELECTOR);
    for (Element element : elementsWithGravityScore) {
      int score = Integer.parseInt(element.attr(ExtractionHelpers.GRAVITY_SCORE_ATTRIBUTE));
      if (score < 0 || element.text().length() < MIN_LENGTH_FOR_PARAGRAPHS) {
        printAndRemove(element, "removeNodesWithNegativeScores");
      }
    }
  }

  static private boolean isUnlikely(Element element) {
    String styleAttribute = element.attr("style");
    String classAttribute = element.attr("class");
    return classAttribute != null && classAttribute.toLowerCase().contains("caption")
        || UNLIKELY_CSS_CLASSES.matcher(styleAttribute).find()
        || classAttribute != null && UNLIKELY_CSS_CLASSES.matcher(classAttribute).find();
  }

  private static void removeDisallowedAttributes(Element rootNode) {
    for (Attribute attribute : rootNode.attributes()) {
      if (!ATTRIBUTES_TO_RETAIN_IN_HTML.contains(attribute.getKey())) {
        rootNode.removeAttr(attribute.getKey());
      }
    }
    for (Element childElement: rootNode.children()) {
      removeDisallowedAttributes(childElement);
    }
  }

  private static void printAndRemove(Node node, String reason) {
    Log.i(String.format("%s [%s…]", reason, node.outerHtml().substring(0, Math.min(node.outerHtml().length(), 80)).replace("\n", "")));
    node.remove();
  }
}
