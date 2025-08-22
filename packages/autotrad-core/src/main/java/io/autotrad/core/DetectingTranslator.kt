package io.autotrad.core

import java.util.Locale

/**
 * Wrapper que detecta automaticamente o idioma de origem antes de traduzir.
 * Útil para tradução automática sem precisar especificar o idioma de origem.
 */
class DetectingTranslator(
    private val detector: MlKitLanguageDetector,
    private val delegate: Translator,
    private val fallbackSource: Locale // idioma base do projeto (ex.: pt-BR)
) : Translator {
    
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        val detected = src ?: detector.detect(text) ?: fallbackSource
        if (detected.language == tgt.language) return text
        return delegate.translate(text, detected, tgt)
    }
}
