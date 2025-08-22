package io.autotrad.core

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Translator on-device usando ML Kit.
 * Faz download do modelo do idioma uma vez (padrão: somente via Wi-Fi).
 */
class MlKitTranslator(
    private val requireWifiForDownload: Boolean = true
) : Translator {

    private val cache = ConcurrentHashMap<String, com.google.mlkit.nl.translate.Translator>()

    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
        val s = src ?: return null
        val srcCode = TranslateLanguage.fromLanguageTag(s.toLanguageTag()) 
            ?: TranslateLanguage.fromLanguageTag(s.language) ?: return null
        val tgtCode = TranslateLanguage.fromLanguageTag(tgt.toLanguageTag()) 
            ?: TranslateLanguage.fromLanguageTag(tgt.language) ?: return null
        
        if (srcCode == tgtCode) return text

        val key = "$srcCode->$tgtCode"
        val client = cache.getOrPut(key) {
            val opts = TranslatorOptions.Builder()
                .setSourceLanguage(srcCode)
                .setTargetLanguage(tgtCode)
                .build()
            Translation.getClient(opts)
        }
        
        val cond = DownloadConditions.Builder()
            .apply { if (requireWifiForDownload) requireWifi() }
            .build()
        client.downloadModelIfNeeded(cond).await()
        return client.translate(text).await()
    }

    /**
     * Pré-baixa modelos de idioma para evitar lag na primeira tradução.
     */
    suspend fun preDownloadLanguages(vararg locales: Locale, requireWifi: Boolean = true) {
        val cond = DownloadConditions.Builder()
            .apply { if (requireWifi) requireWifi() }
            .build()
        val mgr = RemoteModelManager.getInstance()
        
        locales.mapNotNull { 
            TranslateLanguage.fromLanguageTag(it.toLanguageTag()) 
                ?: TranslateLanguage.fromLanguageTag(it.language) 
        }.forEach { code ->
            val model = TranslateRemoteModel.Builder(code).build()
            mgr.download(model, cond).await()
        }
    }

    /** Libere recursos quando não precisar mais. */
    fun close() { 
        cache.values.forEach { runCatching { it.close() } }
        cache.clear() 
    }
}
