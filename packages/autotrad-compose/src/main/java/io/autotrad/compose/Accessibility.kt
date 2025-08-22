package io.autotrad.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.*
import io.autotrad.core.AutoTrad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** Define contentDescription traduzido em runtime. */
fun Modifier.autoContentDescription(raw: String, args: Map<String, Any?> = emptyMap()): Modifier =
  this.then(Modifier.semantics {
    // Para semantics, usamos o texto original por enquanto
    // Em implementações futuras, pode-se criar um wrapper @Composable
    contentDescription = raw
  })
