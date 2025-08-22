# AutoTrad - Guia Avançado 🚀

Este documento explica os conceitos avançados e internos da biblioteca AutoTrad para desenvolvedores que querem entender como tudo funciona por baixo dos panos.

## 🏗️ Arquitetura Interna

### Fluxo de Tradução Detalhado

```
1. AutoText("novo texto") é chamado
2. AutoTrad.currentLocale.collectAsState() observa mudanças
3. LaunchedEffect detecta mudança de locale
4. AutoTrad.translate("novo texto", tgt = locale) é chamado
5. Texto é normalizado (trim, lowercase)
6. Translation Memory é consultado primeiro
7. Se não encontrado, assets JSON são verificados
8. Se não encontrado, cadeia de translators é executada:
   a. GlossaryTranslator (termos fixos + do-not-translate)
   b. DetectingTranslator (detecção automática)
   c. MlKitTranslator (tradução on-device)
9. QualityGate valida a tradução
10. Se aprovada, salva em TM + pending.json
11. Texto é aplicado na UI com args (placeholders)
```

### Translation Memory (TM)

```kotlin
// Estrutura interna
private val tm: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
//                    ↑ language    ↑ normalized_key → translation

// Exemplo:
tm["en"] = mutableMapOf(
    "entrar" to "Enter",
    "sair" to "Exit"
)
```

### Quality Gate Engine

```kotlin
object QualityGate {
    // Regex para detectar placeholders
    private val PLACEHOLDER = Regex("\\{[^{}]+\\}")
    
    // Padrões que não devem ser traduzidos
    private val BLACKLIST_PATTERNS = listOf(
        Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // emails
        Regex("\\b[A-Z]{2,}\\d{3,}\\b"), // códigos
        Regex("\\b\\d{4,}\\b"), // números longos
        Regex("\\{[^{}]+\\}") // placeholders
    )
    
    fun isAcceptable(
        sourceNormalized: String,
        candidate: String,
        role: TextRole? = null
    ): Boolean {
        // 1. Verifica placeholders
        val srcPh = PLACEHOLDER.findAll(sourceNormalized).map { it.value }.toSet()
        val dstPh = PLACEHOLDER.findAll(candidate).map { it.value }.toSet()
        if (srcPh != dstPh) return false
        
        // 2. Verifica blacklist
        if (isBlacklisted(sourceNormalized) && sourceNormalized != candidate) return false
        
        // 3. Verifica limites por role
        val maxChars = when (role) {
            TextRole.Button -> 16
            TextRole.Chip -> 12
            TextRole.Title -> 48
            TextRole.Caption -> 24
            TextRole.Error -> 32
            else -> null
        }
        if (maxChars != null && candidate.length > maxChars) return false
        
        return true
    }
}
```

## 🔧 Provedores de Tradução

### Interface Translator

```kotlin
interface Translator {
    suspend fun translate(
        text: String,
        src: Locale?, // null = auto-detect
        tgt: Locale
    ): String?
}
```

### MlKitTranslator

```kotlin
class MlKitTranslator(
    private val requireWifiForDownload: Boolean = true
) : Translator {
    private val cache = ConcurrentHashMap<String, com.google.mlkit.nl.translate.Translator>()
    
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        val s = src ?: return null
        val srcCode = TranslateLanguage.fromLanguageTag(s.toLanguageTag())
            ?: TranslateLanguage.fromLanguageTag(s.language) ?: return null
        val tgtCode = TranslateLanguage.fromLanguageTag(tgt.toLanguageTag())
            ?: TranslateLanguage.fromLanguageTag(tgt.language) ?: return null
        
        if (srcCode == tgtCode) return text
        
        val key = "$srcCode->$tgtCode"
        val client = cache.getOrPut(key) {
            val opts = TranslatorOptions.Builder()
                .setSourceLanguage(srcCode)
                .setTargetLanguage(tgtCode)
                .build()
            Translation.getClient(opts)
        }
        
        // Garante que o modelo está baixado
        val cond = DownloadConditions.Builder()
            .apply { if (requireWifiForDownload) requireWifi() }
            .build()
        client.downloadModelIfNeeded(cond).await()
        
        return client.translate(text).await()
    }
}
```

