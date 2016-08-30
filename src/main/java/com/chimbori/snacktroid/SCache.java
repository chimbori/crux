package com.chimbori.snacktroid;

/**
 * @author Peter Karich
 */
public interface SCache {

  JResult get(String url);

  void put(String url, JResult res);

  int getSize();
}
