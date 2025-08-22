# AutoTrad - FAQ 🤔

Perguntas frequentes sobre a biblioteca AutoTrad.

## 🚀 Instalação e Configuração

### Q: Como instalar a biblioteca?

**A:** Adicione o repositório JitPack e as dependências:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.robsonjso:autotrad-core:0.1.0")
    implementation("com.github.robsonjso:autotrad-compose:0.1.0")
}
```

### Q: Preciso de permissões especiais?

**A:** Não! A biblioteca funciona completamente offline usando ML Kit on-device. Não são necessárias permissões de internet ou armazenamento.

### Q: O tamanho do APK aumenta muito?

**A:** Sim, aproximadamente +40MB devido aos modelos ML Kit. Isso é compensado pela funcionalidade offline e performance.

## 🔧 Uso Básico

### Q: Como usar AutoText?

**A:** Simples! Substitua `Text()` por `AutoText()`:

```kotlin
// Antes
Text("Olá, mundo!")

// Depois
AutoText("Olá, mundo!") // traduz automaticamente
```

### Q: Como trocar o idioma em runtime?

**A:** Use `AutoTrad.setLocale()`:

```kotlin
Button(onClick = { AutoTrad.setLocale("en") }) {
    AutoText("English")
}
```

### Q: Como usar placeholders?

**A:** Use a sintaxe `{nome}` e passe os valores via `args`:

```kotlin
AutoText("Olá, {name}!", args = mapOf("name" to "João"))
// Resultado: "Hello, João!" (em inglês)
```

## 🛡️ Quality Gate

### Q: O que é Quality Gate?

**A:** Sistema que valida traduções para evitar problemas:
- ✅ Preserva placeholders `{name}`
- ✅ Aplica limites de caracteres por role
- ✅ Bloqueia tradução de emails, códigos, etc.

### Q: Como definir limites de caracteres?

**A:** Use `TextRole`:

```kotlin
Button(onClick = {}) {
    AutoText("Continuar", role = TextRole.Button) // máx 16 chars
}
```

### Q: Como adicionar termos ao glossário?

**A:** Use `GlossaryTranslator`:

```kotlin
GlossaryTranslator(
    map = mapOf("Login" to "Enter"),
    dontTranslate = setOf("AutoTrad", "ID"),
    delegate = DetectingTranslator(...)
)
```

## 🌍 Locale Management

### Q: Como persistir a escolha do usuário?

**A:** Use `LocaleManager`:

```kotlin
val localeManager = LocaleManager(context, LocalePolicy(...))
localeManager.setUserLanguage("en") // salva automaticamente
```

### Q: Como seguir o idioma do sistema?

**A:** Use `LocaleMode.FOLLOW_SYSTEM`:

```kotlin
LocalePolicy(mode = LocaleMode.FOLLOW_SYSTEM)
```

### Q: Como suportar RTL?

**A:** Use `AutoTradLayout`:

```kotlin
AutoTradLayout { // aplica RTL automaticamente para árabe, hebraico, etc.
    DemoScreen()
}
```

## 📊 Performance

### Q: A primeira tradução é lenta?

**A:** Sim, ~500ms para baixar o modelo ML Kit. Use pré-aquecimento:

```kotlin
lifecycleScope.launch {
    MlKitTranslator().preDownloadLanguages(Locale.ENGLISH, Locale("es"))
    AutoTradPrewarm.prewarmCriticalUI(Locale.ENGLISH)
}
```

### Q: Como otimizar performance?

**A:** 
1. Use pré-aquecimento
2. Configure glossário para termos frequentes
3. Use assets JSON para traduções fixas
4. Monitore com telemetria

### Q: O cache funciona offline?

**A:** Sim! Translation Memory fica em memória e funciona offline.

## 🔒 Privacidade e Segurança

### Q: Meus dados são enviados para servidores?

**A:** Não! Tudo roda localmente no dispositivo usando ML Kit on-device.

### Q: A telemetria coleta dados pessoais?

**A:** Não! Apenas métricas agregadas sem conteúdo:
```kotlin
"tmHits": 42,
"mtCalls": 15,
"qualityRejects": 3
```

### Q: Os arquivos pending são seguros?

**A:** Sim! Ficam no sandbox do app e não são acessíveis por outros apps.

## 🛠️ Desenvolvimento

### Q: Como editar traduções em debug?

**A:** Use o Dev Overlay - long-press em qualquer `AutoText` para editar.

### Q: Como exportar traduções pending?

**A:** Use o botão "Exportar pending" no sample ou:

```kotlin
val zip = AutoTradPending.zipPending(context)
// Abre share sheet para e-mail, Drive, etc.
```

### Q: Como mesclar pending nos assets?

**A:** Use o Gradle task:

```bash
./gradlew :app:mergeAutoTradPending
```

### Q: Como criar um translator customizado?

**A:** Implemente a interface `Translator`:

```kotlin
class CustomTranslator : Translator {
    override suspend fun translate(
        text: String, 
        src: Locale?, 
        tgt: Locale
    ): String? {
        // Sua lógica
        return translatedText
    }
}
```

## 📱 UI e Compose

### Q: Como usar com Compose?

**A:** Use `AutoText()` diretamente:

```kotlin
@Composable
fun MyScreen() {
    Column {
        AutoText("Olá, mundo!")
        Button(onClick = {}) {
            AutoText("Continuar", role = TextRole.Button)
        }
    }
}
```

### Q: Como usar acessibilidade?

**A:** Use `Modifier.autoContentDescription`:

```kotlin
Button(
    modifier = Modifier.autoContentDescription("Botão de exemplo")
) {
    AutoText("Exemplo")
}
```

### Q: Como usar com Text() normal?

**A:** Use `Modifier.autoTrad()`:

```kotlin
Text("Olá, mundo!", Modifier.autoTrad())
```

## 🔧 Troubleshooting

### Q: Tradução não funciona?

**A:** Verifique:
1. Se `AutoTrad.init()` foi chamado
2. Se há um translator configurado
3. Se o idioma de destino é suportado
4. Logs para erros de ML Kit

### Q: Modelo ML Kit não baixa?

**A:** Verifique:
1. Conexão com internet
2. Se `requireWifiForDownload = false` para dados móveis
3. Espaço disponível no dispositivo

### Q: Quality Gate rejeita traduções?

**A:** Verifique:
1. Se placeholders estão preservados
2. Se o texto não excede limite do role
3. Se não está na blacklist (emails, códigos)

### Q: Performance ruim?

**A:** Otimize:
1. Use pré-aquecimento
2. Configure glossário
3. Use assets JSON
4. Monitore com telemetria

## 📚 Recursos Avançados

### Q: Como usar DeepL ou OpenAI?

**A:** Crie um translator customizado:

```kotlin
class DeepLTranslator(private val apiKey: String) : Translator {
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        // Implementação com DeepL API
    }
}
```

### Q: Como sincronizar TM entre dispositivos?

**A:** Exporte/importe arquivos pending ou implemente sync customizado.

### Q: Como usar com Kotlin Multiplatform?

**A:** A biblioteca é específica para Android. Para multiplatform, considere implementar uma versão customizada.

### Q: Como contribuir?

**A:** 
1. Fork o projeto
2. Crie uma branch para sua feature
3. Implemente e teste
4. Abra um Pull Request

## 🎯 Casos de Uso

### Q: É bom para apps em produção?

**A:** Sim! A biblioteca é production-ready com:
- ✅ Quality Gate robusto
- ✅ Performance otimizada
- ✅ Segurança e privacidade
- ✅ Telemetria opcional
- ✅ Dev tools completos

### Q: Funciona com apps existentes?

**A:** Sim! Pode ser integrado gradualmente:
1. Adicione AutoTrad
2. Substitua textos críticos por AutoText
3. Configure glossário
4. Migre gradualmente

### Q: É bom para startups?

**A:** Perfeito! Permite:
- ✅ Lançamento rápido em múltiplos idiomas
- ✅ Iteração rápida sem strings.xml
- ✅ Feedback automático via pending
- ✅ Escalabilidade com glossários

---

**Ainda tem dúvidas?** Abra uma [issue](https://github.com/robsonjso/autotrad/issues) ou participe das [discussões](https://github.com/robsonjso/autotrad/discussions)!