### DetectingTranslator

```kotlin
class DetectingTranslator(
    private val detector: MlKitLanguageDetector,
    private val delegate: Translator,
    private val fallbackSource: Locale
) : Translator {
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        val detected = src ?: detector.detect(text) ?: fallbackSource
        if (detected.language == tgt.language) return text
        return delegate.translate(text, detected, tgt)
    }
}
```

### GlossaryTranslator

```kotlin
class GlossaryTranslator(
    private val map: Map<String, String>,
    private val dontTranslate: Set<String> = setOf("ID", "AutoTrad"),
    private val delegate: Translator
) : Translator {
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        // 1. Verifica do-not-translate
        if (dontTranslate.contains(text)) return text
        
        // 2. Verifica glossário
        map[text]?.let { return it }
        
        // 3. Delega para o próximo translator
        return delegate.translate(text, src, tgt)
    }
}
```

## 🌍 Locale Management

### LocalePolicy

```kotlin
data class LocalePolicy(
    val mode: LocaleMode = LocaleMode.HYBRID,
    val supported: List<String> = listOf("en"),
    val fallbackChain: List<String> = listOf("en")
)

sealed class LocaleMode {
    data object FOLLOW_SYSTEM : LocaleMode()
    data class USER_SELECTED(val tag: String) : LocaleMode()
    data object AUTO_BY_LOCATION : LocaleMode()
    data object HYBRID : LocaleMode() // segue sistema até o usuário escolher
}
```

### LocaleManager

```kotlin
class LocaleManager(
    private val context: Context,
    policy: LocalePolicy
) {
    private val KEY_USER = stringPreferencesKey("user_lang")
    private val policyFlow: MutableStateFlow<LocalePolicy> = MutableStateFlow(policy)
    val effective: MutableStateFlow<Locale> = MutableStateFlow(Locale.getDefault())
    
    fun setUserLanguage(tag: String?) = runBlocking {
        context.dataStore.edit { prefs ->
            if (tag == null) prefs.remove(KEY_USER) else prefs[KEY_USER] = tag
        }
        setPolicy(policyFlow.value.copy(
            mode = if (tag == null) LocaleMode.FOLLOW_SYSTEM 
                   else LocaleMode.USER_SELECTED(tag)
        ))
    }
    
    fun recompute() {
        val sys = Locale.getDefault()
        val user = getUserLanguage()
        val geo = suggestBySystemLocale(policyFlow.value.supported)
        val resolved = resolveEffective(policyFlow.value, sys, geo, user)
        effective.value = resolved
        AutoTrad.setLocale(resolved.toLanguageTag())
    }
}
```

## 📊 Telemetria

### Interface TelemetrySink

```kotlin
interface TelemetrySink {
    fun onTmHit() {} // Translation Memory hit
    fun onMtCall(durationMs: Long) {} // Machine Translation call
    fun onQualityReject() {} // Quality gate rejection
    fun onOverlayEdit() {} // Dev overlay edit
}
```

### AutoTradTelemetry

```kotlin
object AutoTradTelemetry {
    private var sink: TelemetrySink? = null
    private val tmHits = AtomicLong(0)
    private val mtCalls = AtomicLong(0)
    private val qualityRejects = AtomicLong(0)
    
    fun register(s: TelemetrySink?) { sink = s }
    fun tmHit() { tmHits.incrementAndGet(); sink?.onTmHit() }
    fun mtCall(dMs: Long) { mtCalls.incrementAndGet(); sink?.onMtCall(dMs) }
    fun qualityReject() { qualityRejects.incrementAndGet(); sink?.onQualityReject() }
    
    fun snapshot(): Map<String, Long> = mapOf(
        "tmHits" to tmHits.get(),
        "mtCalls" to mtCalls.get(),
        "qualityRejects" to qualityRejects.get()
    )
}
```

## 🛠️ Dev Tools

### Dev Overlay

