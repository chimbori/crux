package com.chimbori.crux.urls;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RedirectorsTest {
  @Test
  public void testGoogleRedirectors() {
    assertEquals("https://www.facebook.com/permalink.php?id=111262459538815&story_fbid=534292497235807&ct=ga&cd=CAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA&usg=AFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw",
        CruxURL.parse("https://www.google.com/url?q=https://www.google.com/url?rct%3Dj%26sa%3Dt%26url%3Dhttps://www.facebook.com/permalink.php%253Fid%253D111262459538815%2526story_fbid%253D534292497235807%26ct%3Dga%26cd%3DCAEYACoTOTQxMTQ5NzcyMzExMjAwMTEyMzIcZWNjZWI5M2YwM2E5ZDJiODpjb206ZW46VVM6TA%26usg%3DAFQjCNFSwGsQjcbeVCaSO2rg90RgBpQvzA&source=gmail&ust=1589164930980000&usg=AFQjCNF37pEGpMAz7azFCry-Ib-hwR0VVw").resolveRedirects().toString());
  }
}
