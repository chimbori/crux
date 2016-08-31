package com.chimbori.snacktroid;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Extractor {
  private static final OutputFormatter DEFAULT_FORMATTER = new OutputFormatter();
  private Formatter formatter = DEFAULT_FORMATTER;

  public Extractor setFormatter(Formatter formatter) {
    this.formatter = formatter;
    return this;
  }

  public ParsedResult extractContent(String html) {
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return extractContent(Jsoup.parse(html), formatter);
  }

  private ParsedResult extractContent(Document doc, Formatter formatter) {
    if (doc == null) {
      throw new IllegalArgumentException();
    }

    ParsedResult res = new ParsedResult();
    res.title = ExtractionHelpers.extractTitle(doc);
    res.description = ExtractionHelpers.extractDescription(doc);
    res.canonicalUrl = ExtractionHelpers.extractCanonicalUrl(doc);

    // now remove the clutter
    ExtractionHelpers.prepareDocument(doc);

    // init elements
    Collection<Element> nodes = ExtractionHelpers.getNodes(doc);
    int maxWeight = 0;
    Element bestMatchElement = null;
    for (Element entry : nodes) {
      int currentWeight = ExtractionHelpers.getWeight(entry);
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight;
        bestMatchElement = entry;
        if (maxWeight > 200)
          break;
      }
    }

    if (bestMatchElement != null) {
      List<ParsedResult.ImageResult> images = new ArrayList<>();
      Element imgEl = ExtractionHelpers.determineImageSource(bestMatchElement, images);
      if (imgEl != null) {
        res.imageUrl = StringUtils.urlEncodeSpaceCharacter(imgEl.attr("src"));
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
      res.imageUrl = ExtractionHelpers.extractImageUrl(doc);
    }

    res.feedUrl = ExtractionHelpers.extractFeedUrl(doc);
    res.videoUrl = ExtractionHelpers.extractVideoUrl(doc);
    res.faviconUrl = ExtractionHelpers.extractFaviconUrl(doc);
    res.keywords = ExtractionHelpers.extractKeywords(doc);
    return res;
  }

  public interface Formatter {
    /**
     * takes an element and turns the P tags into \n\n
     */
    String getFormattedText(Element topNode);

    /**
     * Takes an element and returns a list of texts extracted from the P tags.
     */
    List<String> getTextList(Element topNode);
  }
}