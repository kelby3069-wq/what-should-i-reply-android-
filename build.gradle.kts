plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false

    // Match whatever Kotlin you're already using in this repo (likely 2.0+)
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // IMPORTANT: make the compose plugin available to modules
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
