package io.autotrad.core

import java.util.Locale

interface Translator {
    suspend fun translate(text: String, src: Locale?, tgt: Locale): String?
}

/** Provedor "eco" para MVP: não traduz, só retorna o texto. */
class EchoTranslator : Translator {
    override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? = text
}
