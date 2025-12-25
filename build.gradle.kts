plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // ✅ Required for Kotlin 2.x + Compose
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
