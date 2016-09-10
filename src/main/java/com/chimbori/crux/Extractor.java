package com.chimbori.crux;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Extractor {
  public Article extractContent(String html) {
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return extractContent(Jsoup.parse(html));
  }

  private Article extractContent(Document doc) {
    if (doc == null) {
      throw new IllegalArgumentException();
    }

    Article article = new Article();
    article.title = ExtractionHelpers.extractTitle(doc);
    article.description = ExtractionHelpers.extractDescription(doc);
    article.canonicalUrl = ExtractionHelpers.extractCanonicalUrl(doc);

    PreprocessHelpers.preprocess(doc);

    Collection<Element> nodes = ExtractionHelpers.getNodes(doc);
    int maxWeight = 0;
    Element bestMatchElement = null;
    for (Element entry : nodes) {
      int currentWeight = ExtractionHelpers.getWeight(entry);
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight;
        bestMatchElement = entry;
        if (maxWeight > 200) {
          break;
        }
      }
    }

    if (bestMatchElement != null) {
      List<Article.Image> images = new ArrayList<>();
      Element imgEl = ExtractionHelpers.determineImageSource(bestMatchElement, images);
      if (imgEl != null) {
        article.imageUrl = StringUtils.urlEncodeSpaceCharacter(imgEl.attr("src"));
        // TODO remove parent container of image if it is contained in bestMatchElement
        // to avoid image subtitles flooding in
        article.images = images;
      }

      article.document = PostprocessHelpers.postprocess(bestMatchElement);
    }

    if (article.imageUrl.isEmpty()) {
      article.imageUrl = ExtractionHelpers.extractImageUrl(doc);
    }

    article.feedUrl = ExtractionHelpers.extractFeedUrl(doc);
    article.videoUrl = ExtractionHelpers.extractVideoUrl(doc);
    article.faviconUrl = ExtractionHelpers.extractFaviconUrl(doc);
    article.keywords = ExtractionHelpers.extractKeywords(doc);
    return article;
  }
}