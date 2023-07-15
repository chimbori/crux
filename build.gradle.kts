import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ben.manes.versions)
  alias(libs.plugins.maven.publish)
}

repositories {
  mavenCentral()
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(libs.kotlin.plugin)
  }
}

dependencies {
  api(libs.jsoup)
  api(libs.okhttp)

  implementation(libs.coroutines.core)
  implementation(libs.klaxon)

  testImplementation(libs.okhttp.mockwebserver)
  testImplementation(libs.okhttp.logging)
  testImplementation(libs.junit)
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
