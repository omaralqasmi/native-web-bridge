plugins {
    id("com.android.library") version "8.1.1"
    id("org.jetbrains.kotlin.android") version "1.9.0"
    id("maven-publish")
}

android {
    namespace = "com.nativewebbridge"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}