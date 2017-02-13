package com.chimbori.crux.articles;

import com.chimbori.crux.common.UrlUtils;
import com.chimbori.crux.urls.CandidateURL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collection;

public class Extractor {
  private final CandidateURL candidateURL;
  private final Document document;
  private final Article article;

  private Extractor(CandidateURL candidateURL, String html) {
    this.candidateURL = candidateURL;
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.document = Jsoup.parse(html);
    this.article = new Article(candidateURL.url.toString());
  }

  public static Extractor with(CandidateURL candidateURL, String html) {
    return new Extractor(candidateURL, html);
  }

  public Extractor extractMetadata() {
    article.title = MetadataHelpers.extractTitle(document);
    article.description = MetadataHelpers.extractDescription(document);
    article.siteName = MetadataHelpers.extractSiteName(document);
    article.themeColor = MetadataHelpers.extractThemeColor(document);
    article.canonicalUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractCanonicalUrl(document));
    article.ampUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractAmpUrl(document));
    article.feedUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractFeedUrl(document));
    article.videoUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractVideoUrl(document));
    article.faviconUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractFaviconUrl(document));
    article.keywords = MetadataHelpers.extractKeywords(document);
    return this;
  }

  public Extractor extractContent() {
    PreprocessHelpers.preprocess(document);

    Collection<Element> nodes = ExtractionHelpers.getNodes(document);
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

    // Extract images before post-processing, because that step may remove images.
    article.images = ImageHelpers.extractImages(bestMatchElement);
    article.document = PostprocessHelpers.postprocess(bestMatchElement);
    article.imageUrl = UrlUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractImageUrl(document, article.images));
    return this;
  }

  public Article article() {
    return article;
  }

  public CandidateURL url() {
    return candidateURL;
  }
}