# Snacktroid

Snacktroid is a fork of [Snacktory](http://github.com/karussell/snacktory) specially built for Android devices, but by no means exclusive to Android.

Snacktory is a library that can extract text, keywords, main image, and other metadata from a Web article.

Snacktory (and thus Snacktroid) borrow ideas and test cases from [Goose](https://github.com/GravityLabs/goose) 
and [JReadability](https://github.com/ifesdjeen/jReadability).

## Goals

Snacktroid is a work in progress. Not all goals have been met yet, but here’s a list of what we want to achieve with Snacktroid.

- Fewer dependencies: JSoup is the only required dependency. log4j and slf4j-api are not required.
- Fewer setters/getters, to keep the method count low.
- The ability to use HTTP libraries besides the default HttpUrlConnection, such as OkHttp, under the hood.
- First-class support for importing via Gradle.
- Cleaner code, e.g. throwing specific Exceptions instead of `java.lang.Exception` and better error handling.

# Features

  - article text detection 
  - get top image url(s)
  - get top video url
  - extraction of description, keywords, ...
  - good detection for none-english sites (German, Japanese, ...), snacktory does not depend on the word count in its text detection to support CJK languages 
  - good charset detection
  - possible to do URL resolving, but caching is still possible after resolving
  - skipping some known filetypes
  - no http GET required to run the core tests

## TODO

 * Only top text is currently supported.

# Usage
 
## Import via Gradle

Project/`build.gradle`:
```groovy
allprojects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```

Module/`build.gradle`:
```
dependencies {
  compile 'com.github.chimbori:snacktroid:-SNAPSHOT'
}
```

## Sample Code

```java
HtmlFetcher fetcher = new HtmlFetcher();
// set cache. e.g. take the map implementation from google collections:
// fetcher.setCache(new MapMaker().concurrencyLevel(20).maximumSize(count).
//    expireAfterWrite(minutes, TimeUnit.MINUTES).makeMap();

JResult res = fetcher.fetchAndExtract(articleUrl, resolveTimeout, true);
String text = res.getText(); 
String title = res.getTitle(); 
String imageUrl = res.getImageUrl();
```

# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
