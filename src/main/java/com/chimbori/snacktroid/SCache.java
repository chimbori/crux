package com.chimbori.snacktroid;

public interface SCache {

  ParsedResult get(String url);

  void put(String url, ParsedResult res);

  int getSize();
}
