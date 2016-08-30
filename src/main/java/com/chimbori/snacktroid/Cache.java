package com.chimbori.snacktroid;

public interface Cache {
  ParsedResult get(String url);

  void put(String url, ParsedResult res);

  int getSize();
}
