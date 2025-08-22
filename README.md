# AutoTrad 🌍

**Tradução automática em runtime para Android sem `strings.xml`**

[![Build Status](https://github.com/robsonjso/autotrad/workflows/CI/badge.svg)](https://github.com/robsonjso/autotrad/actions)
[![JitPack](https://jitpack.io/v/robsonjso/autotrad.svg)](https://jitpack.io/#robsonjso/autotrad)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[🇺🇸 English](README_EN.md) | [🇪🇸 Español](README_ES.md) | 🇧🇷 Português

AutoTrad é uma biblioteca Android que permite **tradução automática em tempo real** sem necessidade de arquivos `strings.xml`. Use `AutoText("qualquer texto")` e a biblioteca traduz automaticamente usando ML Kit on-device.

## ✨ Características

- 🚀 **Zero Setup** - Funciona out-of-the-box
- 🤖 **ML Kit On-Device** - Tradução offline após download inicial
- 🎯 **Detecção Automática** - Identifica idioma de origem automaticamente
- 📱 **Compose First** - Integração nativa com Jetpack Compose
- 🔄 **Runtime Switching** - Troca de idioma em tempo real
- 🛡️ **Quality Gate** - Preserva placeholders e aplica limites por role
- 📊 **Translation Memory** - Cache inteligente para performance
- 🌍 **RTL Support** - Suporte automático a idiomas RTL
- ♿ **Acessibilidade** - Tradução de contentDescription
- 📈 **Telemetria** - Métricas opcionais sem PII
- 🛠️ **Dev Tools** - Overlay de edição e export de pending

## 🚀 Quickstart

### 1. Dependências

#### settings.gradle.kts (no projeto consumidor)
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
Se você ofusca seu app, inclua:
```pro
-keep class io.autotrad.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
```

### 2. Inicialização

```kotlin
// Application ou MainActivity
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

### 3. Uso na UI

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Tradução automática
        AutoText("Olá, mundo!") // → "Hello, world!"
        
        // Com placeholders
        AutoText("Bem-vindo, {name}!", args = mapOf("name" to "João"))
        
        // Com role (limite de caracteres)
        Button(onClick = {}) {
            AutoText("Continuar", role = TextRole.Button) // máx 16 chars
        }
        
        // Seletor de idioma
        Button(onClick = { AutoTrad.setLocale("en") }) {
            AutoText("English")
        }
    }
}
```

## 📚 Guia Completo

### 🔧 Configuração Avançada

#### LocaleManager com Persistência

```kotlin
val localeManager = LocaleManager(
    context = this,
    policy = LocalePolicy(
        mode = LocaleMode.HYBRID, // segue sistema até usuário escolher
        supported = listOf("en", "es", "pt-BR"),
        fallbackChain = listOf("en")
    )
)

// Persistência automática
localeManager.setUserLanguage("en") // salva no DataStore
```

#### Glossário e Do-Not-Translate

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

#### Pré-aquecimento para Performance

```kotlin
lifecycleScope.launch {
    // Baixa modelos ML Kit
    MlKitTranslator().preDownloadLanguages(
        Locale("pt", "BR"), 
        Locale.ENGLISH, 
        Locale("es")
    )
    
    // Pré-traduz textos críticos
    AutoTradPrewarm.prewarmCriticalUI(Locale.ENGLISH)
}
```

### 🎨 UI Components

#### AutoTradLayout (RTL Automático)

```kotlin
AutoTradLayout { // aplica LayoutDirection.Rtl para árabe, hebraico, etc.
    DemoScreen()
}
```

#### Acessibilidade

```kotlin
Button(
    modifier = Modifier.autoContentDescription("Botão de exemplo")
) {
    AutoText("Exemplo A11y", role = TextRole.Button)
}
```

### 🛡️ Quality Gate

#### TextRole para Limites de Caracteres

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

A QualityGate bloqueia automaticamente tradução de:
- 📧 **Emails** - `user@example.com`
- 🔢 **Códigos** - `ABC123`, `ID456`
- 🔢 **Números longos** - `1234567890`
- 📝 **Placeholders** - `{name}`, `{count}`

### 📊 Telemetria

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

// Métricas em tempo real
val metrics = AutoTradTelemetry.snapshot()
```

### 🛠️ Dev Tools

#### Dev Overlay (Debug)

Long-press em qualquer `AutoText` para editar traduções em tempo real.

#### Export de Pending

```kotlin
@Composable
fun ExportPendingButton() {
    Button(onClick = {
        val zip = AutoTradPending.zipPending(context)
        // Abre share sheet (e-mail, Drive, etc.)
    }) {
        AutoText("Exportar pending", role = TextRole.Button)
    }
}
```

#### Gradle Task para Merge

```bash
./gradlew :app:mergeAutoTradPending
```

Mergeia arquivos `pending/autotrad.pending.*.json` em `src/main/assets/autotrad/`.

### 📁 Assets (Opcional)

Crie arquivos JSON em `src/main/assets/autotrad/`:

```json
// autotrad.en.json
{
  "Entrar": "Enter",
  "Sair": "Exit",
  "Bem-vindo, {name}!": "Welcome, {name}!"
}
```

## 🏗️ Arquitetura

### Módulos

- **`autotrad-core`** - Engine principal, ML Kit, Quality Gate
- **`autotrad-compose`** - Componentes UI para Compose
- **`autotrad-sample`** - App de demonstração completo

### Fluxo de Tradução

```
1. AutoText("novo texto")
2. Translation Memory (cache)
3. Assets JSON (se disponível)
4. Glossary (termos fixos)
5. ML Kit Language Detection
6. ML Kit Translation
7. Quality Gate validation
8. Salva em TM + pending.json
9. Exibe tradução
```

### Provedores de Tradução

- **`MlKitTranslator`** - Google ML Kit on-device
- **`DetectingTranslator`** - Detecção automática + delegate
- **`GlossaryTranslator`** - Glossário + do-not-translate
- **`EchoTranslator`** - Para testes (retorna original)

## 📱 Sample App

O app de demonstração inclui:

- ✅ Tradução automática em tempo real
- ✅ Seletor de idiomas com persistência
- ✅ Exemplos de placeholders
- ✅ Quality Gate por role
- ✅ RTL automático
- ✅ Acessibilidade
- ✅ Dev Overlay
- ✅ Export de pending
- ✅ Telemetria

## 🔧 Configuração Avançada

### LocalePolicy Modes

```kotlin
enum class LocaleMode {
    FOLLOW_SYSTEM,     // Sempre segue sistema
    USER_SELECTED,     // Sempre usa seleção do usuário
    AUTO_BY_LOCATION,  // Baseado em geolocalização
    HYBRID            // Segue sistema até usuário escolher
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
        // Sua lógica de tradução
        return translatedText
    }
}
```

## 🚀 Performance

### Otimizações Incluídas

- **Translation Memory** - Cache em memória
- **Pré-aquecimento** - Download de modelos antecipado
- **Lazy Loading** - Modelos ML Kit sob demanda
- **Quality Gate** - Evita traduções desnecessárias
- **RTL Detection** - Otimizado para idiomas RTL

### Benchmarks (valores típicos)

- **Primeira tradução**: 300–800 ms (inclui download do modelo, cacheado depois)
- **Traduções subsequentes**: ~30–80 ms (cache + on-device)
- **TM hits** (memória): ~1–5 ms
- **Tamanho do APK**: sem impacto relevante pelos modelos
- **Armazenamento no dispositivo**: +~20–40 MB por idioma baixado (uma vez)

## ✅ Compatibilidade
- **Android**: minSdk 21+
- **Compose**: 1.6.x+
- **Kotlin**: 1.9+ (testado com plugin 2.0.x)
- **ML Kit Translate**: modelos baixados on-demand
- **Arquitetura**: funciona com MVVM/Compose padrão

## ⚠️ Limitações conhecidas
- **Termos de negócio**: MT pode variar ("Sign in" vs "Enter"). Use **GlossaryTranslator** ou packs JSON.
- **Plurais complexos**: ICU avançado (plural/gênero) está no roadmap.
- **Idiomas raros/RTL**: teste visuais; use `AutoTradLayout` para direction.
- **Primeiro uso**: pode baixar modelos; use **pré-aquecimento** para evitar lag inicial.

## 🧰 Troubleshooting
- **Nada traduz**: você está usando `EchoTranslator`? Troque por `DetectingTranslator + MlKitTranslator`.
- **Demora na 1ª vez**: faça `preDownloadLanguages(...)` no boot e `prewarm(...)` dos textos críticos.
- **Tradução quebrou o layout**: marque `role = TextRole.Button/Chip` para aplicar limites no Quality Gate.
- **Tradução errada**: edite via **Dev Overlay** (long-press) ou fixe no pack JSON/glossário.
- **Sem internet no 1º uso**: garanta que o modelo do idioma foi baixado antes (ou habilite `requireWifiForDownload=false`).

## 🔒 Segurança

- ✅ **On-Device** - Nenhum dado enviado para servidores
- ✅ **Sem PII** - Telemetria não coleta dados pessoais
- ✅ **Sandbox** - Arquivos pending isolados no app
- ✅ **FileProvider** - Compartilhamento seguro

## 📄 Licença

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

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Desenvolvimento Local

```bash
git clone https://github.com/robsonjso/autotrad.git
cd autotrad
./gradlew build
```

## 📞 Suporte

- 📧 **Issues**: [GitHub Issues](https://github.com/robsonjso/autotrad/issues)
- 📖 **Documentação**: [Wiki](https://github.com/robsonjso/autotrad/wiki)
- 💬 **Discussões**: [GitHub Discussions](https://github.com/robsonjso/autotrad/discussions)

## 🎯 Roadmap

### v0.2.0 (Próximo)
- [ ] iOS Support (SwiftUI)
- [ ] Web Support (Compose for Web)
- [ ] DeepL Integration
- [ ] OpenAI Integration
- [ ] Batch Translation
- [ ] Translation Analytics

### v1.0.0 (Futuro)
- [ ] Enterprise Features
- [ ] Team Collaboration
- [ ] Translation Memory Sync
- [ ] Advanced Quality Gates
- [ ] Custom ML Models

---

**AutoTrad** - Revolucionando a internacionalização Android! 🌍✨

*Desenvolvido com ❤️ pela comunidade AutoTrad*
