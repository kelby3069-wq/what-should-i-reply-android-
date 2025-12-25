plugins {
    // Android
    id("com.android.application") version "8.7.2" apply false

    // Kotlin
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // Kotlin 2 + Compose compiler plugin (required when compose is enabled)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false

    // Kotlinx Serialization plugin (must have a version somewhere)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false

    // KSP for Room compiler
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
}
