package com.chimbori.crux;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CandidateURLTest {
  @Test
  public void testIsHttpURL() {
    assertTrue(new CandidateURL("http://example.com").isHttp());
    assertTrue(new CandidateURL("https://example.com").isHttp());
    assertTrue(new CandidateURL("example.com").isHttp());
    assertFalse(new CandidateURL("file://error").isHttp());
    assertFalse(new CandidateURL("ftp://example.com").isHttp());
    assertFalse(new CandidateURL("mailto:test@example.com").isHttp());
  }

  @Test
  public void testRedirects() {
    assertEquals("http://www.bet.com/collegemarketingreps",
        new CandidateURL("http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketingreps&h=42263")
            .resolveRedirects().url.toString());

    assertEquals("https://www.wired.com/2014/08/maryam-mirzakhani-fields-medal/",
        new CandidateURL("https://lm.facebook.com/l.php?u=https%3A%2F%2Fwww.wired.com%2F2014%2F08%2Fmaryam-mirzakhani-fields-medal%2F&h=ATMfLBdoriaBcr9HOvzkEe68VZ4hLhTiFINvMmq5_e6fC9yi3xe957is3nl8VJSWhUO_7BdOp7Yv9CHx6MwQaTkwbZ1CKgSQCt45CROzUw0C37Tp4V-2EvDSBuBM2H-Qew&enc=AZPhspzfaWR0HGkmbExT_AfCFThsP829S0z2UWadB7ponM3YguqyJXgtn2E9BAv_-IdZvW583OnNC9M6WroEsV1jlilk3FXS4ppeydAzaJU_o9gq6HvoGMj0N_SiIKHRE_Gamq8xVdEGPnCJi078X8fTEW_jrkwpPC6P6p5Z3gv6YkFZfskU6J9qe3YRyarG4dgM25dJFnVgxxH-qyHlHsYbMD69i2MF8QNreww1J6S84y6VbIxXC-m9dVfFlNQVmtWMUvJKDLcPmYNysyQSYvkknfZ9SgwBhimurLFmKWhf39nNNVYjjCszCJ1XT57xX0Q&s=1")
            .resolveRedirects().url.toString());

    assertEquals("http://www.cnn.com/2017/01/25/politics/scientists-march-dc-trnd/index.html",
        new CandidateURL("http://lm.facebook.com/l.php?u=http%3A%2F%2Fwww.cnn.com%2F2017%2F01%2F25%2Fpolitics%2Fscientists-march-dc-trnd%2Findex.html&h=ATO7Ln_rl7DAjRcqSo8yfpOvrFlEmKZmgeYHsOforgXsUYPLDy3nC1KfCYE-hev5oJzz1zydvvzI4utABjHqU1ruwDfw49jiDGCTrjFF-EyE6xfcbWRmDacY_6_R-lSi9g&enc=AZP1hkQfMXuV0vOHa1VeY8kdip2N73EjbXMKx3Zf4Ytdb1MrGHL48by4cl9_DShGYj9nZXvNt9xad9_4jphO9QBpRJLNGoyrRMBHI09eoFyPmxxjw7hHBy5Ouez0q7psi1uvjiphzOKVxjxyYBWnTJKD7m8rvhFz0HespmfvCf-fUiCpi6NDpxwYEw7vZ99fcjOpkiQqaFM_Gvqeat7r0e8axnqM-pJGY0fkjgWvgwTyfiB4fNMRhH3IaAmyL7DXl0xeYMoYSHuITkjTY9aU5dkiETfDVwBABOO9FJi2nTnRMw92E-gMMbiHFoHENlaSVJc&s=1")
            .resolveRedirects().url.toString());

    assertEquals("https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/",
        new CandidateURL("https://plus.url.google.com/url?q=https://arstechnica.com/business/2017/01/before-the-760mph-hyperloop-dream-there-was-the-atmospheric-railway/&rct=j&ust=1485739059621000&usg=AFQjCNH6Cgp4iU0NB5OoDpT3OtOXds7HQg")
            .resolveRedirects().url.toString());
  }
}
