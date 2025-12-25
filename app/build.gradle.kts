plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // ✅ REQUIRED for Kotlin 2.x + Compose
    id("org.jetbrains.kotlin.plugin.compose")

    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
}

android {
    namespace = "com.replysense.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.replysense.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    buildFeatures {
        compose = true
    }

    // ❌ REMOVE composeOptions for Kotlin 2.x Compose compiler (plugin handles it)
    // composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Room (History + Favorites)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
