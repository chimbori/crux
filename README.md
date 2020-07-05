# Crux

Crux parses Web pages to identify the crux of an article — the very essential points — minus all the
fluff. The library consists of multiple independent APIs. You can pick and choose which ones you
want to use. If you use Crux in an Android app, they are designed to be independent so that Proguard
or other minification tools can strip out the parts you don’t use.

## Article Extraction API

- Rich formatted content available, not just plain text.
- Support for more sites & better parsing overall.
- Support for more metadata formats: OpenGraph, Twitter Cards, Schema.org.
- Small footprint and code size: JSoup is the only required dependency.
- Fewer setters/getters, to keep the method count low (this is important for Android).
- The ability to use HTTP libraries besides the default HttpUrlConnection, such as OkHttp, under
  the hood.
- Cleaner, leaner code (compared to other libraries not optimized for Android)
- First-class support for importing into Android Studio projects via Gradle.
- ![Gradle Test](https://github.com/chimbori/crux/workflows/Gradle%20Test/badge.svg) Continuous integration with unit tests and golden file tests.

In a background thread, make a network request and obtain the `rawHTML` of the page you would like
to analyze.

#### Kotlin
```kotlin
val url = "https://example.com/article.html"
val rawHTML = "<html><body><h1>This is an article</h1></body></html>"
val article = ArticleExtractor(url, rawHTML)
    .extractMetadata()
    .extractContent()  // If you only need metadata, you can skip `.extractContent()`
    .article
```

On the UI thread:
```kotlin
// Use article.document, article.title, etc.
```

#### Java
```java
String url = "https://example.com/article.html";
String rawHTML = "<html><body><h1>This is an article</h1></body></html>";  // Intentionally malformed.
HttpUrl httpURL = HttpUrl.Companion.parse(url);
Article article = new ArticleExtractor(url, rawHTML)
    .extractMetadata()
    .extractContent()
    .getArticle();
```

On the UI thread:
```java
// Use article.getDocument(), article.getTitle(), etc.
```

### `crux-keep`

If you control the HTML that is fed into Crux, and would like to instruct Crux to keep certain DOM
nodes, irrespective of what Crux’s algorithm recommends, add the special attribute `crux-keep` to
each such DOM node.

```html
<p crux-keep="true">
  Content that should not be removed.
</p>
```

## Image URL Extractor API

From a single DOM Element root, the Image URL API inspects the sub-tree and returns the best
possible image URL candidate available within it. It does this by scanning within the DOM tree
for interesting `src` & `style` tags.

All URLs are resolved as absolute URLs, even if the HTML contained relative URLs.

#### Kotlin
```kotlin
ImageUrlExtractor(url, domElement).findImage().imageUrl
```

#### Java
```java
new ImageUrlExtractor(url, domElement).findImage().getImageUrl();
```

## Anchor Links Extractor API

From a single DOM Element root, the Image URL API inspects the sub-tree and returns the best
possible link URL candidate available within it. It does this by scanning within the DOM tree
for interesting `href` tags.

All URLs are resolved as absolute URLs, even if the HTML contained relative URLs.

#### Kotlin
```kotlin
LinkUrlExtractor(url, domElement).findLink().linkUrl
```

#### Java
```java
new LinkUrlExtractor(url, domElement).findLink().getLinkUrl();
```

## URL Heuristics API

This API examines a given URL (without connecting to the server), and returns
heuristically-determined answers to questions such as:

- Is this URL likely a video URL?
- Is this URL likely an image URL?
- Is this URL likely an audio URL?
- Is this URL likely an executable URL?
- Is this URL likely an archive URL?

This API also supports resolving redirects for certain well-known redirectors, with the precondition
that the target URL be available as part of this candidate URL. In other words, this API will
not be able to resolve redirectors that perform a HTTP 301 redirect.

#### Kotlin
```kotlin
val url = "https://example.com/article.html".toHttpUrlOrNull()
url?.resolveRedirects()
url?.isLikelyArticle()  // Returns true.
url?.isLikelyImage()  // Returns false.
```

#### Java
```java
HttpUrl url = HttpUrl.Companion.parse("https://example.com/article.html");
url.resolveRedirects();
HttpUrlExtensionsKt.isLikelyArticle(url);  // Returns true.
HttpUrlExtensionsKt.isLikelyImage(url);  // Returns false.
```

# Usage

Include Crux in your project, then see sample code for each API provided above.

Crux uses semantic versioning. If the API changes, then the major version will be incremented.
Upgrading from one minor version to the next minor version within the same major version should
not require any client code to be modified.

## Get Crux via Maven

```xml
<dependency>
  <groupId>com.chimbori.crux</groupId>
  <artifactId>crux</artifactId>
  <version>0.0.0</version>   <!-- Get latest version number from https://github.com/chimbori/crux/releases -->
</dependency>
```

## Get Crux via Gradle

Project/`build.gradle`:
```groovy
allprojects {
  repositories {
    mavenCentral()
  }
}
```

Module/`build.gradle`:

```groovy
dependencies {
  compile 'com.chimbori.crux:crux:0.0.0'  // See the latest version number below.
}
```

The latest version is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chimbori.crux/crux/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chimbori.crux/crux)

# History

Crux began as a fork of [Snacktory](http://github.com/karussell/snacktory) with the goal of making
it more performant on Android devices, but it has quickly gained several new features that are not
available in Snacktory.

Snacktory (and thus Crux) borrow ideas and test cases from [Goose](https://github.com/GravityLabs/goose)
and [JReadability](https://github.com/ifesdjeen/jReadability).

# License

    Copyright 2016, Chimbori, makers of Hermit, the Lite Apps Browser.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
