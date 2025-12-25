plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // ✅ Required for Kotlin 2.0+ when Compose is enabled
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
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

    // ❌ REMOVE composeOptions.kotlinCompilerExtensionVersion for Kotlin 2.0+
    // composeOptions { ... }  <-- intentionally not used

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    /* ---------------- Core Android ---------------- */
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")

    /* ---------------- Compose BOM ---------------- */
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    /* ---------------- Material 3 ---------------- */
    implementation("androidx.compose.material3:material3")

    /* ✅ Icons (ArrowBack, Icons.Default.*) */
    implementation("androidx.compose.material:material-icons-extended")

    /* ---------------- Tooling ---------------- */
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    /* ---------------- Testing ---------------- */
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
