package com.chimbori.crux.urls

import org.junit.Assert.*
import org.junit.Test

class CruxURLTest {
  @Test
  fun testIsHttpURL() {
    assertEquals(true, CruxURL.parse("http://example.com")?.isWebScheme)
    assertEquals(true, CruxURL.parse("https://example.com")?.isWebScheme)
    assertEquals(true, CruxURL.parse("example.com")?.isWebScheme)
    assertEquals(false, CruxURL.parse("file://error")?.isWebScheme)
    assertEquals(false, CruxURL.parse("ftp://example.com")?.isWebScheme)
    assertEquals(false, CruxURL.parse("mailto:test@example.com")?.isWebScheme)
  }

  @Test
  fun testEmptyURLs() {
    assertNull("Failed to catch illegal input: null", CruxURL.parse(null))
    assertNull("Failed to catch illegal input: \"\"", CruxURL.parse(""))
  }

  @Test
  fun testMalformedNonWebSchemes() {
    assertNull(CruxURL.parse("data:text/html;charset=utf-8,%3Cstyle%20tyle=%22text/css%22%3Ebody%7Bmargin:%200%200;%20font-family:%22Roboto%22,arial,sans-serif;%7D@-webkit-keyframes%20CHANGE1%7B0%25,100%25%7Bbackground-color:#FF0000%7D50%25%7Bbackground-color:#FFFF00%7D%7D@-webkit-keyframes%20CHANGE2%7B0%25,100%25%7Bbackground-color:#FFFF00%7D50%25%7Bbackground-color:#FF0000%7D%7D@-moz-keyframes%20CHANGE1%7B0%25,100%25%7Bbackground-color:#FF0000%7D50%25%7Bbackground-color:#FFFF00%7D%7D@-moz-keyframes%20CHANGE2%7B0%25,100%25%7Bbackground-color:#FFFF00%7D50%25%7Bbackground-color:#FF0000%7D%7D@-o-keyframes%20CHANGE1%7B0%25,100%25%7Bbackground-color:#FF0000%7D50%25%7Bbackground-color:#FFFF00%7D%7D@-o-keyframes%20CHANGE2%7B0%25,100%25%7Bbackground-color:#FFFF00%7D50%25%7Bbackground-color:#FF0000%7D%7D@-ms-keyframes%20CHANGE1%7B0%25,100%25%7Bbackground-color:#FF0000%7D50%25%7Bbackground-color:#FFFF00%7D%7D@-ms-keyframes%20CHANGE2%7B0%25,100%25%7Bbackground-color:#FFFF00%7D50%25%7Bbackground-color:#FF0000%7D%7D@keyframes%20CHANGE1%7B0%25,100%25%7Bbackground-color:#FF0000%7D50%25%7Bbackground-color:#FFFF00%7D%7D@keyframes%20CHANGE2%7B0%25,100%25%7Bbackground-color:#FFFF00%7D50%25%7Bbackground-color:#FF0000%7D%7D%3C/style%3E%3Cdiv%20style=%22display:%20block;%20background:#FFF;%20height:56px;%22%3E%3Cdiv%20style=%22overflow:hidden;display:%20inline-block;%20margin-left:20px;%20margin-right:10px;font-size:1.5em;%20background-color:#FF0000;%20color:#FFF;width:150px;%20height:37.333333333333336px;%20text-align:center;%20line-height:37.333333333333336px;%20margin-top:9.333333333333334px;-webkit-animation:CHANGE1%201s%20ease%20infinite;-webkit-border-radius:5px;-webkit-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-moz-animation:CHANGE1%201s%20ease%20infinite;-moz-border-radius:5px;-moz-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-o-animation:CHANGE1%201s%20ease%20infinite;-o-border-radius:5px;-o-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-ms-animation:CHANGE1%201s%20ease%20infinite;-ms-border-radius:5px;-ms-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);animation:CHANGE1%201s%20ease%20infinite;border-radius:5px;box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);%22%3EPlay%3C/div%3E%3Cdiv%20style=%22overflow:hidden;display:%20inline-block;%20margin-left:10px;%20margin-right:20px;%20font-size:1.5em;%20background-color:#FFFF00;%20color:#FFF;width:150px;%20height:37.333333333333336px;%20text-align:center;%20line-height:37.333333333333336px;%20margin-top:9.333333333333334px;-webkit-animation:CHANGE2%201s%20ease%20infinite;-webkit-border-radius:5px;-webkit-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-moz-animation:CHANGE2%201s%20ease%20infinite;-moz-border-radius:5px;-moz-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-o-animation:CHANGE2%201s%20ease%20infinite;-o-border-radius:5px;-o-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);-ms-animation:CHANGE2%201s%20ease%20infinite;-ms-border-radius:5px;-ms-box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);animation:CHANGE2%201s%20ease%20infinite;border-radius:5px;box-shadow:1px%200%201px%200%20rgba(128,128,128,.3);%22%3EDownload%3C/div%3E%3C/div%3E"))
  }

