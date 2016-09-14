package com.chimbori.crux;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parsed result from web page containing important title, text and image.
 * <p>
 * All fields are public for performance reasons on Android.
 *
 * @link https://developer.android.com/training/articles/perf-tips.html#GettersSetters
 * <p>
 * Avoid Internal Getters/Setters
 * <p>
 * In native languages like C++ it's common practice to use getters (i = getCount()) instead of
 * accessing the field directly (i = mCount). This is an excellent habit for C++ and is often
 * practiced in other object oriented languages like C# and Java, because the compiler can usually
 * inline the access, and if you need to restrict or debug field access you can add the code at any
 * time.
 * <p>
 * However, this is a bad idea on Android. Virtual method calls are expensive, much more so than
 * instance field lookups. It's reasonable to follow common object-oriented programming practices
 * and have getters and setters in the public interface, but within a class you should always
 * access fields directly.
 * <p>
 * Without a JIT, direct field access is about 3x faster than invoking a trivial getter. With the
 * JIT (where direct field access is as cheap as accessing a local), direct field access is about
 * 7x faster than invoking a trivial getter.
 */
public class Article {
  public final String url;

  public String title = "";
  public String description = "";

  public String ampUrl = "";
  public String originalUrl = "";
  public String canonicalUrl = "";
  public String imageUrl = "";
  public String videoUrl = "";
  public String feedUrl = "";
  public String faviconUrl = "";

  public Document document;
  public Collection<String> keywords;
  public List<Image> images = new ArrayList<>();

  Article(String url) {
    // Package private constructor to disallow outside the library.
    this.url = url;
    this.canonicalUrl = url;  // Can be overridden later, but we start off by setting it to the URL itself.
  }

  String makeAbsoluteUrl(String relativeUrl) {
    if (relativeUrl == null || relativeUrl.isEmpty()) {
      return null;
    }
    try {
      return new URL(new URL(url), relativeUrl).toString();
    } catch (MalformedURLException e) {
      return relativeUrl;
    }
  }

  @Override
  public String toString() {
    return "Article{" +
        "url='" + url + '\'' +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", ampUrl='" + ampUrl + '\'' +
        ", originalUrl='" + originalUrl + '\'' +
        ", canonicalUrl='" + canonicalUrl + '\'' +
        ", imageUrl='" + imageUrl + '\'' +
        ", videoUrl='" + videoUrl + '\'' +
        ", feedUrl='" + feedUrl + '\'' +
        ", faviconUrl='" + faviconUrl + '\'' +
        ", document=" + document +
        ", keywords=" + keywords +
        ", images=" + images +
        '}';
  }

  /**
   * Class which encapsulates the data from an image found under an element
   */
  static class Image {
    public String src;
    public int weight;
    public String title;
    public int height;
    public int width;
    public String alt;
    public boolean noFollow;
    public Element element;

    private Image() {
    }

    static Image from(Element imgElement) {
      Image image = new Image();
      image.element = imgElement;
      image.src = imgElement.attr("src");
      image.width = StringUtils.parseAttrAsInt(imgElement, "width");
      image.height = StringUtils.parseAttrAsInt(imgElement, "height");
      image.alt = imgElement.attr("alt");
      image.title = imgElement.attr("title");
      image.noFollow = imgElement.parent() != null && imgElement.parent().attr("rel") != null && imgElement.parent().attr("rel").contains("nofollow");
      return image;
    }

    @Override
    public String toString() {
      return "Image{" +
          "src='" + src + '\'' +
          ", weight=" + weight +
          ", title='" + title + '\'' +
          ", height=" + height +
          ", width=" + width +
          ", alt='" + alt + '\'' +
          ", noFollow=" + noFollow +
          ", element=" + element +
          '}';
    }
  }
}
