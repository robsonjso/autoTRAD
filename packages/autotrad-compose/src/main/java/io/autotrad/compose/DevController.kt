package io.autotrad.compose

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

interface AutoTradDevController {
    fun openEditor(raw: String, current: String, locale: Locale)
}

/** Se não for null, AutoText expõe long-press para abrir o editor. */
val LocalAutoTradDevController = staticCompositionLocalOf<AutoTradDevController?>(defaultFactory = { null })
