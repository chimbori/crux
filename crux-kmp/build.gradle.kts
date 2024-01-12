plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain{
          dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ksoup)
            implementation(libs.klaxon)
          }
        }
    }
}

android {
    namespace = "com.chimbori.crux_kmp"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
