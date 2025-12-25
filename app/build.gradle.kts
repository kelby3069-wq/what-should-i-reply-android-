plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.replysense.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.replysense.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    // --- Core Android ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // --- Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Material 3 (Compose) ---
    implementation("androidx.compose.material3:material3")

    // --- Material XML themes (REQUIRED for Theme.Material3.*) ---
    implementation("com.google.android.material:material:1.12.0")

    // --- Icons ---
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // --- ML Kit OCR ---
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // --- JSON ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
