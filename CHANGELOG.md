# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [0.1.0] - 2024-08-22

### Adicionado
- Engine de tradução em tempo de execução sem `strings.xml`
- Translation Memory (TM) em memória
- Componente `AutoText` para Jetpack Compose
- Modifier `autoTrad()` para textos
- Quality Gate para preservar placeholders `{name}`
- DevCapture para gerar `autotrad.pending.<lang>.json` em debug
- Interface `Translator` para provedores plugáveis
- `EchoTranslator` (MVP)
- `MlKitTranslator` (on-device, opcional)
- Suporte a assets JSON (`autotrad.<lang>.json`)
- Troca de locale em runtime (`AutoTrad.setLocale()`)
- Placeholders com substituição de argumentos
- Testes unitários completos
- Sample app demonstrativo

### Módulos
- `autotrad-core`: engine principal, TM, QualityGate, DevCapture
- `autotrad-compose`: componentes UI para Compose
- `samples/autotrad-sample`: app de demonstração

### Configuração
- Maven Publish configurado para JitPack
- GitHub Actions CI
- Documentação completa (README, CONTRIBUTING, etc.)
- Licença Apache-2.0
