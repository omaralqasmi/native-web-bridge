plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.nativewebbridge"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // Bridge Dependencies
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}

// JitPack Publishing Block
afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(components.findByName("release"))
                groupId = "com.github.omaralqasmi"
                artifactId = "native-web-bridge"
                version = System.getenv("JITPACK_VERSION") ?: "local-build"
            }
        }
    }
}