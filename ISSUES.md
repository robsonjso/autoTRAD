# Issues Iniciais - Roadmap AutoTrad

## 🚀 Próximas Features (7-10 dias)

### feat(core): LocalePolicy + LocaleManager
**Descrição**: Implementar política de idioma, persistência via DataStore, integração com AutoTrad.setLocale.

**Detalhes**:
- `LocalePolicy`: FOLLOW_SYSTEM, USER_SELECTED, AUTO_BY_LOCATION, HYBRID
- `LocaleManager`: persistência de preferências do usuário
- Integração com `AutoTrad.setLocale()`
- Menu de seleção de idioma no sample

**Labels**: `enhancement`, `core`

---

### feat(core): ML Kit Translator adapter
**Descrição**: Adicionar MlKitTranslator com download de modelos e cache por par de idiomas.

**Detalhes**:
- Download automático de modelos ML Kit
- Cache de clientes por par de idiomas
- Configuração de Wi-Fi para downloads
- Exemplo no sample

**Labels**: `enhancement`, `core`, `ml-kit`

---

### feat(core): QualityGate — maxChars por role
**Descrição**: Permitir informar role (Button/Label/Chip) para aplicar maxChars.

**Detalhes**:
- Enum `TextRole`: Button, Label, Chip, etc.
- API: `AutoText("...", role = TextRole.Button)`
- Configuração de limites por role
- Validação automática no QualityGate

**Labels**: `enhancement`, `core`, `quality`

---

### feat(compose): Dev Overlay (debug)
**Descrição**: Long-press em textos AutoText/autoTrad abre bottom sheet para editar/salvar.

**Detalhes**:
- Bottom sheet com texto original e tradução atual
- Campo de edição para tradução
- Salvar → vai para TM + pending.json
- Apenas em builds debug

**Labels**: `enhancement`, `compose`, `dev-tools`

---

## 🔧 Ferramentas (2-4 semanas)

### feat(tooling): KSP extractor (autotrad.base.json)
**Descrição**: Processor varrendo AutoText("...")/Text(...).autoTrad() e gerando catálogo base por build.

**Detalhes**:
- KSP processor para extrair strings
- Gera `autotrad.base.json` em build
- Reduz misses de tradução
- Ajuda quem não quer usar MT

**Labels**: `enhancement`, `tooling`, `ksp`

---

### ci: publicar AAR via GitHub Release
**Descrição**: Workflow que gera .aar e anexa à release (além do JitPack).

**Detalhes**:
- GitHub Actions para build de release
- Gera AARs dos módulos
- Anexa à GitHub Release
- Alternativa ao JitPack

**Labels**: `ci`, `publishing`

---

### chore: ktlint/detekt setup
**Descrição**: Configurar lint e tarefa no CI.

**Detalhes**:
- Configuração ktlint
- Configuração detekt
- Tarefa no CI
- Pre-commit hooks

**Labels**: `chore`, `quality`

---

## 📚 Documentação

### docs: Quickstart iOS (SwiftUI) + visão KMM
**Descrição**: Especificar API alvo para iOS e plano KMM do core.

**Detalhes**:
- API SwiftUI equivalente
- Plano KMM para core compartilhado
- Exemplo iOS básico
- Roadmap de implementação

**Labels**: `documentation`, `ios`, `kmm`

---

### docs: FAQ + Privacy
**Descrição**: Explicar que, por padrão, não há rede; BYOK é opt-in.

**Detalhes**:
- FAQ com perguntas comuns
- Política de privacidade
- Explicação sobre rede/offline
- Guia de configuração

**Labels**: `documentation`, `privacy`

---

## 🔄 Melhorias

### feat(core): JSON pack merger
**Descrição**: Tarefa Gradle para mesclar pending.json em autotrad.<lang>.json.

**Detalhes**:
- Tarefa `mergePendingTranslations`
- Mescla pending.json em assets
- Configuração de conflitos
- Integração no build

**Labels**: `enhancement`, `core`, `tooling`

---

### feat(core): AutoTradConfig centralizada
**Descrição**: Configuração centralizada para toggles (rede, ML Kit, locale default, fallback).

**Detalhes**:
```kotlin
data class AutoTradConfig(
  val allowNetwork: Boolean = false,
  val requireWifiForModelDownload: Boolean = true,
  val defaultSource: Locale = Locale.ENGLISH,
  val supportedLocales: List<String> = listOf("en"),
  val fallbackChain: List<String> = listOf("en")
)
```

**Labels**: `enhancement`, `core`, `config`

---

## 🎯 Como Contribuir

1. **Escolha uma issue** da lista acima
2. **Comente na issue** que vai trabalhar nela
3. **Crie uma branch** com nome descritivo
4. **Implemente** seguindo os padrões do projeto
5. **Teste** localmente
6. **Abra um PR** com descrição clara

### Labels Explicadas

- `enhancement`: Nova funcionalidade
- `bug`: Correção de bug
- `documentation`: Melhorias na documentação
- `core`: Módulo autotrad-core
- `compose`: Módulo autotrad-compose
- `tooling`: Ferramentas de desenvolvimento
- `ci`: Integração contínua
- `chore`: Tarefas de manutenção
- `quality`: Melhorias de qualidade
- `dev-tools`: Ferramentas para desenvolvedores
