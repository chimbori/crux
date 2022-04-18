package com.chimbori.crux.articles

import com.chimbori.crux.common.fromFile
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GoldenFilesTest {
  @Test
  fun testAOLNews() {
    fromFile(
      "http://www.aolnews.com/nation/article/the-few-the-proud-the-marines-getting-a-makeover/19592478",
      "aolnews.html"
    ).run {
      assertEquals("http://o.aolcdn.com/art/ch_news/aol_favicon.ico".toHttpUrl(), faviconUrl)
      assertStartsWith("WASHINGTON (Aug. 13) -- Declaring \"the maritime soul of the Marine Corps", document?.text())
      assertEquals("http://o.aolcdn.com/photo-hub/news_gallery/6/8/680919/1281734929876.JPEG".toHttpUrl(), imageUrl)
      assertArrayEquals(
        listOf(
          "news", "update", "breaking", "nation", "U.S.", "elections", "world", "entertainment", "sports", "business",
          "weird news", "health", "science", "latest news articles", "breaking news", "current news", "top news"
        ).toTypedArray(), keywords?.toTypedArray()
      )
    }
  }

  @Test
  fun testBBC() {
    fromFile("http://www.bbc.co.uk/news/world-latin-america-21226565", "bbc.html").run {
      assertEquals("BBC News", siteName)
      assertEquals("Baby born on Mediterranean rescue ship - BBC News", title)
      assertEquals("http://www.bbc.com/news/world-africa-37341871".toHttpUrl(), canonicalUrl)
      assertEquals(
        "http://ichef-1.bbci.co.uk/news/1024/cpsprodpb/146E6/production/_91168638_baby070012-9-20162-1photocreditalvawhitemsf.jpg".toHttpUrl(),
        imageUrl
      )
      assertEquals("http://www.bbc.co.uk/news/amp/37341871".toHttpUrl(), ampUrl)
      assertStartsWith(
        "A Nigerian woman has given birth to a boy on board a rescue ship in the Mediterranean",
        document?.text()
      )
    }
  }

  @Test
  fun testBBC_AMP() {
    fromFile("http://www.bbc.co.uk/news/amp/37341871", "bbc-amp.html").run {
      assertEquals("BBC News", siteName)
      assertEquals("Baby born on Mediterranean rescue ship", title)
      assertEquals(
        "http://ichef.bbci.co.uk/news/999/cpsprodpb/146E6/production/_91168638_baby070012-9-20162-1photocreditalvawhitemsf.jpg".toHttpUrl(),
        imageUrl
      )
      assertStartsWith(
        "A Nigerian woman has given birth to a boy on board a rescue ship in the Mediterranean after being plucked from an overcrowded rubber dinghy.",
        document?.text()
      )
    }
  }

  @Test
  fun testBenjaminStein() {
    fromFile("http://benjaminste.in/post/1223476561/hey-guys-whatcha-doing", "benjaminstein.html").run {
      assertEquals("BenjaminSte.in - Hey guys, whatcha doing?", title)
      assertStartsWith("This month is the 15th anniversary of my last CD.", document?.text())
      assertEquals(true, keywords?.isEmpty())
    }
  }

  @Test
  fun testBlogger() {
    fromFile("http://blog.talawah.net/2011/04/gavin-king-unviels-red-hats-top-secret.html", "blogger.html")
      .run {
        assertStartsWith("Gavin King unveils Red Hat's Java successor: The Ceylon Project", document?.text())
        assertStartsWith(
          "Gavin King of Red Hat/Hibernate/Seam fame recently unveiled the top secret project that he has been working on over the past two years",
          document?.child(1)?.text()
        )
        assertEquals(
          "http://3.bp.blogspot.com/-cyMzveP3IvQ/TaR7f3qkYmI/AAAAAAAAAIk/mrChE-G0b5c/s200/Java.png".toHttpUrl(),
          images?.get(0)?.srcUrl
        )
        assertEquals("The Brain Dump: Gavin King unveils Red Hat's Java killer successor: The Ceylon Project", title)
        assertEquals("http://blog.talawah.net/feeds/posts/default?alt=rss".toHttpUrl(), feedUrl)
      }
  }

  @Test
  fun testBloomberg() {
    fromFile(
      "http://www.bloomberg.com/news/2010-11-01/china-becomes-boss-in-peru-on-50-billion-mountain-bought-for-810-million.html",
      "bloomberg.html"
    ).run {
      assertEquals("Bloomberg", siteName)
      assertStartsWith("The Chinese entrepreneur and the Peruvian shopkeeper", document?.text())
      assertEquals("http://www.bloomberg.com/apps/data?pid=avimage&iid=iimODmqjtcQU".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testBoingBoing() {
    fromFile("http://www.boingboing.net/2010/08/18/dr-laura-criticism-o.html", "boingboing.html").run {
      assertStartsWith(
        "Dr. Laura: criticism of me infringes my first amendment rights Dr. Laura Schlessinger is leaving radio to regain her \"first amendment\" rights on the internet.",
        document?.text()
      )
    }
  }

  @Test
  fun testBrOnline() {
    fromFile(
      "http://www.br-online.de/br-klassik/programmtipps/highlight-bayreuth-tannhaeuser-festspielzeit-2011-ID1309895438808.xml",
      "br-online.html",
      charset = "iso-8859-15"
    ).run {
      assertStartsWith(
        "Wenn ein Dirigent, der Alte Musik liebt, erstmals eine Neuproduktion bei den Bayreuther Richard-Wagner-Festspielen übernimmt,",
        document?.text()
      )
      assertEquals("Eröffnung der 100. Bayreuther Festspiele: Alles neu beim \"Tannhäuser\"", title)
    }
  }

  @Test
  fun testCNet() {
    fromFile("http://www.cnet.com/news/verizon-shows-off-ipad-tv-app-and-more/", "cnet.html").run {
      assertEquals("CNET", siteName)
      assertEquals("http://www.cnet.com/news/verizon-shows-off-ipad-tv-app-and-more/".toHttpUrl(), canonicalUrl)
      assertStartsWith("NEW YORK--Verizon Communications is prepping a new", document?.text())
      assertEquals(
        "https://cnet1.cbsistatic.com/img/Bw23T5rupUVnvVPCQQ3KjfO9qic=/670x503/2010/08/18/53b6d52b-f0fa-11e2-8c7c-d4ae52e62bcc/Verizon_iPad_and_live_TV_with_big_TV.JPG".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testCaltonCaldwell() {
    fromFile("http://daltoncaldwell.com/dear-mark-zuckerberg", "daltoncaldwell.html").run {
      assertEquals("Dear Mark Zuckerberg by Dalton Caldwell", title)
      assertStartsWith(
        "On June 13, 2012, at 4:30 p.m., I attended a meeting at Facebook HQ in Menlo Park, California.",
        document?.text()
      )
    }
  }

  @Test
  fun testCracked() {
    fromFile("http://www.cracked.com/blog/the-9-circles-vacation-hell/", "cracked.html").run {
      assertEquals("http://s3.crackedcdn.com/phpimages/article/0/6/6/573066_v1.jpg".toHttpUrl(), imageUrl)
      assertStartsWith("In theory, everyone likes a nice vacation.", document?.text())
    }
  }

  @Test
  fun testESPN() {
    fromFile("http://www.espn.com/espn/commentary/news/story?id=5461430", "espn.html").run {
      assertStartsWith("If you believe what college football coaches have said about sports", document?.text())
      assertEquals("http://a.espncdn.com/photo/2010/0813/pg2_g_bush3x_300.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testESPN2() {
    fromFile("http://www.espn.com/golf/pgachampionship10/news/story?id=5463456", "espn2.html").run {
      assertStartsWith(
        "SHEBOYGAN, Wis. -- The only number that matters at the PGA Championship is on the scorecard, not the birth certificate.",
        document?.text()
      )
      assertEquals(
        "http://a2.espncdn.com/combiner/i?img=%2Fi%2Fheadshots%2Fgolf%2Fplayers%2Ffull%2F780.png".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testEconomist() {
    fromFile("http://www.economist.com/node/17956885", "economist.html").run {
      assertStartsWith("FOR beleaguered smokers, the world is an increasingly", document?.text())
      assertEquals(
        "http://www.economist.com/sites/default/files/images/articles/migrated/20110122_stp004.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testEhow() {
    fromFile("http://www.ehow.com/how_5122199_eat-fresh-figs.html", "ehow.html").run {
      assertEquals("How to Eat Fresh Figs", title)
      assertStartsWith(
        "While dried figs are more commonly featured in recipes, fresh figs are an absolute treat.",
        document?.text()
      )
    }
  }

  @Test
  fun testEngadget() {
    fromFile(
      "http://www.engadget.com/2010/08/18/verizon-fios-set-top-boxes-getting-a-new-hd-guide-external-stor/",
      "engadget.html"
    ).run {
      assertStartsWith(
        "Streaming and downloading TV content to mobiles is nice, but we enjoy watching TV... on the TV",
        document?.text()
      )
      assertEquals(
        "https://www.blogcdn.com/www.engadget.com/media/2010/08/44ni600.jpg".toHttpUrl(),
        imageUrl
      )
      assertEquals(
        "https://s.blogsmithmedia.com/www.engadget.com/assets-haa9c2740c98180d07c436859c827e9f1/images/favicon-160x160.png?h=1638b0a8bbe7effa8f85c3ecabb63620".toHttpUrl(),
        faviconUrl
      )
    }
  }

  @Test
  fun testEspn3WithFlashVideo() {
    fromFile("http://sports.espn.go.com/nfl/news/story?id=5971053", "espn3.html").run {
      assertStartsWith("PHILADELPHIA -- Michael Vick missed practice Thursday", document?.text())
      assertEquals("http://a.espncdn.com/i/espn/espn_logos/espn_red.png".toHttpUrl(), imageUrl)
      assertEquals(
        "Michael Vick of Philadelphia Eagles misses practice, unlikely to play vs. Dallas Cowboys - ESPN",
        title
      )
    }
  }

  @Test
  fun testFolhaUolComBr() {
    fromFile(
      "http://m.folha.uol.com.br/ciencia/2017/01/1854055-no-futuro-as-pessoas-nao-morrerao-por-envelhecimento-diz-cientista.shtml?mobile",
      "folha_uol_com_br.html",
      charset = "windows-1252"
    ).run {
      assertEquals(
        "No futuro, as pessoas não morrerão por envelhecimento, diz cientista - 30/01/2017 - Ciência - Folha de S.Paulo",
        title
      )
      assertStartsWith(
        "Aubrey de Grey, 53, quer curar o envelhecimento. Sim, para esse pesquisador inglês, formado em ciências da computação na Universidade de Cambridge, envelhecer é uma doença tal como a malária –ou ainda pior, por vitimar muito mais pessoas– que pode ser perfeitamente evitável.",
        document?.text()
      )
      assertStartsWith(
        """|<p> Aubrey de Grey, 53, quer curar o envelhecimento. Sim, para esse pesquisador inglês, formado em ciências da computação na Universidade de Cambridge, envelhecer é uma doença tal como a malária –ou ainda pior, por vitimar muito mais pessoas– que pode ser perfeitamente evitável. </p>
          |<p> A seu ver, para pensar em uma solução é preciso entender o envelhecimento e a morte como resultado de um processo de acúmulo de danos e imperfeições no organismo. </p>
          |""".trimMargin(), document?.html()
      )
    }
  }

  @Test
  fun testFoxNews() {
    fromFile(
      "http://www.foxnews.com/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/",
      "foxnews.html"
    ).run {
      assertStartsWith(
        "Russia's announcement that it will help Iran get nuclear fuel is raising questions about what President Obama calls the \"better-than- ever\" relationship",
        document?.text()
      )
      assertEquals(
        "http://a57.foxnews.com/images.foxnews.com/content/fox-news/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/_jcr_content/par/featured-media/media-0.img.jpg/0/0/1446837847097.jpg?ve=1".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testFoxSports() {
    fromFile(
      "http://msn.foxsports.com/nfl/story/Tom-Cable-fired-contract-option-Oakland-Raiders-coach-010411",
      "foxsports.html"
    ).run {
      assertStartsWith("The Oakland Raiders informed coach Tom Cable", document?.text())
      assertEquals("Oakland Raiders won't bring Tom Cable back as coach - NFL News", title)
    }
  }

  @Test
  fun testGaltimeWhereUrlContainsSpaces() {
    fromFile(
      "http://galtime.com/article/entertainment/37/22938/kris-humphries-avoids-kim-talk-gma",
      "galtime.com.html"
    ).run {
      assertEquals(
        "http://vnetcdn.dtsph.com/files/vnet3/imagecache/opengraph_ogimage/story-images/Kris%20Humphries%20Top%20Bar.JPG".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testGigaOm() {
    fromFile("http://gigaom.com/apple/apples-next-macbook-an-800-mac-for-the-masses/", "gigaom.html").run {
      assertStartsWith("The MacBook Air is a bold move forward ", document?.text())
      assertEquals("http://gigapple.files.wordpress.com/2010/10/macbook-feature.png?w=604".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testGizmodo() {
    fromFile("http://www.gizmodo.com.au/2010/08/xbox-kinect-gets-its-fight-club/", "gizmodo.html").run {
      assertEquals("Gizmodo Australia", siteName)
      assertStartsWith("You love to punch your arms through the air", document?.text())
      assertEquals(
        "http://cache.gawkerassets.com/assets/images/9/2010/08/500x_fighters_uncaged__screenshot_3b__jawbreaker.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testGolem() {
    fromFile("http://www.golem.de/1104/82797.html", "golem.html").run {
      assertStartsWith(
        "Unter dem Namen \"Aurora\" hat Firefox einen neuen Kanal mit Vorabversionen von Firefox eingerichtet.",
        document?.text()
      )
      assertEquals(
        "http://scr3.golem.de/screenshots/1104/Firefox-Aurora/thumb480/aurora-nighly-beta-logos.png".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertEquals("Mozilla: Vorabversionen von Firefox 5 und 6 veröffentlicht - Golem.de", title)
    }
  }

  @Test
  fun testGoogleComTablet() {
    fromFile("https://www.google.com/", "google_tablet.html").run {
      assertEquals("Google", title)
      assertEquals(
        "https://www.google.com/images/branding/googleg/2x/googleg_standard_color_76dp.png".toHttpUrl(),
        faviconUrl
      )
    }
  }

  @Test
  fun testGuardianAmpPage() {
    fromFile(
      "https://cdn.ampproject.org/c/s/amp.theguardian.com/business/2016/nov/07/world-stock-markets-surge-clinton-us-election",
      "guardian-amp.html"
    ).run {
      assertEquals("World stock markets surge amid confidence Clinton will win US election", title)
      assertEquals(
        "The three main US indices were almost 2% higher by noon, following strong gains in markets across the world ahead of the presidential election",
        description
      )
    }
  }

  @Test
  fun testHackerNews() {
    fromFile("https://news.ycombinator.com/", "hackernews.html").run {
      assertEquals("Hacker News", title)
      assertEquals(null, description)
    }
  }

  @Test
  fun testHackerNoon() {
    fromFile(
      "https://hackernoon.com/design-thinking-lessons-from-our-cats-9a43fd71457a",
      "hackernoon.html"
    ).run {
      assertEquals("Design thinking lessons from our cats – Hacker Noon", title)
      assertStartsWith(
        "We can all agree that cats spend the vast majority of their time thinking through complex problems in innovative ways.",
        document?.text()
      )
      assertContains(
        "I’ve never known a cat that didn’t demonstrate a deep understanding of market economics.",
        document?.text()
      )
      assertContains(
        "Cats aren’t discouraged by the risk of looking dumb and being laughed at as they experiment and explore their environment…",
        document?.text()
      )
      assertContains(
        "*I did not actually do this but this is what I imagine would have happened if I had.",
        document?.text()
      )
      assertContains(
        "If you liked this article, be sure to recommend it and help spread good ideas as far and wide as tufts of cat hair caught in a warm summer’s breeze.",
        document?.text()
      )
    }
  }

  @Test
  fun testHeise() {
    fromFile(
      "http://www.heise.de/newsticker/meldung/Internet-Explorer-9-jetzt-mit-schnellster-JavaScript-Engine-1138062.html",
      "heise.html"
    ).run {
      assertEquals(
        "http://m.f.ix.de/scale/geometry/250/q50/imgs/18/1/7/8/2/8/5/5/b6e69ac13bb564dcaba745f4b419e23f_edited_105951127_8168730ae9-255ed03a302fdb50.jpeg@jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertEquals("Internet Explorer 9 jetzt mit schnellster JavaScript-Engine", title)
      assertStartsWith(
        "Internet Explorer 9 jetzt mit schnellster JavaScript-Engine Microsoft hat heute eine siebte Platform Preview ",
        document?.text()
      )
    }
  }

  @Test
  fun testHuffingtonPost() {
    fromFile(
      "http://www.huffingtonpost.com/2010/08/13/federal-reserve-pursuing_n_681540.html",
      "huffingtonpost.html"
    ).run {
      assertEquals("Federal Reserve's Low Rate Policy Is A 'Dangerous Gamble,' Says Top Central Bank Official", title)
      assertStartsWith("A top regional Federal Reserve official sharply", document?.text())
      assertEquals("http://i.huffpost.com/gen/157611/thumbs/s-FED-large.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testI4Online() {
    fromFile("https://i4online.com", "i4online.html").run {
      assertStartsWith("Just one week to go and everything is set for the summer Forum 2013", document?.text())
    }
  }

  @Test
  fun testITunes() {
    fromFile("http://itunes.apple.com/us/album/21/id420075073", "itunes.html").run {
      assertStartsWith(
        "What else can be said of this album other than that it is simply amazing? Adele's voice is powerful, vulnerable, assured, and heartbreaking all in one fell swoop.",
        document?.text()
      )
      assertStartsWith("Preview songs from 21 by ADELE", description)
      assertNull(faviconUrl)
    }
  }

  @Test
  fun testKhaamaPress() {
    fromFile(
      "http://www.khaama.com/over-100-school-girls-poisoned-in-western-afghanistan-0737",
      "khaama.html"
    ).run {
      assertStartsWith(
        "Over 100 school girls have been poisoned in western Farah province of Afghanistan during the school hours.",
        document?.text()
      )
    }
  }

  @Test
  fun testLifehacker() {
    fromFile(
      "http://lifehacker.com/5659837/build-a-rocket-stove-to-heat-your-home-with-wood-scraps",
      "lifehacker.html"
    ).run {
      assertStartsWith("If you find yourself with lots of leftover wood", document?.text())
      assertEquals(
        "https://i.kinja-img.com/gawker-media/image/upload/s--9OsTlIZO--/c_fill,fl_progressive,g_center,h_358,q_80,w_636/18ixs0cqpu927jpg.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testMSNBC() {
    fromFile("http://www.msnbc.msn.com/id/41207891/ns/world_news-europe/", "msnbc.html").run {
      assertStartsWith(
        "DUBLIN — Prime Minister Brian Cowen announced Saturday that he has resigned as leader of Ireland's dominant Fianna Fail party",
        document?.text()
      )
      assertEquals("Irish premier resigns as party leader, stays as PM - Europe", title)
    }
  }

  @Test
  fun testMashable() {
    fromFile("http://mashable.com/2010/08/18/how-tonot-to-ask-someone-out-online/", "mashable.html").run {
      assertStartsWith("Imagine, if you will, a crowded dance floor", document?.text())
      assertEquals("http://9.mshcdn.com/wp-content/uploads/2010/07/love.jpg".toHttpUrl(), imageUrl)
    }
  }

  /**
   * This test checks the crux-keep feature. The crux-keep attributes are
   * dropped into the test file directly for simplicity. The nodes with the
   * crux-keep attribute are the <a> nodes being tested here.
  </a> */
  @Test
  fun testMashable2() {
    fromFile("https://mashable.com/2015/04/24/astronaut-scott-kelly-room-photo/", "mashable2.html").run {
      assertStartsWith("NASA astronaut Scott Kelly", document?.text())
      assertContains("<a href=\"https://twitter.com/StationCDRKelly/status/591594008046084096\">", document?.html())
      assertContains("<a href=\"https://twitter.com/StationCDRKelly/status/591334283081560064\">", document?.html())
      assertEquals(
        "https://i.amz.mshcdn.com/r831Qn9cn1G7A9q2F3-1PH1VIyw=/640x360/2015%2F04%2F24%2Fda%2Fscottkellyr.3f2af.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testNews24() {
    fromFile(
      "https://www.news24.com/World/News/watch-indonesia-frees-bali-nine-drug-smuggler-lawrence-from-prison-20181121",
      "news24.html"
    ).run {
      assertEquals("WATCH: Indonesia frees Bali Nine drug smuggler Lawrence from prison", title)
      assertStartsWith(
        "The first member of the \"Bali Nine\" heroin-trafficking gang was released from prison on Wednesday after serving 13 years, in a case that caused a huge diplomatic rift between Indonesia and Australia.",
        document?.text()
      )
    }
  }

  @Test
  fun testNPR() {
    fromFile("http://www.npr.org/blogs/money/2010/10/04/130329523/how-fake-money-saved-brazil", "npr.html").run {
      assertEquals("How Fake Money Saved Brazil : Planet Money : NPR", title)
      assertEquals(null, siteName)
      assertStartsWith(
        "This is a story about how an economist and his buddies tricked the people of Brazil into saving the country from rampant inflation. They had a crazy, unlikely plan, and it worked. Twenty years ago, Brazil's",
        document?.text()
      )
      assertTrue(document?.text()?.endsWith("\"How Four Drinking Buddies Saved Brazil.\"") == true)
      assertEquals(
        "http://media.npr.org/assets/img/2010/10/04/real_wide.jpg?t=1286218782&s=3".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertTrue(keywords?.isEmpty() == true)
    }
  }

  @Test
  fun testNYT() {
    fromFile(
      "http://dealbook.nytimes.com/2011/04/11/for-defense-in-galleon-trial-no-time-to-rest/",
      "nyt.html"
    ).run {
      assertEquals("DealBook", siteName)
      assertEquals(
        "http://graphics8.nytimes.com/images/2011/04/12/business/dbpix-raj-rajaratnam-1302571800091/dbpix-raj-rajaratnam-1302571800091-tmagSF.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertStartsWith("I wouldn’t want to be Raj Rajaratnam’s lawyer right now.", document?.text())
    }
  }

  @Test
  fun testNYT2() {
    fromFile("http://www.nytimes.com/2010/12/22/world/europe/22start.html", "nyt2.html").run {
      assertStartsWith(
        "WASHINGTON — An arms control treaty paring back American and Russian nuclear arsenals won a decisive vote in the Senate on Tuesday",
        document?.text()
      )
      assertEquals(
        "https://cdn1.nyt.com/images/2010/12/22/world/22start-span/Start-articleLarge.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testNYTCooking() {
    fromFile("https://cooking.nytimes.com/recipes/1018068-chicken-paprikash", "nyt-cooking.html").run {
      assertEquals("Chicken Paprikash Recipe - NYT Cooking", title)
      assertEquals("NYT Cooking", siteName)
      assertStartsWith(
        "Spices lose their flavor over time but few as quickly as paprika, which starts out tasting of pepper and sunshine",
        document?.text()
      )
    }
  }

  @Test
  fun testNature() {
    fromFile("http://www.nature.com/news/2011/110411/full/472146a.html", "nature.html").run {
      assertStartsWith(
        "As the immediate threat from Fukushima Daiichi's damaged nuclear reactors recedes, engineers and scientists are",
        document?.text()
      )
    }
  }

  @Test
  fun testNewYorker() {
    fromFile(
      "http://www.newyorker.com/humor/borowitz-report/scientists-earth-endangered-by-new-strain-of-fact-resistant-humans",
      "newyorker.html"
    ).run {
      assertEquals("Scientists: Earth Endangered by New Strain of Fact-Resistant Humans - The New Yorker", title)
      assertEquals(
        "http://www.newyorker.com/wp-content/uploads/2015/05/Borowitz-Earth-Endangered-by-Fact-Resistant-Humans-1200-630-12152424.jpg".toHttpUrl(),
        imageUrl
      )
      assertStartsWith(
        "MINNEAPOLIS (The Borowitz Report)—Scientists have discovered a powerful new strain of fact-resistant humans who are threatening the ability of Earth to sustain life",
        document?.text()
      )
    }
  }

  @Test
  fun testNewsweek() {
    fromFile("http://www.newsweek.com/sport-rio-2016-paralympics-euthanasia-497932", "newsweek.html").run {
      assertStartsWith(
        "The Paralymics podium is a stage on which to get your voice heard, and Belgium’s Marieke Vervoort is doing just that.",
        document?.text()
      )
      assertEquals(
        "http://s.newsweek.com/sites/www.newsweek.com/files/2016/09/13/marieke-vervoort.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testNinjaTraderBlog() {
    fromFile(
      "http://www.ninjatraderblog.com/im/2010/10/seo-marketing-facts-about-google-instant-and-ranking-your-website/",
      "ninjatraderblog.html"
    ).run {
      assertStartsWith(
        "SEO Marketing- Facts About Google Instant And Ranking Your Website Many users around the world Google their queries",
        document?.text()
      )
    }
  }

  @Test
  fun testPolitico() {
    fromFile("http://www.politico.com/news/stories/1010/43352.html", "politico.html").run {
      assertStartsWith("If the newest Census Bureau estimates stay close to form", document?.text())
      assertEquals("http://images.politico.com/global/news/100927_obama22_ap_328.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testReadWriteWeb() {
    fromFile(
      "http://readwrite.com/2016/09/13/san-francisco-uc-berkeley-keep-smart-transit-city-plan-rolling-cl4/",
      "readwriteweb.html"
    ).run {
      assertEquals("#121212", themeColor)
      assertEquals(
        "http://15809-presscdn-0-93.pagely.netdna-cdn.com/wp-content/uploads/iStock_83628999_SMALL-e1473787242221.jpg".toHttpUrl(),
        imageUrl
      )
      assertStartsWith(
        "San Francisco is using the momentum from its failed Smart City Challenge bid to carry on developing smart transportation initiatives.",
        document?.text()
      )
    }
  }

  @Test
  fun testRedditAtomFeed() {
    fromFile(
      "https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/",
      "reddit.html"
    ).run {
      assertEquals(
        "https://www.reddit.com/r/androidapps/comments/4nle7s/dev_hermit_has_a_new_ad_blocker_50_off_premium/.rss".toHttpUrl(),
        feedUrl
      )
    }
  }

  @Test
  fun testRetractionWatch() {
    fromFile(
      "http://retractionwatch.com/2017/04/26/troubling-new-way-evade-plagiarism-detection-software-tell-used/",
      "retraction_watch.html"
    ).run {
      assertEquals(
        "A troubling new way to evade plagiarism detection software. (And how to tell if it's been used.) - Retraction Watch at Retraction Watch",
        title
      )
      assertStartsWith(
        "Recently, at the end of a tutorial, a student asked Ann Rogerson a question she’d never heard before: Was it okay to use paraphrasing tools to write up assignments?",
        document?.text()
      )
      assertContains("I had my answer about what the student in the previous session had done.", document?.text())
      assertContains(
        "…however I have no experience or evidence whether professional academics are using the tools for their scholarly publishing.",
        document?.text()
      )
      assertContains("SIDEBAR: How to identify text modified by a paraphrasing tool", document?.text())
    }
  }

  @Test
  fun testReuters() {
    fromFile(
      "http://www.reuters.com/article/us-knightcapital-trading-technology-idUSBRE87203X20120803",
      "reuters.html"
    ).run {
      assertEquals("Knight trading loss shows cracks in equity markets", title)
      assertEquals(
        "http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=130&fh=&fw=&ll=&pl=&r=CBRE872074Y00".toHttpUrl(),
        imageUrl
      )
      assertStartsWith(
        "Knight trading loss shows cracks in equity markets Knight's future in balance after trading disaster",
        document?.text()
      )
    }
  }

  @Test
  fun testRian() {
    fromFile("http://en.rian.ru/world/20110410/163458489.html", "rian.html").run {
      assertStartsWith(
        "About 15,000 people took to the streets in Tokyo on Sunday to protest against ",
        document?.text()
      )
      assertEquals("Japanese rally against nuclear power industry", title)
      assertEquals("http://en.rian.ru/favicon.ico".toHttpUrl(), faviconUrl)
      assertEquals(true, keywords?.isEmpty())
    }
  }

  @Test
  fun testScience() {
    fromFile(
      "http://news.sciencemag.org/sciencenow/2011/04/early-birds-smelled-good.html",
      "sciencemag.html"
    ).run {
      assertStartsWith(
        "About 65 million years ago, most of the dinosaurs and many other animals and plants were wiped off Earth, probably due to an asteroid hitting our planet. Researchers have long debated how and why some ",
        document?.text()
      )
    }
  }

  @Test
  fun testScientificDaily() {
    fromFile(
      "http://www.scientificamerican.com/article.cfm?id=bpa-semen-quality",
      "scientificamerican.html"
    ).run {
      assertEquals("Everyday BPA Exposure Decreases Human Semen Quality: Scientific American", title)
      assertEquals(
        "http://www.scientificamerican.com/media/inline/bpa-semen-quality_1.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertStartsWith("The common industrial chemical bisphenol A (BPA) ", document?.text())
    }
  }

  @Test
  fun testSfGate() {
    fromFile(
      "http://www.sfgate.com/business/article/Foreclosure-activity-dips-in-California-Bay-Area-3248321.php",
      "sfgate.html"
    ).run {
      assertStartsWith("Fewer homes in California and", document?.text())
      assertEquals("http://ww4.hdnux.com/photos/11/11/11/2396767/11/rawImage.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testShockYa() {
    fromFile(
      "http://www.shockya.com/news/2011/01/30/daily-shock-jonathan-knight-of-new-kids-on-the-block-publicly-reveals-hes-gay/",
      "shockya.html"
    ).run {
      assertStartsWith("New Kids On The Block singer Jonathan Knight has publicly", document?.text())
      assertEquals(
        "http://www.shockya.com/news/wp-content/uploads/jonathan_knight_new_kids_gay.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testSlamMagazine() {
    fromFile("http://www.slamonline.com/online/nba/2010/10/nba-schoolyard-rankings/", "slamonline.html").run {
      assertEquals("SLAM ONLINE | » NBA Schoolyard Rankings", title)
      assertEquals(
        "http://www.slamonline.com/online/wp-content/uploads/2010/10/celtics.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertStartsWith("Thursday, October 28th, 2010 at 3:32 pm", document?.text())
    }
  }

  @Test
  fun testSpiegel() {
    fromFile(
      "http://www.spiegel.de/netzwelt/gadgets/retro-pc-commodore-reaktiviert-den-c64-a-755090.html",
      "spiegel.html",
      charset = "iso-8859-1"
    ).run {
      assertStartsWith(
        "Da ist er wieder, der C64: Eigentlich längst ein Relikt der Technikgeschichte, soll der ",
        document?.text()
      )
    }
  }

  @Test
  fun testSportingNews() {
    fromFile(
      "http://www.sportingnews.com/nfl/feed/2011-01/nfl-coaches/story/raiders-cut-ties-with-cable",
      "sportingnews.html"
    ).run {
      assertStartsWith(
        "ALAMEDA, Calif. — The Oakland Raiders informed coach Tom Cable on Tuesday that they will not bring him back",
        document?.text()
      )
      assertEquals("http://dy.snimg.com/story-image/0/69/174475/14072-650-366.jpg".toHttpUrl(), imageUrl)
      assertEquals("Raiders cut ties with Cable - NFL - Sporting News", title)
    }
  }

  @Test
  fun testSportsIllustrated() {
    fromFile(
      "http://www.si.com/nba/2016/09/07/shaq-basketball-hall-of-fame-lakers-magic-heat-lsu-tigers",
      "sportsillustrated.html"
    ).run {
      assertStartsWith(
        "Way back in 1994, Shaquille O’Neal, who will be inducted into the Naismith Basketball Hall of Fame on Friday, was asked about Knicks center Patrick Ewing.",
        document?.text()
      )
      assertEquals(
        "http://windows.api.si.com/s3/files/styles/inline_gallery_desktop/public/2016/09/08/shaquille-o-neal-hall-of-fame-lakers-magic-lsu.jpg?itok=oupTSSJY".toHttpUrl(),
        imageUrl
      )
      assertEquals("http://www.si.com/img/favicons/favicon-192.png".toHttpUrl(), faviconUrl)
    }
  }

  @Test
  fun testStackOverflow() {
    fromFile("http://stackoverflow.com/questions/3553693/wicket-vs-vaadin/3660938", "stackoverflow.html").run {
      assertStartsWith("I think I've invested some time for both frameworks", document?.text())
      assertStartsWith("java - wicket vs Vaadin - Stack Overflow", title)
      assertEquals(
        "http://cdn.sstatic.net/Sites/stackoverflow/img/apple-touch-icon@2.png?v=73d79a89bded&a".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testTazBlog() {
    fromFile("http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/", "taz.html").run {
      assertStartsWith(
        "Protestkultur in Japan nach der Katastrophe Absolute Minderheit: Im Shiba-Park in Tokio",
        document?.text()
      )
      assertEquals("Protestkultur in Japan nach der Katastrophe: Anti-Atomkraft? Nein danke! - taz.de", title)
    }
  }

  @Test
  fun testTechCrunch() {
    fromFile("http://techcrunch.com/2011/04/04/twitter-advanced-search/", "techcrunch.html").run {
      assertEquals(
        "Twitter Finally Brings Advanced Search Out Of Purgatory; Updates Discovery Algorithms",
        title
      )
      assertEquals(
        "https://s0.wp.com/wp-content/themes/vip/techcrunch-2013/assets/images/techcrunch.opengraph.default.png".toHttpUrl(),
        imageUrl
      )
      assertStartsWith(
        "A couple weeks ago, we wrote a post wishing Twitter a happy fifth birthday, but also noting ",
        document?.text()
      )
    }
  }

  @Test
  fun testTechCrunch2() {
    fromFile(
      "http://techcrunch.com/2010/08/13/gantto-takes-on-microsoft-project-with-web-based-project-management-application/",
      "techcrunch2.html"
    ).run {
      assertEquals("Gantto Takes On Microsoft Project With Web-Based Project Management Application", title)
      assertStartsWith("Y Combinator-backed Gantto is launching", document?.text())
      assertEquals("http://tctechcrunch.files.wordpress.com/2010/08/gantto.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testTheAtlantic() {
    fromFile(
      "http://www.theatlantic.com/business/archive/2016/09/census-poverty-economy-terrible/499793/",
      "theatlantic.html"
    ).run {
      assertStartsWith(
        "In 2015, median household income increased for the first time in nearly a decade. On its face, that alone is progress.",
        document?.text()
      )
      assertEquals(
        "https://cdn.theatlantic.com/assets/media/img/mt/2016/09/AP_16252467700939/facebook.jpg?1473782708".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testTheDailyBeast() {
    fromFile(
      "http://www.thedailybeast.com/blogs-and-stories/2010-11-01/ted-sorensen-speechwriter-behind-jfks-best-jokes/?cid=topic:featured1",
      "thedailybeast.html"
    ).run {
      assertStartsWith("Legendary Kennedy speechwriter Ted Sorensen passed", document?.text())
      assertEquals(
        "http://www.tdbimg.com/resizeimage/YTo0OntzOjM6ImltZyI7czo2MToiMjAxMC8xMS8wMS9pbWctYnMtYm90dG9tLS0ta2F0ei10ZWQtc29yZW5zZW5fMTYzMjI4NjEwMzUxLmpwZyI7czo1OiJ3aWR0aCI7aTo1MDtzOjY6ImhlaWdodCI7aTo1MDtzOjY6InJhbmRvbSI7czoxOiIxIjt9.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testTheFrisky() {
    fromFile(
      "http://www.thefrisky.com/2010-10-28/rachel-dratch-met-her-baby-daddy-in-a-bar/",
      "thefrisky.html"
    ).run {
      assertStartsWith("Rachel Dratch had been keeping the identity of her baby daddy ", document?.text())
      assertEquals("http://static.thefrisky.com/uploads/2010/10/28/rachel_dratch_102810_m.jpg".toHttpUrl(), imageUrl)
      assertEquals("Rachel Dratch Met Her Baby Daddy At A Bar - The Frisky", title)
    }
  }

  @Test
  fun testTheVacationGals() {
    fromFile(
      "http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/",
      "thevacationgals.html"
    ).run {
      assertStartsWith("Editors’ Note: We are huge proponents of vacation rental homes", document?.text())
      assertEquals(3, images?.size)
      // The first link is absolute, so the domain is correctly specified.
      assertEquals(
        "http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      // The second link has a relative path, and the canonical URL is incorrectly specified as a local hostname, so although the URL is inaccessible, the parsing is correct.
      assertEquals(
        "http://vacationrentalhomesfamilyreunions/wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg".toHttpUrl(),
        images?.get(1)?.srcUrl
      )
      assertEquals("http://www.linkwithin.com/pixel.png".toHttpUrl(), images?.get(2)?.srcUrl)
    }
  }

  @Test
  fun testTimeMagazine() {
    fromFile("http://content.time.com/time/health/article/0,8599,2011497,00.html", "time.html").run {
      assertStartsWith("This month, the federal government released", document?.child(0)?.text())
      assertEquals("http://img.timeinc.net/time/daily/2010/1008/360_bp_oil_spill_0817.jpg".toHttpUrl(), imageUrl)
    }
  }

  @Test
  fun testTraindom() {
    fromFile("http://blog.traindom.com/places-where-to-submit-your-startup-for-coverage/", "traindom.html").run {
      assertEquals("36 places where you can submit your startup for some coverage", title)
      assertArrayEquals(
        listOf("blog coverage", "get coverage", "startup review", "startups", "submit startup").toTypedArray(),
        keywords?.toTypedArray()
      )
      assertStartsWith("So you have a new startup company and want some coverage", document?.text())
    }
  }

  @Test
  fun testTwitpic() {
    fromFile("http://twitpic.com/4k1ku3", "twitpic.html").run {
      assertEquals("It’s hard to be a dinosaur. on Twitpic", title)
      assertStartsWith(
        "Lazypicture from youtube made a video about this book! It cracked me up!!",
        document?.text()
      )
    }
  }

  @Test
  fun testTwitpic2() {
    fromFile("http://twitpic.com/4kuem8", "twitpic2.html").run {
      assertEquals("*Not* what you want to see on the fetal monitor when your wif... on Twitpic", title)
      assertStartsWith(
        "*Not* what you want to see on the fetal monitor when your wife begins to push.",
        document?.text()
      )
    }
  }

  @Test
  fun testTwitterBlog() {
    fromFile(
      "http://engineering.twitter.com/2011/04/twitter-search-is-now-3x-faster_1656.html",
      "twitter.html"
    ).run {
      assertEquals("Twitter Engineering: Twitter Search is Now 3x Faster", title)
      assertEquals(
        "http://4.bp.blogspot.com/-CmXJmr9UAbA/TZy6AsT72fI/AAAAAAAAAAs/aaF5AEzC-e4/s400/Blender_Tsunami.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertStartsWith(
        "In the spring of 2010, the search team at Twitter started to rewrite our search engine in order to serve our ever-growin",
        document?.text()
      )
    }
  }

  @Test
  fun testUniverseToday() {
    fromFile(
      "http://www.universetoday.com/76881/podcast-more-from-tony-colaprete-on-lcross/",
      "universetoday.html"
    ).run {
      assertStartsWith("I had the chance to interview LCROSS", document?.text())
      assertEquals(
        "http://www.universetoday.com/wp-content/uploads/2009/10/lcross-impact_01_01.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertEquals("Podcast: More From Tony Colaprete on LCROSS", title)
    }
  }

  @Test
  fun testUsaToday2() {
    fromFile(
      "http://content.usatoday.com/communities/driveon/post/2010/08/gm-finally-files-for-ipo/1",
      "usatoday2.html"
    ).run {
      assertStartsWith(
        "GM files for IPO, but are taxpayers still on the hook? General Motors just filed with the Securities and Exchange Commission",
        document?.text()
      )
      assertEquals(
        "http://i.usatoday.net/communitymanager/_photos/drive-on/2010/08/18/cruzex-wide-community.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testUsatoday() {
    fromFile(
      "http://content.usatoday.com/communities/thehuddle/post/2010/08/brett-favre-practices-set-to-speak-about-return-to-minnesota-vikings/1",
      "usatoday.html"
    ).run {
      assertStartsWith(
        "Brett Favre says he couldn't give up on one more chance to win the Super Bowl with Vikings Brett Favre couldn't get away from the \"what ifs.\"",
        document?.text()
      )
      assertEquals(
        "http://i.usatoday.net/communitymanager/_photos/the-huddle/2010/08/18/favrespeaksx-inset-community.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testVentureBeat() {
    fromFile(
      "http://social.venturebeat.com/2010/08/18/facebook-reveals-the-details-behind-places/",
      "venturebeat.html"
    ).run {
      assertStartsWith("Facebook just confirmed the rumors", document?.text())
      assertEquals(
        "http://cdn.venturebeat.com/wp-content/uploads/2010/08/mark-zuckerberg-facebook-places.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
    }
  }

  @Test
  fun testWallStreetJournal() {
    fromFile("http://www.wsj.com/articles/SB10001424052748704532204575397061414483040", "wsj.html").run {
      assertStartsWith(
        "The Obama administration has paid out less than a third of the nearly $230 billion",
        document?.text()
      )
      assertEquals(
        "https://si.wsj.net/public/resources/images/OB-JO759_0814st_D_20100814143158.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testWashingtonPost() {
    fromFile(
      "https://www.washingtonpost.com/lifestyle/style/the-nearly-forgotten-story-of-the-black-women-who-helped-land-a-man-on-the-moon/2016/09/12/95f2d356-7504-11e6-8149-b8d05321db62_story.html",
      "washingtonpost.html"
    ).run {
      assertEquals(
        "The nearly forgotten story of the black women who helped land a man on the moon - The Washington Post", title
      )
      assertStartsWith(
        "In 2011, Mary Gainer was a historic preservationist for NASA, and she stumbled on a 1943 picture of a thousand people standing in a huge building.",
        document?.text()
      )
      assertEquals(
        "https://img.washingtonpost.com/rf/image_1484w/2010-2019/WashingtonPost/2016/09/09/Style/Images/hidden-figures-DF-04856_R2_rgb.jpg".toHttpUrl(),
        imageUrl
      )
    }
  }

  @Test
  fun testWikipedia() {
    fromFile("http://en.wikipedia.org/wiki/Therapsids", "wikipedia.html").run {
      assertStartsWith(
        "Therapsida is a group of the most advanced reptile-grade synapsids, and the ancestors of mammals",
        document?.text()
      )
      assertStartsWith(
        "<b>Therapsida</b> is a group of the most advanced reptile-grade <a href=\"/wiki/Synapsid\">synapsids</a>",
        document?.child(0)?.html()
      )
      assertEquals(
        "http://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Pristeroognathus_DB.jpg/240px-Pristeroognathus_DB.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertEquals("http://en.wikipedia.org/apple-touch-icon.png".toHttpUrl(), faviconUrl)
    }
  }

  @Test
  fun testWikipediaDarwin() {
    fromFile("https://en.wikipedia.org/wiki/Charles_Darwin", "wikipedia_darwin.html").run {
      assertEquals("Charles Darwin - Wikipedia", title)
      assertStartsWith("For other people named Charles Darwin, see Charles Darwin (disambiguation).", document?.text())
    }
  }

  @Test
  fun testWikipediaGalileo() {
    fromFile("https://en.wikipedia.org/wiki/Galileo_Galilei", "wikipedia_galileo.html").run {
      assertStartsWith(
        "\"Galileo\" redirects here. For other uses, see Galileo (disambiguation) and Galileo Galilei (disambiguation).",
        document?.text()
      )
    }
  }

  @Test
  fun testWikipediaOktoberfest() {
    fromFile("https://de.m.wikipedia.org/wiki/Oktoberfest", "wikipedia_oktoberfest.html").run {
      assertStartsWith(
        "Das erste Oktoberfest Bearbeiten Anlässlich der Hochzeit zwischen Kronprinz Ludwig und Prinzessin Therese am 12. ",
        document?.text()
      )
    }
  }

  @Test
  fun testWired() {
    fromFile("http://www.wired.com/playbook/2010/08/stress-hormones-boxing/", "wired.html").run {
      assertStartsWith("On November 25, 1980, professional boxing", document?.text())
      assertEquals("Stress Hormones Could Predict Boxing Dominance", title)
      assertEquals(
        "http://www.wired.com/playbook/wp-content/uploads/2010/08/fight_f-660x441.jpg".toHttpUrl(),
        images?.get(0)?.srcUrl
      )
      assertEquals("http://blog.wired.com/gadgets/files/apple-touch-icon.png".toHttpUrl(), faviconUrl)
    }
  }

  @Test
  fun testWiredBitcoin() {
    fromFile("https://www.wired.com/story/bitcoin-will-burn-planet-down-how-fast/", "wired-bitcoin.html").run {
      assertStartsWith("Max Krause was thinking of buying some bitcoin, as one does.", document?.text())
      assertEquals("Bitcoin Will Burn the Planet Down. The Question: How Fast?", title)
    }
  }

  @Test
  fun testWiredScience() {
    fromFile("https://www.wired.com/2017/04/dangerous-volcano-can-tricky-thing-pin/", "wired-volcano.html").run {
      assertEquals("The ‘Most Dangerous’ Volcano Can Be a Tricky Thing to Pin Down", title)
      assertStartsWith(
        "I know you’ve all seen lists like this before: what is the “world’s most dangerous volcano“? Most of the time, that discuss devolves quickly into something about “supervolcanoes“, which is very exciting and all because they can generate massive eruptions. However, they are far from being the “most dangerous” volcano.",
        document?.text()
      )
      assertContains("What is the volcano’s eruptive history?", document?.text())
    }
  }

  @Test
  fun testWordpress() {
    fromFile("http://karussell.wordpress.com/", "wordpress.html").run {
      assertEquals("Twitter API and Me « Find Time for the Karussell", title)
      assertStartsWith("I have a love hate relationship with Twitter. As a user I see ", document?.text())
    }
  }

  @Test
  fun testYCombinator() {
    fromFile("http://paulgraham.com/seesv.html", "ycombinator.html").run {
      assertStartsWith("Want to start a startup? Get funded by Y Combinator.", document?.text())
      assertStartsWith(
        "October 2010 • Silicon Valley proper is mostly suburban sprawl.",
        document?.child(1)?.text()
      )
      assertTrue(document?.text()?.endsWith(" and Jessica Livingston for reading drafts of this. •") == true)
      assertEquals(true, keywords?.isEmpty())
    }
  }

  @Test
  fun testYomiuri() {
    fromFile(
      "http://www.yomiuri.co.jp/e-japan/gifu/news/20110410-OYT8T00124.htm",
      "yomiuri.html",
      charset = "Shift_JIS"
    ).run {
      assertEquals("色とりどりのチューリップ : 岐阜 : 地域 : YOMIURI ONLINE（読売新聞）", title)
      assertContains(
        "海津市海津町の国営木曽三川公園で、チューリップが見頃を迎えている。２０日までは「チューリップ祭」が開かれており、大勢の人たちが多彩な色や形を鑑賞している＝写真＝",
        document?.text()
      )
      assertArrayEquals(listOf("読売新聞", "地域").toTypedArray(), keywords?.toTypedArray())
    }
  }

  @Test
  fun testYouTube() {
    fromFile("https://www.youtube.com/watch?v=wlupmjrfaB4", "youtube.html").run {
      assertStartsWith(
        "Master of the Puppets by Metallica. Converted to 8 bit with GSXCC. Original verson can be found us",
        document?.text()
      )
      assertEquals("YouTube - Metallica - Master of the Puppets 8-bit", title)
      assertEquals("http://i4.ytimg.com/vi/wlupmjrfaB4/default.jpg".toHttpUrl(), imageUrl)
      assertEquals("http://www.youtube.com/v/wlupmjrfaB4?version=3".toHttpUrl(), videoUrl)
      assertEquals("http://s.ytimg.com/yt/favicon-vflZlzSbU.ico".toHttpUrl(), faviconUrl)
    }
  }

  companion object {
    private fun assertStartsWith(expected: String, actual: String?) {
      if (actual?.startsWith(expected) == false) {
        fail("Expected \n[$expected]\n at start of \n[$actual]\n")
      }
    }

    private fun assertContains(expected: String, actual: String?) {
      if (actual?.contains(expected) == false) {
        fail("Expected \n[$expected]\n in \n[$actual]\n")
      }
    }
  }
}
