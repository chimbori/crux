package com.chimbori.crux.plugins

import com.chimbori.crux.Resource
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleStaticRedirectorTest {
  @Test
  fun testGoogleRedirectorPlugin() {
    val googleRedirectorPlugin = GoogleStaticRedirector()
    mapOf(
      "http://example.com" to null,
      "https://plus.url.google.com/url?q=https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/&rct=j&ust=1485739059621000&usg=AFQjCNH6Cgp4iU0NB5OoDpT3OtOXds7HQg"
          to "https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/",
      "https://www.google.com/url?q=https://www.google.com/url?rct%3Dj%26sa%3Dt%26url%3Dhttps://www.facebook.com/permalink.php%253Fid%253D111262459538815%2526story_fbid%253D534292497235807%26ct%3Dga%26cd%3DCAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA%26usg%3DAFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw"
          to "https://www.facebook.com/permalink.php?id=111262459538815&story_fbid=534292497235807",
    ).forEach { (key, value) ->
      assertEquals(value != null, googleRedirectorPlugin.canExtract(key.toHttpUrl()))
      assertEquals(
        value?.toHttpUrl() ?: key.toHttpUrl(),
        runBlocking { googleRedirectorPlugin.extract(Resource(url = key.toHttpUrl())).url }
      )
    }
  }
}
