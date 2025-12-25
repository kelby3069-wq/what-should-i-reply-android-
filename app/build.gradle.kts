plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // ✅ REQUIRED (Kotlin 2.0+ + Compose)
    id("org.jetbrains.kotlin.plugin.compose")
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // With Kotlin 2.x + org.jetbrains.kotlin.plugin.compose:
    // DO NOT set kotlinCompilerExtensionVersion here.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material 3
    implementation("androidx.compose.material3:material3")

    // Icons (your earlier request)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // JSON (works without the serialization plugin unless you use @Serializable)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
