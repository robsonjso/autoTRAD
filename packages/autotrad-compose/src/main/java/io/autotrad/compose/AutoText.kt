package io.autotrad.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.autotrad.core.AutoTrad
import io.autotrad.core.TextRole
import io.autotrad.core.QualityGate
import java.util.Locale
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun AutoText(
    raw: String,
    role: TextRole? = null,
    modifier: Modifier = Modifier,
    args: Map<String, Any?> = emptyMap()
) {
    val locale: Locale by AutoTrad.currentLocale.collectAsState()
    val devController = LocalAutoTradDevController.current
    var text by remember(raw, locale, args) { mutableStateOf(raw) }

    LaunchedEffect(raw, locale, args) {
        val translated = withContext(Dispatchers.IO) {
            AutoTrad.translate(raw, tgt = locale)
        }
        // Aplica QualityGate com role
        val safe = if (QualityGate.isAcceptable(raw, translated, role)) translated else raw
        text = applyArgs(safe, args)
    }

    val devModifier = if (devController != null) {
        modifier.pointerInput(raw, text, locale) {
            detectTapGestures(
                onLongPress = {
                    devController.openEditor(raw = raw, current = text, locale = locale)
                }
            )
        }
    } else modifier

    Text(text, devModifier)
}

private fun applyArgs(template: String, args: Map<String, Any?>): String {
    var out = template
    args.forEach { (k, v) -> out = out.replace("{$k}", v?.toString() ?: "") }
    return out
}
