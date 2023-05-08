import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.8.21"
  id("com.github.ben-manes.versions") version "0.45.0"
  id("com.vanniktech.maven.publish") version "0.24.0"
}

repositories {
  mavenCentral()
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(kotlin("gradle-plugin", version = "1.8.21"))
    classpath("com.github.ben-manes:gradle-versions-plugin:0.45.0")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.24.0")
  }
}

dependencies {
  api("org.jsoup:jsoup:1.16.1")
  api("com.squareup.okhttp3:okhttp:4.10.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
  implementation("com.beust:klaxon:5.6")

  testImplementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
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
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("11"))
  }
}

mavenPublishing {
  signAllPublications()
}
