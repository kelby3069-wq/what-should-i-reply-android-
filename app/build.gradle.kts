plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Kotlin 2.x + Compose requirement
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"

    // kotlinx.serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"

    // Room compiler (KSP)
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
}

android {
    namespace = "com.replysense.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.replysense.app"
        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        // CI-safe defaults (NO secrets committed)
        buildConfigField("String", "API_BASE_URL", "\"https://example.com\"")
        buildConfigField("String", "API_KEY", "\"\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes.addAll(listOf("/META-INF/{AL2.0,LGPL2.1}"))
    }
}

dependencies {
    // Needed for Theme.Material3.* (XML theme attrs like colorSurface)
    implementation("com.google.android.material:material:1.12.0")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")

    // Networking + JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Room (History)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
