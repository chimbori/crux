package com.chimbori.crux.articles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.chimbori.crux.common.Log;
import com.chimbori.crux.common.StringUtils;

/**
 * Cleans up the best-match Element after one has been picked, in order to provide a sanitized
 * output tree to the caller.
 */
class PostprocessHelpers {
  /**
   * If a string is shorter than this limit, it is not considered a paragraph.
   */
  private static final int MIN_LENGTH_FOR_PARAGRAPHS = 50;

  private static final Pattern UNLIKELY_CSS_STYLES = Pattern.compile("display\\:none|visibility\\:hidden");

  /**
   * Tags that should not be output, but still may contain interesting content.
   */
  private static final Set<String> REMOVE_TAGS_BUT_RETAIN_CONTENT = new HashSet<>(Arrays.asList(
      "font", "table", "tbody", "tr", "td", "div", "ol", "ul", "li", "span"
  ));

  /**
   * Tags that should be retained in the output. This list should be fairly minimal, and equivalent
   * to the list of tags that callers can be expected to be able to handle.
   */
  private static final Set<String> RETAIN_TAGS = new HashSet<>(Arrays.asList(
      "p", "b", "i", "u", "strong", "em", "a", "pre", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote"
  ));

  /**
   * Tags that can contain really short content, because they are not paragraph-level tags. Content
   * within these tags is not subject to the {@code MIN_LENGTH_FOR_PARAGRAPHS} requirement.
   */
  private static final Set<String> TAGS_EXEMPT_FROM_MIN_LENGTH_CHECK = new HashSet<>(Arrays.asList(
      "b", "i", "u", "strong", "em", "a", "pre", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote"
  ));

  /**
   * The whitelist of attributes that should be retained in the output. No other attributes
   * will be retained.
   */
  private static final Set<String> ATTRIBUTES_TO_RETAIN_IN_HTML = new HashSet<>(Arrays.asList(
      "href"
  ));

  /**
   * After a final set of top-level nodes has been extracted, all tags except these are removed.
   * This ensures that while inline tags containing shorter text, e.g. <a href="…">one word</a>
   * are kept as part of a larger paragraph, those same short tags are not allowed to be
   * top-level children.
   */
  private static final Set<String> RETAIN_TAGS_TOP_LEVEL = new HashSet<>(Arrays.asList(
      "p", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "li"
  ));
  
  static Document postprocess(Element topNode) {
    Log.i("postprocess");
    Document doc = new Document("");
    if (topNode == null) {
      return doc;
    }
    
    Set<Node> keepers=Collections.newSetFromMap(
        new IdentityHashMap<Node,Boolean>());
    for(Element element : topNode.select("[crux-keep]"))
        keepers.addAll(lineage(topNode, element));
    
    PostprocessHelpers helper=new PostprocessHelpers(keepers);

    helper.removeNodesWithNegativeScores(topNode);
    helper.replaceLineBreaksWithSpaces(topNode);
    helper.removeUnlikelyChildNodes(topNode);
    helper.removeTagsButRetainContent(topNode);
    helper.removeTagsNotLikelyToBeParagraphs(topNode);
    helper.removeTopLevelTagsNotLikelyToBeParagraphs(topNode);
    helper.removeShortParagraphs(topNode);
    helper.removeDisallowedAttributes(topNode);

    for (Node node : topNode.childNodes()) {
      doc.appendChild(node.clone());  // TODO: Don’t copy each item separately.
    }
    return doc;
  }
  
  private static Collection<Node> lineage(Element root, Element e) {
    List<Node> result=new ArrayList<>();
      
    // Add all ancestors, up to the top node
    for (Node n = e; n != root; n = n.parentNode())
      result.add(n);
        
    // Add all descendants
    Queue<Node> nodes = new ArrayDeque<>(e.childNodes());
    while (!nodes.isEmpty()) {
      Node n = nodes.poll();
      result.add(n);
      for (Node c : n.childNodes())
        nodes.offer(c);
    }

    return result;
  }

  private Set<Node> keepers;
  
  private PostprocessHelpers(Set<Node> keepers) {
    this.keepers = keepers;
  }

