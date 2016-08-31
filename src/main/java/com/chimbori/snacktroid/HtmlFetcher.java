package com.chimbori.snacktroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Class to fetch articles. This class is thread safe.
 */
public class HtmlFetcher {

  static {
    StringUtils.enableCookieMgmt();
    StringUtils.enableUserAgentOverwrite();
    StringUtils.enableAnySSL();
  }

  private static final Logger logger = Logger.getInstance();

  public static void main(String[] args) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader("urls.txt"));
    String line;
    Set<String> existing = new LinkedHashSet<>();
    while ((line = reader.readLine()) != null) {
      int index1 = line.indexOf("\"");
      int index2 = line.indexOf("\"", index1 + 1);
      String url = line.substring(index1 + 1, index2);
      String domainStr = StringUtils.extractDomain(url, true);
      String counterStr = "";
      // TODO more similarities
      if (existing.contains(domainStr))
        counterStr = "2";
      else
        existing.add(domainStr);

      String html = new HtmlFetcher().fetchAsString(url, 20000);
      String outFile = domainStr + counterStr + ".html";
      BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
      writer.write(html);
      writer.close();
    }
    reader.close();
  }

  private String referrer = "https://github.com/karussell/snacktory";
  private String userAgent = "Mozilla/5.0 (compatible; Snacktory; +" + referrer + ")";
  private String cacheControl = "max-age=0";
  private String language = "en-us";
  private String accept = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
  private String charset = "UTF-8";
  private Proxy proxy = null;
  private int maxTextLength = -1;
  private Extractor extractor = new Extractor();
  private final Set<String> furtherResolveNecessary = new LinkedHashSet<String>() {
    {
      add("bit.ly");
      add("cli.gs");
      add("deck.ly");
      add("fb.me");
      add("feedproxy.google.com");
      add("flic.kr");
      add("fur.ly");
      add("goo.gl");
      add("is.gd");
      add("ink.co");
      add("j.mp");
      add("lnkd.in");
      add("on.fb.me");
      add("ow.ly");
      add("plurl.us");
      add("sns.mx");
      add("snurl.com");
      add("su.pr");
      add("t.co");
      add("tcrn.ch");
      add("tl.gd");
      add("tiny.cc");
      add("tinyurl.com");
      add("tmi.me");
      add("tr.im");
      add("twurl.nl");
    }
  };

  public HtmlFetcher() {
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public Proxy getProxy() {
    return (proxy != null ? proxy : Proxy.NO_PROXY);
  }

  public ParsedResult fetchAndExtract(String url, int timeout, boolean resolve) throws IOException {
    String originalUrl = url;
    url = StringUtils.removeHashbang(url);
    String gUrl = StringUtils.getUrlFromUglyGoogleRedirect(url);
    if (gUrl != null)
      url = gUrl;
    else {
      gUrl = StringUtils.getUrlFromUglyFacebookRedirect(url);
      if (gUrl != null)
        url = gUrl;
    }

    if (resolve) {
      String resUrl = getResolvedUrl(url, timeout);
      if (resUrl.isEmpty()) {
        if (logger.isDebugEnabled())
          logger.warn("resolved url is empty. Url is: " + url);

        ParsedResult result = new ParsedResult();
        result.url = url;
        return result;
      }

      // if resolved url is longer then use it!
      if (resUrl != null && resUrl.trim().length() > url.length()) {
        // this is necessary e.g. for some homebaken url resolvers which return
        // the resolved url relative to url!
        url = StringUtils.useDomainOfFirstArg4Second(url, resUrl);
      }
    }

    ParsedResult result = new ParsedResult();
    // or should we use? <link rel="canonical" href="http://www.N24.de/news/newsitem_6797232.html"/>
    result.url = url;
    result.originalUrl = originalUrl;
    result.dateString = StringUtils.estimateDate(url);
    
    String lowerUrl = url.toLowerCase();
    if (StringUtils.isDoc(lowerUrl) || StringUtils.isApp(lowerUrl) || StringUtils.isPackage(lowerUrl)) {
      // skip
    } else if (StringUtils.isVideo(lowerUrl) || StringUtils.isAudio(lowerUrl)) {
      result.videoUrl = url;
    } else if (StringUtils.isImage(lowerUrl)) {
      result.imageUrl = url;
    } else {
      extractor.extractContent(result, fetchAsString(url, timeout));
      if (result.faviconUrl.isEmpty()) {
        result.faviconUrl = StringUtils.getDefaultFavicon(url);
      }

      // some links are relative to root and do not include the domain of the url :(
      result.faviconUrl = fixUrl(url, result.faviconUrl);
      result.imageUrl = fixUrl(url, result.imageUrl);
      result.videoUrl = fixUrl(url, result.videoUrl);
      result.rssUrl = fixUrl(url, result.rssUrl);
    }
    result.text = lessText(result.text);
    synchronized (result) {
      result.notifyAll();
    }
    return result;
  }

  private String lessText(String text) {
    if (text == null)
      return "";

    if (maxTextLength >= 0 && text.length() > maxTextLength)
      return text.substring(0, maxTextLength);

    return text;
  }

  private static String fixUrl(String url, String urlOrPath) {
    return StringUtils.useDomainOfFirstArg4Second(url, urlOrPath);
  }

  public String fetchAsString(String urlAsString, int timeout) throws IOException {
    HttpURLConnection hConn = createUrlConnection(urlAsString, timeout);
    hConn.setInstanceFollowRedirects(true);
    String encoding = hConn.getContentEncoding();
    InputStream inputStream;
    if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
      inputStream = new GZIPInputStream(hConn.getInputStream());
    } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
      inputStream = new InflaterInputStream(hConn.getInputStream(), new Inflater(true));
    } else {
      inputStream = hConn.getInputStream();
    }

    String res = CharsetConverter.readStream(inputStream,
        CharsetConverter.extractEncoding(hConn.getContentType())).content;
    if (logger.isDebugEnabled())
      logger.debug(res.length() + " FetchAsString:" + urlAsString);
    return res;
  }

  /**
   * On some devices we have to hack:
   * http://developers.sun.com/mobility/reference/techart/design_guidelines/http_redirection.html
   *
   * @param timeout Sets a specified timeout value, in milliseconds
   * @return the resolved url if any. Or null if it couldn't resolve the url
   * (within the specified time) or the same url if response code is OK
   */
  private String getResolvedUrl(String urlAsString, int timeout) {
    String newUrl = null;
    int responseCode = -1;
    try {
      HttpURLConnection hConn = createUrlConnection(urlAsString, timeout);
      // force no follow
      hConn.setInstanceFollowRedirects(false);
      // the program doesn't care what the content actually is !!
      // http://java.sun.com/developer/JDCTechTips/2003/tt0422.html
      hConn.setRequestMethod("HEAD");
      hConn.connect();
      responseCode = hConn.getResponseCode();
      hConn.getInputStream().close();
      if (responseCode == HttpURLConnection.HTTP_OK)
        return urlAsString;

      newUrl = hConn.getHeaderField("Location");
      if (responseCode / 100 == 3 && newUrl != null) {
        newUrl = newUrl.replaceAll(" ", "+");
        // some services use (none-standard) utf8 in their location header
        if (urlAsString.startsWith("http://bit.ly") || urlAsString.startsWith("http://is.gd"))
          newUrl = encodeUriFromHeader(newUrl);

        // fix problems if shortened twice. as it is often the case after twitters' t.co bullshit
        if (furtherResolveNecessary.contains(StringUtils.extractDomain(newUrl, true)))
          newUrl = getResolvedUrl(newUrl, timeout);

        return newUrl;
      } else
        return urlAsString;

    } catch (Exception ex) {
      logger.warn("getResolvedUrl:" + urlAsString + " Error:" + ex.getMessage(), ex);
      return "";
    } finally {
      if (logger.isDebugEnabled())
        logger.debug(responseCode + " url:" + urlAsString + " resolved:" + newUrl);
    }
  }

  /**
   * Takes a URI that was decoded as ISO-8859-1 and applies percent-encoding
   * to non-ASCII characters. Workaround for broken origin servers that send
   * UTF-8 in the Location: header.
   */
  private static String encodeUriFromHeader(String badLocation) {
    StringBuilder sb = new StringBuilder();

    for (char ch : badLocation.toCharArray()) {
      if (ch < (char) 128) {
        sb.append(ch);
      } else {
        // this is ONLY valid if the uri was decoded using ISO-8859-1
        sb.append(String.format("%%%02X", (int) ch));
      }
    }

    return sb.toString();
  }

  private HttpURLConnection createUrlConnection(String urlAsStr, int timeout) throws IOException {
    URL url = new URL(urlAsStr);
    //using proxy may increase latency
    Proxy proxy = getProxy();
    HttpURLConnection hConn = (HttpURLConnection) url.openConnection(proxy);
    hConn.setRequestProperty("User-Agent", userAgent);
    hConn.setRequestProperty("Accept", accept);

    hConn.setRequestProperty("Accept-Language", language);
    hConn.setRequestProperty("content-charset", charset);
    hConn.addRequestProperty("Referer", referrer);
    // avoid the cache for testing purposes only?
    hConn.setRequestProperty("Cache-Control", cacheControl);

    // suggest respond to be gzipped or deflated (which is just another compression)
    // http://stackoverflow.com/q/3932117
    hConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    hConn.setConnectTimeout(timeout);
    hConn.setReadTimeout(timeout);
    return hConn;
  }
}