  @Test
  fun testURLsRejectedByJavaNetURIsStrictParser() {
    assertEquals(true, CruxURL.parse("http://example.com/?parameter={invalid-character}")?.isWebScheme)
  }

  @Test
  fun testNoOpRedirects() {
    val exampleCruxUrl = CruxURL.parse("http://example.com")?.resolveRedirects()
    assertEquals("http://example.com", exampleCruxUrl.toString())
    assertEquals(true, exampleCruxUrl?.isWebScheme)
    assertEquals(true, exampleCruxUrl?.isLikelyArticle)
    assertEquals("about:blank", CruxURL.parse("about:blank")?.resolveRedirects().toString())
  }

  @Test
  fun testRedirects() {
    assertEquals("http://www.bet.com/collegemarketingreps",
        CruxURL.parse("http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketingreps&h=42263")
            ?.resolveRedirects().toString())
    assertEquals("https://www.wired.com/2014/08/maryam-mirzakhani-fields-medal/",
        CruxURL.parse("https://lm.facebook.com/l.php?u=https%3A%2F%2Fwww.wired.com%2F2014%2F08%2Fmaryam-mirzakhani-fields-medal%2F&h=ATMfLBdoriaBcr9HOvzkEe68VZ4hLhTiFINvMmq5_e6fC9yi3xe957is3nl8VJSWhUO_7BdOp7Yv9CHx6MwQaTkwbZ1CKgSQCt45CROzUw0C37Tp4V-2EvDSBuBM2H-Qew&enc=AZPhspzfaWR0HGkmbExT_AfCFThsP829S0z2UWadB7ponM3YguqyJXgtn2E9BAv_-IdZvW583OnNC9M6WroEsV1jlilk3FXS4ppeydAzaJU_o9gq6HvoGMj0N_SiIKHRE_Gamq8xVdEGPnCJi078X8fTEW_jrkwpPC6P6p5Z3gv6YkFZfskU6J9qe3YRyarG4dgM25dJFnVgxxH-qyHlHsYbMD69i2MF8QNreww1J6S84y6VbIxXC-m9dVfFlNQVmtWMUvJKDLcPmYNysyQSYvkknfZ9SgwBhimurLFmKWhf39nNNVYjjCszCJ1XT57xX0Q&s=1")
            ?.resolveRedirects().toString())
    assertEquals("http://www.cnn.com/2017/01/25/politics/scientists-march-dc-trnd/index.html",
        CruxURL.parse("http://lm.facebook.com/l.php?u=http%3A%2F%2Fwww.cnn.com%2F2017%2F01%2F25%2Fpolitics%2Fscientists-march-dc-trnd%2Findex.html&h=ATO7Ln_rl7DAjRcqSo8yfpOvrFlEmKZmgeYHsOforgXsUYPLDy3nC1KfCYE-hev5oJzz1zydvvzI4utABjHqU1ruwDfw49jiDGCTrjFF-EyE6xfcbWRmDacY_6_R-lSi9g&enc=AZP1hkQfMXuV0vOHa1VeY8kdip2N73EjbXMKx3Zf4Ytdb1MrGHL48by4cl9_DShGYj9nZXvNt9xad9_4jphO9QBpRJLNGoyrRMBHI09eoFyPmxxjw7hHBy5Ouez0q7psi1uvjiphzOKVxjxyYBWnTJKD7m8rvhFz0HespmfvCf-fUiCpi6NDpxwYEw7vZ99fcjOpkiQqaFM_Gvqeat7r0e8axnqM-pJGY0fkjgWvgwTyfiB4fNMRhH3IaAmyL7DXl0xeYMoYSHuITkjTY9aU5dkiETfDVwBABOO9FJi2nTnRMw92E-gMMbiHFoHENlaSVJc&s=1")
            ?.resolveRedirects().toString())
    assertEquals("https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/",
        CruxURL.parse("https://plus.url.google.com/url?q=https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/&rct=j&ust=1485739059621000&usg=AFQjCNH6Cgp4iU0NB5OoDpT3OtOXds7HQg")
            ?.resolveRedirects().toString())
  }

  @Test
  fun testStrictParsing() {
    // Spaces in the domain name are caught by the strict parser but not by the lenient parser!
    assertNotNull(CruxURL.parse("http://ex  ample.com"))
    assertNull(CruxURL.parseStrict("http://ex  ample.com"))
  }
}