  private void replaceLineBreaksWithSpaces(Element topNode) {
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

  private void removeTopLevelTagsNotLikelyToBeParagraphs(Element element) {
    for (Element childElement : element.children()) {
      if (!RETAIN_TAGS_TOP_LEVEL.contains(childElement.tagName())) {
        if (!shouldKeep(childElement))
          Log.printAndRemove(childElement, "removeTopLevelTagsNotLikelyToBeParagraphs");
      }
    }
  }

  private void removeTagsNotLikelyToBeParagraphs(Element element) {
    for (Element childElement : element.children()) {
      if (!RETAIN_TAGS.contains(childElement.tagName())) {
        if (!shouldKeep(childElement))
          Log.printAndRemove(childElement, "removeTagsNotLikelyToBeParagraphs");
      } else if (childElement.children().size() > 0) {
        removeTagsNotLikelyToBeParagraphs(childElement);
      }
    }
  }

  private void removeTagsButRetainContent(Element element) {
    for (Element childElement : element.children()) {
      removeTagsButRetainContent(childElement);
      if (REMOVE_TAGS_BUT_RETAIN_CONTENT.contains(childElement.tagName())) {
        Log.i("removeTagsButRetainContent: [%s] %s", childElement.tagName(), childElement.outerHtml());
        childElement.tagName("p");  // Set the wrapper tag to <p> instead of unwrapping them.
      }
    }
  }

  private void removeShortParagraphs(Element topNode) {
    for (int i = topNode.childNodeSize() - 1; i >= 0; i--) {
      Node childNode = topNode.childNode(i);

      String text = null;
      boolean isExemptFromMinTextLengthCheck = false;
      if (childNode instanceof TextNode) {
        text = ((TextNode) childNode).text().trim();

      } else if (childNode instanceof Element) {
        Element childElement = (Element) childNode;
        text = childElement.text().trim();
        isExemptFromMinTextLengthCheck = TAGS_EXEMPT_FROM_MIN_LENGTH_CHECK.contains(childElement.tagName());
      }

      Log.i("removeShortParagraphs: [%s] isExemptFromMinTextLengthCheck : %b", childNode, isExemptFromMinTextLengthCheck);

      if (text == null ||
          text.isEmpty() ||
          (!isExemptFromMinTextLengthCheck && text.length() < MIN_LENGTH_FOR_PARAGRAPHS) ||
          text.length() > StringUtils.countLetters(text) * 2) {
        if (!shouldKeep(childNode))
          Log.printAndRemove(childNode, "removeShortParagraphs:");
      }
    }
  }

  private void removeUnlikelyChildNodes(Element element) {
    for (Element childElement : element.children()) {
      if (isUnlikely(childElement)) {
        if (!shouldKeep(childElement))
          Log.printAndRemove(childElement, "removeUnlikelyChildNodes");
      } else if (childElement.children().size() > 0) {
        removeUnlikelyChildNodes(childElement);
      }
    }
  }

  private void removeNodesWithNegativeScores(Element topNode) {
    Elements elementsWithGravityScore = topNode.select(ExtractionHelpers.GRAVITY_SCORE_SELECTOR);
    for (Element element : elementsWithGravityScore) {
      int score = Integer.parseInt(element.attr(ExtractionHelpers.GRAVITY_SCORE_ATTRIBUTE));
      if (score < 0 || element.text().length() < MIN_LENGTH_FOR_PARAGRAPHS) {
        if (!shouldKeep(element))
          Log.printAndRemove(element, "removeNodesWithNegativeScores");
      }
    }
  }

  private boolean isUnlikely(Element element) {
    String styleAttribute = element.attr("style");
    String classAttribute = element.attr("class");
    return classAttribute != null && classAttribute.toLowerCase().contains("caption")
        || UNLIKELY_CSS_STYLES.matcher(styleAttribute).find()
        || classAttribute != null && UNLIKELY_CSS_STYLES.matcher(classAttribute).find();
  }

  private void removeDisallowedAttributes(Element node) {
    for (Element childElement : node.children()) {
      removeDisallowedAttributes(childElement);
    }
    List<String> keysToRemove = new LinkedList<>();
    for (Attribute attribute : node.attributes()) {
      if (!ATTRIBUTES_TO_RETAIN_IN_HTML.contains(attribute.getKey())) {
        keysToRemove.add(attribute.getKey());
      }
    }
    for (String key: keysToRemove) {
      node.removeAttr(key);
    }
  }

  private boolean shouldKeep(Node node) {
    return keepers.contains(node);
  }
}
