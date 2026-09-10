# SnapSave ⚡

> Save code snippets and text directly to real files on Android — without clipboard limits.

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="SnapSave Logo" width="112" height="112" />
</p>

<p align="center">
  <a href="https://github.com/dev-hkm/SnapSave/releases"><img src="https://img.shields.io/github/v/release/dev-hkm/SnapSave?color=38BDF8&label=Download%20APK&style=flat-square" alt="Download APK" /></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin & Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material%20You-Dynamic%20Color-blueviolet?style=flat-square" alt="Material You" />
</p>

---

## Why I Built This

I read and write a lot of code on my phone — chatting with AI (ChatGPT, Claude), reading GitHub issues, and browsing Stack Overflow.

Whenever I wanted to save a long snippet or an entire HTML/Python script, I kept hitting the same two annoyances:
1. **The Android clipboard limit**: When a snippet is several hundred lines long, the system clipboard often drops characters, chokes, or strips formatting.
2. **Too much jumping around**: Copy text &rarr; leave browser &rarr; open a file manager or text editor &rarr; create file &rarr; name it &rarr; paste. It gets tiring very fast.

So I built **SnapSave** to skip the clipboard entirely and make saving files a 1-second action.

---

## How It Works

Instead of copying to clipboard, SnapSave hooks directly into Android's native text selection system:

1. **Highlight text anywhere** — In Chrome, Discord, notes, or any app.
2. **Tap "SnapSave"** in the floating menu (next to Copy / Share).
3. **SnapSave pops up a small bottom sheet**:
   - It automatically detects the language (Kotlin, Python, HTML, JSON, SQL, etc.).
   - Suggests a clean filename with the right extension.
   - Saves directly to your phone storage (`/Download/SnapSave` or internal app storage).
4. **Open immediately**: Tap "Open with..." to launch the file right away in your favorite code editor, or let it auto-dismiss.

You can also send text through the normal Android **Share** menu (`ACTION_SEND`).

---

## What's Inside

- **Zero-clipboard capture**: Uses `ACTION_PROCESS_TEXT` so huge text blocks are passed directly via Intent, completely intact.
- **Auto syntax detection**: Guesses between 20+ file formats (JSON parser test, HTML tag checks, code keyword heuristics).
- **Official VS Code Material Icons**: Real file type icons from Material Icon Theme, not generic placeholders.
- **Dual storage**: Save internally (managed with Room Database) or directly to your device storage via MediaStore / Storage Access Framework. You can set your default preference in Settings so you don't have to pick every time.
- **Material You Dynamic Color**: The app theme and launcher icon adapt to your wallpaper palette on Android 12+.
- **Haptic feedback**: Nice tactile clicks when picking chips and a solid confirm buzz when a file is safely written.
- **Lightweight & fast**: No heavy third-party SDKs, no ads, no trackers. Starts instantly.
- **Bilingual**: English and Tiếng Việt (switchable in Settings).

---

## Tech Stack

Built simply and cleanly with modern Android libraries:

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3 (Edge-to-Edge, dynamic theming)
- **Database**: Room (KSP) + Kotlin Coroutines / Flow
- **Settings**: Jetpack DataStore Preferences
- **System**: `ACTION_PROCESS_TEXT`, `ACTION_SEND`, `FileProvider`, `MediaStore`
- **DI**: Manual container (AppContainer) to keep compile times fast and the APK small (~14MB)

---

## Download & Install

Grab the signed release APK from the [Releases](https://github.com/dev-hkm/SnapSave/releases) page:

👉 **[Download Latest Release (v1.0.10)](https://github.com/dev-hkm/SnapSave/releases/tag/v1.0.10)**

Works on Android 8.0 (API 26) up to Android 15 (API 35).

---

## Building From Source

If you want to poke around the code or build it yourself:

```bash
git clone https://github.com/dev-hkm/SnapSave.git
cd SnapSave
./gradlew assembleRelease
```

Open in Android Studio (Ladybug / Panda) with JDK 17.

---

[khanhminh.web.app](https://khanhminh.web.app)