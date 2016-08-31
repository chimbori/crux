package com.chimbori.snacktroid;

import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parsed result from web page containing important title, text and image.
 *
 * All fields are public for performance reasons on Android.
 * @link https://developer.android.com/training/articles/perf-tips.html#GettersSetters
 *
 * Avoid Internal Getters/Setters
 *
 * In native languages like C++ it's common practice to use getters (i = getCount()) instead of
 * accessing the field directly (i = mCount). This is an excellent habit for C++ and is often
 * practiced in other object oriented languages like C# and Java, because the compiler can usually
 * inline the access, and if you need to restrict or debug field access you can add the code at any
 * time.
 *
 * However, this is a bad idea on Android. Virtual method calls are expensive, much more so than
 * instance field lookups. It's reasonable to follow common object-oriented programming practices
 * and have getters and setters in the public interface, but within a class you should always
 * access fields directly.
 *
 * Without a JIT, direct field access is about 3x faster than invoking a trivial getter. With the
 * JIT (where direct field access is as cheap as accessing a local), direct field access is about
 * 7x faster than invoking a trivial getter.
 */
public class ParsedResult {
  public String title = "";
  public String url = "";
  public String originalUrl = "";
  public String canonicalUrl = "";
  public String imageUrl = "";
  public String videoUrl = "";
  public String feedUrl = "";
  public String text = "";
  public String faviconUrl = "";
  public String description = "";
  public String dateString = "";
  public List<String> textList = new ArrayList<>();
  public Collection<String> keywords;
  public List<ImageResult> images = new ArrayList<>();


  ParsedResult() {
    // Package private constructor to disallow outside the library.
  }

  public ParsedResult ensureAbsoluteUrls() throws MalformedURLException {
    URL absoluteArticleUrl = new URL(url);
    canonicalUrl = new URL(absoluteArticleUrl, canonicalUrl).toString();
    imageUrl = new URL(absoluteArticleUrl, imageUrl).toString();
    videoUrl = new URL(absoluteArticleUrl, videoUrl).toString();
    feedUrl = new URL(absoluteArticleUrl, feedUrl).toString();
    faviconUrl = new URL(absoluteArticleUrl, faviconUrl).toString();
    return this;
  }

  @Override
  public String toString() {
    return "ParsedResult{" +
        "title='" + title + '\'' +
        ", url='" + url + '\'' +
        ", originalUrl='" + originalUrl + '\'' +
        ", canonicalUrl='" + canonicalUrl + '\'' +
        ", imageUrl='" + imageUrl + '\'' +
        ", videoUrl='" + videoUrl + '\'' +
        ", feedUrl='" + feedUrl + '\'' +
        ", text='" + text + '\'' +
        ", faviconUrl='" + faviconUrl + '\'' +
        ", description='" + description + '\'' +
        ", dateString='" + dateString + '\'' +
        ", textList=" + textList +
        ", keywords=" + keywords +
        ", images=" + images +
        '}';
  }

  /**
   * Class which encapsulates the data from an image found under an element
   */
  static class ImageResult {
    public final String src;
    public final Integer weight;
    private final String title;
    private final int height;
    private final int width;
    private final String alt;
    private final boolean noFollow;
    public Element element;

    ImageResult(String src, Integer weight, String title, int height, int width, String alt, boolean noFollow) {
      this.src = src;
      this.weight = weight;
      this.title = title;
      this.height = height;
      this.width = width;
      this.alt = alt;
      this.noFollow = noFollow;
    }
  }
}
