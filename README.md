# SnapSave

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="SnapSave Logo" width="128" height="128" />
</p>

<h3 align="center">Instant Code & Text Snippets to Real Files on Android</h3>

<p align="center">
  <a href="https://github.com/dev-hkm/SnapSave/releases"><img src="https://img.shields.io/github/v/release/dev-hkm/SnapSave?color=38BDF8&label=Release&style=flat-square" alt="GitHub Release" /></a>
  <a href="https://developer.android.com/about/versions/oreo"><img src="https://img.shields.io/badge/Android-8.0%2B%20(API%2026--35)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android minSdk 26" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.0.21" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://m3.material.io/"><img src="https://img.shields.io/badge/Material%20Design-3%20Dynamic%20Color-blueviolet?style=flat-square" alt="Material 3" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" alt="License" /></a>
</p>

---

## ⚡ The Problem & The Solution

**The Pain Point:**  
On Android, copying very large text blocks (several hundred lines of code, logs, HTML documents, or JSON payloads) frequently gets truncated or laggy due to clipboard transaction limits. Moreover, pasting long code into mobile text editors requires tedious multi-step app switching.

**The SnapSave Solution:**  
SnapSave **bypasses the clipboard entirely**. By integrating with Android's native text selection toolbar (`ACTION_PROCESS_TEXT`) and share menu (`ACTION_SEND`), SnapSave captures selected text directly from the source app without touching clipboard memory. In a single tap, it identifies the language, formats the filename, and saves it as a real physical file on your device.

---

## ✨ Features

- 🚀 **Zero-Clipboard Selection Capture (`ACTION_PROCESS_TEXT`)**: Highlight text in any application (browser, chat, PDF reader, notes) &rarr; tap **SnapSave** in the floating toolbar &rarr; instant 1-tap save.
- 📤 **System Share Receiver (`ACTION_SEND`)**: Receive code and text directly from any application's share sheet.
- 🧠 **Smart Syntax Detection**: Automatically detects 20+ file formats and programming languages, including Kotlin, Java, Python, TypeScript, JavaScript, HTML, CSS, JSON, SQL, Rust, Go, C/C++, Shell, Markdown, and YAML.
- 🏷️ **Official VS Code Material Icons**: High-fidelity language badges and file type indicators powered by official Material Icon Theme artwork.
- 💾 **Dual Storage Engine**:
  - **App Sandbox**: Fast, isolated storage tracked with **Room Database**.
  - **Device Storage**: Directly write to `/Download/SnapSave` or any custom folder via Storage Access Framework (SAF).
  - Configurable default save target in **Settings** (App only, Device, or Both).
- 🔗 **Forward to System App Chooser**: Immediately open newly saved snippets in your preferred code editors, IDEs, or viewers with the native Android app chooser.
- 🎨 **Material Design 3 & Material You**:
  - Full Dynamic Color support on Android 12+ (accented tonal palettes matching user wallpaper).
  - Specially crafted **Dynamic Themed Icon** with crisp layered contours and grounding drop shadow.
  - Edge-to-edge transparent system bars and fluid spring physics animations.
- 📳 **Refined Haptics**: Subtle tactile clicks on chip selection and confirmation haptic feedback on successful save.
- 🌐 **Full Localization**: English (default) and Tiếng Việt with instant in-app switching.

---

## 🏗️ Architecture & Tech Stack

SnapSave adheres to modern Android app architecture standards, prioritizing clean separation of concerns, unidirectional data flow, and testability.

| Layer | Technologies / Patterns |
|---|---|
| **UI Layer** | Jetpack Compose (BOM `2024.12.01`), Material 3, Single-Activity Navigation |
| **Architecture** | MVVM + Repository Pattern + Clean Architecture |
| **Concurrency** | Kotlin Coroutines & Asynchronous `StateFlow` / `SharedFlow` |
| **Database** | Room 2.6.1 with KSP (Kotlin Symbol Processing) |
| **Preferences** | Jetpack DataStore Preferences |
| **Inter-App Sharing**| Android `FileProvider` with scoped URI permissions & `MediaStore` / SAF |
| **Language** | Kotlin 2.0.21 (JVM 17) |
| **Build Tools** | Android Gradle Plugin 8.7.3, Gradle 8.9, Target SDK 35 |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or Panda
- **JDK 17** (Temurin, Azul Zulu, or Android Studio bundled JBR)
- **Android SDK** API 35 (minSdk 26 - Android 8.0 Oreo)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/dev-hkm/SnapSave.git
   cd SnapSave
   ```

2. Assemble the debug build:
   ```bash
   ./gradlew assembleDebug
   ```

3. Assemble the release build:
   ```bash
   ./gradlew assembleRelease
   ```
   The compiled APK will be located at `app/build/outputs/apk/release/`.

---

## 📦 Downloads

Check out the [Releases](https://github.com/dev-hkm/SnapSave/releases) page for the latest signed APKs and detailed changelogs.

---

[khanhminh.web.app](https://khanhminh.web.app)