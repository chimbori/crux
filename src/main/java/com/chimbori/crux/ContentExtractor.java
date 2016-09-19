package com.chimbori.crux;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collection;

public class ContentExtractor {
  public Article extractContent(String baseUri, String html) {
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return extractContent(baseUri, Jsoup.parse(html));
  }

  private Article extractContent(String baseUri, Document doc) {
    if (doc == null) {
      throw new IllegalArgumentException();
    }

    Article article = new Article(baseUri);
    article.title = MetadataHelpers.extractTitle(doc);
    article.description = MetadataHelpers.extractDescription(doc);
    article.siteName = MetadataHelpers.extractSiteName(doc);
    article.themeColor = MetadataHelpers.extractThemeColor(doc);
    article.canonicalUrl = article.makeAbsoluteUrl(MetadataHelpers.extractCanonicalUrl(doc));
    article.ampUrl = article.makeAbsoluteUrl(MetadataHelpers.extractAmpUrl(doc));

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

    article.images = MetadataHelpers.extractImages(bestMatchElement);
    article.document = PostprocessHelpers.postprocess(bestMatchElement);
    article.imageUrl = article.makeAbsoluteUrl(MetadataHelpers.extractImageUrl(doc, article.images));
    article.feedUrl = article.makeAbsoluteUrl(MetadataHelpers.extractFeedUrl(doc));
    article.videoUrl = article.makeAbsoluteUrl(MetadataHelpers.extractVideoUrl(doc));
    article.faviconUrl = article.makeAbsoluteUrl(MetadataHelpers.extractFaviconUrl(doc));
    article.keywords = MetadataHelpers.extractKeywords(doc);
    return article;
  }
}