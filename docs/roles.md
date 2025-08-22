# TextRole e QualityGate no AutoTrad

## Visão Geral

O sistema de `TextRole` permite aplicar limites de caracteres específicos para diferentes tipos de UI, garantindo que traduções não quebrem o layout.

## Roles Disponíveis

### TextRole.Button
- **Limite**: 16 caracteres
- **Uso**: Botões de ação
- **Exemplo**: "Continuar", "Cancelar", "Salvar"

### TextRole.Chip
- **Limite**: 12 caracteres
- **Uso**: Chips compactos
- **Exemplo**: "Tag", "Categoria", "Filtro"

### TextRole.Title
- **Limite**: 48 caracteres
- **Uso**: Títulos de seção
- **Exemplo**: "Configurações", "Perfil do Usuário"

### TextRole.Caption
- **Limite**: 24 caracteres
- **Uso**: Legendas e textos pequenos
- **Exemplo**: "Foto de perfil", "Última atualização"

### TextRole.Error
- **Limite**: 32 caracteres
- **Uso**: Mensagens de erro
- **Exemplo**: "Campo obrigatório", "Senha incorreta"

### TextRole.Label
- **Limite**: Sem limite
- **Uso**: Labels e textos gerais
- **Exemplo**: Qualquer texto sem restrição

## Como Usar

### No AutoText

```kotlin
// Botão com limite de 16 chars
Button(onClick = {}) { 
    AutoText("Continuar", role = TextRole.Button) 
}

// Chip com limite de 12 chars
Chip(onClick = {}) { 
    AutoText("Tag", role = TextRole.Chip) 
}

// Título com limite de 48 chars
Text(
    text = AutoText("Configurações do Sistema", role = TextRole.Title)
)

// Label sem limite
AutoText("Descrição longa do produto", role = TextRole.Label)
```

### No QualityGate

```kotlin
// Validação manual
val isValid = QualityGate.isAcceptable(
    sourceNormalized = "Continuar",
    candidate = "Continue",
    role = TextRole.Button
)
```

## Blacklist Automática

O QualityGate automaticamente bloqueia tradução de:

- **Emails**: `user@example.com`
- **Códigos**: `ABC123`, `ID456`
- **Números longos**: `1234567890`
- **Placeholders**: `{name}`, `{count}`

## Exemplos Práticos

### App de E-commerce

```kotlin
// Botões de ação
Button(onClick = {}) { AutoText("Comprar", role = TextRole.Button) }
Button(onClick = {}) { AutoText("Adicionar", role = TextRole.Button) }

// Chips de categoria
Chip(onClick = {}) { AutoText("Eletrônicos", role = TextRole.Chip) }
Chip(onClick = {}) { AutoText("Roupas", role = TextRole.Chip) }

// Títulos
AutoText("Produtos em Destaque", role = TextRole.Title)
AutoText("Histórico de Pedidos", role = TextRole.Title)
```

### App Financeiro

```kotlin
// Botões
Button(onClick = {}) { AutoText("Transferir", role = TextRole.Button) }
Button(onClick = {}) { AutoText("Investir", role = TextRole.Button) }

// Mensagens de erro
AutoText("Saldo insuficiente", role = TextRole.Error)
AutoText("Senha incorreta", role = TextRole.Error)

// Labels
AutoText("Saldo disponível na conta", role = TextRole.Label)
```

## Boas Práticas

### 1. Use Roles Consistentemente

**✅ Correto:**
```kotlin
// Todos os botões usam TextRole.Button
Button(onClick = {}) { AutoText("Salvar", role = TextRole.Button) }
Button(onClick = {}) { AutoText("Cancelar", role = TextRole.Button) }
```

**❌ Evite:**
```kotlin
// Mistura roles ou não usa
Button(onClick = {}) { AutoText("Salvar") } // sem role
Button(onClick = {}) { AutoText("Cancelar", role = TextRole.Label) } // role errado
```

### 2. Escolha o Role Apropriado

**✅ Correto:**
```kotlin
// Botão de ação
Button(onClick = {}) { AutoText("Continuar", role = TextRole.Button) }

// Texto informativo
AutoText("Este é um texto longo de descrição", role = TextRole.Label)
```

### 3. Teste os Limites

```kotlin
// Teste se a tradução cabe no limite
val candidate = "Continue" // 8 chars, OK para Button
val isValid = QualityGate.isAcceptable(
    sourceNormalized = "Continuar",
    candidate = candidate,
    role = TextRole.Button
)
```

## Fallback Behavior

Se uma tradução exceder o limite do role:

1. **AutoText**: Usa o texto original (não traduzido)
2. **upsertTranslation**: Retorna `false` (falha na validação)
3. **Dev Overlay**: Mantém o editor aberto para correção

## Customização

Para adicionar novos roles ou modificar limites:

```kotlin
// No QualityGate.kt
val maxChars = when (role) {
    TextRole.Button -> 16
    TextRole.Chip -> 12
    TextRole.Title -> 48
    TextRole.Caption -> 24
    TextRole.Error -> 32
    TextRole.Custom -> 20 // seu limite personalizado
    else -> null
}
```

## Dicas

1. **Planeje os limites** antes de implementar
2. **Teste com diferentes idiomas** (alemão, finlandês são longos)
3. **Use o Dev Overlay** para ajustes rápidos
4. **Documente decisões** sobre limites específicos
5. **Considere o contexto** (mobile vs desktop)
