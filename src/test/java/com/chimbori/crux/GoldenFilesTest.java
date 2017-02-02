package com.chimbori.crux;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class GoldenFilesTest {
  @Test
  public void testNPR() {
    Article article = extractFromTestFile("http://www.npr.org/blogs/money/2010/10/04/130329523/how-fake-money-saved-brazil", "npr.html");
    assertEquals("How Fake Money Saved Brazil : Planet Money : NPR", article.title);
    assertEquals("", article.siteName);
    assertStartsWith("This is a story about how an economist and his buddies tricked the people of Brazil into saving the country from rampant inflation. They had a crazy, unlikely plan, and it worked. Twenty years ago, Brazil's", article.document.text());
    assertTrue(article.document.text(), article.document.text().endsWith("\"How Four Drinking Buddies Saved Brazil.\""));
    assertEquals("http://media.npr.org/assets/img/2010/10/04/real_wide.jpg?t=1286218782&s=3", article.imageUrl);
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testBenjaminStein() {
    Article article = extractFromTestFile("http://benjaminste.in/post/1223476561/hey-guys-whatcha-doing", "benjaminstein.html");
    assertEquals("BenjaminSte.in - Hey guys, whatcha doing?", article.title);
    assertStartsWith("This month is the 15th anniversary of my last CD.", article.document.text());
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testYCombinator() {
    Article article = extractFromTestFile("http://paulgraham.com/seesv.html", "ycombinator.html");
    assertEquals(1, article.document.childNodeSize());
    assertStartsWith("", article.document.text());
    assertStartsWith("October 2010 • Silicon Valley proper is mostly suburban sprawl.", article.document.child(0).text());
    assertTrue(article.document.text(), article.document.text().endsWith(" and Jessica Livingston for reading drafts of this. •"));
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testTraindom() {
    Article article = extractFromTestFile("http://blog.traindom.com/places-where-to-submit-your-startup-for-coverage/", "traindom.html");
    assertEquals("36 places where you can submit your startup for some coverage", article.title);
    assertEquals(Arrays.asList("blog coverage", "get coverage", "startup review", "startups", "submit startup"), article.keywords);
    assertStartsWith("So you have a new startup company and want some coverage", article.document.text());
  }

  @Test
  public void testToloNews() {
    Article article = extractFromTestFile("http://www.tolonews.com/en/video/24865-tolonews-6pm-news-19-april-2016", "tolonews.html");
    assertEquals("Acting Governor of Balkh province, Atta Mohammad Noor, said that differences between leaders of the National Unity Government (NUG) – namely President Ashraf Ghani and CEO Abdullah Abdullah— have paved the ground for mounting insecurity. Hundreds of worried relatives gathered outside Kabul hospitals on Tuesday desperate for news of loved ones following the deadly suicide bombing earlier in the day.", article.document.text());
  }

  @Test
  public void testKhaamaPress() {
    Article article = extractFromTestFile("http://www.khaama.com/over-100-school-girls-poisoned-in-western-afghanistan-0737", "khaama.html");
    assertStartsWith("Over 100 school girls have been poisoned in western Farah province of Afghanistan during the school hours.", article.document.text());
  }

  @Test
  public void testCNN() {
    Article article = extractFromTestFile("http://edition.cnn.com/2011/WORLD/africa/04/06/libya.war/index.html?on.cnn=1", "cnn.html");
    assertEquals("Gadhafi asks Obama to end NATO bombing - CNN.com", article.title);
    assertEquals("http://edition.cnn.com/2011/WORLD/africa/04/06/libya.war/t1larg.libyarebel.gi.jpg", article.imageUrl);
    assertStartsWith("Tripoli, Libya (CNN) -- As rebel and pro-government forces in Libya maneuvered on the battlefield Wedn", article.document.child(0).text());
  }

  @Test
  public void testBBC() {
    Article article = extractFromTestFile("http://www.bbc.co.uk/news/world-latin-america-21226565", "bbc.html");
    assertEquals("BBC News", article.siteName);
    assertEquals("Baby born on Mediterranean rescue ship - BBC News", article.title);
    assertEquals("http://ichef-1.bbci.co.uk/news/1024/cpsprodpb/146E6/production/_91168638_baby070012-9-20162-1photocreditalvawhitemsf.jpg", article.imageUrl);
    assertEquals("http://www.bbc.co.uk/news/amp/37341871", article.ampUrl);
    assertStartsWith("A Nigerian woman has given birth to a boy on board a rescue ship in the Mediterranean after being plucked from an overcrowded rubber dinghy.", article.document.text());
  }

  @Test
  public void testBBC_AMP() {
    Article article = extractFromTestFile("http://www.bbc.co.uk/news/amp/37341871", "bbc-amp.html");
    assertEquals("BBC News", article.siteName);
    assertEquals("Baby born on Mediterranean rescue ship", article.title);
    assertEquals("http://ichef.bbci.co.uk/news/999/cpsprodpb/146E6/production/_91168638_baby070012-9-20162-1photocreditalvawhitemsf.jpg", article.imageUrl);
    assertStartsWith("A Nigerian woman has given birth to a boy on board a rescue ship in the Mediterranean after being plucked from an overcrowded rubber dinghy.", article.document.text());
  }

  @Test
  public void testReuters() {
    Article article = extractFromTestFile("http://www.reuters.com/article/us-knightcapital-trading-technology-idUSBRE87203X20120803", "reuters.html");
    assertEquals("Knight trading loss shows cracks in equity markets", article.title);
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=130&fh=&fw=&ll=&pl=&r=CBRE872074Y00", article.imageUrl);
    assertStartsWith("Knight trading loss shows cracks in equity markets (Reuters) - The software glitch that cost Knight Capital Group", article.document.text());
  }

  @Test
  public void testNewYorker() {
    Article article = extractFromTestFile("http://www.newyorker.com/humor/borowitz-report/scientists-earth-endangered-by-new-strain-of-fact-resistant-humans", "newyorker.html");
    assertEquals("Scientists: Earth Endangered by New Strain of Fact-Resistant Humans - The New Yorker", article.title);
    assertEquals("http://www.newyorker.com/wp-content/uploads/2015/05/Borowitz-Earth-Endangered-by-Fact-Resistant-Humans-1200-630-12152424.jpg", article.imageUrl);
    assertStartsWith("MINNEAPOLIS (The Borowitz Report)—Scientists have discovered a powerful new strain of fact-resistant humans who are threatening the ability of Earth to sustain life", article.document.text());
  }

  @Test
  public void testCaltonCaldwell() {
    Article article = extractFromTestFile("http://daltoncaldwell.com/dear-mark-zuckerberg", "daltoncaldwell.html");
    assertEquals("Dear Mark Zuckerberg by Dalton Caldwell", article.title);
    assertStartsWith("On June 13, 2012, at 4:30 p.m., I attended a meeting at Facebook HQ in Menlo Park, California.", article.document.text());
  }

  @Test
  public void testWordpress() {
    Article article = extractFromTestFile("http://karussell.wordpress.com/", "wordpress.html");
    assertEquals("Twitter API and Me « Find Time for the Karussell", article.title);
    assertStartsWith("I have a love hate relationship with Twitter. As a user I see ", article.document.text());
  }

  @Test
  public void testGolem() {
    Article article = extractFromTestFile("http://www.golem.de/1104/82797.html", "golem.html");
    assertStartsWith("Unter dem Namen \"Aurora\" hat Firefox einen neuen Kanal mit Vorabversionen von Firefox eingerichtet.", article.document.text());
    assertEquals("http://scr3.golem.de/screenshots/1104/Firefox-Aurora/thumb480/aurora-nighly-beta-logos.png", article.imageUrl);
    assertEquals("Mozilla: Vorabversionen von Firefox 5 und 6 veröffentlicht - Golem.de", article.title);
  }

  @Test
  public void testYomiuri() {
    Article article = extractFromTestFile("http://www.yomiuri.co.jp/e-japan/gifu/news/20110410-OYT8T00124.htm", "yomiuri.html");
    assertEquals("色とりどりのチューリップ : 岐阜 : 地域 : YOMIURI ONLINE（読売新聞）", article.title);
    assertTrue("yomiuri:" + article.document.text(), article.document.text().contains("海津市海津町の国営木曽三川公園で、チューリップが見頃を迎えている。２０日までは「チューリップ祭」が開かれており、大勢の人たちが多彩な色や形を鑑賞している＝写真＝"));
    assertEquals(Arrays.asList("読売新聞", "地域"), article.keywords);
  }

  @Test
  public void testRian() {
    Article article = extractFromTestFile("http://en.rian.ru/world/20110410/163458489.html", "rian.html");
    assertStartsWith("About 15,000 people took to the streets in Tokyo on Sunday to protest against th", article.document.text());
    assertEquals("Japanese rally against nuclear power industry", article.title);
    assertEquals("http://en.rian.ru/favicon.ico", article.faviconUrl);
    assertTrue(article.keywords.isEmpty());
  }

  @Test
  public void testYouTube() {
    Article article = extractFromTestFile("https://www.youtube.com/watch?v=wlupmjrfaB4", "youtube.html");
    assertStartsWith("Master of the Puppets by Metallica. Converted to 8 bit with GSXCC. Original verson can be found us", article.document.text());
    assertEquals("YouTube - Metallica - Master of the Puppets 8-bit", article.title);
    assertEquals("http://i4.ytimg.com/vi/wlupmjrfaB4/default.jpg", article.imageUrl);
    assertEquals("http://www.youtube.com/v/wlupmjrfaB4?version=3", article.videoUrl);
    assertEquals("http://s.ytimg.com/yt/favicon-vflZlzSbU.ico", article.faviconUrl);
  }

  @Test
  public void testSpiegel() {
    Article article = extractFromTestFile("http://www.spiegel.de/netzwelt/gadgets/retro-pc-commodore-reaktiviert-den-c64-a-755090.html", "spiegel.html");
    assertStartsWith("Da ist er wieder, der C64: Eigentlich längst ein Relikt der Technikgeschichte, soll der ", article.document.text());
  }

  @Test
  public void testGithub() {
    Article article = extractFromTestFile("https://github.com/ifesdjeen/jReadability", "github.html");
    assertStartsWith("= jReadability This is a small helper utility (only 130 lines of code) for pepole", article.document.text());
    assertEquals("https://github.com/fluidicon.png", article.faviconUrl);
  }

  @Test
  public void testITunes() {
    Article article = extractFromTestFile("http://itunes.apple.com/us/album/21/id420075073", "itunes.html");
    assertStartsWith("What else can be said of this album other than that it is simply amazing? Adele's voice is powerful, vulnerable, assured, and heartbreaking all in one fell swoop.", article.document.text());
    assertStartsWith("Preview songs from 21 by ADELE", article.description);
    assertNull(article.faviconUrl);
  }

  @Test
  public void testTwitpic() {
    Article article = extractFromTestFile("http://twitpic.com/4k1ku3", "twitpic.html");
    assertEquals("It’s hard to be a dinosaur. on Twitpic", article.title);
    assertStartsWith("Lazypicture from youtube made a video about this book! It cracked me up!!", article.document.text());
  }

  @Test
  public void testTwitpic2() {
    Article article = extractFromTestFile("http://twitpic.com/4kuem8", "twitpic2.html");
    assertEquals("*Not* what you want to see on the fetal monitor when your wif... on Twitpic", article.title);
    assertStartsWith("*Not* what you want to see on the fetal monitor when your wife begins to push.", article.document.text());
  }

  @Test
  public void testHeise() {
    Article article = extractFromTestFile("http://www.heise.de/newsticker/meldung/Internet-Explorer-9-jetzt-mit-schnellster-JavaScript-Engine-1138062.html", "heise.html");
    assertEquals("http://m.f.ix.de/scale/geometry/250/q50/imgs/18/1/7/8/2/8/5/5/b6e69ac13bb564dcaba745f4b419e23f_edited_105951127_8168730ae9-255ed03a302fdb50.jpeg@jpg", article.imageUrl);
    assertEquals("Internet Explorer 9 jetzt mit schnellster JavaScript-Engine", article.title);
    assertStartsWith("Internet Explorer 9 jetzt mit schnellster JavaScript-Engine Microsoft hat heute eine siebte Platform Preview ", article.document.text());
  }

  @Test
  public void testTechCrunch() {
    Article article = extractFromTestFile("http://techcrunch.com/2011/04/04/twitter-advanced-search/", "techcrunch.html");
    assertEquals("Twitter Finally Brings Advanced Search Out Of Purgatory; Updates Discovery Algorithms", article.title);
    assertEquals("https://s0.wp.com/wp-content/themes/vip/techcrunch-2013/assets/images/techcrunch.opengraph.default.png", article.imageUrl);
    assertStartsWith("A couple weeks ago, we wrote a post wishing Twitter a happy fifth birthday, but also noting ", article.document.text());
  }

  @Test
  public void testTwitterBlog() {
    Article article = extractFromTestFile("http://engineering.twitter.com/2011/04/twitter-search-is-now-3x-faster_1656.html", "twitter.html");
    assertEquals("Twitter Engineering: Twitter Search is Now 3x Faster", article.title);
    assertEquals("http://4.bp.blogspot.com/-CmXJmr9UAbA/TZy6AsT72fI/AAAAAAAAAAs/aaF5AEzC-e4/s400/Blender_Tsunami.jpg", article.imageUrl);
    assertStartsWith("In the spring of 2010, the search team at Twitter started to rewrite our search engine in order to serve our ever-growin", article.document.text());
  }

  @Test
  public void testTazBlog() {
    Article article = extractFromTestFile("http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/", "taz.html");
    assertStartsWith("Absolute Minderheit: Im Shiba-Park in Tokio treffen sich jetzt jeden Sonntag die Atomkraftgegner. Sie blicken neidisch auf die Anti-AKW-Bewegung in Deutschland.", article.document.text());
    assertEquals("Protestkultur in Japan nach der Katastrophe: Anti-Atomkraft? Nein danke! - taz.de", article.title);
  }

  @Test
  public void testFacebook() {
    Article article = extractFromTestFile("http://www.facebook.com/ejdionne/posts/10150154175658687", "facebook.html");
    assertStartsWith("In my column tomorrow, I urge President Obama to end the spectacle of", article.document.text());
    assertEquals("http://profile.ak.fbcdn.net/hprofile-ak-snc4/41782_97057113686_1357_q.jpg", article.imageUrl);
    assertEquals("In my column...", article.title);
  }

  @Test
  public void testFolhaUolComBr() {
    Article article = extractFromTestFile("http://m.folha.uol.com.br/ciencia/2017/01/1854055-no-futuro-as-pessoas-nao-morrerao-por-envelhecimento-diz-cientista.shtml?mobile", "folha_uol_com_br.html");
    assertEquals("No futuro, as pessoas não morrerão por envelhecimento, diz cientista - 30/01/2017 - Ciência - Folha de S.Paulo", article.title);
    assertStartsWith("Aubrey de Grey, 53, quer curar o envelhecimento. Sim, para esse pesquisador inglês, formado em ciências da computação na Universidade de Cambridge, envelhecer é uma doença tal como a malária –ou ainda pior, por vitimar muito mais pessoas– que pode ser perfeitamente evitável.", article.document.text());
    assertStartsWith("<p> Aubrey de Grey, 53, quer curar o envelhecimento. Sim, para esse pesquisador inglês, formado em ciências da computação na Universidade de Cambridge, envelhecer é uma doença tal como a malária –ou ainda pior, por vitimar muito mais pessoas– que pode ser perfeitamente evitável. </p>\n" +
        "<p> A seu ver, para pensar em uma solução é preciso entender o envelhecimento e a morte como resultado de um processo de acúmulo de danos e imperfeições no organismo. </p>", article.document.html());
  }

  @Test
  public void testBlogger() {
    Article article = extractFromTestFile("http://blog.talawah.net/2011/04/gavin-king-unviels-red-hats-top-secret.html", "blogger.html");
    assertStartsWith("Gavin King unveils Red Hat's Java successor: The Ceylon Project of Red Hat/Hibernate/Seam fame recently unveiled the top secret project", article.document.text());
    assertEquals("http://3.bp.blogspot.com/-cyMzveP3IvQ/TaR7f3qkYmI/AAAAAAAAAIk/mrChE-G0b5c/s200/Java.png", article.imageUrl);
    assertEquals("The Brain Dump: Gavin King unveils Red Hat's Java killer successor: The Ceylon Project", article.title);
    assertEquals("http://blog.talawah.net/feeds/posts/default?alt=rss", article.feedUrl);
  }

  @Test
  public void testNYT() {
    Article article = extractFromTestFile("http://dealbook.nytimes.com/2011/04/11/for-defense-in-galleon-trial-no-time-to-rest/", "nyt.html");
    assertEquals("DealBook", article.siteName);
    assertEquals("http://graphics8.nytimes.com/images/2011/04/12/business/dbpix-raj-rajaratnam-1302571800091/dbpix-raj-rajaratnam-1302571800091-tmagSF.jpg", article.imageUrl);
    assertStartsWith("I wouldn’t want to be Raj Rajaratnam’s lawyer right now.", article.document.text());
  }

  @Test
  public void testHuffingtonPost() {
    Article article = extractFromTestFile("http://www.huffingtonpost.com/2010/08/13/federal-reserve-pursuing_n_681540.html", "huffingtonpost.html");
    assertEquals("Federal Reserve's Low Rate Policy Is A 'Dangerous Gamble,' Says Top Central Bank Official", article.title);
    assertStartsWith("A top regional Federal Reserve official sharply", article.document.text());
    assertEquals("http://i.huffpost.com/gen/157611/thumbs/s-FED-large.jpg", article.imageUrl);
  }

  @Test
  public void testTechCrunch2() {
    Article article = extractFromTestFile("http://techcrunch.com/2010/08/13/gantto-takes-on-microsoft-project-with-web-based-project-management-application/", "techcrunch2.html");
    assertEquals("Gantto Takes On Microsoft Project With Web-Based Project Management Application", article.title);
    assertStartsWith("Y Combinator-backed Gantto is launching", article.document.text());
    assertEquals("http://tctechcrunch.files.wordpress.com/2010/08/gantto.jpg", article.imageUrl);
  }

  @Test
  public void testCNN2() {
    Article article = extractFromTestFile("http://www.cnn.com/2010/POLITICS/08/13/democrats.social.security/index.html", "cnn2.html");
    assertEquals("Democrats to use Social Security against GOP this fall - CNN.com", article.title);
    assertEquals(31, article.document.childNodeSize());
    assertStartsWith("Washington (CNN) -- Democrats pledged ", article.document.child(0).text());
    assertEquals(article.imageUrl, "http://i.cdn.turner.com/cnn/2010/POLITICS/08/13/democrats.social.security/story.kaine.gi.jpg");
  }

  @Test
  public void testFoxNews() {
    Article article = extractFromTestFile("http://www.foxnews.com/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/", "foxnews.html");
    assertStartsWith("Russia's announcement that it will help Iran get nuclear fuel is raising questions about what President Obama calls the \"better-than- ever\" relationship", article.document.text());
    assertEquals("http://a57.foxnews.com/images.foxnews.com/content/fox-news/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/_jcr_content/par/featured-media/media-0.img.jpg/0/0/1446837847097.jpg?ve=1", article.imageUrl);
  }

  @Test
  public void testStackOverflow() {
    Article article = extractFromTestFile("http://stackoverflow.com/questions/3553693/wicket-vs-vaadin/3660938", "stackoverflow.html");
    assertStartsWith("I think I've invested some time for both frameworks", article.document.text());
    assertStartsWith("java - wicket vs Vaadin - Stack Overflow", article.title);
    assertEquals("http://cdn.sstatic.net/Sites/stackoverflow/img/apple-touch-icon@2.png?v=73d79a89bded&a", article.imageUrl);
  }

  @Test
  public void testAOLNews() {
    Article article = extractFromTestFile("http://www.aolnews.com/nation/article/the-few-the-proud-the-marines-getting-a-makeover/19592478", "aolnews.html");
    assertEquals("http://o.aolcdn.com/art/ch_news/aol_favicon.ico", article.faviconUrl);
    assertStartsWith("WASHINGTON (Aug. 13) -- Declaring \"the maritime soul of the Marine Corps", article.document.text());
    assertEquals("http://o.aolcdn.com/photo-hub/news_gallery/6/8/680919/1281734929876.JPEG", article.imageUrl);
    assertEquals(Arrays.asList("news", "update", "breaking", "nation", "U.S.", "elections", "world", "entertainment", "sports", "business",
        "weird news", "health", "science", "latest news articles", "breaking news", "current news", "top news"),
        article.keywords);
  }

  @Test
  public void testWallStreetJournal() {
    Article article = extractFromTestFile("http://www.wsj.com/articles/SB10001424052748704532204575397061414483040", "wsj.html");
    assertStartsWith("The Obama administration has paid out less than a third of the nearly $230 billion", article.document.text());
    assertEquals("https://si.wsj.net/public/resources/images/OB-JO759_0814st_D_20100814143158.jpg", article.imageUrl);
  }

  @Test
  public void testUsatoday() {
    Article article = extractFromTestFile("http://content.usatoday.com/communities/thehuddle/post/2010/08/brett-favre-practices-set-to-speak-about-return-to-minnesota-vikings/1", "usatoday.html");
    assertStartsWith("Brett Favre says he couldn't give up on one more chance to win the Super Bowl with Vikings Brett Favre couldn't get away from the \"what ifs.\"", article.document.text());
    assertEquals("http://i.usatoday.net/communitymanager/_photos/the-huddle/2010/08/18/favrespeaksx-inset-community.jpg", article.imageUrl);
  }

  @Test
  public void testUsaToday2() {
    Article article = extractFromTestFile("http://content.usatoday.com/communities/driveon/post/2010/08/gm-finally-files-for-ipo/1", "usatoday2.html");
    assertStartsWith("GM files for IPO, but are taxpayers still on the hook? General Motors just filed with the Securities and Exchange Commission", article.document.text());
    assertEquals("http://i.usatoday.net/communitymanager/_photos/drive-on/2010/08/18/cruzex-wide-community.jpg", article.imageUrl);
  }

  @Test
  public void testESPN() {
    Article article = extractFromTestFile("http://www.espn.com/espn/commentary/news/story?id=5461430", "espn.html");
    assertStartsWith("If you believe what college football coaches have said about sports", article.document.text());
    assertEquals("http://a.espncdn.com/photo/2010/0813/pg2_g_bush3x_300.jpg", article.imageUrl);
  }

  @Test
  public void testGizmodo() {
    Article article = extractFromTestFile("http://www.gizmodo.com.au/2010/08/xbox-kinect-gets-its-fight-club/", "gizmodo.html");
    assertEquals("Gizmodo Australia", article.siteName);
    assertStartsWith("You love to punch your arms through the air", article.document.text());
    assertEquals("http://cache.gawkerassets.com/assets/images/9/2010/08/500x_fighters_uncaged__screenshot_3b__jawbreaker.jpg", article.imageUrl);
  }

  @Test
  public void testEngadget() {
    Article article = extractFromTestFile("http://www.engadget.com/2010/08/18/verizon-fios-set-top-boxes-getting-a-new-hd-guide-external-stor/", "engadget.html");
    assertStartsWith("Streaming and downloading TV content to mobiles is nice, but we enjoy watching TV... on the TV", article.document.text());
    assertEquals("https://www.blogcdn.com/www.engadget.com/media/2010/08/44ni600.jpg", article.imageUrl);
    assertEquals("https://s.blogsmithmedia.com/www.engadget.com/assets-haa9c2740c98180d07c436859c827e9f1/images/favicon-160x160.png?h=1638b0a8bbe7effa8f85c3ecabb63620", article.faviconUrl);
  }

  @Test
  public void testWired() {
    Article article = extractFromTestFile("http://www.wired.com/playbook/2010/08/stress-hormones-boxing/", "wired.html");
    assertStartsWith("On November 25, 1980, professional boxing", article.document.text());
    assertEquals("Stress Hormones Could Predict Boxing Dominance", article.title);
    assertEquals("http://www.wired.com/playbook/wp-content/uploads/2010/08/fight_f-660x441.jpg", article.imageUrl);
    assertEquals("http://blog.wired.com/gadgets/files/apple-touch-icon.png", article.faviconUrl);
  }

  @Test
  public void testGigaOm() {
    Article article = extractFromTestFile("http://gigaom.com/apple/apples-next-macbook-an-800-mac-for-the-masses/", "gigaom.html");
    assertStartsWith("The MacBook Air is a bold move forward ", article.document.text());
    assertEquals("http://gigapple.files.wordpress.com/2010/10/macbook-feature.png?w=604", article.imageUrl);
  }

  @Test
  public void testMashable() {
    Article article = extractFromTestFile("http://mashable.com/2010/08/18/how-tonot-to-ask-someone-out-online/", "mashable.html");
    assertStartsWith("Imagine, if you will, a crowded dance floor", article.document.text());
    assertEquals("http://9.mshcdn.com/wp-content/uploads/2010/07/love.jpg", article.imageUrl);
  }

  @Test
  public void testVentureBeat() {
    Article article = extractFromTestFile("http://social.venturebeat.com/2010/08/18/facebook-reveals-the-details-behind-places/", "venturebeat.html");
    assertStartsWith("Facebook just confirmed the rumors", article.document.text());
    assertEquals("http://cdn.venturebeat.com/wp-content/uploads/2010/08/mark-zuckerberg-facebook-places.jpg", article.imageUrl);
  }

  @Test
  public void testPolitico() {
    Article article = extractFromTestFile("http://www.politico.com/news/stories/1010/43352.html", "politico.html");
    assertStartsWith("If the newest Census Bureau estimates stay close to form", article.document.text());
    assertEquals("http://images.politico.com/global/news/100927_obama22_ap_328.jpg", article.imageUrl);
  }

  @Test
  public void testNinjaTraderBlog() {
    Article article = extractFromTestFile("http://www.ninjatraderblog.com/im/2010/10/seo-marketing-facts-about-google-instant-and-ranking-your-website/", "ninjatraderblog.html");
    assertStartsWith("SEO Marketing- Facts About Google Instant And Ranking Your Website Many users around the world Google their queries", article.document.text());
  }

  @Test
  public void testSportsIllustrated() {
    Article article = extractFromTestFile("http://www.si.com/nba/2016/09/07/shaq-basketball-hall-of-fame-lakers-magic-heat-lsu-tigers", "sportsillustrated.html");
    assertStartsWith("Way back in 1994, Shaquille O’Neal, who will be inducted into the Naismith Basketball Hall of Fame on Friday, was asked about Knicks center Patrick Ewing.", article.document.text());
    assertEquals("http://windows.api.si.com/s3/files/styles/inline_gallery_desktop/public/2016/09/08/shaquille-o-neal-hall-of-fame-lakers-magic-lsu.jpg?itok=oupTSSJY", article.imageUrl);
    assertEquals("http://www.si.com/img/favicons/favicon-192.png", article.faviconUrl);
  }

  @Test
  public void testTheDailyBeast() {
    Article article = extractFromTestFile("http://www.thedailybeast.com/blogs-and-stories/2010-11-01/ted-sorensen-speechwriter-behind-jfks-best-jokes/?cid=topic:featured1", "thedailybeast.html");
    assertStartsWith("Legendary Kennedy speechwriter Ted Sorensen passed", article.document.text());
    assertEquals("http://www.tdbimg.com/resizeimage/YTo0OntzOjM6ImltZyI7czo2MToiMjAxMC8xMS8wMS9pbWctYnMtYm90dG9tLS0ta2F0ei10ZWQtc29yZW5zZW5fMTYzMjI4NjEwMzUxLmpwZyI7czo1OiJ3aWR0aCI7aTo1MDtzOjY6ImhlaWdodCI7aTo1MDtzOjY6InJhbmRvbSI7czoxOiIxIjt9.jpg", article.imageUrl);
  }

  @Test
  public void testScience() {
    Article article = extractFromTestFile("http://news.sciencemag.org/sciencenow/2011/04/early-birds-smelled-good.html", "sciencemag.html");
    assertStartsWith("About 65 million years ago, most of the dinosaurs and many other animals and plants were wiped off Earth, probably due to an asteroid hitting our planet. Researchers have long debated how and why some ", article.document.text());
  }

  @Test
  public void testSlamMagazine() {
    Article article = extractFromTestFile("http://www.slamonline.com/online/nba/2010/10/nba-schoolyard-rankings/", "slamonline.html");
    assertStartsWith("When in doubt, rank players and add your findings", article.document.text());
    assertEquals(article.imageUrl, "http://www.slamonline.com/online/wp-content/uploads/2010/10/celtics.jpg");
    assertEquals("SLAM ONLINE | » NBA Schoolyard Rankings", article.title);
  }

  @Test
  public void testEspn3WithFlashVideo() {
    Article article = extractFromTestFile("http://sports.espn.go.com/nfl/news/story?id=5971053", "espn3.html");
    assertStartsWith("PHILADELPHIA -- Michael Vick missed practice Thursday", article.document.text());
    assertEquals("http://a.espncdn.com/i/espn/espn_logos/espn_red.png", article.imageUrl);
    assertEquals("Michael Vick of Philadelphia Eagles misses practice, unlikely to play vs. Dallas Cowboys - ESPN", article.title);
  }

  @Test
  public void testSportingNews() {
    Article article = extractFromTestFile("http://www.sportingnews.com/nfl/feed/2011-01/nfl-coaches/story/raiders-cut-ties-with-cable", "sportingnews.html");
    assertStartsWith("ALAMEDA, Calif. — The Oakland Raiders informed coach Tom Cable on Tuesday that they will not bring him back", article.document.text());
    assertEquals("http://dy.snimg.com/story-image/0/69/174475/14072-650-366.jpg",
        article.imageUrl);
    assertEquals("Raiders cut ties with Cable - NFL - Sporting News", article.title);
  }

  @Test
  public void testFoxSports() {
    Article article = extractFromTestFile("http://msn.foxsports.com/nfl/story/Tom-Cable-fired-contract-option-Oakland-Raiders-coach-010411", "foxsports.html");
    assertStartsWith("The Oakland Raiders informed coach Tom Cable", article.document.text());
    assertEquals("Oakland Raiders won't bring Tom Cable back as coach - NFL News", article.title);
  }

  @Test
  public void testEconomist() {
    Article article = extractFromTestFile("http://www.economist.com/node/17956885", "economist.html");
    assertStartsWith("FOR beleaguered smokers, the world is an increasingly", article.document.text());
    assertEquals("http://www.economist.com/sites/default/files/images/articles/migrated/20110122_stp004.jpg", article.imageUrl);
  }

  @Test
  public void testTheVacationGals() {
    Article article = extractFromTestFile("http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/", "thevacationgals.html");
    assertStartsWith("Editors’ Note: We are huge proponents of vacation rental homes", article.document.text());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg", article.imageUrl);
    assertEquals(3, article.images.size());
    assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg", article.images.get(0).src);
    assertEquals("../wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg", article.images.get(1).src);
    assertEquals("http://www.linkwithin.com/pixel.png", article.images.get(2).src);
  }

  @Test
  public void testShockYa() {
    Article article = extractFromTestFile("http://www.shockya.com/news/2011/01/30/daily-shock-jonathan-knight-of-new-kids-on-the-block-publicly-reveals-hes-gay/", "shockya.html");
    assertStartsWith("New Kids On The Block singer Jonathan Knight has publicly", article.document.text());
    assertEquals("http://www.shockya.com/news/wp-content/uploads/jonathan_knight_new_kids_gay.jpg", article.imageUrl);
  }

  @Test
  public void testWikipedia() {
    Article article = extractFromTestFile("http://en.wikipedia.org/wiki/Therapsids", "wikipedia.html");
    assertStartsWith("Therapsida is a group of the most advanced reptile-grade synapsids, and the ancestors of mammals", article.document.text());
    assertStartsWith("<b>Therapsida</b> is a group of the most advanced reptile-grade <a href=\"/wiki/Synapsid\">synapsids</a>", article.document.child(0).html());
    assertEquals("http://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Pristeroognathus_DB.jpg/240px-Pristeroognathus_DB.jpg", article.imageUrl);
    assertEquals("http://en.wikipedia.org/apple-touch-icon.png", article.faviconUrl);
  }

  @Test
  public void testWikipedia2() {
    Article article = extractFromTestFile("http://en.wikipedia.org/wiki/President_of_the_United_States", "wikipedia_president.html");
    assertStartsWith("\"POTUS\" redirects here.", article.document.html());
  }

  @Test
  public void testWikipedia3() {
    Article article = extractFromTestFile("http://en.wikipedia.org/wiki/Muhammad", "wikipedia_muhammad.html");
    assertStartsWith("This article is about the Islamic prophet. For other persons named Muhammad, see Muhammad ( 570 – 8 June 632)", article.document.text());
  }

  @Test
  public void testWikipedia4() {
    Article article = extractFromTestFile("http://de.wikipedia.org/wiki/Henne_Strand", "wikipedia_Henne_Strand.html");
    assertStartsWith("Der dänische Ort Henne Strand befindet sich in Südwest-Jütland und gehört zur Kommune Varde", article.document.text());
  }

  @Test
  public void testWikipedia5() {
    Article article = extractFromTestFile("http://de.wikipedia.org/wiki/Java", "wikipedia_java.html");
    assertStartsWith("This article is about the Indonesian island. For other uses, see Java (Indonesian: Jawa) is an island of Indonesia. ", article.document.text());
  }

  @Test
  public void testWikipedia6() {
    Article article = extractFromTestFile("http://de.wikipedia.org/wiki/Knight_Rider", "wikipedia-knight_rider_de.html");
    assertStartsWith("Knight Rider ist eine US-amerikanische Fernsehserie, "
        + "die von 1982 bis 1986 produziert wurde. Knight Rider ist eine Krimi-Action-Serie mit futuristischen Komponenten "
        + "und hat weltweit Kultstatus erlangt.", article.document.text());
  }

  @Test
  public void testTimeMagazine() {
    Article article = extractFromTestFile("http://www.time.com/time/health/article/0,8599,2011497,00.html", "time.html");
    assertStartsWith("This month, the federal government released", article.document.child(0).text());
    assertEquals(article.document.childNodes().toString(), "http://img.timeinc.net/time/daily/2010/1008/bp_oil_spill_0817.jpg", article.imageUrl);
  }

  @Test
  public void testCNet() {
    Article article = extractFromTestFile("http://www.cnet.com/news/verizon-shows-off-ipad-tv-app-and-more/", "cnet.html");
    assertEquals("CNET", article.siteName);
    assertStartsWith("NEW YORK--Verizon Communications is prepping a new", article.document.text());
    assertEquals("https://cnet1.cbsistatic.com/img/Bw23T5rupUVnvVPCQQ3KjfO9qic=/670x503/2010/08/18/53b6d52b-f0fa-11e2-8c7c-d4ae52e62bcc/Verizon_iPad_and_live_TV_with_big_TV.JPG", article.imageUrl);
  }

  @Test
  public void testBloomberg() {
    Article article = extractFromTestFile("http://www.bloomberg.com/news/2010-11-01/china-becomes-boss-in-peru-on-50-billion-mountain-bought-for-810-million.html", "bloomberg.html");
    assertEquals("Bloomberg", article.siteName);
    assertStartsWith("The Chinese entrepreneur and the Peruvian shopkeeper", article.document.text());
    assertEquals("http://www.bloomberg.com/apps/data?pid=avimage&iid=iimODmqjtcQU", article.imageUrl);
  }

  @Test
  public void testTheFrisky() {
    Article article = extractFromTestFile("http://www.thefrisky.com/2010-10-28/rachel-dratch-met-her-baby-daddy-in-a-bar/", "thefrisky.html");
    assertStartsWith("Rachel Dratch had been keeping the identity of her baby daddy ", article.document.text());
    assertEquals("http://static.thefrisky.com/uploads/2010/10/28/rachel_dratch_102810_m.jpg", article.imageUrl);
    assertEquals("Rachel Dratch Met Her Baby Daddy At A Bar - The Frisky", article.title);
  }

  @Test
  public void testBrOnline() {
    Article article = extractFromTestFile("http://www.br-online.de/br-klassik/programmtipps/highlight-bayreuth-tannhaeuser-festspielzeit-2011-ID1309895438808.xml", "br-online.html");
    assertStartsWith("Wenn ein Dirigent, der Alte Musik liebt, erstmals eine "
        + "Neuproduktion bei den Bayreuther Richard-Wagner-Festspielen übernimmt,", article.document.text());
    assertEquals("Eröffnung der 100. Bayreuther Festspiele: Alles neu beim \"Tannhäuser\" | Programmtipps",
        article.title);
  }

  @Test
  public void testRedditAtomFeed() {
    Article article = extractFromTestFile("https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/", "reddit.html");
    assertEquals("https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/.rss", article.feedUrl);
  }

  @Test
  public void testGaltimeWhereUrlContainsSpaces() {
    Article article = extractFromTestFile("http://galtime.com/article/entertainment/37/22938/kris-humphries-avoids-kim-talk-gma", "galtime.com.html");
    assertEquals("http://vnetcdn.dtsph.com/files/vnet3/imagecache/opengraph_ogimage/story-images/Kris%20Humphries%20Top%20Bar.JPG", article.imageUrl);
  }

  @Test
  public void testI4Online() {
    Article article = extractFromTestFile("https://i4online.com", "i4online.html");
    assertStartsWith("Just one week to go and everything is set for the summer Forum 2013", article.document.text());
  }

  @Test
  public void testESPN2() {
    Article article = extractFromTestFile("http://www.espn.com/golf/pgachampionship10/news/story?id=5463456", "espn2.html");
    assertStartsWith("SHEBOYGAN, Wis. -- The only number that matters at the PGA Championship is on the scorecard, not the birth certificate.", article.document.text());
    assertEquals("http://a2.espncdn.com/combiner/i?img=%2Fi%2Fheadshots%2Fgolf%2Fplayers%2Ffull%2F780.png", article.imageUrl);
  }

  @Test
  public void testWashingtonPost() {
    Article article = extractFromTestFile("https://www.washingtonpost.com/lifestyle/style/the-nearly-forgotten-story-of-the-black-women-who-helped-land-a-man-on-the-moon/2016/09/12/95f2d356-7504-11e6-8149-b8d05321db62_story.html", "washingtonpost.html");
    assertEquals("The nearly forgotten story of the black women who helped land a man on the moon - The Washington Post", article.title);
    assertStartsWith("In 2011, Mary Gainer was a historic preservationist for NASA, and she stumbled on a 1943 picture of a thousand people standing in a huge building.", article.document.text());
    assertEquals("https://img.washingtonpost.com/rf/image_1484w/2010-2019/WashingtonPost/2016/09/09/Style/Images/hidden-figures-DF-04856_R2_rgb.jpg", article.imageUrl);
  }

  @Test
  public void testBoingBoing() {
    Article article = extractFromTestFile("http://www.boingboing.net/2010/08/18/dr-laura-criticism-o.html", "boingboing.html");
    assertStartsWith("Dr. Laura: criticism of me infringes my first amendment rights Dr. Laura Schlessinger is leaving radio to regain her \"first amendment\" rights on the internet.", article.document.text());
  }

  @Test
  public void testReadWriteWeb() {
    Article article = extractFromTestFile("http://readwrite.com/2016/09/13/san-francisco-uc-berkeley-keep-smart-transit-city-plan-rolling-cl4/", "readwriteweb.html");
    assertEquals("#121212", article.themeColor);
    assertEquals("http://15809-presscdn-0-93.pagely.netdna-cdn.com/wp-content/uploads/iStock_83628999_SMALL-e1473787242221.jpg", article.imageUrl);
    assertStartsWith("San Francisco is using the momentum from its failed Smart City Challenge bid to carry on developing smart transportation initiatives.", article.document.text());
  }

  @Test
  public void testLifehacker() {
    Article article = extractFromTestFile("http://lifehacker.com/5659837/build-a-rocket-stove-to-heat-your-home-with-wood-scraps", "lifehacker.html");
    assertStartsWith("If you find yourself with lots of leftover wood", article.document.text());
    assertEquals("https://i.kinja-img.com/gawker-media/image/upload/s--9OsTlIZO--/c_fill,fl_progressive,g_center,h_358,q_80,w_636/18ixs0cqpu927jpg.jpg", article.imageUrl);
  }

  @Test
  public void testSfGate() {
    Article article = extractFromTestFile("http://www.sfgate.com/business/article/Foreclosure-activity-dips-in-California-Bay-Area-3248321.php", "sfgate.html");
    assertStartsWith("Fewer homes in California and", article.document.text());
    assertEquals("http://ww4.hdnux.com/photos/11/11/11/2396767/11/rawImage.jpg", article.imageUrl);
  }

  @Test
  public void testScientificDaily() {
    Article article = extractFromTestFile("http://www.scientificamerican.com/article.cfm?id=bpa-semen-quality", "scientificamerican.html");
    assertEquals("Everyday BPA Exposure Decreases Human Semen Quality: Scientific American", article.title);
    assertEquals("http://www.scientificamerican.com/media/inline/bpa-semen-quality_1.jpg", article.imageUrl);
    assertStartsWith("The common industrial chemical bisphenol A (BPA) ", article.document.text());
  }

  @Test
  public void testUniverseToday() {
    Article article = extractFromTestFile("http://www.universetoday.com/76881/podcast-more-from-tony-colaprete-on-lcross/", "universetoday.html");
    assertStartsWith("I had the chance to interview LCROSS", article.document.text());
    assertEquals("http://www.universetoday.com/wp-content/uploads/2009/10/lcross-impact_01_01.jpg",
        article.imageUrl);
    assertEquals("Podcast: More From Tony Colaprete on LCROSS", article.title);
  }

  @Test
  public void testReuters2() {
    Article article = extractFromTestFile("http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803", "reuters.html");
    assertEquals(1, article.images.size());
    assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=130&fh=&fw=&ll=&pl=&r=CBRE872074Y00", article.imageUrl);
  }

  @Test
  public void testMSNBC() {
    Article article = extractFromTestFile("http://www.msnbc.msn.com/id/41207891/ns/world_news-europe/", "msnbc.html");
    assertStartsWith("DUBLIN — Prime Minister Brian Cowen announced Saturday that he has resigned as leader of Ireland's dominant Fianna Fail party", article.document.text());
    assertEquals("Irish premier resigns as party leader, stays as PM - Europe", article.title);
  }

  @Test
  public void testTheAtlantic() {
    Article article = extractFromTestFile("http://www.theatlantic.com/business/archive/2016/09/census-poverty-economy-terrible/499793/", "theatlantic.html");
    assertStartsWith("In 2015, median household income increased for the first time in nearly a decade. On its face, that alone is progress.", article.document.text());
    assertEquals("https://cdn.theatlantic.com/assets/media/img/mt/2016/09/AP_16252467700939/facebook.jpg?1473782708", article.imageUrl);
  }

  @Test
  public void testNYT2() {
    Article article = extractFromTestFile("http://www.nytimes.com/2010/12/22/world/europe/22start.html", "nyt2.html");
    assertStartsWith("WASHINGTON — An arms control treaty paring back American and Russian nuclear arsenals won a decisive vote in the Senate on Tuesday", article.document.text());
    assertEquals("https://cdn1.nyt.com/images/2010/12/22/world/22start-span/Start-articleLarge.jpg", article.imageUrl);
  }

  @Test
  public void testCracked() {
    Article article = extractFromTestFile("http://www.cracked.com/blog/the-9-circles-vacation-hell/", "cracked.html");
    assertEquals("http://s3.crackedcdn.com/phpimages/article/0/6/6/573066_v1.jpg", article.imageUrl);
    assertStartsWith("In theory, everyone likes a nice vacation.", article.document.text());
  }

  @Test
  public void testEhow() {
    Article article = extractFromTestFile("http://www.ehow.com/how_5122199_eat-fresh-figs.html", "ehow.html");
    assertEquals("How to Eat Fresh Figs", article.title);
    assertStartsWith("While dried figs are more commonly featured in recipes, fresh figs are an absolute treat.", article.document.text());
  }

  @Test
  public void testNewsweek() {
    Article article = extractFromTestFile("http://www.newsweek.com/sport-rio-2016-paralympics-euthanasia-497932", "newsweek.html");
    assertStartsWith("The Paralymics podium is a stage on which to get your voice heard, and Belgium’s Marieke Vervoort is doing just that.", article.document.text());
    assertEquals("http://s.newsweek.com/sites/www.newsweek.com/files/2016/09/13/marieke-vervoort.jpg", article.imageUrl);
  }

  @Test
  public void testNature() {
    Article article = extractFromTestFile("http://www.nature.com/news/2011/110411/full/472146a.html", "nature.html");
    assertStartsWith("As the immediate threat from Fukushima Daiichi's damaged nuclear reactors recedes, engineers and scientists are", article.document.text());
  }

  @Test
  public void testGuardianAmpPage() {
    Article article = extractFromTestFile("https://cdn.ampproject.org/c/s/amp.theguardian.com/business/2016/nov/07/world-stock-markets-surge-clinton-us-election", "guardian-amp.html");
    assertEquals("World stock markets surge amid confidence Clinton will win US election", article.title);
    assertEquals("The three main US indices were almost 2% higher by noon, following strong gains in markets across the world ahead of the presidential election", article.description);
  }

  @Test
  public void testHackerNews() {
    Article article = extractFromTestFile("https://news.ycombinator.com/", "hackernews.html");
    assertEquals("Hacker News", article.title);
    assertEquals("", article.description);
  }

  @Test
  public void testGoogleComTablet() {
    Article article = extractFromTestFile("https://www.google.com/", "google_tablet.html");
    assertEquals("Google", article.title);
    assertEquals("https://www.google.com/images/branding/googleg/2x/googleg_standard_color_76dp.png", article.faviconUrl);
  }

  private Article extractFromTestFile(String baseUri, String testFile) {
    try {
      Article article = Extractor.with(new CandidateURL(baseUri),
          CharsetConverter.readStream(new FileInputStream(new File("test_data/" + testFile))).content)
          .extractMetadata().extractContent().article();
      Log.i("%s", article.document.childNodes().toString());
      return article;
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
