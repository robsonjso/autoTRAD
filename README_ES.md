# AutoTrad 🌍

**Traducción automática en tiempo de ejecución para Android sin `strings.xml`**

[![Build Status](https://github.com/robsonjso/autotrad/workflows/CI/badge.svg)](https://github.com/robsonjso/autotrad/actions)
[![JitPack](https://jitpack.io/v/robsonjso/autotrad.svg)](https://jitpack.io/#robsonjso/autotrad)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[🇺🇸 English](README_EN.md) | [🇪🇸 Español](README_ES.md) | [🇧🇷 Português](README.md)

AutoTrad es una biblioteca Android que permite **traducción automática en tiempo real** sin necesidad de archivos `strings.xml`. Usa `AutoText("cualquier texto")` y la biblioteca traduce automáticamente usando ML Kit on-device.

## ✨ Características

- 🚀 **Configuración Cero** - Funciona out-of-the-box
- 🤖 **ML Kit On-Device** - Traducción offline después de la descarga inicial del modelo
- 🎯 **Detección Automática** - Identifica el idioma de origen automáticamente
- 📱 **Compose First** - Integración nativa con Jetpack Compose
- 🔄 **Cambio en Runtime** - Cambio de idioma en tiempo real
- 🛡️ **Quality Gate** - Preserva placeholders y aplica límites por rol
- 📊 **Memoria de Traducción** - Cache inteligente para rendimiento
- 🌍 **Soporte RTL** - Soporte automático para idiomas RTL
- ♿ **Accesibilidad** - Traducción de contentDescription
- 📈 **Telemetría** - Métricas opcionales sin PII
- 🛠️ **Herramientas de Desarrollo** - Editor overlay y exportación de pendientes

## 🚀 Inicio Rápido

### 1. Dependencias

#### settings.gradle.kts (en el proyecto consumidor)
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

### ProGuard / R8 (opcional)
Si ofuscas tu app, incluye:
```pro
-keep class io.autotrad.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
```

### 2. Inicialización

```kotlin
// Application o MainActivity
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

### 3. Uso en la UI

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Traducción automática
        AutoText("¡Hola, mundo!") // → "Hello, world!" (en inglés)
        
        // Con placeholders
        AutoText("¡Bienvenido, {name}!", args = mapOf("name" to "Juan"))
        
        // Con rol (límite de caracteres)
        Button(onClick = {}) {
            AutoText("Continuar", role = TextRole.Button) // máx 16 chars
        }
        
        // Selector de idioma
        Button(onClick = { AutoTrad.setLocale("en") }) {
            AutoText("Inglés")
        }
    }
}
```

## 📚 Guía Completa

### 🔧 Configuración Avanzada

#### LocaleManager con Persistencia

```kotlin
val localeManager = LocaleManager(
    context = this,
    policy = LocalePolicy(
        mode = LocaleMode.HYBRID, // sigue el sistema hasta que el usuario elija
        supported = listOf("en", "es", "pt-BR"),
        fallbackChain = listOf("en")
    )
)

// Persistencia automática
localeManager.setUserLanguage("en") // guarda en DataStore
```

#### Glosario y No-Traducir

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

#### Pre-calentamiento para Rendimiento

```kotlin
lifecycleScope.launch {
    // Descarga modelos ML Kit
    MlKitTranslator().preDownloadLanguages(
        Locale("pt", "BR"), 
        Locale.ENGLISH, 
        Locale("es")
    )
    
    // Pre-traduce textos críticos
    AutoTradPrewarm.prewarmCriticalUI(Locale.ENGLISH)
}
```

### 🎨 Componentes UI

#### AutoTradLayout (RTL Automático)

```kotlin
AutoTradLayout { // aplica LayoutDirection.Rtl para árabe, hebreo, etc.
    DemoScreen()
}
```

#### Accesibilidad

```kotlin
Button(
    modifier = Modifier.autoContentDescription("Botón de ejemplo")
) {
    AutoText("Ejemplo A11y", role = TextRole.Button)
}
```

### 🛡️ Quality Gate

#### TextRole para Límites de Caracteres

```kotlin
enum class TextRole {
    Button,    // máx 16 chars
    Chip,      // máx 12 chars
    Title,     // máx 48 chars
    Caption,   // máx 24 chars
    Error      // máx 32 chars
}
```

#### Blacklist Automática

QualityGate bloquea automáticamente la traducción de:
- 📧 **Emails** - `user@example.com`
- 🔢 **Códigos** - `ABC123`, `ID456`
- 🔢 **Números largos** - `1234567890`
- 📝 **Placeholders** - `{name}`, `{count}`

### 📊 Telemetría

```kotlin
AutoTradTelemetry.register(object : TelemetrySink {
    override fun onTmHit() {
        println("Hit de Memoria de Traducción")
    }
    override fun onMtCall(durationMs: Long) {
        println("Llamada ML Kit tomó ${durationMs}ms")
    }
    override fun onQualityReject() {
        println("Quality gate rechazó la traducción")
    }
})

// Métricas en tiempo real
val metrics = AutoTradTelemetry.snapshot()
```

### 🛠️ Herramientas de Desarrollo

#### Dev Overlay (Debug)

Long-press en cualquier `AutoText` para editar traducciones en tiempo real.

#### Exportación de Pendientes

```kotlin
@Composable
fun ExportPendingButton() {
    Button(onClick = {
        val zip = AutoTradPending.zipPending(context)
        // Abre share sheet (email, Drive, etc.)
    }) {
        AutoText("Exportar pendientes", role = TextRole.Button)
    }
}
```

#### Gradle Task para Merge

```bash
./gradlew :app:mergeAutoTradPending
```

Fusiona archivos `pending/autotrad.pending.*.json` en `src/main/assets/autotrad/`.

### 📁 Assets (Opcional)

Crea archivos JSON en `src/main/assets/autotrad/`:

```json
// autotrad.en.json
{
  "Entrar": "Enter",
  "Salir": "Exit",
  "¡Bienvenido, {name}!": "Welcome, {name}!"
}
```

## 🏗️ Arquitectura

### Módulos

- **`autotrad-core`** - Motor principal, ML Kit, Quality Gate
- **`autotrad-compose`** - Componentes UI para Compose
- **`autotrad-sample`** - App de demostración completo

### Flujo de Traducción

```
1. AutoText("nuevo texto")
2. Memoria de Traducción (cache)
3. Assets JSON (si está disponible)
4. Glosario (términos fijos)
5. Detección de Idioma ML Kit
6. Traducción ML Kit
7. Validación Quality Gate
8. Guarda en TM + pending.json
9. Muestra traducción
```

### Proveedores de Traducción

- **`MlKitTranslator`** - Google ML Kit on-device
- **`DetectingTranslator`** - Detección automática + delegate
- **`GlossaryTranslator`** - Glosario + no-traducir
- **`EchoTranslator`** - Para pruebas (retorna original)

## 📱 App de Muestra

La app de demostración incluye:

- ✅ Traducción automática en tiempo real
- ✅ Selector de idiomas con persistencia
- ✅ Ejemplos de placeholders
- ✅ Quality Gate por rol
- ✅ RTL automático
- ✅ Accesibilidad
- ✅ Dev Overlay
- ✅ Exportación de pendientes
- ✅ Telemetría

## 🔧 Configuración Avanzada

### Modos LocalePolicy

```kotlin
enum class LocaleMode {
    FOLLOW_SYSTEM,     // Siempre sigue el sistema
    USER_SELECTED,     // Siempre usa la selección del usuario
    AUTO_BY_LOCATION,  // Basado en geolocalización
    HYBRID            // Sigue el sistema hasta que el usuario elija
}
```

### Traductores Personalizados

```kotlin
class CustomTranslator : Translator {
    override suspend fun translate(
        text: String, 
        src: Locale?, 
        tgt: Locale
    ): String? {
        // Tu lógica de traducción
        return translatedText
    }
}
```

## 🚀 Rendimiento

### Optimizaciones Incluidas

- **Memoria de Traducción** - Cache en memoria
- **Pre-calentamiento** - Descarga anticipada de modelos
- **Lazy Loading** - Modelos ML Kit bajo demanda
- **Quality Gate** - Evita traducciones innecesarias
- **Detección RTL** - Optimizado para idiomas RTL

### Benchmarks (valores típicos)

- **Primera traducción**: 300–800 ms (incluye descarga del modelo, cacheado después)
- **Traducciones subsecuentes**: ~30–80 ms (cache + on-device)
- **TM hits** (memoria): ~1–5 ms
- **Tamaño del APK**: sin impacto relevante por los modelos
- **Almacenamiento en el dispositivo**: +~20–40 MB por idioma descargado (una vez)

## ✅ Compatibilidad
- **Android**: minSdk 21+
- **Compose**: 1.6.x+
- **Kotlin**: 1.9+ (probado con plugin 2.0.x)
- **ML Kit Translate**: modelos descargados bajo demanda
- **Arquitectura**: funciona con MVVM/Compose estándar

## ⚠️ Limitaciones conocidas
- **Términos de negocio**: MT puede variar ("Sign in" vs "Enter"). Usa **GlossaryTranslator** o packs JSON.
- **Plurales complejos**: ICU avanzado (plural/género) está en el roadmap.
- **Idiomas raros/RTL**: prueba visuales; usa `AutoTradLayout` para direction.
- **Primer uso**: puede descargar modelos; usa **pre-calentamiento** para evitar lag inicial.

## 🧰 Troubleshooting
- **Nada traduce**: ¿estás usando `EchoTranslator`? Cambia a `DetectingTranslator + MlKitTranslator`.
- **Lento la primera vez**: haz `preDownloadLanguages(...)` en el boot y `prewarm(...)` de textos críticos.
- **Traducción rompió el layout**: marca `role = TextRole.Button/Chip` para aplicar límites del Quality Gate.
- **Traducción errónea**: edita via **Dev Overlay** (long-press) o fija en pack JSON/glosario.
- **Sin internet en el primer uso**: asegura que el modelo del idioma fue descargado antes (o habilita `requireWifiForDownload=false`).

## 🔒 Seguridad

- ✅ **On-Device** - Ningún dato enviado a servidores externos
- ✅ **Sin PII** - Telemetría no recolecta datos personales
- ✅ **Sandbox** - Archivos pending aislados en la app
- ✅ **FileProvider** - Compartir seguro

## 📄 Licencia

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

## 🤝 Contribuyendo

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Desarrollo Local

```bash
git clone https://github.com/robsonjso/autotrad.git
cd autotrad
./gradlew build
```

## 📞 Soporte

- 📧 **Issues**: [GitHub Issues](https://github.com/robsonjso/autotrad/issues)
- 📖 **Documentación**: [Wiki](https://github.com/robsonjso/autotrad/wiki)
- 💬 **Discusiones**: [GitHub Discussions](https://github.com/robsonjso/autotrad/discussions)

## 🎯 Roadmap

### v0.2.0 (Próximo)
- [ ] Soporte iOS (SwiftUI)
- [ ] Soporte Web (Compose for Web)
- [ ] Integración DeepL
- [ ] Integración OpenAI
- [ ] Traducción por Lotes
- [ ] Analytics de Traducción

### v1.0.0 (Futuro)
- [ ] Features Enterprise
- [ ] Colaboración en Equipo
- [ ] Sync de Memoria de Traducción
- [ ] Quality Gates Avanzados
- [ ] Modelos ML Personalizados

---

**AutoTrad** - ¡Revolucionando la internacionalización Android! 🌍✨

*Desarrollado con ❤️ por la comunidad AutoTrad*
