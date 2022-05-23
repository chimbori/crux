package com.chimbori.crux.common

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HttpUrlExtensionsTest {
  @Test
  fun testIsLikelyType() {
    assertEquals(true, "http://example.com/video.mp4".toHttpUrl().isLikelyVideo())
    assertEquals(true, "http://example.com/video.mpg".toHttpUrl().isLikelyVideo())
    assertEquals(true, "http://example.com/video.avi".toHttpUrl().isLikelyVideo())
    assertEquals(false, "http://example.com/test.txt".toHttpUrl().isLikelyVideo())
    assertEquals(false, "http://example.com/test.tmp".toHttpUrl().isLikelyVideo())
    assertEquals(false, "http://example.com/test.log".toHttpUrl().isLikelyVideo())
  }

  @Test
  fun testURLsRejectedByJavaNetURIsStrictParser() {
    assertNotNull("http://example.com/?parameter={invalid-character}".toHttpUrlOrNull())
  }

  @Test
  fun testNoOpRedirects() {
    val exampleNoRedirects = "http://example.com".toHttpUrl().resolveRedirects()
    assertEquals("http://example.com/", exampleNoRedirects.toString())
    assertEquals(true, exampleNoRedirects.isLikelyArticle())
  }

  @Test
  fun testRedirects() {
    assertEquals(
      "http://www.bet.com/collegemarketingreps",
      "http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketingreps&h=42263"
        .toHttpUrl().resolveRedirects().toString()
    )
    assertEquals(
      "https://www.wired.com/2014/08/maryam-mirzakhani-fields-medal/",
      "https://lm.facebook.com/l.php?u=https%3A%2F%2Fwww.wired.com%2F2014%2F08%2Fmaryam-mirzakhani-fields-medal%2F&h=ATMfLBdoriaBcr9HOvzkEe68VZ4hLhTiFINvMmq5_e6fC9yi3xe957is3nl8VJSWhUO_7BdOp7Yv9CHx6MwQaTkwbZ1CKgSQCt45CROzUw0C37Tp4V-2EvDSBuBM2H-Qew&enc=AZPhspzfaWR0HGkmbExT_AfCFThsP829S0z2UWadB7ponM3YguqyJXgtn2E9BAv_-IdZvW583OnNC9M6WroEsV1jlilk3FXS4ppeydAzaJU_o9gq6HvoGMj0N_SiIKHRE_Gamq8xVdEGPnCJi078X8fTEW_jrkwpPC6P6p5Z3gv6YkFZfskU6J9qe3YRyarG4dgM25dJFnVgxxH-qyHlHsYbMD69i2MF8QNreww1J6S84y6VbIxXC-m9dVfFlNQVmtWMUvJKDLcPmYNysyQSYvkknfZ9SgwBhimurLFmKWhf39nNNVYjjCszCJ1XT57xX0Q&s=1"
        .toHttpUrl().resolveRedirects().toString()
    )
    assertEquals(
      "http://www.cnn.com/2017/01/25/politics/scientists-march-dc-trnd/index.html",
      "http://lm.facebook.com/l.php?u=http%3A%2F%2Fwww.cnn.com%2F2017%2F01%2F25%2Fpolitics%2Fscientists-march-dc-trnd%2Findex.html&h=ATO7Ln_rl7DAjRcqSo8yfpOvrFlEmKZmgeYHsOforgXsUYPLDy3nC1KfCYE-hev5oJzz1zydvvzI4utABjHqU1ruwDfw49jiDGCTrjFF-EyE6xfcbWRmDacY_6_R-lSi9g&enc=AZP1hkQfMXuV0vOHa1VeY8kdip2N73EjbXMKx3Zf4Ytdb1MrGHL48by4cl9_DShGYj9nZXvNt9xad9_4jphO9QBpRJLNGoyrRMBHI09eoFyPmxxjw7hHBy5Ouez0q7psi1uvjiphzOKVxjxyYBWnTJKD7m8rvhFz0HespmfvCf-fUiCpi6NDpxwYEw7vZ99fcjOpkiQqaFM_Gvqeat7r0e8axnqM-pJGY0fkjgWvgwTyfiB4fNMRhH3IaAmyL7DXl0xeYMoYSHuITkjTY9aU5dkiETfDVwBABOO9FJi2nTnRMw92E-gMMbiHFoHENlaSVJc&s=1"
        .toHttpUrl().resolveRedirects().toString()
    )
    assertEquals(
      "https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/",
      "https://plus.url.google.com/url?q=https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/&rct=j&ust=1485739059621000&usg=AFQjCNH6Cgp4iU0NB5OoDpT3OtOXds7HQg"
        .toHttpUrl().resolveRedirects().toString()
    )
  }

  @Test
  fun testGoogleRedirectors() {
    assertEquals(
      "https://www.facebook.com/permalink.php?id=111262459538815&story_fbid=534292497235807",
      "https://www.google.com/url?q=https://www.google.com/url?rct%3Dj%26sa%3Dt%26url%3Dhttps://www.facebook.com/permalink.php%253Fid%253D111262459538815%2526story_fbid%253D534292497235807%26ct%3Dga%26cd%3DCAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA%26usg%3DAFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw"
        .toHttpUrl().resolveRedirects().toString()
    )
  }
}
