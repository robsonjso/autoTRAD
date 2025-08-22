# Contribuindo para o AutoTrad

Obrigado por ajudar! Este guia explica como rodar o projeto localmente, abrir PRs e manter a qualidade.

## Requisitos
- JDK 17
- Android SDK (API 34+)
- IntelliJ/Android Studio
- (opcional) Node 18+ para docs/site futuramente

## Como rodar
```bash
# clonar
git clone https://github.com/robsoncastilho/autotrad.git
cd autotrad

# build libs + sample
./gradlew :packages:autotrad-core:assemble \
          :packages:autotrad-compose:assemble \
          :samples:autotrad-sample:assembleDebug
```

Abra o `autotrad-sample` e rode no emulador/dispositivo. Teste os botões EN/ES/PT-BR.

## Estrutura
```
packages/
  autotrad-core/      # engine, TM, QualityGate, DevCapture, Translator API
  autotrad-compose/   # AutoText, Modifier.autoTrad
samples/
  autotrad-sample/    # app demo
```

## Padrões

- **Kotlin**: oficial; Java 17 bytecode
- **Compose**: 1.6+
- **Lint**: mantenha o código limpo; se possível, rode ktlint/detekt antes do PR
- **Commits**: use mensagens claras com DCO (ex.: `git commit -s -m "feat(core): add QualityGate size limit"`)
- **PRs**: pequenos, com descrição do "antes/depois" + prints/GIF se UI

## Testes

- **Unit**: `./gradlew test`
- **Integração (sample)**: "Run" no emulador/dispositivo
- **Dica**: verifique se `autotrad.pending.<lang>.json` é gerado em debug ao navegar na UI

## Como adicionar um Translator

Implemente `Translator`:

```kotlin
class MyTranslator(...) : Translator {
  override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? { ... }
}
```

Registre no `AutoTrad.init(translators = listOf(MyTranslator(...)))`.

## Problemas / Segurança

- Abra issues com reprodução mínima
- Vulnerabilidades: use o e-mail indicado no `SECURITY.md`
