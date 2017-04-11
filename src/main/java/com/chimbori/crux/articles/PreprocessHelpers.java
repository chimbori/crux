package com.chimbori.crux.articles;

import com.chimbori.crux.common.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * Performs basic sanitization before starting the extraction process.
 */
class PreprocessHelpers {
  static void preprocess(Document doc) {
    Log.i("preprocess");
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
      if (ExtractionHelpers.NEGATIVE_CSS_CLASSES_AND_IDS.matcher(className).find() || ExtractionHelpers.NEGATIVE_CSS_CLASSES_AND_IDS.matcher(id).find()) {
        Log.printAndRemove(child, "stripUnlikelyCandidates");
      }
    }
  }

  private static void removeScriptsStylesForms(Document doc) {
    Elements scripts = doc.getElementsByTag("script");
    for (Element item : scripts) {
      Log.printAndRemove(item, "removeScriptsStylesForms('script')");
    }

    Elements noscripts = doc.getElementsByTag("noscript");
    for (Element item : noscripts) {
      Log.printAndRemove(item, "removeScriptsStylesForms('noscript')");
    }

    Elements styles = doc.getElementsByTag("style");
    for (Element item : styles) {
      Log.printAndRemove(item, "removeScriptsStylesForms('style')");
    }

    Elements forms = doc.getElementsByTag("form");
    for (Element item : forms) {
      Log.printAndRemove(item, "removeScriptsStylesForms('form')");
    }
  }

  private static void removeComments(Node node) {
    for (int i = 0; i < node.childNodes().size();) {
      Node child = node.childNode(i);
      if (child.nodeName().equals("#comment"))
        Log.printAndRemove(child, "removeComments");
      else {
        removeComments(child);
        i++;
      }
    }
  }
}
