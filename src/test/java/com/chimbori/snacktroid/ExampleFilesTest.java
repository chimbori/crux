package com.chimbori.snacktroid;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExampleFilesTest {
  private Extractor extractor;

  @Before
  public void setup() {
    extractor = new Extractor();
  }

  @Test
  public void testNPR() {
    // ? http://www.npr.org/blogs/money/2010/10/04/130329523/how-fake-money-saved-brazil
    Article article = extractFromTestFile("npr.html");
    assertEquals("How Fake Money Saved Brazil : Planet Money : NPR", article.title);
    assertStartsWith("This is a story about how an economist and his buddies tricked the people of Brazil into saving the country from rampant inflation. They had a crazy, unlikely plan, and it worked. Twenty years ago, Brazil's", article.document.text());
    assertTrue(article.document.text(), article.document.text().endsWith("\"How Four Drinking Buddies Saved Brazil.\""));
    assertEquals("http://media.npr.org/assets/img/2010/10/04/real_wide.jpg?t=1286218782&s=3", article.imageUrl);
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testBenjaminStein() {
    // http://benjaminste.in/post/1223476561/hey-guys-whatcha-doing
    Article article = extractFromTestFile("benjaminstein.html");
    assertEquals("BenjaminSte.in - Hey guys, whatcha doing?", article.title);
    assertStartsWith("This month is the 15th anniversary of my last CD.", article.document.text());
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testYCombinator() {
    Article article = extractFromTestFile("ycombinator.html");
    assertEquals(1, article.document.childNodeSize());
    assertStartsWith("", article.document.text());
    assertStartsWith("<p> October 2010 • Silicon Valley proper is mostly suburban sprawl.", article.document.html());
    assertTrue(article.document.text(), article.document.text().endsWith(" and Jessica Livingston for reading drafts of this. •"));
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testTraindom() {
    // http://blog.traindom.com/places-where-to-submit-your-startup-for-coverage/
    Article article = extractFromTestFile("traindom.html");
    assertEquals("36 places where you can submit your startup for some coverage", article.title);
    assertEquals(Arrays.asList("blog coverage", "get coverage", "startup review", "startups", "submit startup"), article.keywords);
    assertStartsWith("So you have a new startup company and want some coverage", article.document.text());
  }

  @Test
  public void testAirBnB() {
    Article article = extractFromTestFile("airbnb.html");
    assertStartsWith("Hackers unite in Stanford", article.document.text());
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testToloNews() {
    Article article = extractFromTestFile("tolonews.html");
    assertEquals("Acting Governor of Balkh province, Atta Mohammad Noor, said that differences between leaders of the National Unity Government (NUG) – namely President Ashraf Ghani and CEO Abdullah Abdullah— have paved the ground for mounting insecurity. Hundreds of worried relatives gathered outside Kabul hospitals on Tuesday desperate for news of loved ones following the deadly suicide bombing earlier in the day.", article.document.text());
  }

  @Test
  public void testKhaamaPress() {
    Article article = extractFromTestFile("khaama.html");
    assertStartsWith("Over 100 school girls have been poisoned in western Farah province of Afghanistan during the school hours.", article.document.text());
  }

  @Test
  public void testCNN() {
    // http://edition.cnn.com/2011/WORLD/africa/04/06/libya.war/index.html?on.cnn=1
    Article article = extractFromTestFile("cnn.html");
    assertEquals("Gadhafi asks Obama to end NATO bombing - CNN.com", article.title);
    assertEquals("/2011/WORLD/africa/04/06/libya.war/t1larg.libyarebel.gi.jpg", article.imageUrl);
    assertStartsWith("Tripoli, Libya (CNN) -- As rebel and pro-government forces in Libya maneuvered on the battlefield Wedn", article.document.text());
  }

  @Test
  public void testBBC() {
    // http://www.bbc.co.uk/news/world-latin-america-21226565
    Article article = extractFromTestFile("bbc_noscript.html");
    assertEquals("BBC News - Brazil mourns Santa Maria nightclub fire victims", article.title);
    assertEquals("http://news.bbcimg.co.uk/media/images/65545000/gif/_65545798_brazil_santa_m_kiss_464.gif", article.imageUrl);
    assertStartsWith("Brazil has declared three days of national mourning for 231 people killed in a nightclub fire in the southern city of Santa Maria.", article.document.text());
  }

  @Test
  public void testReuters() {
    // http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803
    Article article = extractFromTestFile("reuters.html");
    assertEquals("Knight trading loss shows cracks in equity markets", article.title);
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00", article.imageUrl);
    assertStartsWith("(Reuters) - The software glitch that cost Knight Capital Group $440 million in just 45 minutes reveals the deep fault lines in stock markets that are increasingly dominated by sophisticated high-speed trading systems. But Wall Street firms and regulators have few easy solutions for such problems.", article.document.text());
    assertEquals(1, article.images.size());
    assertEquals(article.imageUrl, article.images.get(0).src);
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00",
        article.images.get(0).src);
  }

  @Test
  public void testCaltonCaldwell() {
    // http://daltoncaldwell.com/dear-mark-zuckerberg (html5)
    Article article = extractFromTestFile("daltoncaldwell.html");
    assertEquals("Dear Mark Zuckerberg by Dalton Caldwell", article.title);
    assertStartsWith("On June 13, 2012, at 4:30 p.m., I attended a meeting at Facebook HQ in Menlo Park, California.", article.document.text());
  }

  @Test
  public void testWordpress() {
    // http://karussell.wordpress.com/
    Article article = extractFromTestFile("wordpress.html");
    assertEquals("Twitter API and Me « Find Time for the Karussell", article.title);
    assertStartsWith("I have a love hate relationship with Twitter. As a user I see ", article.document.text());
  }

  @Test
  public void testFirefox() {
    // http://www.golem.de/1104/82797.html
    Article article = extractFromTestFile("golem.html");
    assertStartsWith("Mozilla hat Firefox 5.0a2 veröffentlicht und zugleich eine erste Entwicklerversion von Firefox 6 freigegeben.", article.document.text());
    assertEquals("http://scr3.golem.de/screenshots/1104/Firefox-Aurora/thumb480/aurora-nighly-beta-logos.png", article.imageUrl);
    assertEquals("Mozilla: Vorabversionen von Firefox 5 und 6 veröffentlicht - Golem.de", article.title);
  }

  @Test
  public void testYomiuri() {
    // http://www.yomiuri.co.jp/e-japan/gifu/news/20110410-OYT8T00124.htm
    Article article = extractFromTestFile("yomiuri.html");
    assertEquals("色とりどりのチューリップ : 岐阜 : 地域 : YOMIURI ONLINE（読売新聞）", article.title);
    assertTrue("yomiuri:" + article.document.text(), article.document.text().contains("海津市海津町の国営木曽三川公園で、チューリップが見頃を迎えている。２０日までは「チューリップ祭」が開かれており、大勢の人たちが多彩な色や形を鑑賞している＝写真＝"));
    assertEquals(Arrays.asList("読売新聞", "地域"), article.keywords);
  }

  @Test
  public void testFAZ() {
    // http://www.faz.net/s/Rub469C43057F8C437CACC2DE9ED41B7950/Doc~EBA775DE7201E46E0B0C5AD9619BD56E9~ATpl~Ecommon~Scontent.html
    Article article = extractFromTestFile("faz.html");
    assertStartsWith("Deutschland hat vor, ganz auf Atomkraft zu verzichten. Ist das eine gute", article.document.text());
    assertEquals("/m/{5F104CCF-3B5A-4B4C-B83E-4774ECB29889}g225_4.jpg", article.imageUrl);

    assertEquals(Arrays.asList("Atomkraft", "Deutschland", "Jahren", "Atommüll", "Fukushima", "Problem", "Brand", "Kohle", "2011", "11",
        "Stewart", "Atomdebatte", "Jahre", "Boden", "Treibhausgase", "April", "Welt", "Müll", "Radioaktivität",
        "Gesamtbild", "Klimawandel", "Reaktoren", "Verzicht", "Scheinheiligkeit", "Leute", "Risiken", "Löcher",
        "Fusion", "Gefahren", "Land"),
        article.keywords);
  }

  @Test
  public void testRian() {
    // http://en.rian.ru/world/20110410/163458489.html
    Article article = extractFromTestFile("rian.html");
    assertStartsWith("About 15,000 people took to the streets in Tokyo on Sunday to protest against th", article.document.text());
    assertEquals("Japanese rally against nuclear power industry | World", article.title);
    assertEquals("/favicon.ico", article.faviconUrl);
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testJetwick() {
    // http://jetwick.com
    Article article = extractFromTestFile("jetwick.html");
    // TODO: FIX
    // assertStartsWith("Search twitter without noise", article.document.text());
    assertEquals("img/yourkit.png", article.imageUrl);
    assertEquals(Arrays.asList("news", "twitter", "search", "jetwick"), article.keywords);
  }

  @Test
  public void testVimeo() {
    // http://vimeo.com/20910443
    Article article = extractFromTestFile("vimeo.html");
    assertStartsWith("1 month ago 1 month ago: Fri, Mar 11, 2011 2:24am EST (Eastern Standard Time) See all Show me 1. finn. & Dirk von Lowtzow", article.document.text());
    assertTrue(article.title, article.title.startsWith("finn. & Dirk von Lowtzow \"CRYING IN THE RAIN\""));
//        assertEquals("http://b.vimeocdn.com/ts/134/104/134104048_200.jpg", article.imageUrl);
    assertEquals("", article.videoUrl);
    assertEquals(Arrays.asList("finn", "finn.", "Dirk von Lowtzow", "crying in the rain", "I wish I was someone else", "Tocotronic",
        "Sunday Service", "Indigo", "Patrick Zimmer", "Patrick Zimmer aka finn.", "video", "video sharing",
        "digital cameras", "videoblog", "vidblog", "video blogging", "home video", "home movie"),
        article.keywords);
  }

  @Test
  public void testYoutube() {
    Article article = extractFromTestFile("youtube.html");
    assertStartsWith("Master of the Puppets by Metallica. Converted to 8 bit with GSXCC. Original verson can be found us", article.document.text());
    assertEquals("YouTube - Metallica - Master of the Puppets 8-bit", article.title);
    assertEquals("http://i4.ytimg.com/vi/wlupmjrfaB4/default.jpg", article.imageUrl);
    assertEquals("http://www.youtube.com/v/wlupmjrfaB4?version=3", article.videoUrl);
  }

  @Test
  public void testSpiegel() {
    Article article = extractFromTestFile("spiegel.html");
    assertStartsWith("Da ist er wieder, der C64: Eigentlich längst ein Relikt der Technikgeschichte, soll der ", article.document.text());
  }

  @Test
  public void testGithub() {
    // https://github.com/ifesdjeen/jReadability
    Article article = extractFromTestFile("github.html");
    assertStartsWith("= jReadability\n\nThis is a small helper utility (only 130 lines of code) for pepole", article.document.text());
  }

  @Test
  public void testITunes() {
    // http://itunes.apple.com/us/album/21/id420075073
    Article article = extractFromTestFile("itunes.html");
    assertStartsWith("What else can be said of this album other than that it is simply amazing? Adele's voice is powerful, vulnerable, assured, and heartbreaking all in one fell swoop.", article.document.text());
    assertStartsWith("Preview songs from 21 by ADELE", article.description);
  }

  @Test
  public void testTwitpic() {
    // http://twitpic.com/4k1ku3
    Article article = extractFromTestFile("twitpic.html");
    assertEquals("It’s hard to be a dinosaur. on Twitpic", article.title);
    assertStartsWith("Lazypicture from youtube made a video about this book! It cracked me up!!", article.document.text());
  }

  @Test
  public void testTwitpic2() {
    // http://twitpic.com/4kuem8
    Article article = extractFromTestFile("twitpic2.html");
    assertEquals("*Not* what you want to see on the fetal monitor when your wif... on Twitpic", article.title);
    assertStartsWith("*Not* what you want to see on the fetal monitor when your wife begins to push.", article.document.text());
  }

  @Test
  public void testHeise() {
    // http://www.heise.de/newsticker/meldung/Internet-Explorer-9-jetzt-mit-schnellster-JavaScript-Engine-1138062.html
    Article article = extractFromTestFile("heise.html");
    assertEquals("", article.imageUrl);
    assertEquals("heise online - Internet Explorer 9 jetzt mit schnellster JavaScript-Engine", article.title);
    assertStartsWith("Microsoft hat heute eine siebte Platform Preview des Internet Explorer veröffentlicht. In den nur dr", article.document.text());
  }

  @Test
  public void testTechcrunch() {
    // http://techcrunch.com/2011/04/04/twitter-advanced-search/
    Article article = extractFromTestFile("techcrunch.html");
    assertEquals("http://tctechcrunch.files.wordpress.com/2011/04/screen-shot-2011-04-04-at-12-11-36-pm.png?w=285&h=85", article.imageUrl);
    assertEquals("Twitter Finally Brings Advanced Search Out Of Purgatory; Updates Discovery Algorithms", article.title);
    assertStartsWith("A couple weeks ago, we wrote a post wishing Twitter a happy fifth birthday, but also noting ", article.document.text());
  }

  @Test
  public void testEngadget() {
    // http://www.engadget.com/2011/04/09/editorial-androids-problem-isnt-fragmentation-its-contamina/
    Article article = extractFromTestFile("engadget.html");
    assertStartsWith("Editorial: Android's problem isn't fragmentation, it's contamination This thought was first given voice by Myriam Joire on last night's Mobile Podcast, and the", article.document.text());
    assertEquals("http://www.blogcdn.com/www.engadget.com/media/2011/04/11x0409mnbvhg_thumbnail.jpg", article.imageUrl);
    assertEquals("Editorial: Android's problem isn't fragmentation, it's contamination -- Engadget", article.title);
  }

  @Test
  public void testTwitterblog() {
    // http://engineering.twitter.com/2011/04/twitter-search-is-now-3x-faster_1656.html
    Article article = extractFromTestFile("twitter.html");
    assertEquals("Twitter Engineering: Twitter Search is Now 3x Faster", article.title);
    assertEquals("http://4.bp.blogspot.com/-CmXJmr9UAbA/TZy6AsT72fI/AAAAAAAAAAs/aaF5AEzC-e4/s72-c/Blender_Tsunami.jpg", article.imageUrl);
    assertStartsWith("In the spring of 2010, the search team at Twitter started to rewrite our search engine in order to serve our ever-growin", article.document.text());
  }

  @Test
  public void testTazBlog() {
    // http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/
    Article article = extractFromTestFile("taz.html");
    assertStartsWith("Absolute Minderheit: Im Shiba-Park in Tokio treffen sich jetzt jeden Sonntag die Atomkraftgegner. Sie blicken neidisch auf die Anti-AKW-Bewegung in Deutschland.", article.document.text());
    assertEquals("Protestkultur in Japan nach der Katastrophe: Anti-Atomkraft? Nein danke! - taz.de", article.title);
    assertEquals("http://www.taz.de/uploads/hp_taz_img/full/antiakwprotestjapandapd.20110410-19.jpg", article.imageUrl);
  }

  @Test
  public void testFacebook() {
    // http://www.facebook.com/ejdionne/posts/10150154175658687
    Article article = extractFromTestFile("facebook.html");
    assertStartsWith("In my column tomorrow, I urge President Obama to end the spectacle of", article.document.text());
    assertEquals("", article.imageUrl);
    assertEquals("In my column...", article.title);
  }

  @Test
  public void testFacebook2() {
    // http://www.facebook.com/permalink.php?story_fbid=214289195249322&id=101149616624415
    Article article = extractFromTestFile("facebook2.html");
    assertStartsWith("Sommer is the best time to wear Jetwick T-Shirts!", article.document.text());
    assertEquals("", article.imageUrl);
    assertEquals("Sommer is the best...", article.title);
  }

  @Test
  public void testBlogger() {
    // http://blog.talawah.net/2011/04/gavin-king-unviels-red-hats-top-secret.html
    Article article = extractFromTestFile("blogger.html");
    assertStartsWith("Gavin King unveils Red Hat's Java killer", article.document.text());
    assertEquals("http://3.bp.blogspot.com/-cyMzveP3IvQ/TaR7f3qkYmI/AAAAAAAAAIk/mrChE-G0b5c/s200/Java.png", article.imageUrl);
    assertEquals("The Brain Dump: Gavin King unveils Red Hat's Java killer successor: The Ceylon Project", article.title);
    assertEquals("http://blog.talawah.net/feeds/posts/default?alt=rss", article.feedUrl);
  }

  @Test
  public void testNyt() {
    // http://dealbook.nytimes.com/2011/04/11/for-defense-in-galleon-trial-no-time-to-rest/
    Article article = extractFromTestFile("nyt.html");
    assertEquals("http://graphics8.nytimes.com/images/2011/04/12/business/dbpix-raj-rajaratnam-1302571800091/dbpix-raj-rajaratnam-1302571800091-tmagSF.jpg",
        article.imageUrl);
    assertStartsWith("I wouldn’t want to be Raj Rajaratnam’s lawyer right now.", article.document.text());
  }

  @Test
  public void testHuffingtonpost() {
    // "http://www.huffingtonpost.com/2010/08/13/federal-reserve-pursuing_n_681540.html";
    Article article = extractFromTestFile("huffingtonpost.html");
    assertEquals("Federal Reserve's Low Rate Policy Is A 'Dangerous Gamble,' Says Top Central Bank Official", article.title);
    assertStartsWith("A top regional Federal Reserve official sharply", article.document.text());
    assertEquals("http://i.huffpost.com/gen/157611/thumbs/s-FED-large.jpg", article.imageUrl);
  }

  @Test
  public void testTechcrunch2() {
    //String url = "http://techcrunch.com/2010/08/13/gantto-takes-on-microsoft-project-with-web-based-project-management-application/";
    Article article = extractFromTestFile("techcrunch2.html");
    assertEquals("Gantto Takes On Microsoft Project With Web-Based Project Management Application", article.title);
    assertStartsWith("Y Combinator-backed Gantto is launching", article.document.text());
    assertEquals("http://tctechcrunch.files.wordpress.com/2010/08/gantto.jpg", article.imageUrl);
  }

  @Test
  public void testCnn2() {
    //String url = "http://www.cnn.com/2010/POLITICS/08/13/democrats.social.security/index.html";
    Article article = extractFromTestFile("cnn2.html");
    assertEquals("Democrats to use Social Security against GOP this fall - CNN.com", article.title);
    assertStartsWith("Washington (CNN) -- Democrats pledged ", article.document.text());
    assertEquals(article.imageUrl, "http://i.cdn.turner.com/cnn/2010/POLITICS/08/13/democrats.social.security/story.kaine.gi.jpg");
  }

  @Test
  public void testBusinessweek2() {
    //String url = "http://www.businessweek.com/magazine/content/10_34/b4192048613870.htm";
    Article article = extractFromTestFile("businessweek2.html");
    assertStartsWith("There's discord on Wall Street: Strategists at major American investment ", article.document.text());
    assertEquals("http://images.businessweek.com/mz/covers/current_120x160.jpg", article.imageUrl);
  }

  @Test
  public void testFoxnews() {
    //String url = "http://www.foxnews.com/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/";
    Article article = extractFromTestFile("foxnews.html");
    assertStartsWith("Apr. 8: President Obama signs the New START treaty with Russian President Dmitry Medvedev at the Prague Castle. Russia's announcement ", article.document.text());
    assertEquals("http://a57.foxnews.com/static/managed/img/Politics/397/224/startsign.jpg", article.imageUrl);
  }

  @Test
  public void testStackoverflow() {
    //String url = "http://stackoverflow.com/questions/3553693/wicket-vs-vaadin/3660938";
    Article article = extractFromTestFile("stackoverflow.html");
    assertStartsWith("I think I've invested some time for both frameworks", article.document.text());
    assertStartsWith("java - wicket vs Vaadin - Stack Overflow", article.title);
    assertEquals("", article.imageUrl);
  }

  @Test
  public void testAolnews() {
    //String url = "http://www.aolnews.com/nation/article/the-few-the-proud-the-marines-getting-a-makeover/19592478";
    Article article = extractFromTestFile("aolnews.html");
    assertEquals("http://o.aolcdn.com/art/ch_news/aol_favicon.ico", article.faviconUrl);
    assertStartsWith("WASHINGTON (Aug. 13) -- Declaring \"the maritime soul of the Marine Corps", article.document.text());
    assertEquals("http://o.aolcdn.com/photo-hub/news_gallery/6/8/680919/1281734929876.JPEG", article.imageUrl);
    assertEquals(Arrays.asList("news", "update", "breaking", "nation", "U.S.", "elections", "world", "entertainment", "sports", "business",
        "weird news", "health", "science", "latest news articles", "breaking news", "current news", "top news"),
        article.keywords);
  }

  @Test
  public void testWallstreetjournal() {
    //String url = "http://online.wsj.com/article/SB10001424052748704532204575397061414483040.html";
    Article article = extractFromTestFile("wsj.html");
    assertStartsWith("The Obama administration has paid out less than a third of the nearly $230 billion", article.document.text());
    assertEquals("http://si.wsj.net/public/resources/images/OB-JO747_stimul_D_20100814113803.jpg", article.imageUrl);
  }

  @Test
  public void testUsatoday() {
    //String url = "http://content.usatoday.com/communities/thehuddle/post/2010/08/brett-favre-practices-set-to-speak-about-return-to-minnesota-vikings/1";
    Article article = extractFromTestFile("usatoday.html");
    assertStartsWith("Brett Favre couldn't get away from the", article.document.text());
    assertEquals("http://i.usatoday.net/communitymanager/_photos/the-huddle/2010/08/18/favrespeaksx-inset-community.jpg", article.imageUrl);
  }

  @Test
  public void testUsatoday2() {
    //String url = "http://content.usatoday.com/communities/driveon/post/2010/08/gm-finally-files-for-ipo/1";
    Article article = extractFromTestFile("usatoday2.html");
    assertStartsWith("General Motors just filed with the Securities and Exchange ", article.document.text());
    assertEquals("http://i.usatoday.net/communitymanager/_photos/drive-on/2010/08/18/cruzex-wide-community.jpg", article.imageUrl);
  }

  @Test
  public void testEspn() {
    //String url = "http://sports.espn.go.com/espn/commentary/news/story?id=5461430";
    Article article = extractFromTestFile("espn.html");
    assertStartsWith("If you believe what college football coaches have said about sports", article.document.text());
    assertEquals("http://a.espncdn.com/photo/2010/0813/ncf_i_mpouncey1_300.jpg", article.imageUrl);
  }

  @Test
  public void testGizmodo() {
    //String url = "http://www.gizmodo.com.au/2010/08/xbox-kinect-gets-its-fight-club/";
    Article article = extractFromTestFile("gizmodo.html");
    assertStartsWith("You love to punch your arms through the air",article.document.text());
    assertEquals("", article.imageUrl);
  }

  @Test
  public void testEngadget2() {
    //String url = "http://www.engadget.com/2010/08/18/verizon-fios-set-top-boxes-getting-a-new-hd-guide-external-stor/";
    Article article = extractFromTestFile("engadget2.html");
    assertStartsWith("Verizon FiOS set-top boxes getting a new HD guide", article.document.text());
    assertEquals("http://www.blogcdn.com/www.engadget.com/media/2010/08/44ni600_thumbnail.jpg", article.imageUrl);
  }

  @Test
  public void testWired() {
    //String url = "http://www.wired.com/playbook/2010/08/stress-hormones-boxing/";
    Article article = extractFromTestFile("wired.html");
    assertStartsWith("On November 25, 1980, professional boxing", article.document.text());
    assertEquals("Stress Hormones Could Predict Boxing Dominance", article.title);
    assertEquals("http://www.wired.com/playbook/wp-content/uploads/2010/08/fight_f-660x441.jpg", article.imageUrl);
  }

  @Test
  public void tetGigaohm() {
    //String url = "http://gigaom.com/apple/apples-next-macbook-an-800-mac-for-the-masses/";
    Article article = extractFromTestFile("gigaom.html");
    assertStartsWith("The MacBook Air is a bold move forward ", article.document.text());
    assertEquals("http://gigapple.files.wordpress.com/2010/10/macbook-feature.png?w=300&h=200", article.imageUrl);
  }

  @Test
  public void testMashable() {
    //String url = "http://mashable.com/2010/08/18/how-tonot-to-ask-someone-out-online/";
    Article article = extractFromTestFile("mashable.html");
    assertStartsWith("Imagine, if you will, a crowded dance floor", article.document.text());
    assertEquals("http://9.mshcdn.com/wp-content/uploads/2010/07/love.jpg", article.imageUrl);
  }

  @Test
  public void testVenturebeat() {
    //String url = "http://social.venturebeat.com/2010/08/18/facebook-reveals-the-details-behind-places/";
    Article article = extractFromTestFile("venturebeat.html");
    assertStartsWith("Facebook just confirmed the rumors", article.document.text());
    assertEquals("http://cdn.venturebeat.com/wp-content/uploads/2010/08/mark-zuckerberg-facebook-places.jpg", article.imageUrl);
  }

  @Test
  public void testPolitico() {
    //String url = "http://www.politico.com/news/stories/1010/43352.html";
    Article article = extractFromTestFile("politico.html");
    assertStartsWith("If the newest Census Bureau estimates stay close to form", article.document.text());
    assertEquals("http://images.politico.com/global/news/100927_obama22_ap_328.jpg", article.imageUrl);
  }

  @Test
  public void testNinjablog() {
    //String url = "http://www.ninjatraderblog.com/im/2010/10/seo-marketing-facts-about-google-instant-and-ranking-your-website/";
    Article article = extractFromTestFile("ninjatraderblog.html");
    assertStartsWith("Many users around the world Google their queries", article.document.text());
  }

  @Test
  public void testSportsillustrated() {
    //String url = "http://sportsillustrated.cnn.com/2010/football/ncaa/10/15/ohio-state-holmes.ap/index.html?xid=si_ncaaf";
    Article article = extractFromTestFile("sportsillustrated.html");
    assertStartsWith("COLUMBUS, Ohio (AP) -- Ohio State has closed", article.document.text());
    assertEquals("http://i.cdn.turner.com/si/.e1d/img/4.0/global/logos/si_100x100.jpg",
        article.imageUrl);
  }

  @Test
  public void testDailybeast() {
    //String url = "http://www.thedailybeast.com/blogs-and-stories/2010-11-01/ted-sorensen-speechwriter-behind-jfks-best-jokes/?cid=topic:featured1";
    Article article = extractFromTestFile("thedailybeast.html");
    assertStartsWith("Legendary Kennedy speechwriter Ted Sorensen passed", article.document.text());
    assertEquals("http://www.tdbimg.com/files/2010/11/01/img-article---katz-ted-sorensen_163531624950.jpg", article.imageUrl);
  }

  @Test
  public void testScience() {
    //String url = "http://news.sciencemag.org/sciencenow/2011/04/early-birds-smelled-good.html";
    Article article = extractFromTestFile("sciencemag.html");
    assertStartsWith("About 65 million years ago, most of the dinosaurs and many other animals and plants were wiped off Earth, probably due to an asteroid hitting our planet. Researchers have long debated how and why some ", article.document.text());
  }

  @Test
  public void testSlamMagazine() {
    //String url = "http://www.slamonline.com/online/nba/2010/10/nba-schoolyard-rankings/";
    Article article = extractFromTestFile("slamonline.html");
    assertStartsWith("When in doubt, rank players and add your findings", article.document.text());
    assertEquals(article.imageUrl, "http://www.slamonline.com/online/wp-content/uploads/2010/10/celtics.jpg");
    assertEquals("SLAM ONLINE | » NBA Schoolyard Rankings", article.title);
  }

  @Test
  public void testEspn3WithFlashVideo() {
    //String url = "http://sports.espn.go.com/nfl/news/story?id=5971053";
    Article article = extractFromTestFile("espn3.html");
    assertStartsWith("PHILADELPHIA -- Michael Vick missed practice Thursday", article.document.text());
    assertEquals("http://a.espncdn.com/i/espn/espn_logos/espn_red.png", article.imageUrl);
    assertEquals("Michael Vick of Philadelphia Eagles misses practice, unlikely to play vs. Dallas Cowboys - ESPN", article.title);
  }

  @Test
  public void testSportingNews() {
    //String url = "http://www.sportingnews.com/nfl/feed/2011-01/nfl-coaches/story/raiders-cut-ties-with-cable";
    Article article = extractFromTestFile("sportingnews.html");
    assertStartsWith("ALAMEDA, Calif. — The Oakland Raiders informed coach Tom Cable on Tuesday that they will not bring him back", article.document.text());
    assertEquals("http://dy.snimg.com/story-image/0/69/174475/14072-650-366.jpg",
        article.imageUrl);
    assertEquals("Raiders cut ties with Cable - NFL - Sporting News", article.title);
  }

  @Test
  public void testFoxSports() {
    //String url = "http://msn.foxsports.com/nfl/story/Tom-Cable-fired-contract-option-Oakland-Raiders-coach-010411";
    Article article = extractFromTestFile("foxsports.html");
    assertStartsWith("The Oakland Raiders informed coach Tom Cable", article.document.text());
    assertEquals("Oakland Raiders won't bring Tom Cable back as coach - NFL News",
        article.title);
  }

  @Test
  public void testEconomist() {
    //String url = "http://www.economist.com/node/17956885";
    Article article = extractFromTestFile("economist.html");
    assertStartsWith("FOR beleaguered smokers, the world is an increasingly", article.document.text());
    assertEquals("http://www.economist.com/sites/default/files/images/articles/migrated/20110122_stp004.jpg",
        article.imageUrl);
  }

  @Test
  public void testTheVacationGals() {
    //String url = "http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/";
    Article article = extractFromTestFile("thevacationgals.html");
    assertStartsWith("Editors’ Note: We are huge proponents of vacation rental homes", article.document.text());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
        article.imageUrl);
    assertEquals(3, article.images.size());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
        article.images.get(0).src);
    assertEquals("../wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg",
        article.images.get(1).src);
    assertEquals("http://www.linkwithin.com/pixel.png", article.images.get(2).src);
  }

  @Test
  public void testShockYa() {
    //String url = "http://www.shockya.com/news/2011/01/30/daily-shock-jonathan-knight-of-new-kids-on-the-block-publicly-reveals-hes-gay/";
    Article article = extractFromTestFile("shockya.html");
    assertStartsWith("New Kids On The Block singer Jonathan Knight has publicly", article.document.text());
    assertEquals("http://www.shockya.com/news/wp-content/uploads/jonathan_knight_new_kids_gay.jpg",
        article.imageUrl);
  }

  @Test
  public void testWikipedia() {
    // String url = "http://en.wikipedia.org/wiki/Therapsids";
    // Wikipedia has the advantage of also testing protocol relative URL extraction for Favicon and Images.
    Article article = extractFromTestFile("wikipedia.html");
    assertStartsWith("Therapsida is a group of the most advanced reptile-grade synapsids, and the ancestors of mammals", article.document.text());
    assertStartsWith("<b>Therapsida</b> is a group of the most advanced reptile-grade <a href=\"/wiki/Synapsid\">synapsids</a>", article.document.child(0).html());
    assertEquals("//upload.wikimedia.org/wikipedia/commons/thumb/4/42/Pristeroognathus_DB.jpg/240px-Pristeroognathus_DB.jpg",
        article.imageUrl);
    assertEquals("//en.wikipedia.org/apple-touch-icon.png",
        article.faviconUrl);
  }

  @Test
  public void testWikipedia2() {
    // http://en.wikipedia.org/wiki/President_of_the_United_States
    Article article = extractFromTestFile("wikipedia_president.html");
    assertStartsWith("The President of the United States of America (acronym: POTUS)[6] is the head of state and head of government", article.document.text());
  }

  @Test
  public void testWikipedia3() {
    // http://en.wikipedia.org/wiki/Muhammad
    Article article = extractFromTestFile("wikipedia_muhammad.html");
    assertStartsWith("Muhammad (c. 570 – c. 8 June 632);[1] also transliterated as Mohammad, Mohammed, or Muhammed; Arabic: محمد‎, full name: Abū al-Qāsim Muḥammad", article.document.text());
  }

  @Test
  public void testWikipedia4() {
    // http://de.wikipedia.org/wiki/Henne_Strand
    Article article = extractFromTestFile("wikipedia_Henne_Strand.html");
    assertStartsWith("Der dänische Ort Henne Strand befindet sich in Südwest-Jütland und gehört zur Kommune Varde", article.document.text());
  }

  @Test
  public void testWikipedia5() {
    // http://de.wikipedia.org/wiki/Java
    Article article = extractFromTestFile("wikipedia_java.html");
    assertStartsWith("Java (Indonesian: Jawa) is an island of Indonesia. With a population of 135 million", article.document.text());
  }

  @Test
  public void testWikipedia6() {
    // http://de.wikipedia.org/wiki/Knight_Rider
    Article article = extractFromTestFile("wikipedia-knight_rider_de.html");
    assertStartsWith("Knight Rider ist eine US-amerikanische Fernsehserie, "
        + "die von 1982 bis 1986 produziert wurde. Knight Rider ist eine Krimi-Action-Serie mit futuristischen Komponenten "
        + "und hat weltweit Kultstatus erlangt.", article.document.text());
  }

  @Test
  public void testTimemagazine() {
    //String url = "http://www.time.com/time/health/article/0,8599,2011497,00.html";
    Article article = extractFromTestFile("time.html");
    assertStartsWith("This month, the federal government released", article.document.text());
    assertEquals("http://img.timeinc.net/time/daily/2010/1008/bp_oil_spill_0817.jpg", article.imageUrl);
  }

  @Test
  public void testCnet() {
    //String url = "http://news.cnet.com/8301-30686_3-20014053-266.html?tag=topStories1";
    Article article = extractFromTestFile("cnet.html");
    assertStartsWith("NEW YORK--Verizon Communications is prepping a new", article.document.text());
    assertEquals("http://i.i.com.com/cnwk.1d/i/tim//2010/08/18/Verizon_iPad_and_live_TV_610x458.JPG", article.imageUrl);
  }

  @Test
  public void testBloomberg() {
    //String url = "http://www.bloomberg.com/news/2010-11-01/china-becomes-boss-in-peru-on-50-billion-mountain-bought-for-810-million.html";
    Article article = extractFromTestFile("bloomberg.html");
    assertStartsWith("The Chinese entrepreneur and the Peruvian shopkeeper", article.document.text());
    assertEquals("http://www.bloomberg.com/apps/data?pid=avimage&iid=iimODmqjtcQU", article.imageUrl);
  }

  @Test
  public void testTheFrisky() {
    //String url = "http://www.thefrisky.com/post/246-rachel-dratch-met-her-baby-daddy-in-a-bar/";
    Article article = extractFromTestFile("thefrisky.html");
    assertStartsWith("Rachel Dratch had been keeping the identity of her baby daddy ", article.document.text());

    assertEquals("http://cdn.thefrisky.com/images/uploads/rachel_dratch_102810_m.jpg",
        article.imageUrl);
    assertEquals("Rachel Dratch Met Her Baby Daddy At A Bar", article.title);
  }

  @Test
  public void testBrOnline() {
    // TODO charset for opera was removed:
    // <![endif]-->
    // <link rel="stylesheet" type="text/x-opera-css;charset=utf-8" href="/css/opera.css" />

    //String url = "http://www.br-online.de/br-klassik/programmtipps/highlight-bayreuth-tannhaeuser-festspielzeit-2011-ID1309895438808.xml";
    Article article = extractFromTestFile("br-online.html");
    assertStartsWith("Wenn ein Dirigent, der Alte Musik liebt, erstmals eine "
        + "Neuproduktion bei den Bayreuther Richard-Wagner-Festspielen übernimmt,", article.document.text());
    assertEquals("Eröffnung der 100. Bayreuther Festspiele: Alles neu beim \"Tannhäuser\" | Programmtipps | BR-KLASSIK",
        article.title);
  }

  @Test
  public void testRedditAtomFeed() {
    // https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/
    Article article = extractFromTestFile("reddit.html");
    assertEquals("https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/.rss", article.feedUrl);
  }

  @Test
  public void testCleanTitle() {
    String title = "Hacker News | Ask HN: Apart from Hacker News, what else you read?";
    assertEquals("Ask HN: Apart from Hacker News, what else you read?", ExtractionHelpers.cleanTitle(title));
    assertEquals("mytitle irgendwas", ExtractionHelpers.cleanTitle("mytitle irgendwas | Facebook"));
    assertEquals("mytitle irgendwas", ExtractionHelpers.cleanTitle("mytitle irgendwas | Irgendwas"));

    // this should fail as most sites do store their name after the post
    assertEquals("Irgendwas | mytitle irgendwas", ExtractionHelpers.cleanTitle("Irgendwas | mytitle irgendwas"));
  }

  @Test
  public void testGaltimeWhereUrlContainsSpaces() {
    //String url = "http://galtime.com/article/entertainment/37/22938/kris-humphries-avoids-kim-talk-gma";
    Article article = extractFromTestFile("galtime.com.html");
    assertEquals("http://vnetcdn.dtsph.com/files/vnet3/imagecache/opengraph_ogimage/story-images/Kris%20Humphries%20Top%20Bar.JPG", article.imageUrl);
  }

  @Test
  public void testRetainSpaceInsideTags() {
    Article article = extractor.extractContent("<html><body><div> aaa<a> bbb </a>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", article.document.text());

    article = extractor.extractContent("<html><body><div> aaa <strong>bbb </strong>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", article.document.text());

    article = extractor.extractContent("<html><body><div> aaa <strong> bbb </strong>ccc</div></body></html>");
    assertEquals("aaa bbb ccc", article.document.text());
  }

  @Test
  public void testI4Online() {
    //https://i4online.com
    Article article = extractFromTestFile("i4online.html");
    assertStartsWith("Just one week to go and everything is set for the summer Forum 2013", article.document.text());
  }

  //    @Test
  public void testEspn2() {
    //String url = "http://sports.espn.go.com/golf/pgachampionship10/news/story?id=5463456";
    Article article = extractFromTestFile("espn2.html");
    assertStartsWith("PHILADELPHIA -- Michael Vick missed practice Thursday because of a leg injury and is unlikely to play Sunday wh", article.document.text());
    assertEquals("http://a.espncdn.com/media/motion/2010/0813/dm_100814_pga_rinaldi.jpg", article.imageUrl);
  }

  //    @Test
  public void testWashingtonpost() {
    //String url = "http://www.washingtonpost.com/wp-dyn/content/article/2010/12/08/AR2010120803185.html";
    Article article = extractFromTestFile("washingtonpost.html");
    assertStartsWith("The Supreme Court sounded ", article.document.text());
    assertEquals("http://media3.washingtonpost.com/wp-dyn/content/photo/2010/10/09/PH2010100904575.jpg", article.imageUrl);
  }

  //    @Test
  public void testBoingboing() {
    //String url = "http://www.boingboing.net/2010/08/18/dr-laura-criticism-o.html";
    Article article = extractFromTestFile("boingboing.html");
    assertStartsWith("Dr. Laura Schlessinger is leaving radio to regain", article.document.text());
    assertEquals("http://www.boingboing.net/images/drlaura.jpg", article.imageUrl);
  }

  //    @Test
  public void testReadwriteWeb() {
    //String url = "http://www.readwriteweb.com/start/2010/08/pagely-headline.php";
    Article article = extractFromTestFile("readwriteweb.html");
    assertStartsWith("In the heart of downtown Chandler, Arizona", article.document.text());
    assertEquals("http://rww.readwriteweb.netdna-cdn.com/start/images/logopagely_aug10.jpg", article.imageUrl);
  }

  //    @Test
  public void testYahooNews() {
    //String url = "http://news.yahoo.com/s/ap/20110305/ap_on_re_af/af_libya";
    Article article = extractFromTestFile("yahoo.html");
    assertStartsWith("TRIPOLI, Libya – Government forces in tanks rolled into the opposition-held city closest ", article.document.text());
    assertEquals("http://d.yimg.com/a/p/ap/20110305/http://d.yimg.com/a/p/ap/20110305/thumb.23c7d780d8d84bc4a8c77af11ecba277-23c7d780d8d84bc4a8c77af11ecba277-0.jpg?x=130&y=90&xc=1&yc=1&wc=130&hc=90&q=85&sig=LbIZK0rnJlZAcrAWn.brLw--",
        article.imageUrl);
  }

  //    @Test
  public void testLifehacker() {
    //String url = "http://lifehacker.com/#!5659837/build-a-rocket-stove-to-heat-your-home-with-wood-scraps";
    Article article = extractFromTestFile("lifehacker.html");
    assertStartsWith("If you find yourself with lots of leftover wood", article.document.text());
    assertEquals("http://cache.gawker.com/assets/images/lifehacker/2010/10/rocket-stove-finished.jpeg", article.imageUrl);
  }

  //    @Test
  public void testNaturalhomemagazine() {
    //String url = "http://www.naturalhomemagazine.com/diy-projects/try-this-papier-mache-ghostly-lanterns.aspx";
    Article article = extractFromTestFile("naturalhomemagazine.html");
    assertStartsWith("Guide trick or treaters and other friendly spirits to your front", article.document.text());
    assertEquals("http://www.naturalhomemagazine.com/uploadedImages/articles/issues/2010-09-01/NH-SO10-trythis-lantern-final2_resized400X266.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testSfgate() {
    //String url = "http://www.sfgate.com/cgi-bin/article.cgi?f=/c/a/2010/10/27/BUD61G2DBL.DTL";
    Article article = extractFromTestFile("sfgate.html");
    assertStartsWith("Fewer homes in California and", article.document.text());
    assertEquals("http://imgs.sfgate.com/c/pictures/2010/10/26/ba-foreclosures2_SFCG1288130091.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testScientificdaily() {
    //String url = "http://www.scientificamerican.com/article.cfm?id=bpa-semen-quality";
    Article article = extractFromTestFile("scientificamerican.html");
    assertStartsWith("The common industrial chemical bisphenol A (BPA) ", article.document.text());
    assertEquals("http://www.scientificamerican.com/media/inline/bpa-semen-quality_1.jpg", article.imageUrl);
    assertEquals("Everyday BPA Exposure Decreases Human Semen Quality", article.title);
  }

  @Test
  public void testUniverseToday() {
    //String url = "http://www.universetoday.com/76881/podcast-more-from-tony-colaprete-on-lcross/";
    Article article = extractFromTestFile("universetoday.html");
    assertStartsWith("I had the chance to interview LCROSS", article.document.text());
    assertEquals("http://www.universetoday.com/wp-content/uploads/2009/10/lcross-impact_01_01.jpg",
        article.imageUrl);
    assertEquals("Podcast: More From Tony Colaprete on LCROSS", article.title);
  }

  //    @Test
  public void testCNBC() {
    //String url = "http://www.cnbc.com/id/40491584";
    Article article = extractFromTestFile("cnbc.html");
    assertStartsWith("A prominent expert on Chinese works ", article.document.text());
    assertEquals("http://media.cnbc.com/i/CNBC/Sections/News_And_Analysis/__Story_Inserts/graphics/__ART/chinese_vase_150.jpg",
        article.imageUrl);
    assertTrue(article.title.equals("Chinese Art Expert 'Skeptical' of Record-Setting Vase"));
  }

  //    @Test
  public void testMsnbc() {
    //String url = "http://www.msnbc.msn.com/id/41207891/ns/world_news-europe/";
    Article article = extractFromTestFile("msnbc.html");
    assertStartsWith("DUBLIM -- Prime Minister Brian Cowen announced Saturday", article.document.text());
    assertEquals("Irish premier resigns as party leader, stays as PM", article.title);
    assertEquals("http://msnbcmedia3.msn.com/j/ap/ireland government crisis--687575559_v2.grid-6x2.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testTheAtlantic() {
    //String url = "http://www.theatlantic.com/culture/archive/2011/01/how-to-stop-james-bond-from-getting-old/69695/";
    Article article = extractFromTestFile("theatlantic.html");
    assertStartsWith("If James Bond could age, he'd be well into his 90s right now", article.document.text());
    assertEquals("http://assets.theatlantic.com/static/mt/assets/culture_test/James%20Bond_post.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testGawker() {
    //String url = "http://gawker.com/#!5777023/charlie-sheen-is-going-to-haiti-with-sean-penn";
    Article article = extractFromTestFile("gawker.html");
    assertStartsWith("With a backlash brewing against the incessant media", article.document.text());
    assertEquals("http://cache.gawkerassets.com/assets/images/7/2011/03/medium_0304_pennsheen.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testNyt2() {
    //String url = "http://www.nytimes.com/2010/12/22/world/europe/22start.html";
    Article article = extractFromTestFile("nyt2.html");
    assertStartsWith("WASHINGTON &mdash; An arms control treaty paring back American", article.document.text());
    assertEquals("http://graphics8.nytimes.com/images/2010/12/22/world/22start-span/Start-articleInline.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testGettingVideosFromGraphVinyl() {
    //String url = "http://grapevinyl.com/v/84/magnetic-morning/getting-nowhere";
    Article article = extractFromTestFile("grapevinyl.html");
    assertEquals("http://www.youtube.com/v/dsVWVtGWoa4&hl=en_US&fs=1&color1=d6d6d6&color2=ffffff&autoplay=1&iv_load_policy=3&rel=0&showinfo=0&hd=1",
        article.videoUrl);
  }

  //    @Test
  public void testLiveStrong() {
    //String url = "http://www.livestrong.com/article/395538-how-to-decrease-the-rates-of-obesity-in-children/";
    Article article = extractFromTestFile("livestrong.html");
    assertStartsWith("Childhood obesity increases a young person", article.document.text());
    assertEquals("http://photos.demandstudios.com/getty/article/184/46/87576279_XS.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testLiveStrong2() {
    //String url = "http://www.livestrong.com/article/396152-do-resistance-bands-work-for-strength-training/";
    Article article = extractFromTestFile("livestrong2.html");
    assertStartsWith("Resistance bands or tubes are named because", article.document.text());
    assertEquals("http://photos.demandstudios.com/getty/article/142/66/86504893_XS.jpg", article.imageUrl);
  }

  //    @Test
  public void testCracked() {
    //String url = "http://www.cracked.com/article_19029_6-things-social-networking-sites-need-to-stop-doing.html";
    Article article = extractFromTestFile("cracked.html");
    assertStartsWith("Social networking is here to stay", article.document.text());
    assertEquals("http://i-beta.crackedcdn.com/phpimages/article/2/1/6/45216.jpg?v=1", article.imageUrl);
  }

  //    @Test
  public void testMidgetmanofsteel() {
    //String url = "http://www.cracked.com/article_19029_6-things-social-networking-sites-need-to-stop-doing.html";
    Article article = extractFromTestFile("midgetmanofsteel.html");
    assertStartsWith("I've decided to turn my Facebook assholishnessicicity", article.document.text());
    assertEquals("http://4.bp.blogspot.com/_F74vJj-Clzk/TPkzP-Y93jI/AAAAAAAALKM/D3w1sfJqE5U/s200/funny-dog-pictures-will-work-for-hot-dogs.jpg", article.imageUrl);
  }

  //    @Test
  public void testTrailsCom() {
    //String url = "http://www.trails.com/facts_41596_hot-spots-citrus-county-florida.html";
    Article article = extractFromTestFile("trails.html");
    assertStartsWith("Snorkel and view artificial reefs or chase", article.document.text());
    assertEquals("http://cdn-www.trails.com/imagecache/articles/295x195/hot-spots-citrus-county-florida-295x195.png", article.imageUrl);
  }

  //    @Test
  public void testTrailsCom2() {
    //String url = "http://www.trails.com/facts_12408_history-alpine-skis.html";
    Article article = extractFromTestFile("trails2.html");
    assertStartsWith("Derived from the old Norse word", article.document.text());
    assertEquals("http://cdn-www.trails.com/imagecache/articles/295x195/history-alpine-skis-295x195.png", article.imageUrl);
  }

  //    @Test
  public void testEhow() {
    //String url = "http://www.ehow.com/how_7734109_make-white-spaghetti.html";
    Article article = extractFromTestFile("ehow.html");
    assertStartsWith("Heat the oil in the", article.document.text());
    assertEquals("How to Make White Spaghetti", article.title);
  }

  @Test
  public void testGolfLink() {
    //String url = "http://www.golflink.com/how_1496_eat-cheap-las-vegas.html";
    Article article = extractFromTestFile("golflink.html");
    assertStartsWith("Las Vegas, while noted for its glitz", article.document.text());
    assertEquals("http://cdn-www.golflink.com/Cms/images/GlobalPhoto/Articles/2011/2/17/1496/fotolia4152707XS-main_Full.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testNewsweek() {
    //String url = "http://www.newsweek.com/2010/10/09/how-moscow-s-war-on-islamist-rebels-is-backfiring.html";
    Article article = extractFromTestFile("newsweek.html");
    assertStartsWith("At first glance, Kadyrov might seem", article.document.text());
//        assertEquals("http://www.newsweek.com/content/newsweek/2010/10/09/how-moscow-s-war-on-islamist-rebels-is-backfiring.scaled.small.1309768214891.jpg",
//                article.imageUrl);
    assertEquals("http://www.newsweek.com/content/newsweek/2010/10/09/how-moscow-s-war-on-islamist-rebels-is-backfiring.scaled.small.1302869450444.jpg",
        article.imageUrl);
  }

  //    @Test
  public void testBusinessweek() {
    // String url = "http://www.businessweek.com/magazine/content/10_34/b4192066630779.htm";
    Article article = extractFromTestFile("businessweek.html");
    assertEquals("Olivia Munn: Queen of the Uncool - BusinessWeek", article.title);
    assertStartsWith("Six years ago, Olivia Munn arrived in Hollywood with fading ambitions of making it ", article.document.text());
    assertEquals("http://images.businessweek.com/mz/10/34/370/1034_mz_66popmunnessa.jpg", article.imageUrl);
  }

  //    @Test
  public void testNature() {
    //String url = "http://www.nature.com/news/2011/110411/full/472146a.html";
    Article article = extractFromTestFile("nature.html");
    assertStartsWith("As the immediate threat from Fukushima "
        + "Daiichi's damaged nuclear reactors recedes, engineers and scientists are", article.document.text());
  }

  @Test
  public void testImagesList() {
    // http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803
    Article article = extractFromTestFile("reuters.html");
    assertEquals(1, article.images.size());
    assertEquals(article.imageUrl, article.images.get(0).src);
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00",
        article.images.get(0).src);

    // http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/
    article = extractFromTestFile("thevacationgals.html");
    assertEquals(3, article.images.size());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
        article.images.get(0).src);
    assertEquals("../wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg",
        article.images.get(1).src);
    assertEquals("http://www.linkwithin.com/pixel.png", article.images.get(2).src);
  }

  @Test
  public void testTextList() {
    Article article = extractFromTestFile("npr.html");
    String text = article.document.text();
    assertEquals(23, article.document.childNodeSize());
    assertEquals("", article.document.childNode(0).outerHtml());
    assertEquals("", article.document.childNode(22).outerHtml());
  }

  private Article extractFromTestFile(String testFile) {
    try {
      return extractor.extractContent(CharsetConverter.readStream(new FileInputStream(new File("test_data/" + testFile)), null).content);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
      return null;
    }
  }

  private void assertStartsWith(String expected, String actual) {
    if (!actual.startsWith(expected)) {
      fail(String.format("Expected \n[%s]\n at start of \n[%s]\n", expected, actual));
    }
  }
}
