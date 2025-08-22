package io.autotrad.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

private val AutoTradArgsKey = SemanticsPropertyKey<Map<String, Any?>>("AutoTradArgs")
private var SemanticsPropertyReceiver.autoTradArgs by AutoTradArgsKey

/**
 * Modifier declarativo. Para MVP, recomendamos usar AutoText(...).
 * Este modifier existe para a sintaxe Text("...").autoTrad().
 * Uma implementação mais profunda exigiria um wrapper/relay do Text.
 */
fun Modifier.autoTrad(args: Map<String, Any?> = emptyMap()): Modifier = composed {
    val mArgs = remember(args) { args }
    this.then(Modifier.semantics { autoTradArgs = mArgs })
}
