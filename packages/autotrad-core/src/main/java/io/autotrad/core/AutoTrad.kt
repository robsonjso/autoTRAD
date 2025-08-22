package io.autotrad.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Núcleo da engine: troca de locale em runtime, TM em memória
 * e leitura de catálogos JSON em assets.
 */
object AutoTrad {
    val currentLocale: MutableStateFlow<Locale> = MutableStateFlow(Locale.getDefault())

    private val tm: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    private var translators: List<Translator> = emptyList()
    private var devCapture: DevCapture? = null
    private var appContext: Context? = null

    fun init(
        context: Context,
        locale: Locale = Locale.getDefault(),
        translators: List<Translator> = emptyList(),
        loadFromAssets: Boolean = true,
        devCaptureEnabled: Boolean = false
    ) {
        appContext = context.applicationContext
        currentLocale.value = locale
        this.translators = translators
        if (loadFromAssets) preloadFromAssets(locale)
        if (devCaptureEnabled) devCapture = DevCapture(context.applicationContext)
    }

    fun setLocale(tag: String) {
        val loc = Locale.forLanguageTag(tag)
        currentLocale.value = loc
        // Opcional: recarregar pack desse idioma
        preloadFromAssets(loc)
    }

    suspend fun translate(raw: String, tgt: Locale = currentLocale.value): String {
        val key = normalize(raw)
        // 1) TM
        tm[tgt.language]?.get(key)?.let {
            AutoTradTelemetry.tmHit()
            return it
        }

        // 2) Provedores (on-device / online)
        for (prov in translators) {
            val start = System.nanoTime()
            val out = runCatching { prov.translate(key, null, tgt) }.getOrNull()
            val durMs = (System.nanoTime() - start) / 1_000_000
            if (!out.isNullOrBlank()) {
                // Quality Gate (preserva placeholders e aplica limites por role)
                val ok = QualityGate.isAcceptable(
                    sourceNormalized = key,
                    candidate = out
                )
                if (!ok) {
                    AutoTradTelemetry.qualityReject()
                    continue
                }
                AutoTradTelemetry.mtCall(durMs)
                putTM(tgt, key, out)
                devCapture?.appendPending(tgt, key, out)
                return out
            }
        }

        // 3) Fallback
        devCapture?.appendPending(tgt, key, raw)
        return raw
    }

    private fun putTM(locale: Locale, key: String, value: String) {
        val lang = locale.language
        val map = tm.getOrPut(lang) { mutableMapOf() }
        map[key] = value
    }

    private fun preloadFromAssets(locale: Locale) {
        val ctx = appContext ?: return
        val lang = locale.toLanguageTag()
        val candidates = listOf(
            "autotrad/autotrad.$lang.json",
            "autotrad/autotrad.${locale.language}.json"
        )
        candidates.forEach { path ->
            runCatching {
                ctx.assets.open(path).bufferedReader().use { it.readText() }
            }.onSuccess { json ->
                val bucket = tm.getOrPut(locale.language) { mutableMapOf() }
                val obj = JSONObject(json)
                obj.keys().forEach { k -> bucket[k] = obj.getString(k) }
                return // carregou um pack, encerra
            }
        }
    }

    private fun normalize(s: String): String = s.trim()

    /** Sobrescreve/insere uma tradução manualmente no TM + pending.json (retorna sucesso na validação). */
    fun upsertTranslation(
        raw: String,
        translated: String,
        locale: Locale = currentLocale.value,
        role: TextRole? = null
    ): Boolean {
        val key = normalize(raw)
        if (!QualityGate.isAcceptable(sourceNormalized = key, candidate = translated, role = role)) {
            AutoTradTelemetry.qualityReject()
            return false
        }
        putTM(locale, key, translated)
        devCapture?.appendPending(locale, key, translated)
        return true
    }
}
