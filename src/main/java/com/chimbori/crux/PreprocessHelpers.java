package com.chimbori.crux;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * Performs basic sanitization before starting the extraction process.
 */
class PreprocessHelpers {
  static void preprocess(Document doc) {
    stripUnlikelyCandidates(doc);
    removeScriptsStylesForms(doc);
    removeComments(doc.body());
  }

  /**
   * Removes unlikely candidates from HTML. It often ends up removing more than just the unlikely
   * candidates, so exercise caution when enabling this.
   */
  private static void stripUnlikelyCandidates(Document doc) {
    if (true) {
      return;  // Temporarily disabled; see comment above.
    }

    for (Element child : doc.select("body").select("*")) {
      String className = child.className().toLowerCase();
      String id = child.id().toLowerCase();
      if (ExtractionHelpers.NEGATIVE.matcher(className).find() || ExtractionHelpers.NEGATIVE.matcher(id).find()) {
        child.remove();
      }
    }
  }

  private static Document removeScriptsStylesForms(Document doc) {
    Elements scripts = doc.getElementsByTag("script");
    for (Element item : scripts) {
      item.remove();
    }

    Elements noscripts = doc.getElementsByTag("noscript");
    for (Element item : noscripts) {
      item.remove();
    }

    Elements styles = doc.getElementsByTag("style");
    for (Element item : styles) {
      item.remove();
    }

    Elements forms = doc.getElementsByTag("form");
    for (Element item : forms) {
      item.remove();
    }

    return doc;
  }

  private static void removeComments(Node node) {
    for (int i = 0; i < node.childNodes().size();) {
      Node child = node.childNode(i);
      if (child.nodeName().equals("#comment"))
        child.remove();
      else {
        removeComments(child);
        i++;
      }
    }
  }
}
