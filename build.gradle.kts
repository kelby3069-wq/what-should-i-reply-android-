plugins {
    // AGP compatible with Gradle 8.13
    id("com.android.application") version "8.5.2" apply false

    // Kotlin 2.x
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // ✅ REQUIRED for Compose when using Kotlin 2.0+
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
