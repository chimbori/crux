package com.chimbori.snacktroid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CandidateURLTest {
  @Test
  public void testIsVideoHost() {
    assertTrue(new CandidateURL("m.vimeo.com").isVideoHost());
    assertTrue(new CandidateURL("m.youtube.com").isVideoHost());
    assertTrue(new CandidateURL("www.youtube.com").isVideoHost());
    assertTrue(new CandidateURL("http://youtube.com").isVideoHost());
    assertTrue(new CandidateURL("http://www.youtube.com").isVideoHost());
    assertTrue(new CandidateURL("https://youtube.com").isVideoHost());
    assertFalse(new CandidateURL("test.com").isVideoHost());
    assertFalse(new CandidateURL("irgendwas.com/youtube.com").isVideoHost());
  }

  @Test
  public void testRedirects() {
    assertEquals("http://www.bet.com/collegemarketingreps&h=42263",
        new CandidateURL("http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.bet.com%2Fcollegemarketingreps&h=42263").resolveRedirects().url.toString());
  }
}
