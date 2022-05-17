import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.20"
  id("com.vanniktech.maven.publish") version "0.19.0"
  id("com.github.ben-manes.versions") version "0.42.0"
}

repositories {
  mavenCentral()
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(kotlin("gradle-plugin", version = "1.6.20"))
    classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
  }
}

dependencies {
  api("org.jsoup:jsoup:1.14.3")
  api("com.squareup.okhttp3:okhttp:4.9.3")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

  testImplementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
  testImplementation("junit:junit:4.13.2")
}

configurations.all {
  // Re-run tests every time test cases are updated, even if sources haven’t changed.
  resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.jar {
  archiveBaseName.set("crux")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    allWarningsAsErrors = true
  }
}

kotlin {
  explicitApi = Strict
}