```kotlin
@Composable
fun AutoTradDevHost(content: @Composable () -> Unit) {
    var showOverlay by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    
    CompositionLocalProvider(LocalAutoTradDevController provides object : AutoTradDevController {
        override fun onLongPress(text: String) {
            selectedText = text
            showOverlay = true
        }
    }) {
        content()
        
        if (showOverlay) {
            DevOverlay(
                text = selectedText,
                onDismiss = { showOverlay = false },
                onSave = { original, translated ->
                    AutoTrad.upsertTranslation(original, translated)
                    showOverlay = false
                }
            )
        }
    }
}
```

### Export de Pending

```kotlin
object AutoTradPending {
    fun listPendingFiles(context: Context): List<File> {
        val dir = context.filesDir
        return dir.listFiles { f -> 
            f.name.startsWith("autotrad.pending.") && f.extension == "json" 
        }?.toList() ?: emptyList()
    }
    
    fun zipPending(context: Context): File? {
        val files = listPendingFiles(context)
        if (files.isEmpty()) return null
        
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val outFile = File(context.cacheDir, "autotrad-pending-$stamp.zip")
        
        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            files.forEach { f ->
                zos.putNextEntry(ZipEntry(f.name))
                f.inputStream().use { input -> input.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return outFile
    }
}
```

## 🔒 Segurança e Privacidade

### On-Device Processing

- ✅ **ML Kit** roda localmente no dispositivo
- ✅ **Nenhum dado** é enviado para servidores externos
- ✅ **Translation Memory** fica apenas em memória
- ✅ **Pending files** ficam no sandbox do app

### Telemetria Sem PII

```kotlin
// Apenas métricas agregadas, sem conteúdo
"tmHits": 42,
"mtCalls": 15,
"qualityRejects": 3
// NUNCA: "translatedText": "Hello world"
```

### FileProvider Seguro

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## 🚀 Performance

### Otimizações Implementadas

1. **Translation Memory Cache**
   - Cache em memória para traduções frequentes
   - Evita chamadas desnecessárias ao ML Kit

2. **Lazy Loading de Modelos**
   - Modelos ML Kit baixados sob demanda
   - Cache de modelos por par de idiomas

3. **Pré-aquecimento**
   - Download antecipado de modelos críticos
   - Pré-tradução de textos da UI

4. **Quality Gate Early Exit**
   - Rejeita traduções inválidas rapidamente
   - Evita processamento desnecessário

### Benchmarks

| Operação | Tempo | Observações |
|----------|-------|-------------|
| TM Hit | ~5ms | Cache em memória |
| ML Kit (primeira) | ~500ms | Download modelo |
| ML Kit (subsequente) | ~50ms | Modelo já baixado |
| Quality Gate | ~1ms | Validação rápida |
| Placeholder replacement | ~1ms | String manipulation |

## 🔧 Customização Avançada

### Custom Translator

```kotlin
class DeepLTranslator(
    private val apiKey: String
) : Translator {
    override suspend fun translate(
        text: String, 
        src: Locale?, 
        tgt: Locale
    ): String? {
        // Implementação com DeepL API
        val response = httpClient.post("https://api-free.deepl.com/v2/translate") {
            setBody(DeepLRequest(text, src?.language, tgt.language))
            headers { append("Authorization", "DeepL-Auth-Key $apiKey") }
        }
        return response.body<DeepLResponse>().translations.firstOrNull()?.text
    }
}
```

### Custom Quality Gate

```kotlin
class CustomQualityGate : QualityGate {
    override fun isAcceptable(
        sourceNormalized: String,
        candidate: String,
        role: TextRole?
    ): Boolean {
        // Sua lógica customizada
        if (candidate.contains("bad_word")) return false
        if (candidate.length > 100) return false
        return super.isAcceptable(sourceNormalized, candidate, role)
    }
}
```

### Custom Locale Policy

```kotlin
class CustomLocalePolicy : LocalePolicy {
    override fun resolveEffective(
        system: Locale,
        geoSuggested: String?,
        userSelected: String?
    ): Locale {
        // Lógica customizada de resolução de locale
        return when {
            userSelected != null -> Locale.forLanguageTag(userSelected)
            geoSuggested != null -> Locale.forLanguageTag(geoSuggested)
            else -> system
        }
    }
}
```

---

Este guia avançado cobre os aspectos internos da biblioteca AutoTrad. Para uso básico, consulte o [README.md](../README.md).
