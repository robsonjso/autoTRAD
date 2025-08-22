# Glossário e Do-Not-Translate no AutoTrad

## Visão Geral

O `GlossaryTranslator` permite controlar traduções específicas e definir termos que não devem ser traduzidos. Isso garante consistência e precisão em termos de negócio.

## Como Funciona

### 1. Glossário (Traduções Forçadas)

```kotlin
val glossary = mapOf(
    "Login" to "Enter",           // Força "Enter" em vez de "Sign in"
    "Home" to "Home",             // Mantém "Home" (não traduz)
    "Continuar" to "Continue"     // Força "Continue" em vez de "Next"
)
```

### 2. Do-Not-Translate (Termos Preservados)

```kotlin
val dontTranslate = setOf(
    "AutoTrad",     // Nome da marca
    "ID",           // Abreviações
    "v1.0",         // Versões
    "API"           // Termos técnicos
)
```

## Configuração

```kotlin
AutoTrad.init(
    context = this,
    translators = listOf(
        GlossaryTranslator(
            map = mapOf(
                "Login" to "Enter",
                "Home" to "Home",
                "Continuar" to "Continue"
            ),
            dontTranslate = setOf("AutoTrad", "ID", "v1.0"),
            delegate = DetectingTranslator(
                detector = MlKitLanguageDetector(),
                delegate = MlKitTranslator(),
                fallbackSource = Locale("pt", "BR")
            )
        )
    )
)
```

## Boas Práticas

### 1. Termos de Negócio

**✅ Correto:**
```kotlin
mapOf(
    "Login" to "Enter",           // Termo específico do app
    "Dashboard" to "Dashboard",   // Termo técnico mantido
    "Perfil" to "Profile"         // Tradução consistente
)
```

**❌ Evite:**
```kotlin
mapOf(
    "Olá" to "Hello",             // Termo genérico (deixe MT fazer)
    "Sim" to "Yes",               // Termo básico (deixe MT fazer)
    "Não" to "No"                 // Termo básico (deixe MT fazer)
)
```

### 2. Do-Not-Translate

**✅ Preserve:**
- **Marcas**: "AutoTrad", "Google", "Apple"
- **Códigos**: "ID123", "ABC456", "v1.0"
- **Emails**: "user@example.com"
- **URLs**: "https://example.com"
- **Termos técnicos**: "API", "SDK", "JSON"

### 3. Case e Plurais

**✅ Consistente:**
```kotlin
mapOf(
    "Login" to "Enter",           // Singular
    "Logins" to "Entries",        // Plural específico
    "HOME" to "Home",             // Case correto
    "home" to "Home"              // Case correto
)
```

### 4. Nomes Próprios

**✅ Preserve:**
```kotlin
dontTranslate = setOf(
    "João Silva",     // Nome de pessoa
    "São Paulo",      // Nome de cidade
    "Brasil",         // Nome de país
    "iPhone"          // Nome de produto
)
```

## Quando Usar

### Use Glossário para:
- ✅ Termos específicos do seu app
- ✅ Traduções que devem ser consistentes
- ✅ Termos técnicos com tradução preferida
- ✅ Nomes de features/produtos

### Use Do-Not-Translate para:
- ✅ Marcas e nomes próprios
- ✅ Códigos e identificadores
- ✅ Emails e URLs
- ✅ Termos que devem permanecer no original

### Deixe MT fazer:
- ✅ Textos genéricos da UI
- ✅ Mensagens de erro comuns
- ✅ Descrições e textos longos
- ✅ Termos básicos (sim/não, ok/cancelar)

## Exemplos Práticos

### App de E-commerce
```kotlin
GlossaryTranslator(
    map = mapOf(
        "Carrinho" to "Cart",
        "Checkout" to "Checkout",
        "Produto" to "Product",
        "Frete" to "Shipping"
    ),
    dontTranslate = setOf(
        "ShopApp", "SKU123", "user@shop.com"
    ),
    delegate = DetectingTranslator(...)
)
```

### App Financeiro
```kotlin
GlossaryTranslator(
    map = mapOf(
        "Saldo" to "Balance",
        "Transferência" to "Transfer",
        "Investimento" to "Investment"
    ),
    dontTranslate = setOf(
        "BankApp", "ACC123", "R$", "$"
    ),
    delegate = DetectingTranslator(...)
)
```

## Dicas

1. **Mantenha o glossário pequeno** - só termos realmente importantes
2. **Use o Dev Overlay** para ajustes pontuais durante desenvolvimento
3. **Versionize o glossário** junto com o código
4. **Teste com diferentes idiomas** para garantir consistência
5. **Documente decisões** sobre termos específicos
