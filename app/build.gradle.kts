plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // ✅ Required for Kotlin 2.0+ when Compose is enabled
    id("org.jetbrains.kotlin.plugin.compose")
    // ❌ REMOVE serialization plugin (was failing: "plugin ...serialization not found")
    // id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.replysense.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.replysense.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Compose BOM (keeps versions aligned) ---
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // --- Compose UI ---
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Material 3 (Compose) ---
    implementation("androidx.compose.material3:material3")
    // ✅ This is the one that provides the XML theme resources if you reference Theme.Material3.*
    implementation("androidx.compose.material3:material3-android")

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // --- ML Kit OCR (keep whatever you already had; these are standard) ---
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
