package io.autotrad.core

/**
 * Roles de texto para aplicar limites de caracteres específicos.
 * Útil para garantir que traduções não quebrem a UI.
 */
enum class TextRole {
    Button,     // Máx 16 chars (botões curtos)
    Label,      // Sem limite específico
    Title,      // Máx 48 chars (títulos)
    Chip,       // Máx 12 chars (chips compactos)
    Caption,    // Máx 24 chars (legendas)
    Error       // Máx 32 chars (mensagens de erro)
}
