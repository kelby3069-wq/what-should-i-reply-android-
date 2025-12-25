plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") // ✅ REQUIRED
}

android {
    namespace = "com.replysense.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.replysense.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true // ✅ FIX BuildConfig unresolved
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    /* =======================
       Compose Core
       ======================= */
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")

    /* =======================
       Material 3 (Compose)
       ======================= */
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-android")

    /* Icons */
    implementation("androidx.compose.material:material-icons-extended")

    /* Tooling */
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    /* =======================
       Kotlin Serialization
       ======================= */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    /* =======================
       ML Kit – Text Recognition
       ======================= */
    implementation("com.google.mlkit:text-recognition:16.0.0")

    /* =======================
       Image / Bitmap
       ======================= */
    implementation("androidx.core:core-ktx:1.13.1")
}
