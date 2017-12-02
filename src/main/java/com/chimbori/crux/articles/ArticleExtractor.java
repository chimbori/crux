package com.chimbori.crux.articles;

import com.chimbori.crux.common.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collection;

public class ArticleExtractor {
  private final String url;
  private final Document document;
  private final Article article;

  /**
   * Create an {@link ArticleExtractor} from a raw HTML string. The HTML must exist and should be
   * non-empty.
   */
  private ArticleExtractor(String url, String html) {
    this(url, Jsoup.parse(html));
  }

  /**
   * Create an {@link ArticleExtractor} from an already-parsed JSoup document, to be used when a
   * JSoup document has already been parsed outside this library, and saves a second duplicate
   * re-parse of the same content.
   */
  private ArticleExtractor(String url, Document document) {
    this.url = url;
    this.article = new Article(this.url);
    this.document = document;
  }

  /**
   * Create an {@link ArticleExtractor} from a raw HTML string. The HTML must exist and should be
   * non-empty.
   */
  public static ArticleExtractor with(String url, String html) {
    if (html.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return new ArticleExtractor(url, html);
  }

  /**
   * Create an {@link ArticleExtractor} from an already-parsed JSoup document, to be used when a
   * JSoup document has already been parsed outside this library, and saves a second duplicate
   * re-parse of the same content.
   */
  public static ArticleExtractor with(String url, Document document) {
    return new ArticleExtractor(url, document);
  }

  public ArticleExtractor extractMetadata() {
    article.title = MetadataHelpers.extractTitle(document);
    article.description = MetadataHelpers.extractDescription(document);
    article.siteName = MetadataHelpers.extractSiteName(document);
    article.themeColor = MetadataHelpers.extractThemeColor(document);
    article.canonicalUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractCanonicalUrl(document));
    article.ampUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractAmpUrl(document));
    article.feedUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractFeedUrl(document));
    article.videoUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractVideoUrl(document));
    article.faviconUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractFaviconUrl(document));
    article.keywords = MetadataHelpers.extractKeywords(document);
    return this;
  }

  public ArticleExtractor extractContent() {
    PreprocessHelpers.preprocess(document);

    Collection<Element> nodes = ExtractionHelpers.getNodes(document);
    int maxWeight = 0;
    Element bestMatchElement = null;
    for (Element element : nodes) {
      int currentWeight = ExtractionHelpers.getWeight(element);
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight;
        bestMatchElement = element;
        if (maxWeight > 200) {
          break;
        }
      }
    }

    // Extract images before post-processing, because that step may remove images.
    article.images = ImageHelpers.extractImages(bestMatchElement);
    article.document = PostprocessHelpers.postprocess(bestMatchElement);
    article.imageUrl = StringUtils.makeAbsoluteUrl(article.url, MetadataHelpers.extractImageUrl(document, article.images));
    return this;
  }

  public Article article() {
    return article;
  }

  public String url() {
    return url;
  }
}
