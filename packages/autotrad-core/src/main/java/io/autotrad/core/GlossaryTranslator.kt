package io.autotrad.core

import java.util.Locale

/**
 * Translator que aplica glossário e regras de "não traduzir" antes do MT.
 * Garante que termos de negócio sejam traduzidos corretamente.
 */
class GlossaryTranslator(
    private val map: Map<String, String>,         // ex.: "Login"->"Enter"
    private val dontTranslate: Set<String> = setOf("ID", "AutoTrad"),
    private val delegate: Translator
) : Translator {
    
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        // 1) Não traduzir (IDs, nomes próprios, etc.)
        if (dontTranslate.contains(text)) return text
        
        // 2) Glossário (força termo preferido)
        map[text]?.let { return it }
        
        // 3) Segue fluxo normal (detecção + MT)
        return delegate.translate(text, src, tgt)
    }
}
