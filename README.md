# 🤖 ReplySense
**ReplySense** is an intelligent AI-powered assistant that helps users craft perfect text and message replies in seconds. Built with **Jetpack Compose**, **Kotlin 2.0**, and **Material 3**, it combines elegant UX, clean architecture, and real-time AI inference for a fast, intuitive experience.
---
## ✨ Features
- 🧠 **Smart Reply Generation** – Context-aware responses powered by OpenAI integration
- 🖼️ **Screenshot-to-Reply** – Upload or crop a screenshot and auto-extract text using ML Kit OCR
- 🎙️ **Voice Input & TTS** – Speak your prompt or have the AI read replies aloud
- ⚡ **Instant Preview** – Compare AI suggestions vs. your own drafted reply
- 🌓 **Dynamic Material 3 Theme** – Light/Dark adaptive UI
- 💾 **Offline Mode (DataStore)** – Saves recent replies, history, and preferences
- 🔄 **Continuous Improvement** – App learns from selections to improve tone, accuracy, and empathy
---
## 🧩 Architecture
**Clean MVVM + Repository Pattern**
ui/
├── composables/
├── screens/
├── components/
data/
├── repository/
├── model/
domain/
├── usecase/
viewmodel/
├── ReplyViewModel.kt
└── SettingsViewModel.kt
**Tech Stack:**
- **UI:** Jetpack Compose (Material 3, Navigation, Accompanist)
- **Logic:** Kotlin Coroutines + Flow + ViewModel
- **Storage:** DataStore Preferences
- **Network:** Retrofit + Moshi
- **ML/OCR:** Google ML Kit
- **Voice:** Text-to-Speech + SpeechRecognizer
- **Image Loading:** Coil
- **Dependency Injection:** (Optional) Hilt / Koin ready
---
## ⚙️ Build Configuration
| Component | Version |
|------------|----------|
| **Android Studio** | Ladybug |
| **Kotlin** | 2.0.21 |
| **Compose Compiler** | 2.0.21 |
| **AGP** | 8.1.3 |
| **Gradle** | 8.13 |
| **Material3** | 1.3.1 |
| **Compile SDK** | 34 |
| **JVM Toolchain** | Java 17 |
Default JVM Args:  
-Xmx4096m -Dfile.encoding=UTF-8
**Required Plugins:**
```kotlin
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.plugin.compose")
id("com.google.devtools.ksp")
🧠 Core Principles

Speed: Minimal latency from input to suggestion

Clarity: Every UI element must serve purpose

Privacy: All screenshot parsing happens locally

Consistency: Material 3 first, clean hierarchy, accessible contrast

Stability: Compile cleanly on first sync — always
🧩 Development Workflow

Clone repo

git clone https://github.com/<yourname>/ReplySense.git
cd ReplySense


Open in Android Studio Ladybug

Sync Gradle

Build & Run

./gradlew assembleDebug


Test

./gradlew testDebugUnitTest
🎨 Design Philosophy

ReplySense’s UI follows premium design heuristics:

Intentional whitespace and rhythm

Pixel-perfect 8dp layout grid

Typography as hierarchy

Color communicates state, not decoration

Feels modern, calm, and confident

🛠️ Future Roadmap

🔄 Multi-turn context replies

🌐 Inline translation + tone adjustment

📱 Share Extension support

🧩 Cloud sync of preferences

🗣️ Multi-voice response options

🧾 License
© 2026 K.R. Becker — All Rights Reserved.
Codebase proprietary and protected under applicable copyright law.
🧰 For Internal Developers

Full engineering, design, and build policies are detailed in:

PROJECT_INSTRUCTIONS.md