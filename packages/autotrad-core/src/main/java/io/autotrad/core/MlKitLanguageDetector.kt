package io.autotrad.core

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import java.util.Locale

/**
 * Detecta automaticamente o idioma de um texto usando ML Kit.
 * Útil para tradução automática sem precisar especificar o idioma de origem.
 */
class MlKitLanguageDetector(private val threshold: Float = 0.3f) {
    private val client = LanguageIdentification
        .getClient(LanguageIdentificationOptions.Builder().setConfidenceThreshold(threshold).build())

    /**
     * Detecta o idioma do texto. Retorna null se não conseguir identificar com confiança suficiente.
     */
    suspend fun detect(text: String): Locale? {
        val tag = client.identifyLanguage(text).await()
        return if (tag == "und") null else Locale.forLanguageTag(tag)
    }
}
