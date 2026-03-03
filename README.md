# 🇯🇵 JapanLib

**JapanLib** is a fully functional Android application for Japanese language learning, built entirely
with Kotlin and Jetpack Compose. It follows Android design and development best practices — including
[Clean Architecture](https://developer.android.com/topic/architecture), MVVM, and modularization —
and is intended to be a useful reference for building modern, offline-first Android apps with
on-device ML capabilities.

## Features

JapanLib provides a comprehensive set of tools for learners at every JLPT level:

- 📖 **Dictionary** — Search words by kanji, hiragana, katakana, or romaji with Full-Text Search
  (FTS) powered by a local SQLite database ([JMDict](https://www.edrdg.org/wiki/index.php/JMDict-EDICT_Dictionary_Project)).
- 🎓 **JLPT Test Preparation** — Practice tests for all levels (N5 → N1) fetched via REST API,
  with flashcards, spaced repetition, and detailed result review.
- ✍️ **Handwritten Kanji Recognition** — Draw kanji on-screen and get instant classification using
  a TensorFlow Lite model trained on the [ETL8 dataset](http://etlcdb.db.aist.go.jp/).
- 📷 **OCR Text Recognition** — Recognize Japanese text from images using Google ML Kit.
- 🔊 **Audio & Voice** — Text-to-speech pronunciation and speech-to-text input via ExoPlayer and
  Android Speech API.
- 🌐 **Translation** — Real-time Japanese ↔ multilingual translation via Google Translate API.
- 📚 **Vocabulary Management** — Create custom word lists, browse thematic collections, and organize
  by JLPT level.
- ☁️ **Cloud Sync** — Firebase Authentication (Google Sign-In) with Firestore for syncing
  vocabulary, test results, and learning progress across devices.
- 🌏 **Localization** — Multilingual UI support (English, Vietnamese, Japanese) with per-app
  language preferences.

## Architecture

JapanLib follows the
and is structured around **Clean Architecture** with the **MVVM** pattern.

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer (app)                │
│  Jetpack Compose UI  ·  ViewModels  ·  Navigation   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│               Domain Layer (domain)                  │
│  Use Cases  ·  Repository Interfaces     │
│  Domain Models  ·  Pure Kotlin — no Android deps     │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                Data Layer (data)                     │
│  Repository Implementations (15)  ·  Room DAOs (6)   │
│  Retrofit APIs  ·  Firebase  ·  DataStore            │
│  TensorFlow Lite  ·  Kuromoji Tokenizer              │
└─────────────────────────────────────────────────────┘
```

## Modularization

The app is split into three Gradle modules following the

| Module    | Responsibility                                    | Dependencies          |
|-----------|---------------------------------------------------|-----------------------|
| **`:app`**    | UI (Compose screens, ViewModels, Navigation, DI setup) | `:domain`, `:data` |
| **`:domain`** | Business logic (Use Cases, Repository interfaces, Models) | None (pure Kotlin) |
| **`:data`**   | Data access (Room, Retrofit, Firebase, ML, Mappers)     | `:domain`            |

## Tech Stack

JapanLib leverages a modern Android tech stack:

| Category              | Libraries & Tools                                                    |
|-----------------------|----------------------------------------------------------------------|
| **Language**          | Kotlin                                                               |
| **UI**                | Jetpack Compose, Material 3, Google Fonts      |
| **Architecture**      | MVVM, Clean Architecture, Hilt (DI), Navigation Compose              |
| **Local Storage**     | Room (with FTS), DataStore Preferences                               |
| **Networking**        | Retrofit, OkHttp, Moshi, Gson                                       |
| **Backend**           | Firebase Auth, Cloud Firestore, Firebase Analytics                   |
| **AI / ML**           | TensorFlow Lite (kanji classification), ML Kit (Japanese OCR)        |
| **NLP**               | Kuromoji (Japanese text tokenization)                                |
| **Media**             | ExoPlayer (Media3)                                                   |
| **Image Loading**     | Glide Compose                                                        |
| **Background Work**   | WorkManager, Hilt WorkManager integration                            |
| **Build**             | Gradle (Kotlin DSL), KSP    |
| **Logging**           | Timber                                                               |

## Development Environment

JapanLib uses the Gradle build system and can be imported directly into
[Android Studio](https://developer.android.com/studio) (latest stable version recommended).

### Requirements

- **Min SDK**: 24 (Android 7.0)
- **Target / Compile SDK**: 35
- **JDK**: 11+

### API Keys

The app requires the following keys configured in `local.properties`:

```properties
# Google Translate API
TRANSLATE_API_KEY=<your_key>

# JLPT Test API base URL
EXAM_BASE_URL=<your_url>
```

## Database

JapanLib uses a pre-packaged Room database sourced from the
[JMDict project](https://www.edrdg.org/wiki/index.php/JMDict-EDICT_Dictionary_Project),
containing **9 core entities** and **2 FTS virtual tables** for fast full-text search.

Key entities include `JapaneseWordEntity`, `JlptWordEntity`, `KanjiDetailEntity`,
`CustomWordListEntity`, and `EntryTranslationEntity` — connected through cross-reference tables
for many-to-many relationships.

## Build

The app contains the usual `debug` and `release` build variants.

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

## License

```
Copyright 2024 JapanLib

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
