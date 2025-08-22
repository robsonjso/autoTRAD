package io.autotrad.core

object QualityGate {

    private val PLACEHOLDER = Regex("\\{[^{}]+\\}")
    private val BLACKLIST_PATTERNS = listOf(
        Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // emails
        Regex("\\b[A-Z]{2,}\\d{3,}\\b"), // códigos (ex: ABC123)
        Regex("\\b\\d{4,}\\b"), // números longos
        Regex("\\{[^{}]+\\}") // placeholders
    )

    /**
     * Valida se a tradução preserva placeholders e respeita limites por role.
     */
    fun isAcceptable(
        sourceNormalized: String,
        candidate: String,
        role: TextRole? = null
    ): Boolean {
        // 1) Placeholders idênticos
        val srcPh = PLACEHOLDER.findAll(sourceNormalized).map { it.value }.toSet()
        val dstPh = PLACEHOLDER.findAll(candidate).map { it.value }.toSet()
        if (srcPh != dstPh) return false

        // 2) Blacklist (não traduzir padrões específicos)
        if (isBlacklisted(sourceNormalized) && sourceNormalized != candidate) return false

        // 3) Limite por role
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

    /**
     * Verifica se o texto deve ser preservado (não traduzido).
     */
    private fun isBlacklisted(text: String): Boolean {
        return BLACKLIST_PATTERNS.any { it.matches(text) }
    }
}
