# Crux

Crux parses Web pages to identify the crux of an article — the very essential points — minus all the fluff.

## Features

- Rich formatted content available, not just plain text.
- Support for more sites & better parsing overall.
- Support for more metadata formats: OpenGraph, Twitter Cards, Schema.org.
- Small footprint and code size: JSoup is the only required dependency.
- Fewer setters/getters, to keep the method count low (this is important for Android).
- The ability to use HTTP libraries besides the default HttpUrlConnection, such as OkHttp, under the hood.
- Cleaner, leaner code (compared to other libraries not optimized for Android)
- First-class support for importing into Android Studio projects via Gradle.
- [![Build Status](https://travis-ci.org/chimbori/crux.svg?branch=master)](https://travis-ci.org/chimbori/crux): Continuous integration with unit tests and golden file tests.  

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
  compile 'com.github.chimbori:crux:-SNAPSHOT'
}
```

## Sample Code

Using a singleton instance of OkHttp.
```java
OkHttpClient okHttpClient = new OkHttpClient();
```

In a background thread:
```java
CandidateURL candidateUrl = new CandidateURL(url); 
if (candidateURL.isLikelyArticle()) {
  Request request = new Request.Builder()
      .url(url)  // Customize your network request as you see fit.
      .build();
  Response response = okHttpClient.newCall().execute();
  Article article = new Extractor().extractContent(response.body().string());
}
```

On the UI thread:
```java
// Use article.document, article.title, etc.
```

# History

Crux began as a fork of [Snacktory](http://github.com/karussell/snacktory) with the goal of making it more performant on Android devices, 
but it has quickly gained several new features that are not available in Snacktory.   

Snacktory (and thus Crux) borrow ideas and test cases from [Goose](https://github.com/GravityLabs/goose) 
and [JReadability](https://github.com/ifesdjeen/jReadability).

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
