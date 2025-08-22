# Tradução Automática no AutoTrad

## Visão Geral

O AutoTrad suporta tradução automática usando ML Kit on-device. Isso significa que qualquer texto novo pode ser traduzido automaticamente sem precisar de `strings.xml` ou cadastro manual.

## Como Funciona

### 1. Detecção Automática de Idioma

O `MlKitLanguageDetector` identifica automaticamente o idioma do texto:

```kotlin
val detector = MlKitLanguageDetector()
val detected = detector.detect("papagaio") // retorna Locale("pt", "BR")
```

### 2. Tradução On-Device

O `MlKitTranslator` traduz usando modelos baixados localmente:

```kotlin
val translator = MlKitTranslator(requireWifiForDownload = false)
val translated = translator.translate("papagaio", Locale("pt", "BR"), Locale.ENGLISH)
// retorna "parrot"
```

### 3. Wrapper Automático

O `DetectingTranslator` combina detecção + tradução:

```kotlin
val autoTranslator = DetectingTranslator(
    detector = MlKitLanguageDetector(),
    delegate = MlKitTranslator(),
    fallbackSource = Locale("pt", "BR")
)
```

## Configuração Completa

### Dependências

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("com.google.mlkit:language-id:17.0.5")
}
```

### Inicialização

```kotlin
AutoTrad.init(
    context = applicationContext,
    translators = listOf(
        DetectingTranslator(
            detector = MlKitLanguageDetector(),
            delegate = MlKitTranslator(requireWifiForDownload = false),
            fallbackSource = Locale("pt", "BR") // idioma base do projeto
        )
    ),
    devCaptureEnabled = BuildConfig.DEBUG
)
```

### Pré-download de Modelos (Opcional)

Para evitar lag na primeira tradução:

```kotlin
lifecycleScope.launch {
    MlKitTranslator().preDownloadLanguages(
        Locale("pt", "BR"), 
        Locale.ENGLISH, 
        Locale("es")
    )
}
```

## Uso na UI

```kotlin
// Qualquer texto será traduzido automaticamente
AutoText("papagaio")        // "parrot" em inglês
AutoText("computador")      // "computer" em inglês
AutoText("teclado")         // "keyboard" em inglês

// Troca de idioma
AutoTrad.setLocale("en")    // traduz automaticamente
AutoTrad.setLocale("es")    // traduz para espanhol
```

## Fluxo Completo

1. **Primeiro uso**: ML Kit baixa os modelos necessários
2. **Detecção**: Identifica que "papagaio" está em português
3. **Tradução**: Traduz para o idioma selecionado
4. **Quality Gate**: Valida placeholders e tamanho
5. **Cache**: Salva no TM + `autotrad.pending.<lang>.json`
6. **Exibição**: Mostra a tradução instantaneamente

## Vantagens

- **Sem strings.xml**: Use literais diretamente
- **Offline**: Funciona sem internet após download dos modelos
- **Automático**: Detecta idioma e traduz sem configuração
- **Cache**: Traduções ficam salvas para uso futuro
- **Quality Gate**: Evita traduções que quebram a UI

## Observações

- **Primeiro uso**: Precisa de internet para baixar modelos
- **Fallback**: Se não detectar, usa o idioma base do projeto
- **Performance**: Primeira tradução pode ter lag, depois é instantânea
- **Qualidade**: ML Kit on-device tem qualidade boa para UI
- **Customização**: Use Dev Overlay para ajustar traduções específicas
