# AutoTrad 🌍

**Runtime automatic translation for Android without `strings.xml`**

[![Build Status](https://github.com/robsonjso/autotrad/workflows/CI/badge.svg)](https://github.com/robsonjso/autotrad/actions)
[![JitPack](https://jitpack.io/v/robsonjso/autotrad.svg)](https://jitpack.io/#robsonjso/autotrad)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[🇺🇸 English](README_EN.md) | [🇪🇸 Español](README_ES.md) | [🇧🇷 Português](README.md)

AutoTrad is an Android library that enables **real-time automatic translation** without the need for `strings.xml` files. Use `AutoText("any text")` and the library automatically translates using ML Kit on-device.

## ✨ Features

- 🚀 **Zero Setup** - Works out-of-the-box
- 🤖 **ML Kit On-Device** - Offline translation after initial model download
- 🎯 **Automatic Detection** - Identifies source language automatically
- 📱 **Compose First** - Native integration with Jetpack Compose
- 🔄 **Runtime Switching** - Real-time language switching
- 🛡️ **Quality Gate** - Preserves placeholders and applies role-based limits
- 📊 **Translation Memory** - Intelligent cache for performance
- 🌍 **RTL Support** - Automatic support for RTL languages
- ♿ **Accessibility** - Translation of contentDescription
- 📈 **Telemetry** - Optional metrics without PII
- 🛠️ **Dev Tools** - Overlay editor and pending export

## 🚀 Quickstart

### 1. Dependencies

#### settings.gradle.kts (in consumer project)
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

#### build.gradle.kts
```kotlin
dependencies {
    implementation("com.github.robsonjso:autotrad-core:0.1.0")
    implementation("com.github.robsonjso:autotrad-compose:0.1.0")
}
```

### ProGuard / R8 (optional)
If you obfuscate your app, include:
```pro
-keep class io.autotrad.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
```

### 2. Initialization

```kotlin
// Application or MainActivity
AutoTrad.init(
    context = this,
    translators = listOf(
        DetectingTranslator(
            detector = MlKitLanguageDetector(),
            delegate = MlKitTranslator(),
            fallbackSource = Locale("pt", "BR")
        )
    ),
    devCaptureEnabled = BuildConfig.DEBUG
)
```

### 3. Usage in UI

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Automatic translation
        AutoText("Hello, world!") // → "Hola, mundo!" (in Spanish)
        
        // With placeholders
        AutoText("Welcome, {name}!", args = mapOf("name" to "John"))
        
        // With role (character limit)
        Button(onClick = {}) {
            AutoText("Continue", role = TextRole.Button) // max 16 chars
        }
        
        // Language selector
        Button(onClick = { AutoTrad.setLocale("es") }) {
            AutoText("Spanish")
        }
    }
}
```

## 📚 Complete Guide

### 🔧 Advanced Configuration

#### LocaleManager with Persistence

```kotlin
val localeManager = LocaleManager(
    context = this,
    policy = LocalePolicy(
        mode = LocaleMode.HYBRID, // follows system until user chooses
        supported = listOf("en", "es", "pt-BR"),
        fallbackChain = listOf("en")
    )
)

// Automatic persistence
localeManager.setUserLanguage("en") // saves to DataStore
```

#### Glossary and Do-Not-Translate

```kotlin
GlossaryTranslator(
    map = mapOf(
        "Login" to "Enter",
        "Home" to "Home"
    ),
    dontTranslate = setOf("AutoTrad", "ID", "v1.0"),
    delegate = DetectingTranslator(...)
)
```

#### Pre-warming for Performance

```kotlin
lifecycleScope.launch {
    // Download ML Kit models
    MlKitTranslator().preDownloadLanguages(
        Locale("pt", "BR"), 
        Locale.ENGLISH, 
        Locale("es")
    )
    
    // Pre-translate critical texts
    AutoTradPrewarm.prewarmCriticalUI(Locale.ENGLISH)
}
```

### 🎨 UI Components

#### AutoTradLayout (Automatic RTL)

```kotlin
AutoTradLayout { // applies LayoutDirection.Rtl for Arabic, Hebrew, etc.
    DemoScreen()
}
```

#### Accessibility

```kotlin
Button(
    modifier = Modifier.autoContentDescription("Example button")
) {
    AutoText("A11y Example", role = TextRole.Button)
}
```

### 🛡️ Quality Gate

#### TextRole for Character Limits

```kotlin
enum class TextRole {
    Button,    // max 16 chars
    Chip,      // max 12 chars
    Title,     // max 48 chars
    Caption,   // max 24 chars
    Error      // max 32 chars
}
```

#### Automatic Blacklist

QualityGate automatically blocks translation of:
- 📧 **Emails** - `user@example.com`
- 🔢 **Codes** - `ABC123`, `ID456`
- 🔢 **Long numbers** - `1234567890`
- 📝 **Placeholders** - `{name}`, `{count}`

### 📊 Telemetry

```kotlin
AutoTradTelemetry.register(object : TelemetrySink {
    override fun onTmHit() {
        println("Translation Memory hit")
    }
    override fun onMtCall(durationMs: Long) {
        println("ML Kit call took ${durationMs}ms")
    }
    override fun onQualityReject() {
        println("Quality gate rejected translation")
    }
})

// Real-time metrics
val metrics = AutoTradTelemetry.snapshot()
```

### 🛠️ Dev Tools

#### Dev Overlay (Debug)

Long-press any `AutoText` to edit translations in real-time.

#### Pending Export

```kotlin
@Composable
fun ExportPendingButton() {
    Button(onClick = {
        val zip = AutoTradPending.zipPending(context)
        // Opens share sheet (email, Drive, etc.)
    }) {
        AutoText("Export pending", role = TextRole.Button)
    }
}
```

#### Gradle Task for Merge

```bash
./gradlew :app:mergeAutoTradPending
```

Merges `pending/autotrad.pending.*.json` files into `src/main/assets/autotrad/`.

### 📁 Assets (Optional)

Create JSON files in `src/main/assets/autotrad/`:

```json
// autotrad.en.json
{
  "Enter": "Enter",
  "Exit": "Exit",
  "Welcome, {name}!": "Welcome, {name}!"
}
```

## 🏗️ Architecture

### Modules

- **`autotrad-core`** - Main engine, ML Kit, Quality Gate
- **`autotrad-compose`** - UI components for Compose
- **`autotrad-sample`** - Complete demonstration app

### Translation Flow

```
1. AutoText("new text")
2. Translation Memory (cache)
3. Assets JSON (if available)
4. Glossary (fixed terms)
5. ML Kit Language Detection
6. ML Kit Translation
7. Quality Gate validation
8. Saves to TM + pending.json
9. Displays translation
```

### Translation Providers

- **`MlKitTranslator`** - Google ML Kit on-device
- **`DetectingTranslator`** - Automatic detection + delegate
- **`GlossaryTranslator`** - Glossary + do-not-translate
- **`EchoTranslator`** - For testing (returns original)

## 📱 Sample App

The demonstration app includes:

- ✅ Real-time automatic translation
- ✅ Language selector with persistence
- ✅ Placeholder examples
- ✅ Quality Gate by role
- ✅ Automatic RTL
- ✅ Accessibility
- ✅ Dev Overlay
- ✅ Pending export
- ✅ Telemetry

## 🔧 Advanced Configuration

### LocalePolicy Modes

```kotlin
enum class LocaleMode {
    FOLLOW_SYSTEM,     // Always follows system
    USER_SELECTED,     // Always uses user selection
    AUTO_BY_LOCATION,  // Based on geolocation
    HYBRID            // Follows system until user chooses
}
```

### Custom Translators

```kotlin
class CustomTranslator : Translator {
    override suspend fun translate(
        text: String, 
        src: Locale?, 
        tgt: Locale
    ): String? {
        // Your translation logic
        return translatedText
    }
}
```

## 🚀 Performance

### Included Optimizations

- **Translation Memory** - In-memory cache
- **Pre-warming** - Anticipated model download
- **Lazy Loading** - ML Kit models on demand
- **Quality Gate** - Avoids unnecessary translations
- **RTL Detection** - Optimized for RTL languages

### Benchmarks (typical values)

- **First translation**: 300–800 ms (includes model download, cached afterwards)
- **Subsequent translations**: ~30–80 ms (cache + on-device)
- **TM hits** (memory): ~1–5 ms
- **APK size**: no relevant impact from models
- **Device storage**: +~20–40 MB per downloaded language (once)

## ✅ Compatibility
- **Android**: minSdk 21+
- **Compose**: 1.6.x+
- **Kotlin**: 1.9+ (tested with plugin 2.0.x)
- **ML Kit Translate**: models downloaded on-demand
- **Architecture**: works with standard MVVM/Compose

## ⚠️ Known Limitations
- **Business terms**: MT may vary ("Sign in" vs "Enter"). Use **GlossaryTranslator** or JSON packs.
- **Complex plurals**: Advanced ICU (plural/gender) is on the roadmap.
- **Rare/RTL languages**: test visuals; use `AutoTradLayout` for direction.
- **First use**: may download models; use **pre-warming** to avoid initial lag.

## 🧰 Troubleshooting
- **Nothing translates**: are you using `EchoTranslator`? Switch to `DetectingTranslator + MlKitTranslator`.
- **Slow first time**: do `preDownloadLanguages(...)` on boot and `prewarm(...)` critical texts.
- **Translation broke layout**: mark `role = TextRole.Button/Chip` to apply Quality Gate limits.
- **Wrong translation**: edit via **Dev Overlay** (long-press) or fix in JSON pack/glossary.
- **No internet on first use**: ensure language model was downloaded before (or enable `requireWifiForDownload=false`).

## 🔒 Security

- ✅ **On-Device** - No data sent to external servers
- ✅ **No PII** - Telemetry doesn't collect personal data
- ✅ **Sandbox** - Pending files isolated in app
- ✅ **FileProvider** - Secure sharing

## 📄 License

```
Copyright 2025 Robson Josue (robsonjso)

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

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Local Development

```bash
git clone https://github.com/robsonjso/autotrad.git
cd autotrad
./gradlew build
```

## 📞 Support

- 📧 **Issues**: [GitHub Issues](https://github.com/robsonjso/autotrad/issues)
- 📖 **Documentation**: [Wiki](https://github.com/robsonjso/autotrad/wiki)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/robsonjso/autotrad/discussions)

## 🎯 Roadmap

### v0.2.0 (Next)
- [ ] iOS Support (SwiftUI)
- [ ] Web Support (Compose for Web)
- [ ] DeepL Integration
- [ ] OpenAI Integration
- [ ] Batch Translation
- [ ] Translation Analytics

### v1.0.0 (Future)
- [ ] Enterprise Features
- [ ] Team Collaboration
- [ ] Translation Memory Sync
- [ ] Advanced Quality Gates
- [ ] Custom ML Models

---

**AutoTrad** - Revolutionizing Android internationalization! 🌍✨

*Developed with ❤️ by the AutoTrad community*
