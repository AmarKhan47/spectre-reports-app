import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun prop(name: String): String =
    providers.gradleProperty(name).orNull
        ?: error("Missing '$name' in gradle.properties")

val portalUrl = prop("PORTAL_URL")
val appName = prop("APP_NAME")
val keystorePath: String? = System.getenv("KEYSTORE_FILE")

android {
    namespace = "com.dgcoders.spectrereports"
    compileSdk = 36

    defaultConfig {
        applicationId = prop("APP_ID")
        minSdk = 29
        targetSdk = 36
        versionCode = prop("VERSION_CODE").toInt()
        versionName = prop("VERSION_NAME")

        buildConfigField("String", "PORTAL_URL", "\"$portalUrl\"")
        buildConfigField("String", "UA_SUFFIX", "\"SpectreApp/${prop("VERSION_NAME")}\"")
        resValue("string", "app_name", appName)
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (keystorePath != null && File(keystorePath).exists()) {
            create("release") {
                storeFile = File(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Falls back to the debug key when no release keystore is configured,
            // so the APK is still installable for testing.
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
}
